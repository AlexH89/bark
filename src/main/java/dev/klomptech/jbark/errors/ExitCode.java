package dev.klomptech.jbark.errors;

public enum ExitCode {
  OK(0),
  INVALID_ARGUMENTS(1),
  USAGE(64),
  DATA_ERROR(65);

  public final int code;

  ExitCode(final int code) {
    this.code = code;
  }
}
