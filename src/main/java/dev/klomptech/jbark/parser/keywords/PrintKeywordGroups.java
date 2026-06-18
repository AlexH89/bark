package dev.klomptech.jbark.parser.keywords;

import java.util.List;

public final class PrintKeywordGroups {

  public final List<String> printWords;
  public final List<String> exitWords;
  public final List<String> stdinWords;
  public final List<String> waitWords;

  public PrintKeywordGroups(final KeywordRegistry registry) {
    printWords =
        registry.heardWords(
            "say",
            "says",
            "shout",
            "shouts",
            "mention",
            "mentions",
            "command",
            "commands",
            "announce",
            "announces",
            "declare",
            "declares",
            "tell",
            "tells",
            "whisper",
            "whispers",
            "mutter",
            "mutters",
            "squeak",
            "squeaks",
            "pant",
            "pants",
            "bark",
            "barks",
            "yap",
            "yaps",
            "growl",
            "growls",
            "whimper",
            "whimpers",
            "howl",
            "howls",
            "woof",
            "woofs",
            "whine",
            "whines",
            "whining",
            "yapping",
            "yapps");
    exitWords = registry.heardWords("escape", "leave", "depart", "scram", "wander");
    stdinWords =
        registry.heardWords(
            "listen",
            "listens",
            "listening",
            "sniff",
            "sniffs",
            "perk",
            "perks",
            "hear",
            "hears",
            "eavesdrop",
            "eavesdrops",
            "ask",
            "asks",
            "prompt",
            "prompts",
            "await",
            "awaits");
    waitWords =
        registry.heardWords(
            "wait",
            "waits",
            "waiting",
            "sleep",
            "sleeps",
            "sleeping",
            "nap",
            "naps",
            "napping",
            "doze",
            "dozes",
            "dozing",
            "snooze",
            "snoozes",
            "snoozing",
            "settle",
            "settles",
            "settling",
            "rest",
            "rests",
            "resting",
            "lounge",
            "lounges",
            "lounging",
            "pause",
            "pauses",
            "pausing",
            "linger",
            "lingers",
            "lingering",
            "loiter",
            "loiters",
            "loitering",
            "dream",
            "dreams",
            "dreaming");
  }
}
