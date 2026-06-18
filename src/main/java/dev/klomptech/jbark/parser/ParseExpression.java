package dev.klomptech.jbark.parser;

import dev.klomptech.jbark.parser.expression.ComparisonOp;
import java.util.List;

public sealed interface ParseExpression
    permits ParseExpression.StringLiteral,
        ParseExpression.NumberLiteral,
        ParseExpression.BooleanLiteral,
        ParseExpression.PeriodLiteral,
        ParseExpression.Empty,
        ParseExpression.NullLiteral,
        ParseExpression.Variable,
        ParseExpression.Field,
        ParseExpression.Binary,
        ParseExpression.Not,
        ParseExpression.Comparison,
        ParseExpression.HasExact,
        ParseExpression.HasTrait,
        ParseExpression.Logical,
        ParseExpression.StashAccess,
        ParseExpression.PileAccess,
        ParseExpression.Join,
        ParseExpression.FunctionCall,
        ParseExpression.Length,
        ParseExpression.Contains,
        ParseExpression.TypeCheck {

  enum BinaryOp {
    PLUS,
    MINUS,
    STAR,
    SLASH
  }

  enum LogicalOp {
    AND,
    OR,
    NOR
  }

  enum StashPart {
    ALL,
    COUNT,
    ELEMENT
  }

  enum PilePart {
    ALL,
    COUNT,
    TOP
  }

  enum ValueType {
    NUMBER,
    WORDS,
    NOTHING
  }

  record StringLiteral(String value) implements ParseExpression {}

  record NumberLiteral(double value) implements ParseExpression {}

  record BooleanLiteral(boolean value) implements ParseExpression {}

  record PeriodLiteral() implements ParseExpression {}

  record Empty() implements ParseExpression {}

  record NullLiteral() implements ParseExpression {}

  record Variable(String value) implements ParseExpression {}

  record Field(String subject, String topic) implements ParseExpression {}

  record Binary(ParseExpression left, BinaryOp op, ParseExpression right)
      implements ParseExpression {}

  record Not(ParseExpression operand) implements ParseExpression {}

  record Comparison(ParseExpression left, ComparisonOp op, ParseExpression right)
      implements ParseExpression {}

  record HasExact(String subject, String topic, double amount) implements ParseExpression {}

  record HasTrait(String subject, String trait) implements ParseExpression {}

  record Logical(ParseExpression left, LogicalOp op, ParseExpression right)
      implements ParseExpression {}

  record StashAccess(String stash, StashPart part, String which) implements ParseExpression {}

  record PileAccess(String pile, PilePart part) implements ParseExpression {}

  record Join(String stash, ParseExpression delimiter) implements ParseExpression {}

  record FunctionCall(String name, List<ParseExpression> args) implements ParseExpression {}

  record Length(ParseExpression value) implements ParseExpression {}

  record Contains(ParseExpression haystack, ParseExpression needle) implements ParseExpression {}

  record TypeCheck(ParseExpression value, ValueType type) implements ParseExpression {}
}
