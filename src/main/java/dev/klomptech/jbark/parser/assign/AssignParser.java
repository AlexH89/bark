package dev.klomptech.jbark.parser.assign;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.config.DogTopics;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.expression.ValueParser;
import java.util.ArrayList;

public class AssignParser {

  private final Parser parser;

  public AssignParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: "labrador" on its own, just adds a dog to the story
  public AstNode parseRegisterLine() throws BarkError {
    String breed = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (!ConfigLoader.isBreed(word)) {
        continue;
      }
      if (breed != null) {
        return null;
      }
      breed = word;
    }
    if (breed == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Assign(breed, new ParseExpression.NumberLiteral(0), line);
  }

  // Example: "5 balls" sets how many of that object exist in the story
  public AstNode parseLeadingObjectLine() throws BarkError {
    if (!parser.peek().is(TokenType.NUMBER)) {
      return null;
    }
    double value = Double.parseDouble(parser.normalise(parser.peek().value()));
    String object = findObjectAfterLeadingNumber();
    if (object == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Assign(object, new ParseExpression.NumberLiteral(value), line);
  }

  // First registered object word after the number (balls becomes ball)
  private String findObjectAfterLeadingNumber() {
    for (int offset = 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String object = Keywords.resolveObjectName(parser.normalise(token.value()));
      if (object != null) {
        return object;
      }
    }
    return null;
  }

  // Example: "ball 5" sets how many of that object exist (object word then number)
  public AstNode parseObjectCountLine() throws BarkError {
    String object = null;
    Double value = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (object == null && token.is(TokenType.IDENTIFIER)) {
        String resolved = Keywords.resolveObjectName(parser.normalise(token.value()));
        if (resolved != null) {
          object = resolved;
        }
      }
      if (value == null && token.is(TokenType.NUMBER)) {
        value = Double.valueOf(parser.normalise(token.value()));
      }
    }
    if (object == null || value == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Assign(object, new ParseExpression.NumberLiteral(value), line);
  }

  // Example: "my labrador has 3 toys" or "she has 3 toys"
  public AstNode parseHasLine() throws BarkError {
    String who = null;
    Double amount = null;
    String topic = null;
    // Look for who, amount, and topic in order
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      // Who is the first token after the has verb
      if (who == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isDogSubjectReference(word, token.value())) {
          who = word;
        }
      }
      // Amount is the next token after the who
      if (amount == null && token.is(TokenType.NUMBER)) {
        amount = Double.valueOf(parser.normalise(token.value()));
      }
      // Topic is the last token after the amount
      if (amount != null && topic == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        // Make sure to filter out filler words
        String resolved = Keywords.explicitAttributeTopic(word);
        if (resolved != null) {
          topic = resolved;
        }
      }
    }
    if (who == null || amount == null || topic == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(who, topic, new ParseExpression.NumberLiteral(amount), line);
  }

