package dev.klomptech.jbark.parser.collection;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.errors.BarkError;

// Stash item words like first, second, last
public final class StashSpots {

  private StashSpots() {}

  private static final String LAST = "last";
  private static final String FIRST = "first";

  // True when the word successfully parses as a valid number or matches "last"
  public static boolean isStashSpot(final String word) {
    String normalized = ConfigLoader.normalise(word);
    if (LAST.equals(normalized) || FIRST.equals(normalized)) {
      return true;
    }
    // If it parses to a valid index (>0), it's a valid spot phrase
    return SmartOrdinalParser.parseWordToNumber(normalized) > 0;
  }

  // Turns any dynamic written ordinal or "last" into a zero-based index
  public static int resolveIndex(final int size, final String which, final int line)
      throws BarkError {
    String normalized = ConfigLoader.normalise(which);
    // Handle "last" based on size
    if (LAST.equals(normalized)) {
      if (size == 0) {
        throw new BarkError(line, "The stash is empty, there is no last bone to dig up.");
      }
      return size - 1;
    }
    // Handle "first"
    if (FIRST.equals(normalized)) {
      return 0;
    }
    // Parse text dynamically using the rule-based engine
    int oneBased = SmartOrdinalParser.parseWordToNumber(normalized);
    // If the word isn't a known number combination, the parser returns 0
    if (oneBased <= 0) {
      throw new BarkError(line, "\"" + which + "\" is not a stash spot the dogs understand.");
    }
    // Size boundary check
    if (oneBased > size) {
      throw new BarkError(
          line,
          "That stash only has "
              + size
              + " items, the dog checked twice looking for spot "
              + which
              + ".");
    }
    // Return zero-based index
    return oneBased - 1;
  }
}
