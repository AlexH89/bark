package dev.klomptech.jbark.parser.printing;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.collection.StashSpots;
import dev.klomptech.jbark.parser.expression.ValueParser;
import dev.klomptech.jbark.print.PrintStyle;
import java.util.List;

public class PrintParser {

  private final Parser parser;
  private final ValueParser valueParser;

  private record PrintParts(PrintStyle style, List<ParseExpression> values, String dogSpeaker) {}

  public PrintParser(final Parser parser) {
    this.parser = parser;
    this.valueParser = new ValueParser(parser);
  }

  // Full print line from current cursor to newline
  public AstNode parsePrintLine() throws BarkError {
    int line = parser.peek().line();
    PrintStyle tripleStyle = findTriplePrintStyle();
    if (tripleStyle != null) {
      parser.consumeUntilLineEnd();
      return new AstNode.Print(tripleStyle, List.of(), line, true, null);
    }
    return parsePrintForNextTokens(parser.countTokensAhead(0));
  }

  // Parse a print using only the next tokenCount tokens from the cursor
  // Advances the cursor by tokenCount (next token could be "otherwise")
  public AstNode parsePrintForNextTokens(final int tokenCount) throws BarkError {
    PrintParts parts = findPrintPartsInNextTokens(tokenCount);
    AstNode node = toPrintNode(parts, parser.peek().line());
    parser.advanceBy(tokenCount);
    return node;
  }

  private AstNode toPrintNode(final PrintParts parts, final int line) {
    List<ParseExpression> values = parts.values();
    if (values == null || values.isEmpty()) {
      values = List.of(new ParseExpression.Empty());
    }
    return new AstNode.Print(
        parts.style() != null ? parts.style() : PrintStyle.BARK,
        values,
        line,
        false,
        parts.dogSpeaker());
  }

