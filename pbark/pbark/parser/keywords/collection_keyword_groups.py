from __future__ import annotations

from pbark.parser.keywords.keyword_registry import KeywordRegistry


class CollectionKeywordGroups:
    def __init__(self, registry: KeywordRegistry) -> None:
        self.holds_words = registry.heard_words(
            "holds", "hold", "keeps", "keep", "stores", "store", "contains", "hoards", "guards", "stockpiles"
        )
        self.finds_words = registry.heard_words(
            "finds", "find", "adds", "puts", "fetches", "retrieves", "discovers", "unearths", "snags"
        )
        self.stash_add_words = registry.heard_words("stow", "stows", "snag", "snags", "get", "gets", "collect", "collects")
        self.pile_grab_from_words = registry.heard_words(
            "grab", "grabs", "snatch", "snatches", "pull", "pulls", "take", "takes"
        )
        self.pile_push_into_words = registry.heard_words(
            "toss", "tosses", "drop", "drops", "throw", "throws", "plop", "plops", "put", "puts", "store", "stores"
        )
        self.drops_words = registry.heard_words(
            "drops", "drop", "loses", "lose", "misplaces", "tosses", "abandons", "removes", "forgets"
        )
        self.dig_words = registry.heard_words(
            "digs", "dig", "pops", "pop", "pulls", "grabs", "extracts", "uncovers", "fishes"
        )
        self.count_words = registry.heard_words("count", "tally")
        self.join_words = registry.heard_words("join", "merge", "combines")
        self.takes_words = registry.heard_words("takes", "take", "learns", "learn", "knows", "know")
        self.take_from_stash_words = registry.heard_words("takes", "take", "wants", "want", "gets", "get")
        self.gives_words = registry.heard_words("gives", "give", "returns", "return", "hands", "hand")
        self.back_words = registry.heard_words("back")
        self.with_words = registry.heard_words("with")
        self.top_words = registry.heard_words("top", "peak", "summit")
        self.stash_spot_words = registry.heard_words(
            "first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth", "ninth",
            "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "last",
        )
        self.shares_words = registry.heard_words("share", "shares", "shared", "split", "splits")
        self.passes_words = registry.heard_words("pass", "passes", "passed", "toss", "tosses", "tossed")
        self.to_words = registry.heard_words("to")