  // Example: 'Her name is "Bimba"'
  public AstNode parseNameLine() throws BarkError {
    int nameAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      // Look for the name topic
      if (DogTopics.NAME.equals(Keywords.explicitAttributeTopic(parser.normalise(token.value())))) {
        nameAt = offset;
        break;
      }
    }
    if (nameAt < 0) {
      return null;
    }
    String who = null;
    for (int offset = 0; offset < nameAt; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      // Look for the who
      String word = parser.normalise(token.value());
      if (ConfigLoader.isBreed(word) || Keywords.isPronounWord(word)) {
        who = word;
        break;
      }
    }
    if (who == null) {
      return null;
    }
    ParseExpression value = findNameValueAfter(nameAt);
    if (value == null) {
      return null;
    }

    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(who, DogTopics.NAME, value, line);
  }

  // Value after the name topic. string literal or capitalized pet name
  private ParseExpression findNameValueAfter(final int nameAt) {
    for (int offset = nameAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.STRING)) {
        return new ParseExpression.StringLiteral(token.value());
      }
      if (token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isPetNameWord(word, token.value())) {
          return new ParseExpression.StringLiteral(token.value());
        }
      }
    }
    return null;
  }

  // Example: "He is 5 years old"
  public AstNode parseYearsOldLine() throws BarkError {
    int numberAt = findYearsOldNumber();
    if (numberAt < 0) {
      return null;
    }
    String who = null;
    for (int offset = 0; offset < numberAt; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (ConfigLoader.isBreed(word) || Keywords.isPronounWord(word)) {
        who = word;
        break;
      }
    }
    if (who == null) {
      return null;
    }
    double age = Double.parseDouble(parser.normalise(parser.peekAt(numberAt).value()));
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(
        who, DogTopics.AGE, new ParseExpression.NumberLiteral(age), line);
  }

  // Example: "She is 2"
  public AstNode parseIsAgeLine() throws BarkError {
    String who = null;
    Double age = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (who == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isDogSubjectReference(word, token.value())) {
          who = word;
        }
      }
      if (age == null && token.is(TokenType.NUMBER)) {
        age = Double.valueOf(parser.normalise(token.value()));
      }
    }
    if (who == null || age == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(
        who, DogTopics.AGE, new ParseExpression.NumberLiteral(age), line);
  }

  // Add or remove from a dog field (example: "she misplaces a toy", "she gains 2 treats")
  public AstNode parseAdjustLine() throws BarkError {
    String who = null;
    int verbAt = -1;
    boolean increment = false;
    Double amount = null;
    String topicWord = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      // Look for the who
      if (who == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isDogSubjectReference(word, token.value())) {
          who = word;
        }
      }
      // Look for the verb like "misplaces", "gains"
      if (verbAt < 0 && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        // Look for the decrement verb like "misplaces"
        if (Keywords.isAttributeAdjustDecrement(word)) {
          verbAt = offset;
          increment = false;

        }
        // Look for the increment verb like "gains"
        else if (Keywords.isAttributeAdjustIncrement(word)) {
          verbAt = offset;
          increment = true;
        }
      }
      // Look for the amount
      if (verbAt >= 0 && amount == null && offset > verbAt && token.is(TokenType.NUMBER)) {
        amount = Double.valueOf(parser.normalise(token.value()));
      }
      // Topic word after the verb (toys, treats); only attribute words count
      if (verbAt >= 0 && offset > verbAt && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        String resolved = Keywords.explicitAttributeTopic(word);
        if (resolved != null) {
          topicWord = word;
        }
      }
    }
    if (who == null || verbAt < 0) {
      return null;
    }
    // No number on the line means change by 1 ('she misplaces a toy')
    double changeBy = amount != null ? amount : 1.0;
    String verb = parser.normalise(parser.peekAt(verbAt).value());
    String topic = Keywords.resolveAdjustTopic(topicWord, verb);
    ParseExpression field = new ParseExpression.Field(who, topic);
    ParseExpression changeByExpr = new ParseExpression.NumberLiteral(changeBy);
    ParseExpression value =
        increment
            ? new ParseExpression.Binary(field, ParseExpression.BinaryOp.PLUS, changeByExpr)
            : new ParseExpression.Binary(field, ParseExpression.BinaryOp.MINUS, changeByExpr);
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(who, topic, value, line);
  }

  // Sets the trait on the dog. Only when the line has no print verb (see PrintParser.findTraitCheck
  // for read-only bark).
  public AstNode parseTraitLine() throws BarkError {
    if (parser.lineHasWord(Keywords.PRINT_KEYWORDS)) {
      return null;
    }
    String breed = null;
    String trait = null;
    int traitAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (breed == null && ConfigLoader.isBreed(word)) {
        breed = word;
      }
      if (trait == null && Keywords.isTraitKeyword(word)) {
        trait = word;
        traitAt = offset;
      }
    }
    if (breed == null || trait == null) {
      return null;
    }
    if (findNumberOnLine() != null) {
      return null;
    }
    int line = parser.peek().line();
    boolean enabled = traitEnabledOnLine(traitAt);
    parser.consumeUntilLineEnd();
    return new AstNode.SetTrait(breed, trait, enabled, line);
  }

  private int findYearsOldNumber() {
    int numberAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      if (parser.peekAt(offset).is(TokenType.NUMBER)) {
        numberAt = offset;
        break;
      }
    }
    if (numberAt < 0) {
      return -1;
    }
    ArrayList<String> afterNumber = new ArrayList<>();
    for (int scan = numberAt + 1; !parser.isAtEndOrLineEndOffset(scan); scan++) {
      Token token = parser.peekAt(scan);
      if (!token.is(TokenType.IDENTIFIER)) {
        break;
      }
      afterNumber.add(parser.normalise(token.value()));
    }
    return Keywords.isYearsOldSuffix(afterNumber) ? numberAt : -1;
  }

  private Double findNumberOnLine() {
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.NUMBER)) {
        return Double.valueOf(parser.normalise(token.value()));
      }
    }
    return null;
  }

  private boolean traitEnabledOnLine(final int traitAt) {
    for (int offset = 0; offset < traitAt; offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.NOT_KEYWORDS.contains(parser.normalise(token.value()))) {
        return false;
      }
    }
    for (int offset = traitAt + 1; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.isNullClearWord(word)) {
        return false;
      }
      if (Keywords.parseBoolean(word).isPresent()) {
        return Keywords.parseBoolean(word).orElseThrow();
      }
    }
    return true;
  }

  // Example: "labrador 5" or "she 2"
  public AstNode parseBareAgeLine() throws BarkError {
    String who = null;
    Double age = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (who == null && token.is(TokenType.IDENTIFIER)) {
        String word = parser.normalise(token.value());
        if (Keywords.isDogSubjectReference(word, token.value())) {
          who = word;
        }
      }
      if (age == null && token.is(TokenType.NUMBER)) {
        age = Double.valueOf(parser.normalise(token.value()));
      }
    }
    if (who == null || age == null) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(
        who, DogTopics.AGE, new ParseExpression.NumberLiteral(age), line);
  }

  // Example: memory is count of the items in the cookie jar
  public AstNode parseStoryConstantAssignLine() throws BarkError {
    String constant = null;
    int subjectAt = -1;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (ConfigLoader.isStoryNumberConstant(word) || ConfigLoader.isStoryTextConstant(word)) {
        constant = word;
        subjectAt = offset;
        break;
      }
    }
    if (constant == null || subjectAt < 0) {
      return null;
    }
    int valueStart = findOffsetAfterAssignGlue(subjectAt + 1);
    if (valueStart < 0) {
      return null;
    }
    int line = parser.peek().line();
    ParseExpression value =
        new ValueParser(parser).parsePart(valueStart, parser.countTokensAhead(0));
    parser.consumeUntilLineEnd();
    return new AstNode.Assign(constant, value, line);
  }

  // Example: my labrador treats is count of the items in the cookie jar
  public AstNode parseAttributeExpressionAssignLine() throws BarkError {
    if (parser.lineHasWord(Keywords.PRINT_KEYWORDS)) {
      return null;
    }
    String who = null;
    int attributeAt = -1;
    String topic = null;
    for (int offset = 0; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (who == null && (ConfigLoader.isBreed(word) || Keywords.isPronounWord(word))) {
        who = word;
      }
      if (attributeAt < 0) {
        String resolved = Keywords.explicitAttributeTopic(word);
        if (resolved != null
            && !DogTopics.NAME.equals(resolved)
            && !Keywords.isTraitKeyword(word)) {
          attributeAt = offset;
          topic = resolved;
        }
      }
    }
    if (who == null || attributeAt < 0 || topic == null) {
      return null;
    }
    int valueStart = findOffsetAfterAssignGlue(attributeAt + 1);
    if (valueStart < 0) {
      return null;
    }
    int line = parser.peek().line();
    ParseExpression value =
        new ValueParser(parser).parsePart(valueStart, parser.countTokensAhead(0));
    parser.consumeUntilLineEnd();
    return new AstNode.SetAttribute(who, topic, value, line);
  }

  // First is/has/have after fromOffset. return index of the value word after it
  private int findOffsetAfterAssignGlue(final int fromOffset) {
    for (int offset = fromOffset; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)
          && Keywords.isAssignSubjectGlue(parser.normalise(token.value()))) {
        return offset + 1;
      }
    }
    return -1;
  }
}
