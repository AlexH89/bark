package dev.klomptech.jbark.interpreter;

public sealed interface BarkValue
    permits BarkValue.BarkString, BarkValue.BarkNumber, BarkValue.BarkBoolean, BarkValue.BarkNull {

  // Human readable representation of the value
  String display();

  // For error messages when value may be null
  static String describe(final BarkValue value) {
    return value == null ? "nothing" : value.display();
  }

  // Same type and same data?
  boolean equalTo(BarkValue other);

  record BarkString(String value) implements BarkValue {
    @Override
    public String display() {
      return value;
    }

    @Override
    public boolean equalTo(final BarkValue other) {
      return other instanceof BarkString(String v) && value.equals(v);
    }
  }

  record BarkNumber(double value) implements BarkValue {
    @Override
    public String display() {
      boolean isWholeNumber = Math.floor(value) == value && !Double.isInfinite(value);
      return isWholeNumber ? String.valueOf((long) value) : String.valueOf(value);
    }

    @Override
    public boolean equalTo(final BarkValue other) {
      return other instanceof BarkNumber(double v) && value == v;
    }
  }

  record BarkBoolean(boolean value) implements BarkValue {
    @Override
    public String display() {
      return value ? "true" : "false";
    }

    @Override
    public boolean equalTo(final BarkValue other) {
      return other instanceof BarkBoolean(boolean v) && value == v;
    }
  }

  record BarkNull() implements BarkValue {
    @Override
    public String display() {
      return "null";
    }

    @Override
    public boolean equalTo(final BarkValue other) {
      return other instanceof BarkNull;
    }
  }

  static BarkValue of(final String value) {
    return new BarkString(value);
  }

  static BarkValue of(final double value) {
    return new BarkNumber(value);
  }

  static BarkValue of(final boolean value) {
    return new BarkBoolean(value);
  }
}
