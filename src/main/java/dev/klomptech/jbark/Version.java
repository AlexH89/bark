package dev.klomptech.jbark;

public final class Version {

    public static final String NAME = "bark";
    public static final String NUMBER = "0.1.0";

    private Version() {}

    public static void print() {
        System.out.println(NAME + " " + NUMBER);
    }
}
