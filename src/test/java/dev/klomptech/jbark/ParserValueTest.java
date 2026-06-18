package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.interpreter.Interpreter;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.Keywords;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.parser.expression.ValueParser;
import org.junit.jupiter.api.Test;

class ParserValueTest {

  @Test
  void barkNotFalse() throws BarkError {
    AstNode.Print print = parseLine("bark not false", AstNode.Print.class);
    ParseExpression.Not not = (ParseExpression.Not) print.values().get(0);
    assertInstanceOf(ParseExpression.BooleanLiteral.class, not.operand());
  }

  @Test
  void barkStashAll() throws BarkError {
    AstNode.Print print = parseLine("bark cookie_jar", AstNode.Print.class);
    ParseExpression.StashAccess stash = (ParseExpression.StashAccess) print.values().get(0);
    assertEquals("cookie_jar", stash.stash());
    assertEquals(ParseExpression.StashPart.ALL, stash.part());
  }

  @Test
  void barkHerCookieJar() throws BarkError {
    AstNode.Print print = parseLine("bark her cookie jar", AstNode.Print.class);
    ParseExpression.StashAccess stash = (ParseExpression.StashAccess) print.values().get(0);
    assertEquals("cookie_jar", stash.stash());
    assertEquals(ParseExpression.StashPart.ALL, stash.part());
  }

  @Test
  void barkHowManyToysTheToyBoxHas() throws BarkError {
    AstNode.Print print = parseLine("bark how many toys the toy box has", AstNode.Print.class);
    ParseExpression.StashAccess stash = (ParseExpression.StashAccess) print.values().get(0);
    assertEquals("toy_box", stash.stash());
    assertEquals(ParseExpression.StashPart.COUNT, stash.part());
  }

  @Test
  void barkLetters() throws BarkError {
    AstNode.Print print = parseLine("bark \"woof\" letters", AstNode.Print.class);
    assertInstanceOf(ParseExpression.Length.class, print.values().get(0));
  }

  @Test
  void storyConstantIsCountOfJar() throws BarkError {
    AstNode.Assign assign =
        parseLine("memory is count of the items in the cookie jar", AstNode.Assign.class);
    assertEquals(ConfigLoader.memoryName(), assign.variable());
    ParseExpression.StashAccess stash = (ParseExpression.StashAccess) assign.value();
    assertEquals("cookie_jar", stash.stash());
    assertEquals(ParseExpression.StashPart.COUNT, stash.part());
  }

  @Test
  void storyConstantIsNumber() throws BarkError {
    AstNode.Assign assign = parseLine("memory is 4", AstNode.Assign.class);
    assertEquals(4.0, ((ParseExpression.NumberLiteral) assign.value()).value());
  }

  @Test
  void storyJournalIsString() throws BarkError {
    AstNode.Assign assign = parseLine("journal is \"walk time\"", AstNode.Assign.class);
    assertEquals(ConfigLoader.journalName(), assign.variable());
    assertEquals("walk time", ((ParseExpression.StringLiteral) assign.value()).value());
  }

  @Test
  void growlStoryConstantMinusHerTreats() throws BarkError {
    AstNode.Print print =
        parseLine(
            "they growl memory minus how many treats she has, \"missing!\"", AstNode.Print.class);
    assertEquals(2, print.values().size());
    ParseExpression.Binary math = (ParseExpression.Binary) print.values().get(0);
    assertInstanceOf(ParseExpression.Variable.class, math.left());
    ParseExpression.Field treats = (ParseExpression.Field) math.right();
    assertEquals("she", treats.subject());
    assertEquals("food", treats.topic());
  }

  @Test
  void wifeHasLabradorRegistersBreed() throws BarkError {
    AstNode.Assign register = parseLine("So, my wife has a labrador", AstNode.Assign.class);
    assertEquals("labrador", register.variable());
  }

