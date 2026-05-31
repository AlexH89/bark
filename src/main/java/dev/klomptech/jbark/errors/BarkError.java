package dev.klomptech.jbark.errors;

public class BarkError extends Exception {

    private final int line;

    public BarkError(final int line, final String message) {
        super(message);
        this.line = line;
    }

    public int getLine() {
        return line;
    }

    public static BarkError error(final int line, final String message) {
        return new BarkError(line, message);
    }

    @Override
    public String toString() {
        return line > 0
            ? "Line " + line + " — " + getMessage()
            : getMessage();
    }
}
