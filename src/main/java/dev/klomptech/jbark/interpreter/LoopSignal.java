package dev.klomptech.jbark.interpreter;

final class LoopSignal extends RuntimeException {

  enum Kind {
    BREAK,
    CONTINUE,
  }

  private final Kind kind;

  LoopSignal(final Kind kind) {
    super(null, null, false, false);
    this.kind = kind;
  }

  Kind kind() {
    return kind;
  }
}
