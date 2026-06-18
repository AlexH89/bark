package dev.klomptech.jbark.parser.controlflow;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.expression.ConditionParser;
import java.util.ArrayList;
import java.util.List;

public class WhileParser {

  private final Parser parser;
  private final ConditionParser conditionParser;

  private record WhileLineMarkers(int whileOffset, int thenOffset) {}

  public WhileParser(final Parser parser) {
    this.parser = parser;
    this.conditionParser = new ConditionParser(parser);
  }

  public AstNode parseWhileLine() throws BarkError {
    int line = parser.peek().line();
    WhileLineMarkers markers = findWhileLineMarkers();
    if (markers.whileOffset() < 0 || markers.thenOffset() < 0) {
      throw new BarkError(
          line, "The dogs were told to repeat a trick but not where to run or when to stop");
    }
    ParseExpression condition =
        conditionParser.parseCondition(markers.whileOffset() + 1, markers.thenOffset(), line);
    List<AstNode> steps = parseLoopBody(markers, line);
    if (!parser.currentLineIsBury()) {
      throw new BarkError(line, "The dog started something but forgot what he was doing");
    }
    parser.consumeBuryLine();
    return new AstNode.WhileLoop(condition, steps, line);
  }

  private WhileLineMarkers findWhileLineMarkers() {
    int whileOffset = -1;
    int thenOffset = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (whileOffset < 0 && Keywords.WHILE_KEYWORDS.contains(word)) {
        whileOffset = offset;
        continue;
      }
      if (whileOffset >= 0 && thenOffset < 0 && Keywords.isThenWord(word)) {
        thenOffset = offset;
        break;
      }
    }
    return new WhileLineMarkers(whileOffset, thenOffset);
  }

  private List<AstNode> parseLoopBody(final WhileLineMarkers markers, final int line)
      throws BarkError {
    if (parser.isAtEndOrLineEndOffset(markers.thenOffset() + 1)) {
      parser.advanceBy(markers.thenOffset() + 1);
      parser.skipNewlines();
      return parseMultilineBody(line);
    }
    int tokenCount = parser.countTokensAhead(markers.thenOffset() + 1);
    parser.advanceBy(markers.thenOffset() + 1);
    return InlineBodyParser.parseThenSteps(
        parser, tokenCount, line, "Then what? The dogs are waiting for something to do.");
  }

  private List<AstNode> parseMultilineBody(final int line) throws BarkError {
    List<AstNode> steps = new ArrayList<>();
    while (!parser.isAtEnd()) {
      parser.skipNewlines();
      if (parser.isAtEnd()) {
        break;
      }
      if (parser.currentLineIsBury()) {
        break;
      }
      parser.parseStatementInto(steps);
    }
    if (steps.isEmpty()) {
      throw new BarkError(line, "Then what? The dogs are waiting for something to do.");
    }
    return steps;
  }
}
