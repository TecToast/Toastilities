package de.tectoast.toastilities.interactive;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@SuppressWarnings("unused")

public class Interactive {

    private final MessageChannel tco;
    private final List<Layer> layers;
    private final User user;
    private final Listener listener;
    private final Finisher<User, MessageChannel, Map<String, Object>> onFinish;
    private final int maxtime;
    private final TimeUnit timeunit;
    private final String timermsg;
    private final String cancelmsg;
    private final List<String> cancelcommands;
    private final Consumer<Interactive> onCancel;
    private final ScheduledExecutorService threadpool = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> tocancel;

    public MessageChannel getChannel() {
        return tco;
    }

    public User getUser() {
        return user;
    }

    public Interactive(MessageChannel tco, List<Layer> layers, User user, Finisher<User, MessageChannel, Map<String, Object>> onFinish,
                       int maxtime, TimeUnit timeunit, String timermsg, String cancelmsg, List<String> cancelcommands, Consumer<Interactive> onCancel) {
        if (maxtime < 0)
            throw new IllegalArgumentException("maxtime has to be higher or equal than 0 (value: " + maxtime + ")");
        this.tco = tco;
        this.layers = layers;
        this.user = user;
        this.onFinish = onFinish;
        this.maxtime = maxtime;
        this.timeunit = timeunit;
        this.timermsg = timermsg == null ? "" : timermsg;
        this.cancelmsg = cancelmsg;
        this.cancelcommands = cancelcommands;
        this.onCancel = onCancel == null ? (i) -> {} : onCancel;
        tco.getJDA().addEventListener(listener = new Listener());
        tco.sendMessage(getFirstUnfinishedLayer().getMsg()).queue();
        tocancel = threadpool.schedule(() -> {
            this.tco.sendMessage(this.timermsg).queue();
            this.tco.getJDA().removeEventListener(listener);
            this.onCancel.accept(this);
        }, maxtime, timeunit);
    }

    private Layer getFirstUnfinishedLayer() {
        return layers.stream().filter(layer -> !layer.isFinished()).findFirst().orElse(null);
    }

    private void finish() {
        tco.getJDA().removeEventListener(listener);
        Map<String, Object> answers = new HashMap<>();
        layers.forEach(l -> answers.put(l.getId(), l.getAnswer()));
        onFinish.accept(user, tco, answers);
    }

    private class Listener extends ListenerAdapter {
        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent e) {
            if (!e.getChannel().getId().equals(tco.getId()) || !e.getAuthor().getId().equals(user.getId())) return;
            Message m = e.getMessage();
            String msg = m.getContentDisplay();
            if (tocancel != null) {
                tocancel.cancel(false);
            }
            if (cancelcommands.stream().anyMatch(s -> s.equalsIgnoreCase(msg))) {
                tco.sendMessage(cancelmsg).queue();
                tco.getJDA().removeEventListener(listener);
                onCancel.accept(Interactive.this);
                return;
            }
            Layer l = getFirstUnfinishedLayer();
            if (l == null) return;
            Object o = l.getCheck().apply(m);
            if (o instanceof ErrorMessage) {
                tco.sendMessage(((ErrorMessage) o).getMsg()).queue();
                if (maxtime > 0) {
                    tocancel = threadpool.schedule(() -> {
                        tco.sendMessage(timermsg).queue();
                        tco.getJDA().removeEventListener(listener);
                        onCancel.accept(Interactive.this);
                    }, maxtime, timeunit);
                }
                return;
            }
            l.setAnswer(o);
            Layer nl = getFirstUnfinishedLayer();
            if (nl == null) {
                finish();
                return;
            }
            String tosend = nl.getMsg();
            for (Layer layer : layers) {
                if (layer.isFinished()) tosend = tosend.replace("{" + layer.getId() + "}", layer.getAnswerAsString());
            }
            tco.sendMessage(tosend).queue();
            if (maxtime > 0) {
                tocancel = threadpool.schedule(() -> {
                    tco.sendMessage(timermsg).queue();
                    tco.getJDA().removeEventListener(listener);
                    onCancel.accept(Interactive.this);
                }, maxtime, timeunit);
            }
        }
    }
}
