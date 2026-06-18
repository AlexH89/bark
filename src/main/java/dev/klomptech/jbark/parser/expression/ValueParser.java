package dev.klomptech.jbark.parser.expression;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.collection.StashSpots;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Reads a value from part of the current line (math, trick calls, stash, etc.)
public class ValueParser {

  private final Parser parser;

  // parsed value, and the next word offset to read from
  private record Step(ParseExpression value, int next) {}

  public ValueParser(final Parser parser) {
    this.parser = parser;
  }

  public ParseExpression parsePart(final int startOffset, final int endOffset) throws BarkError {
    if (startOffset >= endOffset) {
      return new ParseExpression.Empty();
    }
    return readExpression(startOffset, endOffset).value();
  }

  // Comma-separated print values, each piece between commas is one expression: growl 2, "hi!"
  public List<ParseExpression> parseCommaSeparated(final int startOffset, final int endOffset)
      throws BarkError {
    List<ParseExpression> values = new ArrayList<>();
    int pos = startOffset;
    while (pos < endOffset) {
      while (pos < endOffset && parser.peekAt(pos).is(TokenType.COMMA)) {
        pos++;
      }
      if (pos >= endOffset) {
        break;
      }
      Step step = readExpression(pos, endOffset);
      values.add(step.value());
      pos = step.next();
    }
    return values;
  }

  // One value on the line: a string, a number, a name, or math like a plus b
  private Step readExpression(final int from, final int to) throws BarkError {
    Step step = readMultiplyOrDivide(from, to);
    while (step.next() < to) {
      ParseExpression.BinaryOp op = addOpAt(step.next());
      if (op == null) {
        break;
      }
      Step right = readMultiplyOrDivide(step.next() + 1, to);
      step = new Step(new ParseExpression.Binary(step.value(), op, right.value()), right.next());
    }
    return step;
  }

  // Example: 2 times 3
  private Step readMultiplyOrDivide(final int from, final int to) throws BarkError {
    Step step = readSingleValue(from, to);
    while (step.next() < to) {
      ParseExpression.BinaryOp op = mulOpAt(step.next());
      if (op == null) {
        break;
      }
      Step right = readSingleValue(step.next() + 1, to);
      step = new Step(new ParseExpression.Binary(step.value(), op, right.value()), right.next());
    }
    return step;
  }

  // One whole value: not false, a number, a string, a trick call, and so on
  private Step readSingleValue(final int from, final int to) throws BarkError {
    if (from >= to) {
      return new Step(new ParseExpression.Empty(), from);
    }
    if (wordAt(from, Keywords.NOT_KEYWORDS)) {
      Step inner = readSingleValue(from + 1, to);
      return new Step(new ParseExpression.Not(inner.value()), inner.next());
    }
    Step start = readNumberStringOrWord(from, to);
    return readWordsAfterValue(start, to);
  }

  // The first piece: a quoted string, a number, or a word
  private Step readNumberStringOrWord(final int from, final int to) throws BarkError {
    Token token = parser.peekAt(from);
    return switch (token.type()) {
      case STRING -> new Step(new ParseExpression.StringLiteral(token.value()), from + 1);
      case NUMBER ->
          new Step(
              new ParseExpression.NumberLiteral(
                  Double.parseDouble(parser.normalise(token.value()))),
              from + 1);
      case IDENTIFIER -> readWord(from, to);
      default ->
          throw new BarkError(
              token.line(), "The dogs don't know what to do with \"" + token.value() + "\" here.");
    };
  }

  // Extra word after a string, like letters in bark "woof" letters
  private Step readWordsAfterValue(final Step step, final int to) {
    int pos = step.next();
    if (pos < to && wordAt(pos, Keywords.LETTERS_WORDS)) {
      return new Step(new ParseExpression.Length(step.value()), pos + 1);
    }
    return step;
  }

  // One identifier: a jar/pile name, nothing, true/false, a trick call, or a plain name
  private Step readWord(final int from, final int to) throws BarkError {
    // Example: "how many toys she has"
    Step howMany = tryParseHowManyFieldCount(from, to);
    if (howMany != null) {
      return howMany;
    }
    // "count of the items in the cookie jar" means how many things are in that jar
    Step jarCount = tryParseItemsInJarCount(from, to);
    if (jarCount != null) {
      return jarCount;
    }
    Step collection = readCollection(from, to);
    if (collection != null) {
      return collection;
    }
    String word = parser.normalise(parser.peekAt(from).value());
    if (Keywords.TYPE_NOTHING_WORDS.contains(word) || Keywords.NULL_VALUES.contains(word)) {
      return new Step(new ParseExpression.NullLiteral(), from + 1);
    }
    Optional<Boolean> bool = Keywords.parseBoolean(word);
    if (bool.isPresent()) {
      return new Step(new ParseExpression.BooleanLiteral(bool.get()), from + 1);
    }
    if (from + 1 < to && wordAt(from + 1, Keywords.WITH_WORDS)) {
      return readTrickCall(word, from + 2, to);
    }
    return new Step(new ParseExpression.Variable(word), from + 1);
  }