  private PrintParts findPrintPartsInNextTokens(final int tokenCount) throws BarkError {
    List<ParseExpression> printValues = null;
    PrintVerb printVerb = new PrintVerb(null, -1);
    for (int offset = 0; offset < tokenCount && !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      capturePrintVerb(printVerb, token, offset);
      // It needs to support bark "hello", bark 2 plus 3 but also bark add with 2, 3
      if (printValues == null
          && printVerb.getOffset() >= 0
          && offset == printVerb.getOffset() + 1) {
        boolean loneValue = offset + 1 >= tokenCount;
        if (loneValue && token.is(TokenType.STRING)) {
          printValues = List.of(new ParseExpression.StringLiteral(token.value()));
        } else if (loneValue && token.is(TokenType.NUMBER)) {
          double num = Double.parseDouble(parser.normalise(token.value()));
          printValues = List.of(new ParseExpression.NumberLiteral(num));
        }
      }
      if (printVerb.getOffset() >= 0 && printValues != null) {
        break;
      }
    }
    // After the verb: math first (example: growl memory minus how many treats she has)
    if (printValues == null && printVerb.getOffset() >= 0) {
      int afterVerb = printVerb.getOffset();
      if (hasMathAfterVerb(afterVerb, tokenCount)) {
        printValues = findValuesAfterVerb(afterVerb, tokenCount);
      } else {
        ParseExpression phrase = findHowManyItemsInCollection(afterVerb, tokenCount);
        if (phrase == null) {
          phrase = findTopOfPile(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findStashSlotFromJar(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findTraitCheck(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findHowManyTopicInCollection(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findHowManyFieldAfterVerb(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findCollectionAfterVerb(afterVerb, tokenCount);
        }
        if (phrase == null) {
          phrase = findFieldAfterVerb(afterVerb, tokenCount);
        }
        if (phrase != null) {
          printValues = List.of(phrase);
        } else if (shouldUseValueAfterVerb(afterVerb, tokenCount)) {
          printValues = findValuesAfterVerb(afterVerb, tokenCount);
        }
      }
    }
    String dogSpeaker = findDogSpeakerBeforePrintVerb(printVerb.getOffset());
    return new PrintParts(printVerb.getStyle(), printValues, dogSpeaker);
  }

  // Example: "she woofs how many items are in the cookie jar"
  private ParseExpression findHowManyItemsInCollection(
      final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount - 3; offset++) {
      if (!wordAt(offset, Keywords.HOW_WORDS)) {
        continue;
      }
      if (!wordAt(offset + 1, Keywords.MANY_WORDS)) {
        continue;
      }
      if (!wordAt(offset + 2, Keywords.ITEMS_WORDS)) {
        continue;
      }
      if (!wordAt(offset + 3, Keywords.IN_WORDS)) {
        continue;
      }
      String name =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + offset + 4, parser.lineEndIndex());
      if (name == null) {
        return null;
      }
      if (ConfigLoader.isStash(name)) {
        return new ParseExpression.StashAccess(name, ParseExpression.StashPart.COUNT, null);
      }
      if (ConfigLoader.isPile(name)) {
        return new ParseExpression.PileAccess(name, ParseExpression.PilePart.COUNT);
      }
    }
    return null;
  }

  // Example: "she woofs the top of the laundry basket"
  private ParseExpression findTopOfPile(final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      if (!wordAt(offset, Keywords.TOP_WORDS)) {
        continue;
      }
      int nameStart = offset + 1;
      if (nameStart < tokenCount && wordAt(nameStart, Keywords.OF_WORDS)) {
        nameStart++;
      }
      String name =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + nameStart, parser.lineEndIndex());
      if (name != null && ConfigLoader.isPile(name)) {
        return new ParseExpression.PileAccess(name, ParseExpression.PilePart.TOP);
      }
    }
    return null;
  }

  // Example: "she woofs the first biscuit from her cookie jar"
  private ParseExpression findStashSlotFromJar(final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String spot = parser.normalise(token.value());
      if (!StashSpots.isStashSpot(spot)) {
        continue;
      }
      int fromAt = findFromAfter(offset + 1, tokenCount);
      if (fromAt < 0) {
        continue;
      }
      String stash =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + fromAt + 1, parser.lineEndIndex());
      if (stash != null && ConfigLoader.isStash(stash)) {
        return new ParseExpression.StashAccess(stash, ParseExpression.StashPart.ELEMENT, spot);
      }
    }
    return null;
  }

  // Example: "bark my labrador is loud" prints true or false
  private ParseExpression findTraitCheck(final int printOffset, final int tokenCount) {
    String breed = null;
    int traitAt = -1;
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      Token token = parser.peekAt(offset);
      if (breed == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (ConfigLoader.isBreed(word)) {
          breed = word;
        }
      }
      if (traitAt < 0
          && token.is(TokenType.IDENTIFIER)
          && Keywords.isTraitKeyword(parser.normalise(token.value()))) {
        traitAt = offset;
      }
      if (token.is(TokenType.NUMBER)) {
        return null;
      }
    }
    if (breed == null || traitAt < 0) {
      return null;
    }
    ParseExpression check =
        new ParseExpression.HasTrait(breed, parser.normalise(parser.peekAt(traitAt).value()));
    if (hasNotBefore(traitAt, printOffset + 1)) {
      return new ParseExpression.Not(check);
    }
    return check;
  }

  private int findFromAfter(final int start, final int tokenCount) {
    for (int offset = start; offset < tokenCount; offset++) {
      if (wordAt(offset, Keywords.FROM_WORDS)) {
        return offset;
      }
    }
    return -1;
  }

  private boolean hasNotBefore(final int traitAt, final int start) {
    for (int offset = start; offset < traitAt; offset++) {
      if (wordAt(offset, Keywords.NOT_KEYWORDS)) {
        return true;
      }
    }
    return false;
  }

  private boolean wordAt(final int offset, final List<String> words) {
    Token token = parser.peekAt(offset);
    return token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()));
  }

  // Example: "bark add with 2, 3" or "growl 2, "missing!""
  private List<ParseExpression> findValuesAfterVerb(final int printOffset, final int tokenCount)
      throws BarkError {
    int start = parser.skipIgnoredIdentifiers(printOffset + 1, tokenCount);
    if (looksLikeTrickCallAfterVerb(start, tokenCount)) {
      return List.of(valueParser.parsePart(start, tokenCount));
    }
    if (hasCommaAfterVerb(start, tokenCount)) {
      return valueParser.parseCommaSeparated(start, tokenCount);
    }
    return List.of(valueParser.parsePart(start, tokenCount));
  }

  // Example: bark add with 2, 3, comma is between trick args, not a print list
  private boolean looksLikeTrickCallAfterVerb(final int start, final int tokenCount) {
    for (int offset = start; offset < tokenCount - 1; offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && wordAt(offset + 1, Keywords.WITH_WORDS)) {
        return true;
      }
    }
    return false;
  }

  private boolean hasMathAfterVerb(final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.PLUS)
          || token.is(TokenType.MINUS)
          || token.is(TokenType.STAR)
          || token.is(TokenType.SLASH)) {
        return true;
      }
      if (token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isMathAddKeyword(word)
            || Keywords.isMathSubtractKeyword(word)
            || Keywords.STAR_KEYWORDS.contains(word)
            || Keywords.SLASH_KEYWORDS.contains(word)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean hasCommaAfterVerb(final int start, final int tokenCount) {
    for (int offset = start; offset < tokenCount; offset++) {
      if (parser.peekAt(offset).is(TokenType.COMMA)) {
        return true;
      }
    }
    return false;
  }

  private boolean shouldUseValueAfterVerb(final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isIgnored(parser.normalise(token.value()))) {
        continue;
      }
      if (tokenLooksLikeExpressionValue(token, offset)) {
        return true;
      }
    }
    return false;
  }

  private boolean tokenLooksLikeExpressionValue(final Token token, final int offset) {
    return switch (token.type()) {
      case PLUS, MINUS, STAR, SLASH, NUMBER, STRING -> true;
      case IDENTIFIER -> identifierLooksLikeExpressionValue(token, offset);
      default -> false;
    };
  }

  private boolean identifierLooksLikeExpressionValue(final Token token, final int offset) {
    String word = parser.normalise(token.value());
    if (Keywords.isPrintExpressionCue(word)) {
      return true;
    }
    if (ConfigLoader.isValidName(word)) {
      return true;
    }
    if (ConfigLoader.isStash(word) || ConfigLoader.isPile(word)) {
      return true;
    }
    return ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex() + offset, parser.lineEndIndex())
        != null;
  }

  // Example: "she woofs her name" or "he growls his age"
  private ParseExpression findFieldAfterVerb(final int printOffset, final int tokenCount) {
    String who = null;
    String what = null;
    for (int offset = printOffset + 1; offset < tokenCount; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.isAttributeKeyword(word)) {
        what = Keywords.resolveAttributeTopic(word);
      } else if (Keywords.isDogSubjectReference(word, token.value())) {
        who = word;
      }
    }
    if (who != null && what != null) {
      return new ParseExpression.Field(who, what);
    }
    return null;
  }

  // Example: "bark her cookie jar" or "bark the toy box"
  private ParseExpression findCollectionAfterVerb(final int printOffset, final int tokenCount) {
    int start = parser.skipIgnoredIdentifiers(printOffset + 1, tokenCount);
    if (start >= tokenCount || wordAt(start, Keywords.HOW_WORDS)) {
      return null;
    }
    String name =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex() + start, parser.lineEndIndex());
    if (name == null) {
      return null;
    }
    if (ConfigLoader.isStash(name)) {
      return new ParseExpression.StashAccess(name, ParseExpression.StashPart.ALL, null);
    }
    if (ConfigLoader.isPile(name)) {
      return new ParseExpression.PileAccess(name, ParseExpression.PilePart.ALL);
    }
    return null;
  }

  // Example: "bark how many toys the toy box has"
  private ParseExpression findHowManyTopicInCollection(
      final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount - 2; offset++) {
      if (!wordAt(offset, Keywords.HOW_WORDS)) {
        continue;
      }
      if (!wordAt(offset + 1, Keywords.MANY_WORDS)) {
        continue;
      }
      int topicOffset = offset + 2;
      if (topicOffset >= tokenCount) {
        return null;
      }
      Token topicToken = parser.peekAt(topicOffset);
      if (!topicToken.is(TokenType.IDENTIFIER)) {
        continue;
      }
      if (Keywords.explicitAttributeTopic(parser.normalise(topicToken.value())) == null) {
        continue;
      }
      String name =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + topicOffset + 1, parser.lineEndIndex());
      if (name == null) {
        continue;
      }
      if (ConfigLoader.isStash(name)) {
        return new ParseExpression.StashAccess(name, ParseExpression.StashPart.COUNT, null);
      }
      if (ConfigLoader.isPile(name)) {
        return new ParseExpression.PileAccess(name, ParseExpression.PilePart.COUNT);
      }
    }
    return null;
  }

  // Example: "she woofs how many toys she has"
  private ParseExpression findHowManyFieldAfterVerb(final int printOffset, final int tokenCount) {
    for (int offset = printOffset + 1; offset < tokenCount - 1; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      // Phrase must start with how (or synonym)
      String word = parser.normalise(token.value());
      if (!Keywords.HOW_WORDS.contains(word)) {
        continue;
      }
      // Must be many right after how
      Token manyToken = parser.peekAt(offset + 1);
      if (!manyToken.is(TokenType.IDENTIFIER)) {
        continue;
      }
      if (!Keywords.MANY_WORDS.contains(parser.normalise(manyToken.value()))) {
        continue;
      }
      // Topic is the next token after many
      int topicOffset = offset + 2;
      if (topicOffset >= tokenCount) {
        return null;
      }
      Token topicToken = parser.peekAt(topicOffset);
      if (!topicToken.is(TokenType.IDENTIFIER)) {
        return null;
      }
      String topic = Keywords.explicitAttributeTopic(parser.normalise(topicToken.value()));
      if (topic == null) {
        continue;
      }
      // Who comes after the topic: she, Bimba, the golden retriever, ...
      int whoStart = topicOffset + 1;
      if (whoStart >= tokenCount) {
        return null;
      }
      String who =
          ConfigLoader.resolveNameFromTokens(
              parser.allTokens(), parser.cursorIndex() + whoStart, parser.lineEndIndex());
      if (who == null) {
        Token whoToken = parser.peekAt(whoStart);
        if (!whoToken.is(TokenType.IDENTIFIER)) {
          return null;
        }
        String whoWord = parser.normalise(whoToken.value());
        if (Keywords.isDogSubjectReference(whoWord, whoToken.value())) {
          who = whoWord;
        }
      }
      if (who != null && (ConfigLoader.isValidName(who) || Keywords.isDogSubjectReference(who))) {
        return new ParseExpression.Field(who, topic);
      }
    }
    return null;
  }

  private void capturePrintVerb(final PrintVerb printVerb, final Token token, final int offset) {
    if (!token.is(TokenType.IDENTIFIER)) {
      return;
    }
    String word = parser.normalise(token.value());
    if (!Keywords.PRINT_KEYWORDS.contains(word)) {
      return;
    }
    if (printVerb.getOffset() >= 0) {
      return;
    }
    PrintStyle style = Keywords.PRINT_STYLES.getOrDefault(word, PrintStyle.BARK);
    printVerb.setStyle(style);
    printVerb.setOffset(offset);
  }

  private String findDogSpeakerBeforePrintVerb(final int printOffset) {
    if (printOffset <= 0) {
      return null;
    }
    for (int offset = 0; offset < printOffset; offset++) {
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

  private PrintStyle findTriplePrintStyle() {
    String printWord = null;
    int printCount = 0;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.PRINT_KEYWORDS.contains(word)) {
        if (printWord != null && !printWord.equals(word)) {
          return null;
        }
        printWord = word;
        printCount++;
      } else if (Keywords.isHeard(word)) {
        return null;
      }
    }
    if (printCount >= 3) {
      return Keywords.PRINT_STYLES.getOrDefault(printWord, PrintStyle.BARK);
    }
    return null;
  }
}
