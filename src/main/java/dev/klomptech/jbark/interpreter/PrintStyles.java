package dev.klomptech.jbark.interpreter;

/**
 * Formats output differently per print verb.
 */
public final class PrintStyles {

    private PrintStyles() {}

    public static String format(final PrintStyle style, final String text) {
        return switch (style) {
            case BARK -> text;
            case GROWL -> text.toUpperCase();
            case WOOF -> "Woof! " + text;
            case HOWL -> "Awoooo~ " + text;
            case WHIMPER, WHINE -> "(" + text + ")";
            case YAPPING -> text + "! " + text + "!";
            case WHINING -> "…" + text + "…";
        };
    }
}
