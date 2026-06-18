package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarktimeError;

@FunctionalInterface
public interface Comparator {
  boolean compare(BarkValue left, BarkValue right, int line) throws BarktimeError;
}
