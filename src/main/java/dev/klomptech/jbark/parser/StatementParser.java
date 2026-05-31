package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.errors.BarkError;

@FunctionalInterface
public interface StatementParser {
    Node parse() throws BarkError;
}
