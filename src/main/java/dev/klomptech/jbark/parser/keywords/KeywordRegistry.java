package dev.klomptech.jbark.parser.keywords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Collects synonym lists while Keywords initializes.
public final class KeywordRegistry {

  private final List<List<String>> heardWordGroups = new ArrayList<>();

  // Phrase-only synonyms. Parsed in fixed order; not registered as heard words.
  public List<String> phraseWords(final String... items) {
    Set<String> unique = new LinkedHashSet<>();
    unique.addAll(Arrays.asList(items));
    return List.copyOf(unique);
  }

  // Standalone heard words. Anything else on a line is story glue (isIgnored).
  public List<String> heardWords(final String... items) {
    Set<String> unique = new LinkedHashSet<>();
    unique.addAll(Arrays.asList(items));
    List<String> copy = List.copyOf(unique);
    heardWordGroups.add(copy);
    return copy;
  }

  public void fillKeywordSet(final Set<String> target) {
    for (List<String> group : heardWordGroups) {
      target.addAll(group);
    }
  }
}
