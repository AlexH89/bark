package dev.klomptech.jbark.interpreter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import dev.klomptech.jbark.errors.BarkError;

public final class StdinReader {

    private StdinReader() {}

    public static BarkValue read(final int line) throws BarkError {
        try {
            String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
            if (input == null) {
                return new BarkValue.BarkNull();
            }
            return BarkValue.of(input);
        } catch (final IOException e) {
            throw new BarkError(line, "The human went quiet — couldn't catch anything from stdin.");
        }
    }
}
