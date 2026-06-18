package dev.klomptech.jbark.parser.collection;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.print.PrintStyle;
import java.util.ArrayList;
import java.util.List;

public class TakeParser {

  // Which stash and which item word (first, second, last, etc.)
  private record StashTake(String stash, String which) {}

  private final Parser parser;

  public TakeParser(final Parser parser) {
    this.parser = parser;
  }

  // Dog takes one item from a stash (example: she wants the first biscuit from her cookie jar)
  public AstNode parseTakeLine() throws BarkError {
    // Look for the stash and the item word
    StashTake take = findStashTakeOnLine();
    // Look for the take from stash word
    if (take == null || !parser.lineHasWord(Keywords.TAKE_FROM_STASH_WORDS)) {
      return null;
    }
    int line = parser.peek().line();
    // Look for the dog
    String subject = findDogOnLine();
    if (subject == null) {
      throw new BarkError(line, "Who is taking from the jar? Name the dog or use she/he first");
    }
    // Edge case: "she whimpers as she wants the first biscuit from her cookie jar"
    List<AstNode> steps = new ArrayList<>();
    if (lineHasPrintVerb()) {
      PrintStyle style = findPrintStyleOnLine();
      steps.add(
          new AstNode.Print(
              style != null ? style : PrintStyle.BARK,
              List.of(new ParseExpression.Empty()),
              line,
              false,
              findDogSpeakerOnLine()));
    }
    steps.add(new AstNode.TakeFromStash(subject, take.stash(), take.which(), line));
    parser.consumeUntilLineEnd();
    if (steps.size() == 1) {
      return steps.get(0);
    }
    return new AstNode.LineGroup(steps, line);
  }

  // Find "from her cookie jar" on the line, then read backwards for first/last or the item
  private StashTake findStashTakeOnLine() {
    // Where this line starts and ends in the token list
    int lineStart = parser.cursorIndex();
    int lineEnd = parser.lineEndIndex();
    List<Token> tokens = parser.allTokens();
    // Walk each token on the line until we find a from word
    for (int fromAt = lineStart; fromAt < lineEnd; fromAt++) {
      Token token = tokens.get(fromAt);
      // Skip filler words
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      // Skip words that are not from
      if (!Keywords.FROM_WORDS.contains(parser.normalise(token.value()))) {
        continue;
      }
      // Read the registered stash name after from (example: her cookie jar becomes cookie_jar)
      String stash = ConfigLoader.resolveCollectionFromTokens(tokens, fromAt + 1, lineEnd);
      if (stash == null || !ConfigLoader.isStash(stash)) {
        continue;
      }
      // Read backwards from 'from' for words like first or "bone"
      String which = findWhichBeforeFrom(tokens, lineStart, fromAt);
      if (which != null) {
        return new StashTake(stash, which);
      }
      return null;
    }
    return null;
  }

  private String findWhichBeforeFrom(
      final List<Token> tokens, final int lineStart, final int fromAt) {
    // Go left from 'from', looking for the item the dog wants
    for (int i = fromAt - 1; i >= lineStart; i--) {
      Token token = tokens.get(i);
      // Name on the line (example: "bone")
      if (token.is(TokenType.STRING)) {
        return token.value();
      }
      // Number item on the line (example: '3')
      if (token.is(TokenType.NUMBER)) {
        return parser.normalise(token.value());
      }
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      // Spot word (example: first, second, last)
      if (StashSpots.isStashSpot(word)) {
        return word;
      }
    }
    return null;
  }

  private String findDogOnLine() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.isDogSubjectReference(word, token.value())) {
        return word;
      }
    }
    return null;
  }

  private String findDogSpeakerOnLine() {
    return findDogOnLine();
  }

  private boolean lineHasPrintVerb() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.PRINT_KEYWORDS.contains(parser.normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  private PrintStyle findPrintStyleOnLine() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.PRINT_KEYWORDS.contains(word)) {
          return Keywords.PRINT_STYLES.getOrDefault(word, PrintStyle.BARK);
        }
      }
    }
    return null;
  }
}
