package dev.klomptech.jbark;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.klomptech.jbark.errors.BarkError;
import dev.klomptech.jbark.lexer.Lexer;
import dev.klomptech.jbark.parser.AstNode;
import dev.klomptech.jbark.parser.ParseExpression;
import dev.klomptech.jbark.parser.Parser;
import org.junit.jupiter.api.Test;

class ParserRegisterTest {

  @Test
  void bareBreedRegisters() throws BarkError {
    assertRegister("labrador", "labrador");
  }

  @Test
  void storyIntroduceBreed() throws BarkError {
    assertRegister("I have a labrador", "labrador");
  }

  @Test
  void possessiveIntroduceBreed() throws BarkError {
    assertRegister("My wife has a dachshund", "dachshund");
  }

  @Test
  void pronounIntroduceBreed() throws BarkError {
    assertRegister("she has a labrador", "labrador");
  }

  @Test
  void quotedName() throws BarkError {
    AstNode.SetAttribute name = parseLine("Her name is \"Bimba\"", AstNode.SetAttribute.class);
    assertEquals("her", name.subject());
    assertEquals("name", name.topic());
    assertEquals("Bimba", ((ParseExpression.StringLiteral) name.value()).value());
  }

  @Test
  void toyCountBeatsAge() throws BarkError {
    AstNode.SetAttribute toys = parseLine("she is 3 toys", AstNode.SetAttribute.class);
    assertEquals("she", toys.subject());
    assertEquals("items", toys.topic());
    assertEquals(3.0, ((ParseExpression.NumberLiteral) toys.value()).value());
  }

  private static <T extends AstNode> T parseLine(final String line, final Class<T> type)
      throws BarkError {
    var program = new Parser(new Lexer(line + "\n").tokenise(), BarkOptions.defaults()).parse();
    assertEquals(1, program.statements().size());
    return type.cast(program.statements().get(0));
  }

  private static void assertRegister(final String line, final String breed) throws BarkError {
    var program = new Parser(new Lexer(line + "\n").tokenise(), BarkOptions.defaults()).parse();
    assertEquals(1, program.statements().size());
    AstNode.Assign assign = (AstNode.Assign) program.statements().get(0);
    assertEquals(breed, assign.variable());
  }
}
