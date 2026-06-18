package dev.klomptech.jbark.lexer;

public record Token(TokenType type, String value, int line) {

  public boolean is(final TokenType type) {
    return this.type == type;
  }
}
