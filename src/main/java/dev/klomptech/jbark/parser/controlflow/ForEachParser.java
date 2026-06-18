package dev.klomptech.jbark.parser.controlflow;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.Parser;
import java.util.ArrayList;
import java.util.List;

public class ForEachParser {

  private final Parser parser;

  private record ForEachLineMarkers(int thenOffset) {}

  public ForEachParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: "for each treat from her cookie jar then"
  public AstNode parseForEachLine() throws BarkError {
    int line = parser.peek().line();
    if (!parser.currentLineStartsWithAny(Keywords.FOR_WORDS)) {
      return null;
    }
    if (findWordOffset(Keywords.EACH_WORDS) < 0) {
      throw new BarkError(
          line, "For loops start with for each, like 'for each bone from cookie jar'");
    }
    int eachAt = findWordOffset(Keywords.EACH_WORDS);
    // the variable name after "each"
    String variable = findLoopVariableAfter(eachAt);
    if (variable == null) {
      throw new BarkError(line, "For each what? Name the loop bone or treat.");
    }
    int fromAt = findFromOrInAfter(eachAt + 1);
    if (fromAt < 0) {
      throw new BarkError(line, "For each needs from or in, like 'for each bone from cookie jar'");
    }
    // Which stash to walk through
    String stash =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex() + fromAt + 1, parser.lineEndIndex());
    if (stash == null || !ConfigLoader.isStash(stash)) {
      throw new BarkError(line, "After from, name a stash the dogs can dig through");
    }
    ConfigLoader.requireStash(stash, line);
    ForEachLineMarkers markers = findForEachLineMarkers();
    if (markers.thenOffset() < 0) {
      throw new BarkError(line, "The dogs heard the start of a command but no 'then'");
    }
    // Read the lines below "then" (or one print on the same line) until bury
    List<AstNode> steps = parseLoopBody(markers, line);
    if (!parser.currentLineIsBury()) {
      throw new BarkError(line, "The dog started something but forgot what he was doing");
    }
    parser.consumeBuryLine();
    return new AstNode.ForEach(variable, stash, steps, line);
  }

  // First identifier after "each" and before "from" or "in" (example: treat in "for each treat
  // from")
  private String findLoopVariableAfter(final int eachAt) {
    for (int offset = eachAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.FROM_WORDS.contains(word) || Keywords.IN_WORDS.contains(word)) {
        return null;
      }
      if (Keywords.isThenWord(word)) {
        return null;
      }
      return word;
    }
    return null;
  }

  private int findFromOrInAfter(final int startOffset) {
    for (int offset = startOffset; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.FROM_WORDS.contains(word) || Keywords.IN_WORDS.contains(word)) {
        return offset;
      }
    }
    return -1;
  }

  private ForEachLineMarkers findForEachLineMarkers() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && Keywords.isThenWord(parser.normalise(token.value()))) {
        return new ForEachLineMarkers(offset);
      }
    }
    return new ForEachLineMarkers(-1);
  }

  // Find one of the words in the list
  private int findWordOffset(final List<String> words) {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()))) {
        return offset;
      }
    }
    return -1;
  }

  // Build the list of steps inside the for each block (multiline lines below "then", or one print
  // on the same line)
  private List<AstNode> parseLoopBody(final ForEachLineMarkers markers, final int line)
      throws BarkError {
    // Multiline: "for each treat from her cookie jar then" and the body starts on the next line
    if (parser.isAtEndOrLineEndOffset(markers.thenOffset() + 1)) {
      parser.advanceBy(markers.thenOffset() + 1);
      parser.skipNewlines();
      return parseMultilineBody(line);
    }
    // One line: "for each treat from her cookie jar then she woofs x" on one line (print only)
    int tokenCount = parser.countTokensAhead(markers.thenOffset() + 1);
    parser.advanceBy(markers.thenOffset() + 1);
    return InlineBodyParser.parseThenSteps(
        parser, tokenCount, line, "Then what? The dogs are waiting for something to do.");
  }

  // Body lines below "for each-then" until a bury line is found
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
