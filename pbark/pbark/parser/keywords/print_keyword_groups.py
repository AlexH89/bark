from __future__ import annotations

from pbark.parser.keywords.keyword_registry import KeywordRegistry


class PrintKeywordGroups:
    def __init__(self, registry: KeywordRegistry) -> None:
        self.print_words = registry.heard_words(
            "say", "says", "shout", "shouts", "mention", "mentions", "command", "commands",
            "announce", "announces", "declare", "declares", "tell", "tells", "whisper", "whispers",
            "mutter", "mutters", "squeak", "squeaks", "pant", "pants", "bark", "barks", "yap", "yaps",
            "growl", "growls", "whimper", "whimpers", "howl", "howls", "woof", "woofs", "whine",
            "whines", "whining", "yapping", "yapps",
        )
        self.exit_words = registry.heard_words("escape", "leave", "depart", "scram", "wander")
        self.stdin_words = registry.heard_words(
            "listen", "listens", "listening", "sniff", "sniffs", "perk", "perks", "hear", "hears",
            "eavesdrop", "eavesdrops", "ask", "asks", "prompt", "prompts", "await", "awaits",
        )
        self.wait_words = registry.heard_words(
            "wait", "waits", "waiting", "sleep", "sleeps", "sleeping", "nap", "naps", "napping",
            "doze", "dozes", "dozing", "snooze", "snoozes", "snoozing", "settle", "settles",
            "settling", "rest", "rests", "resting", "lounge", "lounges", "lounging", "pause",
            "pauses", "pausing", "linger", "lingers", "lingering", "loiter", "loiters", "loitering",
            "dream", "dreams", "dreaming",
        )
