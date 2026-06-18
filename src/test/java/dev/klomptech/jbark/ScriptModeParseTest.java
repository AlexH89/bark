package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import java.util.List;
import org.junit.jupiter.api.Test;

class ScriptModeParseTest {

  @Test
  void scriptStashFormsWithoutFillerWord() throws BarkError {
    assertEquals(
        "cookie_jar",
        parseLine("cookie_jar holds \"a\", \"b\"", AstNode.StashInit.class).stash());
    assertEquals(
        "cookie_jar", parseLine("cookie_jar empty", AstNode.StashClear.class).stash());
    assertEquals(
        "cookie_jar", parseLine("cookie_jar drops first", AstNode.StashRemove.class).stash());
  }

  @Test
  void scriptPrintFirstStashItem() throws BarkError {
    AstNode.Print print = parseLine("whimper first from cookie_jar", AstNode.Print.class);
    ParseExpression.StashAccess access =
        (ParseExpression.StashAccess) print.values().get(0);
    assertEquals("cookie_jar", access.stash());
    assertEquals("first", access.which());
    assertEquals(ParseExpression.StashPart.ELEMENT, access.part());
  }

  @Test
  void storyGlueAfterStashNameDoesNotChangeScriptParse() throws BarkError {
    assertEquals(
        parseLine("cookie_jar holds \"a\", \"b\"", AstNode.StashInit.class).stash(),
        parseLine("cookie_jar stash holds \"a\", \"b\"", AstNode.StashInit.class).stash());
    AstNode.Print plain = parseLine("whimper first from cookie_jar", AstNode.Print.class);
    AstNode.Print withGlue = parseLine("whimper first from cookie_jar stash", AstNode.Print.class);
    assertEquals(plain.values(), withGlue.values());
  }

  private static <T extends AstNode> T parseLine(final String line, final Class<T> type)
      throws BarkError {
    return type.cast(parseProgram(line + "\n").get(0));
  }

  private static List<AstNode> parseProgram(final String source) throws BarkError {
    return new Parser(new Lexer(source).tokenise(), BarkOptions.defaults()).parse().statements();
  }
}
