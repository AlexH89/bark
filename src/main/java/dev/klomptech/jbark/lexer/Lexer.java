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
      case ' ', '\r', '\t', ':' -> {
        /* whitespace and story filler */
      }
      case '\n' -> newline();
      case '"' -> string();
      // Safe as it comes before default with number check
      case '.' -> {
        if (isDigit(peek())) {
          number();
        } else {
          tokens.add(new Token(TokenType.PERIOD, ".", line));
        }
      }
      case ',' -> tokens.add(new Token(TokenType.COMMA, ",", line));
      case '+' -> tokens.add(new Token(TokenType.PLUS, "+", line));
      case '*' -> tokens.add(new Token(TokenType.STAR, "*", line));
      case '-' -> {
        if (isDigit(peek()) || (peek() == '.' && isDigit(peekNext()))) {
          number();
        } else {
          tokens.add(new Token(TokenType.MINUS, "-", line));
        }
      }
      // These following two comment cases are safe as the string comparison came first
      case '/' -> {
        if (match('/')) {
          skipToEndOfLine();
        } else {
          tokens.add(new Token(TokenType.SLASH, "/", line));
        }
      }
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

  private void skipToEndOfLine() {
    while (peek() != '\n' && !isAtEnd()) {
      advance();
    }
  }

  private void string() throws BarkError {
    int start = current;
    while (!isAtEnd()) {
      if (peek() == '"') {
        advance();
        String value = source.substring(start, current - 1);
        tokens.add(new Token(TokenType.STRING, value, line));
        return;
      }
      if (peek() == '\n') {
        throw error("Unfinished commands, you have some confused dog faces starting at you!");
      }
      advance();
    }
    throw error("You started a howl with \" but it never finished?");
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
    int end = current;
    consumePossessive();
    String text = source.substring(start, end);
    TokenType type = TokenType.IDENTIFIER;
    tokens.add(new Token(type, text, line));
  }

  // Strip trailing possessive ('s) so we can use something like "labrador's toys'"
  private void consumePossessive() {
    if (peek() == '\'' && (peekNext() == 's' || peekNext() == 'S')) {
      advance();
      advance();
    }
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
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '\\';
  }

  private boolean isAlphaNumeric(final char c) {
    return isAlpha(c) || isDigit(c);
  }

  private BarkError error(final String message) {
    return new BarkError(line, message);
  }
}
