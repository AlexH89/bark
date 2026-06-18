package dev.klomptech.jbark.parser.expression;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import java.util.List;

// Example: when she has 2 toys then
// Example: when she has 3 toys and my labrador is loud then
// Example: when my labrador does not have 9 toys then
// Example: when neither my labrador nor my beagle has 9 toys then
public class ConditionParser {

  private final Parser parser;

  // amount + topic from "9 toys"
  private record CountAndTopic(double amount, String topic) {}

  public ConditionParser(final Parser parser) {
    this.parser = parser;
  }

  // Words between when and then
  public ParseExpression parseCondition(final int start, final int end, final int line)
      throws BarkError {
    if (wordAt(start, Keywords.NEITHER_WORDS)) {
      return parseNeitherCondition(start, end, line);
    }
    return parseAndJoinedTests(start, end, line);
  }

  // Example: "neither my labrador nor my beagle has 9 toys"
  private ParseExpression parseNeitherCondition(final int start, final int end, final int line)
      throws BarkError {
    int norAt = findKeywordIndex(start + 1, end, Keywords.NOR_WORDS);
    if (norAt < 0) {
      throw new BarkError(
          line,
          "This command without nor really confused the dogs... They are doing random stuff now!");
    }
    String firstDog = findBreedName(start + 1, norAt);
    String secondDog = findBreedName(norAt + 1, end);
    CountAndTopic count = readCountAndTopic(norAt + 1, end);
    if (firstDog == null || secondDog == null || count == null) {
      throw new BarkError(
          line, "Either one of the dogs or the toy is missing! Did you look around?");
    }
    ParseExpression firstHas =
        new ParseExpression.HasExact(firstDog, count.topic(), count.amount());
    ParseExpression secondHas =
        new ParseExpression.HasExact(secondDog, count.topic(), count.amount());
    return new ParseExpression.Logical(firstHas, ParseExpression.LogicalOp.NOR, secondHas);
  }

  // One test, or several joined by 'and'. Each part ends at 'and' or at 'then'
  private ParseExpression parseAndJoinedTests(final int start, final int end, final int line)
      throws BarkError {
    ParseExpression combined = null;
    int testStart = start;
    while (testStart < end) {
      int andAt = findKeywordIndex(testStart, end, Keywords.CONDITION_AND_WORDS);
      int testEnd = andAt >= 0 ? andAt : end;
      // Read one test between when and then (or one half before 'and')
      ParseExpression oneTest = parseOneTest(testStart, testEnd, line);
      if (combined == null) {
        combined = oneTest;
      } else {
        combined = new ParseExpression.Logical(combined, ParseExpression.LogicalOp.AND, oneTest);
      }
      // No 'and' on the line, one test only
      if (andAt < 0) {
        return combined;
      }
      testStart = andAt + 1;
    }
    return combined;
  }

  // Pick which phrase this part is. Order matters, 'more than' before 'has'
  private ParseExpression parseOneTest(final int start, final int end, final int line)
      throws BarkError {
    ParseExpression test = matchMoreThanCount(start, end);
    if (test != null) {
      return test;
    }
    // Example: "she sniffs less toys than 8"
    test = matchSniffsLessThan(start, end);
    if (test != null) {
      return test;
    }
    // Example: "my labrador is loud"
    test = matchTraitCheck(start, end);
    if (test != null) {
      return test;
    }
    // Example: "she has 2 toys"
    test = matchExactCount(start, end);
    if (test != null) {
      return test;
    }
    throw new BarkError(
        line, "The dogs tilted their heads at you as they have no clue what you want them to do!");
  }

  // "she has more than 2 toys"
  private ParseExpression matchMoreThanCount(final int start, final int end) {
    for (int offset = start; offset < end - 1; offset++) {
      // Example: "she has more than 2 toys"
      if (!isMoreThanPhrase(offset)) {
        continue;
      }
      // Pull out who, limit, and topic
      String who = findDogName(start, offset);
      Double limit = null;
      String topic = null;
      for (int scan = offset + 2; scan < end; scan++) {
        Token token = at(scan);
        if (limit == null && token.is(TokenType.NUMBER)) {
          limit = Double.valueOf(parser.normalise(token.value()));
        } else if (limit != null && topic == null && token.is(TokenType.IDENTIFIER)) {
          String resolved = Keywords.explicitAttributeTopic(parser.normalise(token.value()));
          if (resolved != null) {
            topic = resolved;
          }
        }
      }
      if (who == null || limit == null || topic == null) {
        return null;
      }
      // Create the comparison expression
      ParseExpression compare =
          new ParseExpression.Comparison(
              new ParseExpression.Field(who, topic),
              ComparisonOp.GREATER_THAN,
              new ParseExpression.NumberLiteral(limit));
      if (isNegatedBeforeCount(start, end)) {
        return new ParseExpression.Not(compare);
      }
      return compare;
    }
    return null;
  }

