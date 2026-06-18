package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.config.ConfigLoader;
import dev.klomptech.jbark.config.DogTopics;
import dev.klomptech.jbark.errors.BarkError;
import java.util.ArrayList;
import java.util.List;

// One breed's runtime state: food, toys, name, and the rest
public final class Dog {

  private double food;
  private double items;
  private final List<String> inventory = new ArrayList<>();
  private double age;
  private String name = "";
  private String description = "";
  private String color = "";
  private boolean happy;
  private boolean fed;

  public static Dog fresh() {
    return new Dog();
  }

  public BarkValue get(final String topic, final int line) throws BarkError {
    String field = DogTopics.resolve(topic);
    if (field == null) {
      throw new BarkError(
          line,
          "This dog doesn't track \"" + topic + "\". Try one of: " + DogTopics.CANONICAL + ".");
    }
    return switch (field) {
      case DogTopics.FOOD -> BarkValue.of(food);
      case DogTopics.ITEMS -> BarkValue.of(items);
      case DogTopics.INVENTORY -> BarkValue.of(formatInventory());
      case DogTopics.AGE -> BarkValue.of(age);
      case DogTopics.NAME -> BarkValue.of(name);
      case DogTopics.DESCRIPTION -> BarkValue.of(description);
      case DogTopics.COLOR -> BarkValue.of(color);
      case DogTopics.HAPPY -> BarkValue.of(happy);
      case DogTopics.FED -> BarkValue.of(fed);
      default -> throw new BarkError(line, "Unknown dog field \"" + field + "\".");
    };
  }

  public void set(final String topic, final BarkValue value, final int line) throws BarkError {
    String field = DogTopics.resolve(topic);
    if (field == null) {
      throw new BarkError(
          line,
          "This dog doesn't track \"" + topic + "\". Try one of: " + DogTopics.CANONICAL + ".");
    }
    switch (field) {
      case DogTopics.FOOD -> food = asNumber(value, line);
      case DogTopics.ITEMS -> items = asNumber(value, line);
      case DogTopics.INVENTORY -> setInventory(value, line);
      case DogTopics.AGE -> age = asNumber(value, line);
      case DogTopics.NAME -> name = asString(value, line);
      case DogTopics.DESCRIPTION -> description = asString(value, line);
      case DogTopics.COLOR -> color = asString(value, line);
      case DogTopics.HAPPY -> happy = asBool(value, line);
      case DogTopics.FED -> fed = asBool(value, line);
      default -> throw new BarkError(line, "Unknown dog field \"" + field + "\".");
    }
  }

  public int countObject(final String object) {
    String key = ConfigLoader.normalise(object);
    int count = 0;
    for (String held : inventory) {
      if (held.equals(key)) {
        count++;
      }
    }
    return count;
  }

  public void addHeldObject(final String object) {
    inventory.add(ConfigLoader.normalise(object));
  }

  public void removeHeldObject(final String object) {
    String key = ConfigLoader.normalise(object);
    for (int i = 0; i < inventory.size(); i++) {
      if (inventory.get(i).equals(key)) {
        inventory.remove(i);
        return;
      }
    }
  }

  public void removeAnyHeldObject() {
    if (!inventory.isEmpty()) {
      inventory.remove(inventory.size() - 1);
    }
  }

  private String formatInventory() {
    if (inventory.isEmpty()) {
      return "";
    }
    List<String> parts = new ArrayList<>();
    for (String held : inventory) {
      parts.add(held.replace('_', ' '));
    }
    return String.join(", ", parts);
  }

  private void setInventory(final BarkValue value, final int line) throws BarkError {
    inventory.clear();
    if (value instanceof BarkValue.BarkNull) {
      return;
    }
    if (!(value instanceof BarkValue.BarkString text)) {
      throw new BarkError(
          line,
          "She can't carry "
              + BarkValue.describe(value)
              + " in her mouth. "
              + "List things like ball, stick (comma-separated words).");
    }
    if (text.value().isBlank()) {
      return;
    }
    for (String part : text.value().split(",")) {
      String trimmed = part.trim();
      if (!trimmed.isEmpty()) {
        inventory.add(ConfigLoader.normalise(trimmed.replace(' ', '_')));
      }
    }
  }

  private static double asNumber(final BarkValue value, final int line) throws BarkError {
    if (value instanceof BarkValue.BarkNumber n) {
      return n.value();
    }
    throw new BarkError(line, "That needs a number, not " + BarkValue.describe(value) + ".");
  }

  private static String asString(final BarkValue value, final int line) throws BarkError {
    if (value instanceof BarkValue.BarkString s) {
      return s.value();
    }
    throw new BarkError(line, "That needs words on her tag, not " + BarkValue.describe(value) + ".");
  }

  private static boolean asBool(final BarkValue value, final int line) throws BarkError {
    if (value instanceof BarkValue.BarkBoolean b) {
      return b.value();
    }
    throw new BarkError(line, "Wag yes or naw no. Not " + BarkValue.describe(value) + ".");
  }
}
