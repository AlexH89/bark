package dev.klomptech.jbark.lexer;

import dev.klomptech.jbark.errors.BarkError;

import java.util.ArrayList;
import java.util.List;

public class Lexer {

    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int current = 0;
    private int line = 1;

    public Lexer(final String source) {
        this.source = source;
    }

    public List<Token> tokenise() throws BarkError {
        while (!isAtEnd()) {
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", line));
        return tokens;
    }

    private void scanToken() throws BarkError {
        char c = advance();
        switch (c) {
            case ' ', '\r', '\t' -> { /* ignore whitespace */ }
            case '\n' -> newline();
            case '"' -> string();
            // These following two comment cases are safe as the string comparison came first
            case '/' -> comment();
            case '#' -> skipToEndOfLine();
            default -> {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    word();
                } else {
                    throw BarkError.error(line, "Sniffed a '" + c + "'. Dogs don't bury symbols like that.");
                }
            }
        }
    }

    private void newline() {
        tokens.add(new Token(TokenType.NEWLINE, "\n", line));
        line++;
    }

    private void comment() throws BarkError {
        if (match('/')) {
            skipToEndOfLine();
            return;
        }
        throw error("Only one slash on the lawn, dogs need a full bone like //, not half.");
    }

    private void skipToEndOfLine() {
        while (peek() != '\n' && !isAtEnd()) {
            advance();
        }
    }

    private void string() throws BarkError {
        int start = current;
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') {
                throw error("You started a howl with \" but never finished, dogs are still waiting.");
            }
            advance();
        }
        if (isAtEnd()) {
            throw error("You started a howl with \" but it never came home.");
        }
        advance(); // closing quote
        String value = source.substring(start, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
    }

    private void number() {
        int start = current - 1;
        while (isDigit(peek())) {
            advance();
        }
        if (peek() == '.' && isDigit(peekNext())) {
            advance(); // consume '.'
            while (isDigit(peek())) {
                advance();
            }
        }
        String value = source.substring(start, current);
        tokens.add(new Token(TokenType.NUMBER, value, line));
    }

    private void word() {
        int start = current - 1;
        while (isAlphaNumeric(peek())) {
            advance();
        }
        String text = source.substring(start, current);
        String lower = text.toLowerCase();
        TokenType type = Keywords.TYPES.getOrDefault(lower, TokenType.IDENTIFIER);
        tokens.add(new Token(type, text, line));
    }

    private char advance() {
        return source.charAt(current++);
    }

    private boolean match(final char expected) {
        if (isAtEnd() || source.charAt(current) != expected) {
            return false;
        }
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) {
            return '\0';
        }
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) {
            return '\0';
        }
        return source.charAt(current + 1);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(final char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(final char c) {
        return (c >= 'a' && c <= 'z')
            || (c >= 'A' && c <= 'Z')
            || c == '_';
    }

    private boolean isAlphaNumeric(final char c) {
        return isAlpha(c) || isDigit(c);
    }

    private BarkError error(final String message) {
        return new BarkError(line, message);
    }
}
