package dev.klomptech.jbark.parser.controlflow;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.Parser;
import java.util.List;

// One-line then bodies: print, heel, again. No bury needed.
final class InlineBodyParser {

  private InlineBodyParser() {}

  static List<AstNode> parseThenSteps(
      final Parser parser, final int tokenCount, final int line, final String emptyError)
      throws BarkError {
    if (tokenCount <= 0 || parser.isAtEndOrLineEndOffset(0)) {
      throw new BarkError(line, emptyError);
    }
    Token first = parser.peekAt(0);
    if (first.is(TokenType.IDENTIFIER) && tokenCount == 1) {
      String word = parser.normalise(first.value());
      if (Keywords.BREAK_KEYWORDS.contains(word)) {
        parser.advanceBy(1);
        return List.of(new AstNode.Break(line));
      }
      if (Keywords.CONTINUE_KEYWORDS.contains(word)) {
        parser.advanceBy(1);
        return List.of(new AstNode.Continue(line));
      }
    }
    if (parser.lineHasWord(Keywords.PRINT_KEYWORDS)) {
      return List.of(parser.getPrintParser().parsePrintForNextTokens(tokenCount));
    }
    throw new BarkError(line, emptyError);
  }
}
