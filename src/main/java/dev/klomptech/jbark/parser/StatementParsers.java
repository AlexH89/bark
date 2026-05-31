package dev.klomptech.jbark.parser;

import java.util.LinkedHashMap;
import java.util.Map;

import dev.klomptech.jbark.lexer.TokenType;

/**
 * Maps the first token of a line to the parser method that handles that statement.
 */
public final class StatementParsers {

    private StatementParsers() {}

    public static Map<TokenType, StatementParser> create(final Parser parser) {
        Map<TokenType, StatementParser> parsers = new LinkedHashMap<>();

        parsers.put(TokenType.PRINT, parser::parsePrint);
        parsers.put(TokenType.IDENTIFIER, parser::parseAssign);

        return parsers;
    }
}
