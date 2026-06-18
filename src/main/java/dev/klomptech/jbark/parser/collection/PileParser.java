package dev.klomptech.jbark.parser.collection;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import java.util.ArrayList;
import java.util.List;

public class PileParser {

  private final Parser parser;

  public PileParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: 'the laundry basket holds "shirt", "sock"' or 'the laundry basket is empty'
  public AstNode parsePileLine() throws BarkError {
    int line = parser.peek().line();
    String pile = findPileOnLine();
    if (pile == null) {
      return null;
    }
    ConfigLoader.requirePile(pile, line);

    if (parser.lineHasWord(Keywords.HOLDS_WORDS)) {
      List<ParseExpression> items = parseItemsAfterHolds();
      if (items.isEmpty()) {
        throw new BarkError(line, "The pile is empty on arrival. Put something in it first.");
      }
      parser.consumeUntilLineEnd();
      return new AstNode.PileInit(pile, items, line);
    }
    if (lineHasNullClear()) {
      parser.consumeUntilLineEnd();
      return new AstNode.PileClear(pile, line);
    }
    return null;
  }

  private String findPileOnLine() {
    String name =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex(), parser.lineEndIndex());
    if (name == null || !ConfigLoader.isPile(name)) {
      return null;
    }
    return name;
  }

  private boolean lineHasNullClear() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isNullClearWord(parser.normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  private List<ParseExpression> parseItemsAfterHolds() {
    int holdsAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.HOLDS_WORDS.contains(parser.normalise(token.value()))) {
        holdsAt = offset;
        break;
      }
    }
    List<ParseExpression> items = new ArrayList<>();
    if (holdsAt < 0) {
      return items;
    }
    for (int offset = holdsAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.COMMA)) {
        continue;
      }
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.LIST_AND_WORDS.contains(parser.normalise(token.value()))) {
        continue;
      }
      if (token.is(TokenType.STRING)) {
        items.add(new ParseExpression.StringLiteral(token.value()));
      } else if (token.is(TokenType.NUMBER)) {
        items.add(
            new ParseExpression.NumberLiteral(Double.parseDouble(parser.normalise(token.value()))));
      }
    }
    return items;
  }
}
