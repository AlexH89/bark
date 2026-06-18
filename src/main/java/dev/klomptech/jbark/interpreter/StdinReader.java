package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.errors.BarkError;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class StdinReader {

  private StdinReader() {}

  public static BarkValue read(final int line) throws BarkError {
    try {
      String input = new BufferedReader(new InputStreamReader(System.in)).readLine();
      if (input == null) {
        return new BarkValue.BarkNull();
      }
      return BarkValue.of(input);
    } catch (IOException _) {
      throw new BarkError(line, "The human went quiet. Couldn't catch anything from stdin.");
    }
  }
}
