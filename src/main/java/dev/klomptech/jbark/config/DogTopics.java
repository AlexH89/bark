package dev.klomptech.jbark.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

// Dog fields: food and items are separate counters, plus inventory for held objects
// Story words like toys or treats map to the right counter
public final class DogTopics {

  // Treats, snacks, stolen cookies. Greedy applies here.
  public static final String FOOD = "food";
  // Toy count on the collar. Shares and snags use this.
  public static final String ITEMS = "items";
  // Objects the dog is carrying (ball, stick, etc.).
  public static final String INVENTORY = "inventory";

  public static final String AGE = "age";
  public static final String NAME = "name";
  public static final String DESCRIPTION = "description";
  public static final String COLOR = "color";
  public static final String HAPPY = "happy";
  public static final String FED = "fed";

  public static final List<String> CANONICAL =
      List.of(FOOD, ITEMS, INVENTORY, AGE, NAME, DESCRIPTION, COLOR, HAPPY, FED);

  public static final List<String> INT_WORDS =
      List.of(
          "age",
          "food",
          "items",
          "toys",
          "treats",
          "puppies",
          "beds",
          "squirrels",
          "friends",
          "walks",
          "naps",
          "baths",
          "zoomies",
          "tricks");

  public static final List<String> NAME_WORDS = List.of("name", "nickname");

  public static final List<String> DESCRIPTION_WORDS =
      List.of("description", "collar", "tag", "owner", "family");

  public static final List<String> COLOR_WORDS = List.of("color", "species", "gender");

  public static final List<String> STRING_WORDS =
      List.of(
          "name",
          "nickname",
          "description",
          "collar",
          "tag",
          "owner",
          "family",
          "color",
          "species",
          "gender",
          "inventory");

  public static final List<String> NICKNAME_WORDS = List.of("nickname");

  public static final List<String> BOOLEAN_WORDS =
      List.of(
          "goodboy",
          "goodgirl",
          "gooddog",
          "happy",
          "trained",
          "fed",
          "groomed",
          "washed",
          "vetted",
          "sleepy",
          "tired",
          "excited",
          "muddy",
          "wet",
          "dirty",
          "clean",
          "sick",
          "healthy",
          "rescued",
          "adopted",
          "loyal",
          "stubborn",
          "clever",
          "silly",
          "calm",
          "noisy",
          "quiet",
          "lonely");

  private static final Set<String> FED_WORDS = Set.of("fed", "groomed", "washed", "vetted");

  private static final Set<String> HAPPY_WORDS =
      Set.of(
          "goodboy",
          "goodgirl",
          "gooddog",
          "happy",
          "trained",
          "sleepy",
          "tired",
          "excited",
          "muddy",
          "wet",
          "dirty",
          "clean",
          "sick",
          "healthy",
          "rescued",
          "adopted",
          "loyal",
          "stubborn",
          "clever",
          "silly",
          "calm",
          "noisy",
          "quiet",
          "lonely");

  private static final Set<String> FOOD_WORDS =
      Set.of(
          "food", "treat", "treats", "snack", "snacks", "cookie", "cookies", "biscuit", "biscuits");

  private static final Set<String> ITEM_WORDS = Set.of("item", "items", "toy", "toys");

  private static final Map<String, String> ALIAS = buildAliases();

  private DogTopics() {}

  public static String defaultAssignTopic() {
    return AGE;
  }

  // Lines like "she pinches a treat" use the treat word. Plain "she finds a toy" adds to items
  public static String defaultAdjustTopic() {
    return ITEMS;
  }

  public static String defaultFoodAdjustTopic() {
    return FOOD;
  }

  public static String resolve(final String word) {
    if (word == null) {
      return null;
    }
    String normalized = ConfigLoader.normalise(word);
    String mapped = ALIAS.get(normalized);
    if (mapped != null) {
      return mapped;
    }
    if (ConfigLoader.typeOf(normalized).orElse(null) == VariableType.OBJECT) {
      return null;
    }
    if (CANONICAL_SET.contains(normalized)) {
      return normalized;
    }
    return null;
  }

  public static boolean isFieldWord(final String word) {
    return resolve(word) != null;
  }

  public static boolean isFoodField(final String field) {
    return FOOD.equals(field);
  }

  public static boolean isItemsField(final String field) {
    return ITEMS.equals(field);
  }

  public static boolean isNumericField(final String field) {
    return FOOD.equals(field) || ITEMS.equals(field) || AGE.equals(field);
  }

  private static final Set<String> CANONICAL_SET = Set.copyOf(CANONICAL);

  private static Map<String, String> buildAliases() {
    Map<String, String> map = new HashMap<>();
    for (String word : FOOD_WORDS) {
      map.put(word, FOOD);
    }
    for (String word : ITEM_WORDS) {
      map.put(word, ITEMS);
    }
    map.put("puppy", ITEMS);
    map.put("bed", ITEMS);
    map.put("walk", ITEMS);
    map.put("nap", ITEMS);
    map.put("trick", ITEMS);
    map.put("friend", ITEMS);
    map.put("squirrel", ITEMS);
    for (String word : INT_WORDS) {
      if (!AGE.equals(word) && !FOOD.equals(word) && !ITEMS.equals(word)) {
        map.putIfAbsent(word, ITEMS);
      }
    }
    for (String word : NAME_WORDS) {
      map.putIfAbsent(word, NAME);
    }
    for (String word : DESCRIPTION_WORDS) {
      map.putIfAbsent(word, DESCRIPTION);
    }
    for (String word : COLOR_WORDS) {
      if (!COLOR.equals(word)) {
        map.putIfAbsent(word, COLOR);
      }
    }
    for (String word : BOOLEAN_WORDS) {
      if (FED_WORDS.contains(word)) {
        map.putIfAbsent(word, FED);
      } else if (HAPPY_WORDS.contains(word)) {
        map.putIfAbsent(word, HAPPY);
      }
    }
    map.put("inventory", INVENTORY);
    map.put("belongings", INVENTORY);
    return Map.copyOf(map);
  }
}
