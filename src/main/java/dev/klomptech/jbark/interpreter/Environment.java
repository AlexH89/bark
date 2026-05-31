package dev.klomptech.jbark.interpreter;

import java.util.HashMap;
import java.util.Map;

import dev.klomptech.jbark.errors.BarkError;

public class Environment {
    private final Map<String, BarkValue> values = new HashMap<>();

    public void define(final String name, final BarkValue value) {
        values.put(name, value);
    }

    public BarkValue get(final String name, final int line) throws BarkError {
        if (!has(name)) {
            throw new BarkError(line, "No scent of \"" + name + "\" anywhere.");
        }
        return values.get(name);
    }

    public boolean has(final String name) {
        return values.containsKey(name);
    }
}
