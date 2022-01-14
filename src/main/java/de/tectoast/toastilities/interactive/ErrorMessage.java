package de.tectoast.toastilities.interactive;

public record ErrorMessage(String msg) {

    public String getMsg() {
        return msg;
    }
}