  // Example: "how many treats Bimba has" or "how many toys the labrador has"
  private Step tryParseHowManyFieldCount(final int from, final int to) {
    if (from + 2 >= to
        || !wordAt(from, Keywords.HOW_WORDS)
        || !wordAt(from + 1, Keywords.MANY_WORDS)) {
      return null;
    }
    int topicOffset = from + 2;
    if (!parser.peekAt(topicOffset).is(TokenType.IDENTIFIER)) {
      return null;
    }
    String topic =
        Keywords.explicitAttributeTopic(parser.normalise(parser.peekAt(topicOffset).value()));
    if (topic == null) {
      return null;
    }
    int whoStart = topicOffset + 1;
    if (whoStart >= to) {
      return null;
    }
    String who =
        ConfigLoader.resolveNameFromTokens(
            parser.allTokens(), parser.cursorIndex() + whoStart, parser.cursorIndex() + to);
    int next;
    if (who == null) {
      Token whoToken = parser.peekAt(whoStart);
      if (!whoToken.is(TokenType.IDENTIFIER)) {
        return null;
      }
      String whoWord = parser.normalise(whoToken.value());
      if (!Keywords.isDogSubjectReference(whoWord, whoToken.value())) {
        return null;
      }
      who = whoWord;
      next = whoStart + 1;
    } else {
      int whoEnd =
          ConfigLoader.tokenIndexAfterResolvedName(
              parser.allTokens(), parser.cursorIndex() + whoStart, parser.cursorIndex() + to, who);
      next = whoEnd < 0 ? whoStart + 1 : whoEnd - parser.cursorIndex();
    }
    if (next < to && Keywords.isAssignSubjectGlue(parser.normalise(parser.peekAt(next).value()))) {
      next++;
    }
    return new Step(new ParseExpression.Field(who, topic), next);
  }

