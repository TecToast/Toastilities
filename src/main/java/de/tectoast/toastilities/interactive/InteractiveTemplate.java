package de.tectoast.toastilities.interactive;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings({"UnusedReturnValue", "unused"})
public class InteractiveTemplate {

    private final List<Layer> layers = new LinkedList<>();
    private final Finisher<User, MessageChannel, Map<String, Object>> onFinish;
    private final List<String> cancelcommands = new LinkedList<>();
    private final Map<String, BiPredicate<User, MessageChannel>> skip = new HashMap<>();
    private final String cancelmsg;
    private int maxtime = 0;
    private TimeUnit timeunit = TimeUnit.SECONDS;
    private String timermsg;
    private Consumer<Interactive> onCancel;

    public InteractiveTemplate(Finisher<User, MessageChannel, Map<String, Object>> onFinish, String cancelmsg) {
        this.onFinish = onFinish;
        this.cancelmsg = cancelmsg;
    }

    public InteractiveTemplate addLayer(Layer layer) {
        layers.add(layer);
        return this;
    }

    public InteractiveTemplate addLayer(String id, String msg, Function<Message, Object> check) {
        return addLayer(id, msg, check, null);
    }

    public InteractiveTemplate addLayer(String id, String msg, Function<Message, Object> check, Function<Object, String> toString) {
        layers.add(new Layer(id, msg, check, toString));
        return this;
    }

    public InteractiveTemplate addCancelCommand(String cmd) {
        cancelcommands.add(cmd);
        return this;
    }

    public InteractiveTemplate setTimer(int maxtime, TimeUnit timeunit, String timermsg) {
        this.maxtime = maxtime;
        this.timeunit = timeunit;
        this.timermsg = timermsg;
        return this;
    }

    public InteractiveTemplate setSkip(String id, BiPredicate<User, MessageChannel> check) {
        skip.put(id, check);
        return this;
    }

    public void createInteractive(User user, MessageChannel tco) {
        new Interactive(tco, layers.stream().filter(l -> skip.getOrDefault(l.getId(), (a, b) -> true).test(user, tco)).map(Layer::copy).collect(Collectors.toCollection(ArrayList::new)), user, onFinish, maxtime, timeunit, timermsg, cancelmsg, cancelcommands, onCancel);
    }

    public void setOnCancel(Consumer<Interactive> onCancel) {
        this.onCancel = onCancel;
    }
}