  // "she sniffs less toys than 8", find 'sniffs' first, then read the words after it
  private ParseExpression matchSniffsLessThan(final int start, final int end) {
    int sniffsAt = findKeywordIndex(start, end, Keywords.SNIFFS_WORDS);
    if (sniffsAt < 0) {
      return null;
    }
    String who = findDogName(start, sniffsAt);
    int scan = sniffsAt + 1;
    // After 'sniffs', we expect 'less'
    if (scan >= end || !wordAt(scan, Keywords.COMPARISON_LESS_WORDS)) {
      return null;
    }
    scan++;
    // After 'less', we expect a specific attribute topic
    if (scan >= end || !at(scan).is(TokenType.IDENTIFIER)) {
      return null;
    }
    String topic = Keywords.explicitAttributeTopic(parser.normalise(at(scan).value()));
    if (topic == null) {
      return null;
    }
    scan++;
    // After the topic, we expect 'than'
    if (scan >= end || !wordAt(scan, Keywords.THAN_WORDS)) {
      return null;
    }
    scan++;
    // After 'than', we expect a number
    if (scan >= end || !at(scan).is(TokenType.NUMBER)) {
      return null;
    }
    if (who == null) {
      return null;
    }
    double limit = Double.parseDouble(parser.normalise(at(scan).value()));
    ParseExpression compare =
        new ParseExpression.Comparison(
            new ParseExpression.Field(who, topic),
            ComparisonOp.LESS_THAN,
            new ParseExpression.NumberLiteral(limit));
    // Example: "she does not sniff less toys than 8"
    if (isNegatedBeforeCount(start, end)) {
      return new ParseExpression.Not(compare);
    }
    return compare;
  }

  // "my labrador is loud" or "my labrador is not greedy"
  private ParseExpression matchTraitCheck(final int start, final int end) {
    String breed = findBreedName(start, end);
    int traitAt = findTraitWord(start, end);
    if (breed == null || traitAt < 0 || hasNumberInRange(start, end)) {
      return null;
    }
    String trait = parser.normalise(at(traitAt).value());
    ParseExpression check = new ParseExpression.HasTrait(breed, trait);
    if (isNegatedBefore(traitAt, start)) {
      return new ParseExpression.Not(check);
    }
    return check;
  }

  // "she has 2 toys" or "she does not have 9 toys"
  private ParseExpression matchExactCount(final int start, final int end) {
    String who = findDogName(start, end);
    // Example: "she has 2 toys"
    CountAndTopic count = readCountAndTopic(start, end);
    if (who == null || count == null) {
      return null;
    }
    ParseExpression exact = new ParseExpression.HasExact(who, count.topic(), count.amount());
    // Example: "she does not have 9 toys"
    if (isNegatedBeforeCount(start, end)) {
      return new ParseExpression.Not(exact);
    }
    return exact;
  }

  // Pulls "9 toys" out of a word range
  private CountAndTopic readCountAndTopic(final int start, final int end) {
    Double amount = null;
    String topic = null;
    for (int offset = start; offset < end; offset++) {
      Token token = at(offset);
      // Example: "she has 2 toys"
      if (amount == null && token.is(TokenType.NUMBER)) {
        amount = Double.valueOf(parser.normalise(token.value()));
        continue;
      }
      if (amount != null && topic == null && token.is(TokenType.IDENTIFIER)) {
        String resolved = Keywords.explicitAttributeTopic(parser.normalise(token.value()));
        if (Keywords.isAttributeKeyword(resolved)) {
          topic = resolved;
        }
      }
    }
    if (amount == null || topic == null) {
      return null;
    }
    return new CountAndTopic(amount, topic);
  }

  // "does not have 9", 'not' appears before the number
  private boolean isNegatedBeforeCount(final int start, final int end) {
    for (int offset = start; offset < end; offset++) {
      // Example: "she does not have 9 toys"
      if (at(offset).is(TokenType.NUMBER)) {
        return false;
      }
      if (wordAt(offset, Keywords.NOT_KEYWORDS)) {
        return true;
      }
    }
    return false;
  }

  // "is not greedy", 'not' appears before the trait
  private boolean isNegatedBefore(final int before, final int start) {
    for (int offset = start; offset < before; offset++) {
      if (wordAt(offset, Keywords.NOT_KEYWORDS)) {
        return true;
      }
    }
    return false;
  }

  private int findKeywordIndex(final int from, final int end, final List<String> words) {
    for (int offset = from; offset < end; offset++) {
      if (wordAt(offset, words)) {
        return offset;
      }
    }
    return -1;
  }

  private String findDogName(final int start, final int end) {
    for (int offset = start; offset < end; offset++) {
      Token token = at(offset);
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

  private String findBreedName(final int start, final int end) {
    for (int offset = start; offset < end; offset++) {
      Token token = at(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (ConfigLoader.isBreed(word)) {
        return word;
      }
    }
    return null;
  }

  private int findTraitWord(final int start, final int end) {
    for (int offset = start; offset < end; offset++) {
      Token token = at(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isTraitKeyword(parser.normalise(token.value()))) {
        return offset;
      }
    }
    return -1;
  }

  private boolean hasNumberInRange(final int start, final int end) {
    for (int offset = start; offset < end; offset++) {
      if (at(offset).is(TokenType.NUMBER)) {
        return true;
      }
    }
    return false;
  }

  private boolean isMoreThanPhrase(final int offset) {
    return wordAt(offset, Keywords.COMPARISON_GREATER_WORDS)
        && wordAt(offset + 1, Keywords.THAN_WORDS);
  }

  private boolean wordAt(final int offset, final List<String> words) {
    Token token = at(offset);
    return token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()));
  }

  private Token at(final int offset) {
    return parser.peekAt(offset);
  }
}
