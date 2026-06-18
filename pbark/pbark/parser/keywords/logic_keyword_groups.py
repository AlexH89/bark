from __future__ import annotations

from pbark.parser.keywords.keyword_registry import KeywordRegistry


class LogicKeywordGroups:
    def __init__(self, registry: KeywordRegistry) -> None:
        self.logical_and_words = registry.phrase_words("both", "and", "also")
        self.condition_and_words = registry.phrase_words("and")
        self.logical_or_words = registry.phrase_words("either")
        self.neither_words = registry.phrase_words("neither")
        self.nor_words = registry.phrase_words("nor")
        self.comparison_equal_words = registry.phrase_words("matches", "equals", "matching")
        self.comparison_not_equal_words = registry.phrase_words("different", "unlike", "differs")
        self.comparison_greater_words = registry.phrase_words("louder", "bigger", "more", "over", "above", "exceeds")
        self.comparison_less_words = registry.phrase_words("less", "fewer", "smaller", "under", "below", "quieter")
        self.comparison_at_least_words = registry.phrase_words("least")
        self.comparison_at_most_words = registry.phrase_words("most")
        self.not_words = registry.heard_words("not", "never")
        self.counts_words = registry.heard_words("counts")
        self.letters_words = registry.heard_words("letters", "length", "characters")
        self.inside_words = registry.heard_words("inside", "within", "through")
        self.plus_words = registry.heard_words("plus", "add")
        self.minus_words = registry.heard_words("minus", "spends")
        self.star_words = registry.phrase_words(
            "times", "multiply", "multiplies", "multiplied", "birthed", "births", "whelps", "whelped",
            "double", "doubles", "duplicates", "clones",
        )
        self.slash_words = registry.heard_words("broke", "divide", "divides", "halves")
        self.boolean_values = registry.heard_words("true", "false", "yes", "no", "yeah", "nope", "yep")
        self.null_values = registry.heard_words(
            "null", "nothing", "notreat", "emptybowl", "no-bone-here", "nobodyhome", "hungry", "starving",
            "empty", "barebowl", "emptykennel", "allgone", "vanished", "missing", "nowhere", "treatless",
            "boneless", "squeakless", "nofetch",
        )
