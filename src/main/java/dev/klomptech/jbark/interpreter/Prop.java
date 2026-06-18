package dev.klomptech.jbark.interpreter;

// One registry object at runtime (ball, bone, etc.). Just holds a value.
public record Prop(BarkValue value) {

  public static Prop zero() {
    return new Prop(BarkValue.of(0));
  }
}
