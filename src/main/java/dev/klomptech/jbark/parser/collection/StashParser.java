package dev.klomptech.jbark.parser.collection;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.expression.ValueParser;
import java.util.ArrayList;
import java.util.List;

public class StashParser {

  private final Parser parser;

  public StashParser(final Parser parser) {
    this.parser = parser;
  }

  // Fill or clear a registered stash (my list type) on the line
  public AstNode parseStashLine() throws BarkError {
    int line = parser.peek().line();
    String stash = findStashOnLine();
    if (stash == null) {
      return null;
    }
    // Make sure the stash is registered
    ConfigLoader.requireStash(stash, line);

    // Look for the holds keyword
    if (parser.lineHasWord(Keywords.HOLDS_WORDS)) {
      List<ParseExpression> items = parseItemsAfterHolds();
      if (items.isEmpty()) {
        throw new BarkError(line, "The jar is open but nothing went inside. How dare you?");
      }
      parser.consumeUntilLineEnd();
      return new AstNode.StashInit(stash, items, line);
    }
    // Stash gains one item
    if (parser.lineHasWord(Keywords.STASH_ADD_WORDS) && !lineHasFrom()) {
      ParseExpression item = parseItemAfterStashAdd();
      if (item == null) {
        throw new BarkError(
            line, "You didn't say what will be added to the jar for the dogs to steal later!");
      }
      parser.consumeUntilLineEnd();
      return new AstNode.StashAppend(stash, item, line);
    }
    // Example: "her cookie jar drops first" removes the item in that list slot
    if (parser.lineHasWord(Keywords.DROPS_WORDS)) {
      String slot = findJarSlotOnLine();
      if (slot == null) {
        throw new BarkError(line, "Which item? The dogs need to know which one to steal");
      }
      parser.consumeUntilLineEnd();
      return new AstNode.StashRemove(stash, slot, line);
    }
    if (!lineHasFrom() && !parser.lineHasWord(Keywords.TAKE_FROM_STASH_WORDS)) {
      // slot is first, second, third, last, etc
      String slot = findJarSlotOnLine();
      if (slot != null) {
        ParseExpression newValue = parseNewValueAfterSlot(slot);
        if (newValue == null) {
          return null;
        }
        parser.consumeUntilLineEnd();
        return new AstNode.StashSet(stash, slot, newValue, line);
      }
    }
    // Look for the clear keyword
    if (lineHasNullClear()) {
      parser.consumeUntilLineEnd();
      return new AstNode.StashClear(stash, line);
    }
    return null;
  }

  private String findStashOnLine() {
    // Look for the stash name
    String name =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex(), parser.lineEndIndex());
    if (name == null || !ConfigLoader.isStash(name)) {
      return null;
    }
    return name;
  }

  private boolean lineHasFrom() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.FROM_WORDS.contains(parser.normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  private boolean lineHasNullClear() {
    // Look for the clear keyword (which means empty list)
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isNullClearWord(parser.normalise(token.value()))) {
        return true;
      }
    }
    return false;
  }

  // List slot word on the line: first, second, third, last, etc
  private String findJarSlotOnLine() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (StashSpots.isStashSpot(word)) {
        return word;
      }
    }
    return null;
  }

  // Value after the slot word: a plain number, a string, or a full expression
  private ParseExpression parseNewValueAfterSlot(final String slot) throws BarkError {
    int slotAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && slot.equals(parser.normalise(token.value()))) {
        slotAt = offset;
        break;
      }
    }
    if (slotAt < 0) {
      return null;
    }
    int valueStart = -1;
    for (int offset = slotAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isAssignSubjectGlue(parser.normalise(token.value()))) {
        valueStart = offset + 1;
        break;
      }
    }
    if (valueStart < 0) {
      return null;
    }
    return new ValueParser(parser).parsePart(valueStart, parser.countTokensAhead(0));
  }

  private List<ParseExpression> parseItemsAfterHolds() {
    int holdsAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      // Look for the holds keyword
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
      // Skip commas and list and words
      if (token.is(TokenType.COMMA)) {
        continue;
      }
      // Skip list and words
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.LIST_AND_WORDS.contains(parser.normalise(token.value()))) {
        continue;
      }
      // Look for the items
      if (token.is(TokenType.STRING)) {
        items.add(new ParseExpression.StringLiteral(token.value()));
        // Look for the numbers
      } else if (token.is(TokenType.NUMBER)) {
        items.add(
            new ParseExpression.NumberLiteral(Double.parseDouble(parser.normalise(token.value()))));
      }
    }
    return items;
  }

  // First string or number after stow/snag/get/collect on this line
  private ParseExpression parseItemAfterStashAdd() {
    int addAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      // Look for the stash add word
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.STASH_ADD_WORDS.contains(parser.normalise(token.value()))) {
        addAt = offset;
        break;
      }
    }
    if (addAt < 0) {
      return null;
    }
    // Look for the item
    for (int offset = addAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
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
}
