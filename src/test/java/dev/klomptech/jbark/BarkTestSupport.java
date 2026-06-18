package dev.klomptech.jbark;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.interpreter.Interpreter;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Parser;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

// Runs Bark source in tests without the startup banner
final class BarkTestSupport {

  private BarkTestSupport() {}

  static final String FETCHY_REPEAT = "*tail THUMP THUMP, same stick, still the best stick*";

  static List<String> runLines(final String source) throws BarkError {
    Lexer lexer = new Lexer(source);
    Parser parser = new Parser(lexer.tokenise(), BarkOptions.defaults());
    AstNode.Program program = parser.parse();

    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    PrintStream capture = new PrintStream(buffer, true, StandardCharsets.UTF_8);
    PrintStream previous = System.out;
    System.setOut(capture);
    try {
      new Interpreter().run(program);
    } finally {
      System.setOut(previous);
    }
    return buffer
        .toString(StandardCharsets.UTF_8)
        .lines()
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .toList();
  }

  static String runJoined(final String source) throws BarkError {
    return String.join("\n", runLines(source));
  }
}
