package dev.klomptech.jbark;

public record BarkOptions(boolean strict, boolean quiet) {

  public static BarkOptions defaults() {
    return new BarkOptions(false, false);
  }
}
