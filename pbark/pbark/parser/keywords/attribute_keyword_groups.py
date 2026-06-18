from __future__ import annotations

from pbark.parser.keywords.keyword_registry import KeywordRegistry


class AttributeKeywordGroups:
    def __init__(self, registry: KeywordRegistry) -> None:
        self.trait_words = registry.heard_words("loud", "greedy", "lazy", "wet", "fetchy", "chaser", "playful")
        self.food_increment_words = registry.heard_words("feed", "feeds", "refills", "steals", "pinch", "pinches")
        self.item_increment_words = registry.heard_words(
            "grow", "grows", "grew", "gets", "receives", "earns", "gains", "collects", "snags", "nabs",
            "inherits", "scores", "find", "finds", "fetch", "fetches", "discovers", "unearths",
        )
        self.increment_words = list(dict.fromkeys(self.food_increment_words + self.item_increment_words))
        self.food_decrement_words = registry.heard_words(
            "gulp", "gulps", "eat", "eats", "ate", "devour", "devours", "chews", "munches", "scarfs", "wolfs", "nibbles"
        )
        self.item_decrement_words = registry.heard_words(
            "buried", "lost", "misplaces", "breaks", "destroys", "hides", "buries", "forgets", "tosses", "abandons"
        )
        self.decrement_words = list(dict.fromkeys(self.food_decrement_words + self.item_decrement_words))
        self.type_number_words = registry.heard_words("number", "digit", "digits")
        self.type_words_words = registry.heard_words("words", "text")
        self.type_nothing_words = registry.heard_words("nothing")
        self.sniff_words = registry.heard_words("sniffs", "smells", "scents", "checks", "peeks")
        self.growl_words = registry.heard_words("growls", "roars", "bellows")
        self.same_words = registry.phrase_words("same")
        self.than_words = registry.phrase_words("than")
        self.at_words = registry.phrase_words("at")
        self.list_and_words = registry.phrase_words("and")
        self.the_words = registry.phrase_words("the")
        self.of_words = registry.phrase_words("of")
        self.items_words = registry.phrase_words("items")
        self.in_words = registry.phrase_words("in", "into")
        self.as_words = registry.phrase_words("as")
        self.by_words = registry.phrase_words("by")
        self.how_words = registry.phrase_words("how")
        self.many_words = registry.phrase_words("many")
        self.years_words = registry.phrase_words("years", "year")
        self.old_words = registry.phrase_words("old")
        self.together_words = registry.phrase_words("together")
        self.or_words = registry.phrase_words("or")
