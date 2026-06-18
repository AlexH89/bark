package dev.klomptech.jbark.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TraitLoader {

  // After this many food grabs in a row, a greedy dog loses one treat
  public static final int GREEDY_FEEDS_BEFORE_LOSS = 3;

  private static final Map<String, Set<String>> DEFAULT_TRAITS = loadTraits();

  private TraitLoader() {}

  private static Map<String, Set<String>> loadTraits() {
    try (InputStream in = TraitLoader.class.getResourceAsStream("/traits.json")) {
      if (in == null) {
        return Map.of();
      }
      ObjectMapper mapper = new ObjectMapper();
      Map<String, List<String>> raw = mapper.readValue(in, new TypeReference<>() {});
      Map<String, Set<String>> traits = new HashMap<>();
      for (Map.Entry<String, List<String>> entry : raw.entrySet()) {
        String breed = ConfigLoader.normalise(entry.getKey());
        Set<String> names = new HashSet<>();
        for (String trait : entry.getValue()) {
          names.add(ConfigLoader.normalise(trait));
        }
        traits.put(breed, names);
      }
      return traits;
    } catch (IOException e) {
      throw new IllegalStateException("Failed to load traits.json", e);
    }
  }

  public static Map<String, Set<String>> defaultTraits() {
    Map<String, Set<String>> copy = new HashMap<>();
    for (Map.Entry<String, Set<String>> entry : DEFAULT_TRAITS.entrySet()) {
      copy.put(entry.getKey(), new HashSet<>(entry.getValue()));
    }
    return copy;
  }
}
