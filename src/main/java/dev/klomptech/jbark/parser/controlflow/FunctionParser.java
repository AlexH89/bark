package dev.klomptech.jbark.parser.controlflow;

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

public class FunctionParser {

  private final Parser parser;
  private final ValueParser valueParser;

  public FunctionParser(final Parser parser) {
    this.parser = parser;
    this.valueParser = new ValueParser(parser);
  }

  // Example: add expects a b returns a plus b bury
  public AstNode parseTrickLine() throws BarkError {
    if (!parser.lineHasWord(Keywords.EXPECTS_WORDS)) {
      return null;
    }
    int line = parser.peek().line();
    int expectsAt = findWord(Keywords.EXPECTS_WORDS);
    if (expectsAt < 0) {
      return null;
    }
    String name = findNameBefore(expectsAt);
    if (name == null) {
      throw new BarkError(line, "Every trick needs a name");
    }
    int returnsAt = findWordAfter(Keywords.TRICK_RETURNS_WORDS, expectsAt + 1);
    if (returnsAt < 0) {
      throw new BarkError(line, "Trick \"" + name + "\" never says what the dogs get out of it!");
    }
    List<String> params = readParams(expectsAt + 1, returnsAt);
    int stopAt = findWordAfter(Keywords.BURY_KEYWORDS, returnsAt + 1);
    int valueEnd = stopAt >= 0 ? stopAt : lineEndOffset();
    ParseExpression returnExpr = valueParser.parsePart(returnsAt + 1, valueEnd);
    List<AstNode> steps;
    if (stopAt >= 0) {
      parser.advanceBy(stopAt + 1);
      parser.skipNewlines();
      steps = List.of();
    } else {
      parser.consumeUntilLineEnd();
      parser.skipNewlines();
      steps = readBodyUntilStop();
      if (!parser.currentLineIsBury()) {
        throw new BarkError(line, "The trick started but you forgot to say stop!");
      }
      parser.consumeBuryLine();
    }
    return new AstNode.FunctionDef(name, params, steps, returnExpr, line);
  }

  private String findNameBefore(final int expectsAt) {
    for (int offset = 0; offset < expectsAt; offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER)) {
        return parser.normalise(token.value());
      }
    }
    return null;
  }

  private List<String> readParams(final int from, final int to) {
    List<String> params = new ArrayList<>();
    for (int offset = from; offset < to; offset++) {
      Token token = parser.peekAt(offset);
      if (!token.is(TokenType.IDENTIFIER)) {
        continue;
      }
      String word = parser.normalise(token.value());
      if (Keywords.EXPECTS_WORDS.contains(word) || Keywords.TRICK_RETURNS_WORDS.contains(word)) {
        continue;
      }
      if (Keywords.LIST_AND_WORDS.contains(word)) {
        continue;
      }
      params.add(word);
    }
    return params;
  }

  private int lineEndOffset() {
    int offset = 0;
    while (!parser.isAtEndOrLineEndOffset(offset)) {
      offset++;
    }
    return offset;
  }

  private List<AstNode> readBodyUntilStop() throws BarkError {
    List<AstNode> steps = new ArrayList<>();
    while (!parser.isAtEnd()) {
      parser.skipNewlines();
      if (parser.isAtEnd() || parser.currentLineIsBury()) {
        break;
      }
      parser.parseStatementInto(steps);
    }
    return steps;
  }

  private int findWord(final List<String> words) {
    return findWordAfter(words, 0);
  }

  private int findWordAfter(final List<String> words, final int from) {
    for (int offset = from; !parser.isAtEndOrLineEndOffset(offset); offset++) {
      Token token = parser.peekAt(offset);
      if (token.is(TokenType.IDENTIFIER) && words.contains(parser.normalise(token.value()))) {
        return offset;
      }
    }
    return -1;
  }
}
