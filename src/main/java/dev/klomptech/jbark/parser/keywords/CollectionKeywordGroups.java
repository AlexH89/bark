package dev.klomptech.jbark.parser.keywords;

import java.util.List;

public final class CollectionKeywordGroups {

  public final List<String> holdsWords;
  public final List<String> findsWords;
  public final List<String> stashAddWords;
  public final List<String> pileGrabFromWords;
  public final List<String> pilePushIntoWords;
  public final List<String> dropsWords;
  public final List<String> digWords;
  public final List<String> countWords;
  public final List<String> joinWords;
  public final List<String> takesWords;
  public final List<String> takeFromStashWords;
  public final List<String> givesWords;
  public final List<String> backWords;
  public final List<String> withWords;
  public final List<String> topWords;
  public final List<String> stashSpotWords;
  public final List<String> sharesWords;
  public final List<String> passesWords;
  public final List<String> toWords;

  public CollectionKeywordGroups(final KeywordRegistry registry) {
    holdsWords =
        registry.heardWords(
            "holds",
            "hold",
            "keeps",
            "keep",
            "stores",
            "store",
            "contains",
            "hoards",
            "guards",
            "stockpiles");
    findsWords =
        registry.heardWords(
            "finds",
            "find",
            "adds",
            "puts",
            "fetches",
            "retrieves",
            "discovers",
            "unearths",
            "snags");
    stashAddWords =
        registry.heardWords(
            "stow", "stows", "snag", "snags", "get", "gets", "collect", "collects");
    pileGrabFromWords =
        registry.heardWords(
            "grab", "grabs", "snatch", "snatches", "pull", "pulls", "take", "takes");
    pilePushIntoWords =
        registry.heardWords(
            "toss", "tosses", "drop", "drops", "throw", "throws", "plop", "plops", "put", "puts",
            "store", "stores");
    dropsWords =
        registry.heardWords(
            "drops",
            "drop",
            "loses",
            "lose",
            "misplaces",
            "tosses",
            "abandons",
            "removes",
            "forgets");
    digWords =
        registry.heardWords(
            "digs", "dig", "pops", "pop", "pulls", "grabs", "extracts", "uncovers", "fishes");
    countWords = registry.heardWords("count", "tally");
    joinWords = registry.heardWords("join", "merge", "combines");
    takesWords = registry.heardWords("takes", "take", "learns", "learn", "knows", "know");
    takeFromStashWords = registry.heardWords("takes", "take", "wants", "want", "gets", "get");
    givesWords = registry.heardWords("gives", "give", "returns", "return", "hands", "hand");
    backWords = registry.heardWords("back");
    withWords = registry.heardWords("with");
    topWords = registry.heardWords("top", "peak", "summit");
    stashSpotWords =
        registry.heardWords(
            "first",
            "second",
            "third",
            "fourth",
            "fifth",
            "sixth",
            "seventh",
            "eighth",
            "ninth",
            "tenth",
            "eleventh",
            "twelfth",
            "thirteenth",
            "fourteenth",
            "fifteenth",
            "last");
    sharesWords = registry.heardWords("share", "shares", "shared", "split", "splits");
    passesWords = registry.heardWords("pass", "passes", "passed", "toss", "tosses", "tossed");
    toWords = registry.heardWords("to");
  }
}
