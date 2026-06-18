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

public class IfParser {

  private final Parser parser;
  private final ConditionParser conditionParser;

  // Where when, then, and otherwise sit on the line (offsets from the start of that line)
  private record IfLineMarkers(int ifStartOffset, int thenOffset, int otherwiseOffset) {}

  public IfParser(final Parser parser) {
    this.parser = parser;
    this.conditionParser = new ConditionParser(parser);
  }

  public AstNode parseIfLine() throws BarkError {
    int line = parser.peek().line();
    List<AstNode.IfBranch> branches = new ArrayList<>();
    // First branch: "when she has 2 toys then" on the current line
    branches.add(parseBranch(line));
    // More branches while we see "otherwise when" (elif), not a plain otherwise
    while (parser.currentLineIsOtherwiseWhen()) {
      parser.advance();
      branches.add(parseBranch(line));
    }
    // Final else if the line is plain "otherwise" (no when after it)
    List<AstNode> elseSteps = parseElseBody(line);
    return new AstNode.IfChain(branches, elseSteps, line);
  }

  private AstNode.IfBranch parseBranch(final int line) throws BarkError {
    IfLineMarkers markers = findIfLineMarkers();
    if (markers.ifStartOffset() < 0 || markers.thenOffset() < 0) {
      throw new BarkError(
          line, "The dogs look at you with tilted heads, waiting for a condition or consequence.");
    }
    // Read the test between when and then (example: "she has 3 toys and my labrador is loud")
    ParseExpression condition =
        conditionParser.parseCondition(markers.ifStartOffset() + 1, markers.thenOffset(), line);
    // Read what happens when that test is true (inline or lines until otherwise/bury)
    List<AstNode> thenSteps = parseThenBody(markers, line);
    return new AstNode.IfBranch(condition, thenSteps);
  }

  private List<AstNode> parseThenBody(final IfLineMarkers markers, final int line)
      throws BarkError {
    // Nothing after "then" on this line means the body starts on the next line(s)
    if (parser.isAtEndOrLineEndOffset(markers.thenOffset() + 1)) {
      parser.advanceBy(markers.thenOffset() + 1);
      parser.skipNewlines();
      return parseMultilineThenBody(line);
    }
    // Inline then: "when she has 2 toys then she woofs "hi"" on one line
    int tokenCount = thenBodyTokenCount(markers);
    parser.advanceBy(markers.thenOffset() + 1);
    return InlineBodyParser.parseThenSteps(
        parser, tokenCount, line, "Then what? The dogs are waiting for something to do.");
  }

  private List<AstNode> parseMultilineThenBody(final int line) throws BarkError {
    List<AstNode> steps = new ArrayList<>();
    while (!parser.isAtEnd()) {
      parser.skipNewlines();
      if (parser.isAtEnd()) {
        break;
      }
      // Stop before otherwise or bury. those end the block, they are not then steps
      if (parser.currentLineIsMultilineIfStop()) {
        break;
      }
      // Each body line uses full parseLine routing (print, take, adjust, and so on)
      parser.parseStatementInto(steps);
    }
    if (steps.isEmpty()) {
      throw new BarkError(line, "Then what? The dogs are waiting for something to do.");
    }
    // Bury closes the block here (if no otherwise, cursor stays on bury for parseElseBody)
    if (parser.currentLineIsBury()) {
      parser.consumeBuryLine();
    }
    return steps;
  }

  private List<AstNode> parseElseBody(final int line) throws BarkError {
    if (!parser.currentLineStartsWithOtherwise()) {
      return null;
    }
    if (parser.currentLineIsOtherwiseWhen()) {
      return null;
    }
    parser.advance();
    // "otherwise" alone on the line. else body is on the following line(s)
    if (parser.isAtEndOrLineEndOffset(0)) {
      parser.skipNewlines();
      return parseMultilineElseBody(line);
    }
    // "otherwise she woofs "nope"" on the same line as otherwise
    return InlineBodyParser.parseThenSteps(
        parser, parser.countTokensAhead(0), line, "Otherwise what? The dogs need something to do.");
  }

  private List<AstNode> parseMultilineElseBody(final int line) throws BarkError {
    List<AstNode> steps = new ArrayList<>();
    while (!parser.isAtEnd()) {
      parser.skipNewlines();
      if (parser.isAtEnd()) {
        break;
      }
      // Else body ends at bury, same idea as then body stopping at otherwise
      if (parser.currentLineIsBury()) {
        break;
      }
      parser.parseStatementInto(steps);
    }
    if (steps.isEmpty()) {
      throw new BarkError(line, "Otherwise what? The dogs need something to do.");
    }
    // Multiline if/else must close with bury
    if (!parser.currentLineIsBury()) {
      throw new BarkError(line, "The dog started something but forgot what he was doing");
    }
    parser.consumeBuryLine();
    return steps;
  }

  private int thenBodyTokenCount(final IfLineMarkers markers) {
    // Inline otherwise on the when line. do not read past it into the then slice
    if (markers.otherwiseOffset() >= 0) {
      return markers.otherwiseOffset() - (markers.thenOffset() + 1);
    }
    // No otherwise on the when line. then slice runs to end of line
    return parser.countTokensAhead(markers.thenOffset() + 1);
  }

  private IfLineMarkers findIfLineMarkers() {
    int ifStartOffset = -1;
    int thenOffset = -1;
    int otherwiseOffset = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (ifStartOffset < 0 && Keywords.IF_START_WORDS.contains(word)) {
          ifStartOffset = offset;
          continue;
        }
        if (ifStartOffset >= 0 && thenOffset < 0 && Keywords.isThenWord(word)) {
          thenOffset = offset;
          continue;
        }
        if (ifStartOffset >= 0
            && thenOffset >= 0
            && otherwiseOffset < 0
            && Keywords.isOtherwiseWord(word)) {
          otherwiseOffset = offset;
          break;
        }
      }
    }
    return new IfLineMarkers(ifStartOffset, thenOffset, otherwiseOffset);
  }
}
