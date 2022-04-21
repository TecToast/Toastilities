package de.tectoast.toastilities.managers;


import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Eine Klasse, die automatisierte ReactionRoles ermöglicht. Näheres dazu in {@link #registerReaction(String, String, String, String)}
 *
 * @author TecToast / Flo
 */
@SuppressWarnings("ALL")
public class ReactionManager {
    private static final List<ReactionManager> reactionManagers = new ArrayList<>();
    //private static final JSONObject json = Utils.load("reactionmanagers.json");
    private final JDA jda;
    private final List<ReactionMessage> reactionMessages = new ArrayList<>();

    /**
     * Erstellt einen ReactionManager auf Basis der angegebenen JDA, ohne die bisherigen ReactionMessages zu laden
     *
     * @param jda Die JDA, die zum Empfangen der Events und zum Geben/Nehmen der Rollen benutzt wird
     */
    public ReactionManager(JDA jda) {
        this(jda, false);
    }

    /**
     * Erstellt einen ReactionManager auf Basis der angegebenen JDA
     *
     * @param jda      Die JDA, die zum empfangen der Events und zum Geben/Nehmen der Rollen benutzt wird
     * @param fromFile true, wenn das Programm nach bisherigen ReactionMessages in der Speicherdatei suchen soll
     */
    public ReactionManager(JDA jda, boolean fromFile) {
        this.jda = jda;
        jda.addEventListener(new ReactionListener());
        reactionManagers.add(this);
        /*if (fromFile) {
            if (json.has(jda.getSelfUser().getId())) {
                JSONArray arr = json.getJSONArray(jda.getSelfUser().getId());
                for (Object obj : arr) {
                    JSONObject o = (JSONObject) obj;
                    registerReaction(o.getString("channelId"), o.getString("messageId"), o.getString("emoji"), o.getString("roleId"));
                }
            }
        }*/
    }

    /**
     * Speichert die ReactionManager im JSON-Format in der angegebenen Datei
     *
     * @param path Der Dateipfad zum Speichern
     */
    /*public static void saveReactionManagers(String path) {
        JSONObject json = new JSONObject();
        reactionManagers.forEach(m -> json.put(m.jda.getSelfUser().getId(), m.toJSONArray()));
        Utils.save(json, "reactionmanagers.json");
    }*/

    /**
     * Gibt den ReactionManager zurück, der die angegebene JDA als Basis benutzt
     *
     * @param jda Die JDA, nach der gefiltert werden soll
     * @return Der zur JDA passende ReactionManager oder null, wenn keiner die angegebene JDA als Basis verwendet
     */
    public static ReactionManager byJDA(JDA jda) {
        return reactionManagers.stream().filter(r -> r.jda.equals(jda)).findFirst().orElse(null);
    }

    public JSONArray toJSONArray() {
        JSONArray arr = new JSONArray();
        reactionMessages.stream().map(ReactionMessage::toJSONObject).forEach(arr::put);
        return arr;
    }

    /**
     * Gibt ein {@link Optional} einer ReactionMessage zurück, die auf das angegebene Event passt
     *
     * @param e Das ReactionEvent, nach dem gefiltert werden soll
     * @return Ein {@link Optional}, dass potenziell die zum Event passende ReactionMessage enthält
     */
    private Optional<ReactionMessage> getByEvent(GenericMessageReactionEvent e) {
        return reactionMessages.stream().filter(r -> r.check(e.getChannel().getId(), e.getMessageId(), e.getReactionEmote().isEmote() ? e.getReactionEmote().getId() : e.getReactionEmote().getEmoji())).findFirst();
    }

    /**
     * Registriert eine neue ReactionMessage
     *
     * @param channelId Die Discord Textchannel ID, in welcher die Nachricht sich befindet
     * @param messageId Die Discord Message ID, auf die reagiert werden soll
     * @param emoji     Das Emoji als Unicode ODER die Emote ID, nach dem gefiltert werden soll
     * @param roleId    Die Discord Role ID, die vergeben werden soll, wenn man auf die Nachricht reagiert
     * @return Diesen ReactionManager für Chain Calls
     */
    public ReactionManager registerReaction(String channelId, String messageId, String emoji, String roleId) {
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ReactionMessage m = new ReactionMessage(channelId, messageId, emoji, roleId);
        Guild g = jda.getTextChannelById(channelId).getGuild();
        if (!reactionMessages.stream().anyMatch(m::equals))
            reactionMessages.add(m);
        /*jda.getTextChannelById(channelId).retrieveMessageById(messageId).queue(message -> {
            Optional<MessageReaction> optional;
            if (Helpers.isNumeric(emoji)) {
                message.addReaction(jda.getTextChannelById(channelId).getGuild().getEmoteById(emoji)).queue();
                optional = message.getReactions().stream().filter(r -> r.getReactionEmote().isEmote() && r.getReactionEmote().getEmote().getId().equals(emoji)).findFirst();
            } else {
                jda.getTextChannelById(channelId).addReactionById(messageId, emoji).queue();
                optional = message.getReactions().stream().filter(r -> r.getReactionEmote().isEmoji() && r.getReactionEmote().getEmoji().equals(emoji)).findFirst();
            }
            optional.ifPresent(r -> r.retrieveUsers().queue(l -> l.forEach(u -> {
                try {
                    g.addRoleToMember(u, g.getRoleById(roleId));
                } catch (Exception ignored) {
                }
            })));
        });*/
        return this;
    }

    private class ReactionListener extends ListenerAdapter {
        @Override
        public void onMessageReactionAdd(@NotNull MessageReactionAddEvent e) {
            if (e.getUserId().equals(e.getJDA().getSelfUser().getId())) return;
            getByEvent(e).ifPresent(r -> e.getGuild().addRoleToMember(e.getUser(), e.getGuild().getRoleById(r.getRoleId())).queue());
        }

        @Override
        public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent e) {
            if (e.getUserId().equals(e.getJDA().getSelfUser().getId())) return;
            getByEvent(e).ifPresent(r -> e.getGuild().removeRoleFromMember(e.getUser(), e.getGuild().getRoleById(r.getRoleId())).queue());
        }
    }

    private class ReactionMessage {
        String channelId;
        String messageId;
        String emoji;
        String roleId;

        public ReactionMessage(String channelId, String messageId, String emoji, String roleId) {
            this.channelId = channelId;
            this.messageId = messageId;
            this.emoji = emoji;
            this.roleId = roleId;
        }

        public boolean check(String channelId, String messageId, String emoji) {
            return this.channelId.equals(channelId) && this.messageId.equals(messageId) && this.emoji.equals(emoji);
        }

        public JSONObject toJSONObject() {
            return new JSONObject().put("channelId", channelId).put("messageId", messageId).put("emoji", emoji).put("roleId", roleId);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReactionMessage that = (ReactionMessage) o;
            return Objects.equals(channelId, that.channelId) &&
                    Objects.equals(messageId, that.messageId) &&
                    Objects.equals(emoji, that.emoji) &&
                    Objects.equals(roleId, that.roleId);
        }

        public String getRoleId() {
            return roleId;
        }
    }
}
