package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.print.PrintStyle;
import java.util.List;

// One statement in a .woof program; line() is the source line for error messages only
public sealed interface AstNode
    permits AstNode.Program,
        AstNode.Print,
        AstNode.Listen,
        AstNode.Exit,
        AstNode.Wait,
        AstNode.Assign,
        AstNode.SetAttribute,
        AstNode.IfChain,
        AstNode.WhileLoop,
        AstNode.UntilLoop,
        AstNode.ForEach,
        AstNode.FunctionDef,
        AstNode.Break,
        AstNode.Continue,
        AstNode.StashInit,
        AstNode.StashAppend,
        AstNode.StashSet,
        AstNode.StashRemove,
        AstNode.StashClear,
        AstNode.PileInit,
        AstNode.PilePush,
        AstNode.PilePop,
        AstNode.PileClear,
        AstNode.Share,
        AstNode.Pass,
        AstNode.SetTrait,
        AstNode.TakeFromStash,
        AstNode.LineGroup {

  int line();

  record Program(List<AstNode> statements) implements AstNode {
    @Override
    public int line() {
      return 1;
    }
  }

  record Print(
      PrintStyle style, List<ParseExpression> values, int line, boolean pawPrint, String dogSpeaker)
      implements AstNode {
    public Print(final PrintStyle style, final List<ParseExpression> values, final int line) {
      this(style, values, line, false, null);
    }

    public Print(
        final PrintStyle style,
        final List<ParseExpression> values,
        final int line,
        final boolean pawPrint) {
      this(style, values, line, pawPrint, null);
    }
  }

  record Listen(String dog, int line) implements AstNode {}

  record Exit(int line) implements AstNode {}

  record Wait(double seconds, int line) implements AstNode {}

  record Assign(String variable, ParseExpression value, int line) implements AstNode {}

  record SetAttribute(String subject, String topic, ParseExpression value, int line)
      implements AstNode {}

  record IfBranch(ParseExpression condition, List<AstNode> steps) {}

  record IfChain(List<IfBranch> branches, List<AstNode> elseSteps, int line) implements AstNode {}

  record WhileLoop(ParseExpression condition, List<AstNode> steps, int line) implements AstNode {}

  record UntilLoop(ParseExpression condition, List<AstNode> steps, int line) implements AstNode {}

  record ForEach(String variable, String stash, List<AstNode> steps, int line) implements AstNode {}

  record FunctionDef(
      String name,
      List<String> params,
      List<AstNode> steps,
      ParseExpression returnExpression,
      int line)
      implements AstNode {}

  record Break(int line) implements AstNode {}

  record Continue(int line) implements AstNode {}

  record StashInit(String stash, List<ParseExpression> items, int line) implements AstNode {}

  record StashAppend(String stash, ParseExpression item, int line) implements AstNode {}

  record StashSet(String stash, String which, ParseExpression value, int line) implements AstNode {}

  record StashRemove(String stash, String which, int line) implements AstNode {}

  record StashClear(String stash, int line) implements AstNode {}

  record PileInit(String pile, List<ParseExpression> items, int line) implements AstNode {}

  record PilePush(String pile, ParseExpression item, int line) implements AstNode {}

  record PilePop(String pile, int line) implements AstNode {}

  record PileClear(String pile, int line) implements AstNode {}

  record Share(String giver, String stash, List<String> recipients, int line) implements AstNode {}

  record Pass(String giver, String recipient, int line) implements AstNode {}

  record SetTrait(String subject, String trait, boolean enabled, int line) implements AstNode {}

  record TakeFromStash(String subject, String stash, String which, int line) implements AstNode {}

  // Multiple statements parsed from one story line (example: whimper + take)
  record LineGroup(List<AstNode> statements, int line) implements AstNode {}
}
