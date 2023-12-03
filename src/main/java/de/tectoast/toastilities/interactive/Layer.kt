package de.tectoast.toastilities.interactive;

import net.dv8tion.jda.api.entities.Message;

import java.util.function.BiFunction;
import java.util.function.Function;

@SuppressWarnings("unused")
public class Layer {
    private final String msg;
    private final String id;
    private final BiFunction<Message, Interactive, Object> check;
    private final Function<Object, String> toString;
    private Object answer;

    public Layer(String id, String msg, BiFunction<Message, Interactive, Object> check) {
        this(id, msg, check, null);
    }

    public Layer(String id, String msg, BiFunction<Message, Interactive, Object> check, Function<Object, String> toString) {
        this.id = id;
        this.msg = msg;
        this.check = check;
        this.toString = toString != null ? toString : Object::toString;
    }

    public String getAnswerAsString() {
        return toString.apply(answer);
    }

    public Object getAnswer() {
        return answer;
    }

    public void setAnswer(Object answer) {
        this.answer = answer;
    }

    public String getId() {
        return id;
    }

    public String getMsg() {
        return msg;
    }

    public BiFunction<Message, Interactive, Object> getCheck() {
        return check;
    }

    public boolean isFinished() {
        return answer != null;
    }

    public Layer copy() {
        return new Layer(id, msg, check, toString);
    }
}
