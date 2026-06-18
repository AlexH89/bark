package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarktimeError;
import dev.klomptech.jbark.parser.expression.ComparisonOp;
import java.util.LinkedHashMap;
import java.util.Map;

// Maps each comparison operator to the method that evaluates it
public final class Comparators {

  private Comparators() {}

  public static Map<ComparisonOp, Comparator> create() {
    Map<ComparisonOp, Comparator> comparators = new LinkedHashMap<>();
    comparators.put(ComparisonOp.EQUAL, (left, right, _) -> left.equalTo(right));
    comparators.put(ComparisonOp.NOT_EQUAL, (left, right, _) -> !left.equalTo(right));
    comparators.put(ComparisonOp.GREATER_THAN, Comparators::greaterThan);
    comparators.put(ComparisonOp.LESS_THAN, Comparators::lessThan);
    comparators.put(ComparisonOp.GREATER_THAN_OR_EQUAL, Comparators::greaterThanOrEqual);
    comparators.put(ComparisonOp.LESS_THAN_OR_EQUAL, Comparators::lessThanOrEqual);
    return comparators;
  }

  private static boolean greaterThanOrEqual(
      final BarkValue left, final BarkValue right, final int line) throws BarktimeError {
    return greaterThan(left, right, line) || left.equalTo(right);
  }

  private static boolean lessThanOrEqual(
      final BarkValue left, final BarkValue right, final int line) throws BarktimeError {
    return lessThan(left, right, line) || left.equalTo(right);
  }

  private static boolean greaterThan(final BarkValue left, final BarkValue right, final int line)
      throws BarktimeError {
    if (left instanceof BarkValue.BarkNumber l && right instanceof BarkValue.BarkNumber r) {
      return l.value() > r.value();
    }
    throw new BarktimeError(
        line, "Only bone counts can be ranked bigger, words and woofs don't make sense to dogs.");
  }

  private static boolean lessThan(final BarkValue left, final BarkValue right, final int line)
      throws BarktimeError {
    if (left instanceof BarkValue.BarkNumber l && right instanceof BarkValue.BarkNumber r) {
      return l.value() < r.value();
    }
    throw new BarktimeError(line, "Only bone counts can be ranked smaller, dogs can't eat words.");
  }
}