  @Test
  void stashWordIsStoryGlue() {
    assertTrue(Keywords.isIgnored("stash"));
    assertTrue(Keywords.isIgnored("pile"));
    assertFalse(Keywords.isIgnored("first"));
    assertFalse(Keywords.isIgnored("last"));
  }

  @Test
  void valueParserSkipsGlueAfterCollectionName() throws BarkError {
    Parser parser =
        new Parser(new Lexer("cookie_jar stash first\n").tokenise(), BarkOptions.defaults());
    ParseExpression.StashAccess withGlue =
        (ParseExpression.StashAccess)
            new ValueParser(parser).parsePart(0, parser.countTokensAhead(0));
    parser = new Parser(new Lexer("cookie_jar first\n").tokenise(), BarkOptions.defaults());
    ParseExpression.StashAccess plain =
        (ParseExpression.StashAccess)
            new ValueParser(parser).parsePart(0, parser.countTokensAhead(0));
    assertEquals(plain, withGlue);
    assertEquals(ParseExpression.StashPart.ELEMENT, withGlue.part());
    assertEquals("first", withGlue.which());
  }

  @Test
  void barkHerCookieJarWithTrailingStashGlue() throws BarkError {
    AstNode.Print plain = parseLine("bark her cookie jar", AstNode.Print.class);
    AstNode.Print withGlue = parseLine("bark her cookie jar stash", AstNode.Print.class);
    assertEquals(plain.values(), withGlue.values());
  }

  @Test
  void barkStashGlueBeforeCollectionName() throws BarkError {
    AstNode.Print print = parseLine("bark stash her cookie jar", AstNode.Print.class);
    ParseExpression.StashAccess access = (ParseExpression.StashAccess) print.values().get(0);
    assertEquals("cookie_jar", access.stash());
    assertEquals(ParseExpression.StashPart.ALL, access.part());
  }

  @Test
  void barkMixedCommaSeparatedValues() throws BarkError {
    AstNode.Print print = parseLine("bark \"test\", memory, 5", AstNode.Print.class);
    assertEquals(3, print.values().size());
    assertInstanceOf(ParseExpression.StringLiteral.class, print.values().get(0));
    assertEquals("test", ((ParseExpression.StringLiteral) print.values().get(0)).value());
    assertInstanceOf(ParseExpression.Variable.class, print.values().get(1));
    assertEquals(
        ConfigLoader.memoryName(), ((ParseExpression.Variable) print.values().get(1)).value());
    assertInstanceOf(ParseExpression.NumberLiteral.class, print.values().get(2));
    assertEquals(5.0, ((ParseExpression.NumberLiteral) print.values().get(2)).value());
  }

  @Test
  void growlCommaSeparatedPrintList() throws BarkError {
    AstNode.Print print = parseLine("they growl memory, \"missing!\"", AstNode.Print.class);
    assertEquals(2, print.values().size());
    assertInstanceOf(ParseExpression.Variable.class, print.values().get(0));
    assertInstanceOf(ParseExpression.StringLiteral.class, print.values().get(1));
  }

  @Test
  void barkTrickCallCommaIsNotPrintList() throws BarkError {
    AstNode.Print print = parseLine("bark add with 2, 3", AstNode.Print.class);
    assertEquals(1, print.values().size());
    assertInstanceOf(ParseExpression.FunctionCall.class, print.values().get(0));
  }

  @Test
  void barkStashAllPrintsCommaSeparated() throws BarkError {
    AstNode.Program program =
        new Parser(
                new Lexer(
                        """
            cookie_jar holds "a", "b"
            bark her cookie jar
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
    assertEquals("a, b", buffer.toString(java.nio.charset.StandardCharsets.UTF_8).trim());
  }

  private static <T extends AstNode> T parseLine(final String line, final Class<T> type)
      throws BarkError {
    var program = new Parser(new Lexer(line + "\n").tokenise(), BarkOptions.defaults()).parse();
    assertEquals(1, program.statements().size());
    return type.cast(program.statements().get(0));
  }
}
