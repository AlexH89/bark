package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import dev.klomptech.jbark.print.PrintStyle;
import org.junit.jupiter.api.Test;

class ParserShapeTest {

  @Test
  void belongingsAreSetsInventory() throws BarkError {
    AstNode.SetAttribute inventory =
        parseLine("my labrador belongings are \"ball, stick\"", AstNode.SetAttribute.class);
    assertEquals("labrador", inventory.subject());
    assertEquals("inventory", inventory.topic());
    assertEquals(
        "ball, stick", ((ParseExpression.StringLiteral) inventory.value()).value());
  }

  @Test
  void findsAddsOneToy() throws BarkError {
    AstNode.SetAttribute adjust = parseLine("he finds a toy", AstNode.SetAttribute.class);
    assertEquals("he", adjust.subject());
    assertEquals("items", adjust.topic());
    assertInstanceOf(ParseExpression.Binary.class, adjust.value());
  }

  @Test
  void yearsOldSetsAge() throws BarkError {
    AstNode.SetAttribute age = parseLine("He is 5 years old", AstNode.SetAttribute.class);
    assertEquals("he", age.subject());
    assertEquals("age", age.topic());
    assertEquals(5.0, ((ParseExpression.NumberLiteral) age.value()).value());
  }

  @Test
  void howlPrintStyle() throws BarkError {
    AstNode.Print print = parseLine("he howls \"WHOOOO!\"", AstNode.Print.class);
    assertEquals(PrintStyle.HOWL, print.style());
    assertEquals("WHOOOO!", ((ParseExpression.StringLiteral) print.values().get(0)).value());
  }

  @Test
  void growlPrintsField() throws BarkError {
    AstNode.IfChain chain =
        parseLine("when he has 1 toy then he growls his name", AstNode.IfChain.class);
    AstNode.Print print = (AstNode.Print) chain.branches().get(0).steps().get(0);
    assertEquals(PrintStyle.GROWL, print.style());
    ParseExpression.Field field = (ParseExpression.Field) print.values().get(0);
    assertEquals("his", field.subject());
    assertEquals("name", field.topic());
  }

  @Test
  void woofPrintsNameField() throws BarkError {
    AstNode.IfChain chain =
        parseLine("when she has 1 toy then she woof her name", AstNode.IfChain.class);
    AstNode.Print print = (AstNode.Print) chain.branches().get(0).steps().get(0);
    assertEquals(PrintStyle.WOOF, print.style());
    ParseExpression.Field field = (ParseExpression.Field) print.values().get(0);
    assertEquals("her", field.subject());
    assertEquals("name", field.topic());
  }

  private static <T extends AstNode> T parseLine(final String line, final Class<T> type)
      throws BarkError {
    var program = new Parser(new Lexer(line + "\n").tokenise(), BarkOptions.defaults()).parse();
    assertEquals(1, program.statements().size());
    T node = type.cast(program.statements().get(0));
    assertNotNull(node);
    return node;
  }
}
