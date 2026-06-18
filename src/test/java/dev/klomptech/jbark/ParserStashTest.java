package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.interpreter.Interpreter;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import java.util.List;
import org.junit.jupiter.api.Test;

class ParserStashTest {

  @Test
  void cookieJarIsEmptyClearsStash() throws BarkError {
    AstNode.StashClear clear = parseLine("her cookie jar is empty", AstNode.StashClear.class);
    assertEquals("cookie_jar", clear.stash());
  }

  @Test
  void trickOnOneLineWithBury() throws BarkError {
    AstNode.FunctionDef trick =
        (AstNode.FunctionDef) parseProgram("add expects a b returns a bury\n").get(0);
    assertEquals("add", trick.name());
    assertEquals(List.of("a", "b"), trick.params());
    assertEquals("a", ((ParseExpression.Variable) trick.returnExpression()).value());
    assertTrue(trick.steps().isEmpty());
  }

  @Test
  void trickOnOneLineWithEnd() throws BarkError {
    AstNode.FunctionDef trick =
        (AstNode.FunctionDef) parseProgram("add expects a b returns a end\n").get(0);
    assertEquals("add", trick.name());
    assertTrue(trick.steps().isEmpty());
  }

  @Test
  void trickWithReturnValue() throws BarkError {
    AstNode.FunctionDef trick =
        (AstNode.FunctionDef)
            parseProgram(
                    """
            add expects a b returns a
            bury
            """)
                .get(0);
    assertEquals("add", trick.name());
    assertEquals(List.of("a", "b"), trick.params());
    assertInstanceOf(ParseExpression.Variable.class, trick.returnExpression());
    assertEquals("a", ((ParseExpression.Variable) trick.returnExpression()).value());
    assertTrue(trick.steps().isEmpty());
  }

  @Test
  void trickReturnExpression() throws BarkError {
    AstNode.FunctionDef trick =
        (AstNode.FunctionDef) parseProgram("add expects a b returns a plus b bury\n").get(0);
    ParseExpression.Binary sum = (ParseExpression.Binary) trick.returnExpression();
    assertEquals(ParseExpression.BinaryOp.PLUS, sum.op());
    assertEquals("a", ((ParseExpression.Variable) sum.left()).value());
    assertEquals("b", ((ParseExpression.Variable) sum.right()).value());
  }

  @Test
  void barkTrickCall() throws BarkError {
    AstNode.Program program =
        new Parser(
                new Lexer(
                        """
            add expects a b returns a plus b bury
            bark add with 2, 3
            """)
                    .tokenise(),
                BarkOptions.defaults())
            .parse();
    AstNode.Print print = (AstNode.Print) program.statements().get(1);
    ParseExpression.FunctionCall call = (ParseExpression.FunctionCall) print.values().get(0);
    assertEquals("add", call.name());
    assertEquals(2, call.args().size());
    assertEquals(2.0, ((ParseExpression.NumberLiteral) call.args().get(0)).value());
    assertEquals(3.0, ((ParseExpression.NumberLiteral) call.args().get(1)).value());
  }

  @Test
  void barkTrickCallRuns() throws BarkError {
    AstNode.Program program =
        new Parser(
                new Lexer(
                        """
            add expects a b returns a plus b bury
            bark add with 2, 3
            """)
                    .tokenise(),
                BarkOptions.defaults())
            .parse();
    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
    java.io.PrintStream capture =
        new java.io.PrintStream(buffer, true, java.nio.charset.StandardCharsets.UTF_8);
    java.io.PrintStream previous = System.out;
    System.setOut(capture);
    try {
      new Interpreter().run(program);
    } finally {
      System.setOut(previous);
    }
    assertEquals("5", buffer.toString(java.nio.charset.StandardCharsets.UTF_8).trim());
  }

  @Test
  void trickWithBodyReturnsNothing() throws BarkError {
    AstNode.FunctionDef trick =
        (AstNode.FunctionDef)
            parseProgram(
                    """
            greet expects name returns nothing
                bark name
            end
            """)
                .get(0);
    assertEquals("greet", trick.name());
    assertEquals(List.of("name"), trick.params());
    assertInstanceOf(ParseExpression.NullLiteral.class, trick.returnExpression());
    assertEquals(1, trick.steps().size());
    assertInstanceOf(AstNode.Print.class, trick.steps().get(0));
  }

  @Test
  void stashSlotSetWithPlainNumber() throws BarkError {
    AstNode.StashSet set = parseLine("cookie_jar first is 5", AstNode.StashSet.class);
    assertEquals("cookie_jar", set.stash());
    assertEquals("first", set.which());
    assertEquals(5.0, ((ParseExpression.NumberLiteral) set.value()).value());
  }

  @Test
  void stashSlotSetWithExpression() throws BarkError {
    AstNode.StashSet set =
        parseLine(
            "cookie_jar first is count of the items in the cookie jar", AstNode.StashSet.class);
    assertEquals("cookie_jar", set.stash());
    assertEquals("first", set.which());
    ParseExpression.StashAccess count = (ParseExpression.StashAccess) set.value();
    assertEquals(ParseExpression.StashPart.COUNT, count.part());
  }

  private static <T extends AstNode> T parseLine(final String line, final Class<T> type)
      throws BarkError {
    return type.cast(parseProgram(line + "\n").get(0));
  }

  private static List<AstNode> parseProgram(final String source) throws BarkError {
    return new Parser(new Lexer(source).tokenise(), BarkOptions.defaults()).parse().statements();
  }
}
