from __future__ import annotations

from pbark.parser.keywords.keyword_registry import KeywordRegistry


class ControlFlowKeywordGroups:
    def __init__(self, registry: KeywordRegistry) -> None:
        self.if_start_words = registry.phrase_words("if", "when", "whenever", "should")
        self.while_words = registry.phrase_words("while", "during")
        self.until_words = registry.phrase_words("until", "till")
        self.for_words = registry.phrase_words("for")
        self.each_words = registry.phrase_words("each", "every")
        self.from_words = registry.phrase_words("from")
        self.then_words = registry.phrase_words("then", "do")
        self.otherwise_words = registry.phrase_words("otherwise", "instead")
        self.else_words = registry.phrase_words("else")
        self.bury_words = registry.phrase_words(
            "bury", "end", "goodnight", "enough", "bedtime", "lightsout", "hush", "done", "finished"
        )
        self.break_words = registry.phrase_words("heel", "stop", "stay", "halt")
        self.continue_words = registry.phrase_words("again", "repeat", "resume", "onward")
        self.expects_words = registry.phrase_words("expects", "expect")
        self.trick_returns_words = registry.phrase_words("returns", "return")
