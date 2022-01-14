package de.tectoast.toastilities.interactive;

@FunctionalInterface
public interface Finisher<U,C,A> {
    void accept(U u, C c, A a);
}