  // Phrase: count of items in <jar or pile>. Story words between those keywords are ignored.
  private Step tryParseItemsInJarCount(final int from, final int to) throws BarkError {
    if (!wordAt(from, Keywords.COUNT_WORDS)) {
      return null;
    }
    int pos = from + 1;
    if (pos >= to || !wordAt(pos, Keywords.OF_WORDS)) {
      return null;
    }
    pos = findNextPhraseWord(pos + 1, to, Keywords.ITEMS_WORDS);
    if (pos < 0) {
      return null;
    }
    pos = findNextPhraseWord(pos + 1, to, Keywords.IN_WORDS);
    if (pos < 0) {
      return null;
    }
    int afterIn = pos + 1;
    String name =
        ConfigLoader.resolveCollectionFromTokens(
            parser.allTokens(), parser.cursorIndex() + afterIn, parser.cursorIndex() + to);
    if (name == null) {
      throw new BarkError(
          parser.peekAt(from).line(),
          "The dogs were waiting for a jar to investigate, but you never gave them one!");
    }
    int afterName = afterIn;
    for (int end = afterIn + 1; end <= Math.min(to, afterIn + 4); end++) {
      String found =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + afterIn, parser.cursorIndex() + end);
      if (name.equals(found)) {
        afterName = end;
        break;
      }
    }
    if (ConfigLoader.isStash(name)) {
      return new Step(
          new ParseExpression.StashAccess(name, ParseExpression.StashPart.COUNT, null), afterName);
    }
    if (ConfigLoader.isPile(name)) {
      return new Step(
          new ParseExpression.PileAccess(name, ParseExpression.PilePart.COUNT), afterName);
    }
    // A name followed "in" but it is not a registered jar or pile
    throw new BarkError(
        parser.peekAt(from).line(),
        "The dogs are confused by what you wanted them to investigate! That was not a jar!");
  }

  // Scan forward for the next required phrase word; skip anything the dogs do not recognize here
  private int findNextPhraseWord(final int from, final int to, final List<String> words) {
    for (int pos = from; pos < to; pos++) {
      if (wordAt(pos, words)) {
        return pos;
      }
    }
    return -1;
  }

  // cookie_jar or her cookie jar when the line is talking about a jar or pile
  private Step readCollection(final int from, final int to) throws BarkError {
    int absStart = parser.cursorIndex() + from;
    int absEnd = parser.cursorIndex() + to;
    String name = ConfigLoader.resolveCollectionFromTokens(parser.allTokens(), absStart, absEnd);
    if (name == null) {
      return null;
    }
    int afterName = countTokensInCollectionName(from, to, name);
    if (ConfigLoader.isStash(name)) {
      return readStashValue(name, afterName, to);
    }
    if (ConfigLoader.isPile(name)) {
      return readPileValue(name, afterName, to);
    }
    return null;
  }

  // How many words the jar or pile name takes (cookie_jar is one word, her cookie jar is three)
  private int countTokensInCollectionName(final int from, final int to, final String name) {
    for (int end = from + 1; end <= Math.min(to, from + 4); end++) {
      String found =
          ConfigLoader.resolveCollectionFromTokens(
              parser.allTokens(), parser.cursorIndex() + from, parser.cursorIndex() + end);
      if (name.equals(found)) {
        return end;
      }
    }
    return from + 1;
  }

  // After the jar name: all items, one slot, or count
  private Step readStashValue(final String stash, final int from, final int to) throws BarkError {
    int pos = parser.skipIgnoredIdentifiers(from, to);
    if (pos < to && parser.peekAt(pos).is(TokenType.IDENTIFIER)) {
      String spot = parser.normalise(parser.peekAt(pos).value());
      if (StashSpots.isStashSpot(spot)) {
        return new Step(
            new ParseExpression.StashAccess(stash, ParseExpression.StashPart.ELEMENT, spot),
            pos + 1);
      }
    }
    if (pos < to && wordAt(pos, Keywords.COUNT_WORDS)) {
      return new Step(
          new ParseExpression.StashAccess(stash, ParseExpression.StashPart.COUNT, null), pos + 1);
    }
    return new Step(
        new ParseExpression.StashAccess(stash, ParseExpression.StashPart.ALL, null), pos);
  }

  // bark laundry_basket prints everything in that pile
  private Step readPileValue(final String pile, final int from, final int to) {
    int pos = parser.skipIgnoredIdentifiers(from, to);
    return new Step(new ParseExpression.PileAccess(pile, ParseExpression.PilePart.ALL), pos);
  }

  // Example: add with 2, 3
  private Step readTrickCall(final String name, final int from, final int to) throws BarkError {
    List<ParseExpression> args = new ArrayList<>();
    int pos = from;
    while (pos < to) {
      Step arg = readExpression(pos, to);
      args.add(arg.value());
      pos = arg.next();
      if (pos < to && Keywords.isListSeparator(parser.peekAt(pos))) {
        pos++;
        continue;
      }
      break;
    }
    return new Step(new ParseExpression.FunctionCall(name, args), pos);
  }

  // Is this word plus or minus?
  private ParseExpression.BinaryOp addOpAt(final int offset) {
    Token token = parser.peekAt(offset);
    if (token.is(TokenType.PLUS)) {
      return ParseExpression.BinaryOp.PLUS;
    }
    if (token.is(TokenType.MINUS)) {
      return ParseExpression.BinaryOp.MINUS;
    }
    if (token.is(TokenType.IDENTIFIER)) {
      String word = parser.normalise(token.value());
      if (Keywords.isMathAddKeyword(word)) {
        return ParseExpression.BinaryOp.PLUS;
      }
      if (Keywords.isMathSubtractKeyword(word)) {
        return ParseExpression.BinaryOp.MINUS;
      }
    }
    return null;
  }

  // Is this word times or divided by?
  private ParseExpression.BinaryOp mulOpAt(final int offset) {
    Token token = parser.peekAt(offset);
    if (token.is(TokenType.STAR)) {
      return ParseExpression.BinaryOp.STAR;
    }
    if (token.is(TokenType.SLASH)) {
      return ParseExpression.BinaryOp.SLASH;
    }
    if (token.is(TokenType.IDENTIFIER)) {
      String word = parser.normalise(token.value());
      if (Keywords.STAR_KEYWORDS.contains(word)) {
        return ParseExpression.BinaryOp.STAR;
      }
      if (Keywords.SLASH_KEYWORDS.contains(word)) {
        return ParseExpression.BinaryOp.SLASH;
      }
    }
    return null;
  }

  private boolean wordAt(final int offset, final List<String> words) {
    Token token = parser.peekAt(offset);
    return token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()));
  }
}
