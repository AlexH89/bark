package dev.klomptech.jbark.parser.collection;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;

public class PileActionParser {

  private final Parser parser;

  public PileActionParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: "she takes the top item from the laundry basket" (top item comes off the stack)
  public AstNode parseGrabTopLine() throws BarkError {
    String pile = findPileAfterFrom();
    if (pile == null || !parser.lineHasWord(Keywords.PILE_GRAB_FROM_WORDS)) {
      return null;
    }
    int line = parser.peek().line();
    if (!lineHasTopBeforeFrom()) {
      throw new BarkError(
          line, "Grab what from the pile? The dogs need to know which item they can have!");
    }
    String subject = findDogOnLine();
    if (subject == null) {
      throw new BarkError(line, "Who is grabbing from the pile? Name the dog or use she/he first");
    }
    ConfigLoader.requirePile(pile, line);
    parser.consumeUntilLineEnd();
    return new AstNode.PilePop(pile, line);
  }

  // Example: "she puts "slipper" into the laundry basket" (one item goes on top)
  public AstNode parsePushIntoLine() throws BarkError {
    int intoAt = findInOrIntoOffset();
    if (intoAt < 0) {
      return null;
    }
    String pile =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex() + intoAt + 1, parser.lineEndIndex());
    if (pile == null || !ConfigLoader.isPile(pile)) {
      return null;
    }
    if (!parser.lineHasWord(Keywords.PILE_PUSH_INTO_WORDS)) {
      return null;
    }
    int line = parser.peek().line();
    ParseExpression item = findItemBeforeIn(intoAt);
    if (item == null) {
      throw new BarkError(line, "Toss what into the pile? The dogs are curious and want to know!");
    }
    String subject = findDogOnLine();
    if (subject == null) {
      throw new BarkError(
          line, "Who is putting something in the pile? Name the dog or use she/he first");
    }
    ConfigLoader.requirePile(pile, line);
    parser.consumeUntilLineEnd();
    return new AstNode.PilePush(pile, item, line);
  }

  private String findPileAfterFrom() {
    int lineStart = parser.cursorIndex();
    int lineEnd = parser.lineEndIndex();
    for (int fromAt = lineStart; fromAt < lineEnd; fromAt++) {
      Token token = parser.allTokens().get(fromAt);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      if (!Keywords.FROM_WORDS.contains(parser.normalise(token.value()))) {
        continue;
      }
      String pile =
          ConfigLoader.resolveCollectionFromTokens(parser.allTokens(), fromAt + 1, lineEnd);
      if (pile != null && ConfigLoader.isPile(pile)) {
        return pile;
      }
    }
    return null;
  }

  private boolean lineHasTopBeforeFrom() {
    int lineStart = parser.cursorIndex();
    int lineEnd = parser.lineEndIndex();
    for (int fromAt = lineStart; fromAt < lineEnd; fromAt++) {
      Token token = parser.allTokens().get(fromAt);
      if (!token.is(TokenType.IDENTIFIER)
          || !Keywords.FROM_WORDS.contains(parser.normalise(token.value()))) {
        continue;
      }
      for (int offset = fromAt - 1; offset >= lineStart; offset--) {
        Token before = parser.allTokens().get(offset);
        if (!before.is(TokenType.IDENTIFIER)) {
          continue;
        }
        String word = parser.normalise(before.value());
        if (Keywords.TOP_WORDS.contains(word)) {
          return true;
        }
      }
      return false;
    }
    return false;
  }

  private int findInOrIntoOffset() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.IN_WORDS.contains(word)) {
        return offset;
      }
    }
    return -1;
  }

  private ParseExpression findItemBeforeIn(final int intoAt) {
    for (int offset = intoAt - 1; offset >= 0; offset--) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.STRING)) {
        return new ParseExpression.StringLiteral(token.value());
      }
      if (token.is(TokenType.NUMBER)) {
        return new ParseExpression.NumberLiteral(
            Double.parseDouble(parser.normalise(token.value())));
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
}
