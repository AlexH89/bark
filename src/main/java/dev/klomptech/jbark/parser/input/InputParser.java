package dev.klomptech.jbark.parser.input;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Token;
import dev.klomptech.jbark.lexer.TokenType;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.Parser;

public class InputParser {

  private final Parser parser;

  public InputParser(final Parser parser) {
    this.parser = parser;
  }

  // Example: "escape" stops the program (leave, depart, scram, wander work too)
  public AstNode parseExitLine() {
    if (!parser.currentLineStartsWithAny(Keywords.EXIT_KEYWORDS)) {
      return null;
    }
    int line = parser.peek().line();
    parser.consumeUntilLineEnd();
    return new AstNode.Exit(line);
  }

  // Example: "wait 2 seconds" or "sleep" (number optional, defaults to 1 second)
  public AstNode parseWaitLine() throws BarkError {
    if (!parser.lineHasWord(Keywords.WAIT_KEYWORDS)) {
      return null;
    }
    int line = parser.peek().line();
    Double seconds = findNumberOnLine();
    if (seconds == null) {
      seconds = 1.0;
    }
    parser.consumeUntilLineEnd();
    return new AstNode.Wait(seconds, line);
  }

  // Example: "the labrador listens" reads one line from the keyboard into that dog
  public AstNode parseListenLine() throws BarkError {
    if (!parser.lineHasWord(Keywords.STDIN_KEYWORDS)) {
      return null;
    }
    int line = parser.peek().line();
    String dog = findDogOnLine();
    if (dog == null) {
      throw new BarkError(line, "Who is listening? Name the dog first, like the labrador listens.");
    }
    parser.consumeUntilLineEnd();
    return new AstNode.Listen(dog, line);
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
