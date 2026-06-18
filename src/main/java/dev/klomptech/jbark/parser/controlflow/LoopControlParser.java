package dev.klomptech.jbark.parser.controlflow;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.Parser;
import java.util.List;

public class LoopControlParser {

  private final Parser parser;

  public LoopControlParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: "heel" on its own line inside a while/until/for-each body (break)
  public AstNode parseBreakLine() throws BarkError {
    if (!lineStartsWith(Keywords.BREAK_KEYWORDS)) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Break(line);
  }

  // Example: "again" on its own line inside a loop body (continue)
  public AstNode parseContinueLine() throws BarkError {
    if (!lineStartsWith(Keywords.CONTINUE_KEYWORDS)) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Continue(line);
  }

  // Break and continue must start the line, same as when/while/for
  private boolean lineStartsWith(final List<String> words) {
    Token token = parser.peek();
    return token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()));
  }
}
