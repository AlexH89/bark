from __future__ import annotations

from dataclasses import dataclass

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords
from pbark.parser.collection import stash_spots as StashSpots
from pbark.parser.parse_expression import Empty
from pbark.print_style import PrintStyle


@dataclass
class _StashTake:
    stash: str
    which: str


class TakeParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_take_line(self):
        take = self._find_stash_take_on_line()
        if take is None or not self._parser.line_has_word(Keywords.TAKE_FROM_STASH_WORDS):
            return None
        line = self._parser.peek().line
        subject = self._find_dog_on_line()
        if subject is None:
            raise BarkError(line, "Who is taking from the jar? Name the dog or use she/he first")
        steps = []
        if self._line_has_print_verb():
            style = self._find_print_style_on_line()
            steps.append(
                AstNode.Print(
                    style if style is not None else PrintStyle.BARK,
                    [Empty()],
                    line,
                    False,
                    self._find_dog_speaker_on_line(),
                )
            )
        steps.append(AstNode.TakeFromStash(subject, take.stash, take.which, line))
        self._parser.consume_until_line_end()
        if len(steps) == 1:
            return steps[0]
        return AstNode.LineGroup(steps, line)

    def _find_stash_take_on_line(self) -> _StashTake | None:
        line_start = self._parser.cursor_index()
        line_end = self._parser.line_end_index()
        tokens = self._parser.all_tokens()
        for from_at in range(line_start, line_end):
            token = tokens[from_at]
            if not token.is_(TokenType.IDENTIFIER):
                continue
            if self._parser.normalise(token.value) not in Keywords.FROM_WORDS:
                continue
            stash = ConfigLoader.resolve_collection_from_tokens(tokens, from_at + 1, line_end)
            if stash is None or not ConfigLoader.is_stash(stash):
                continue
            which = self._find_which_before_from(tokens, line_start, from_at)
            if which is not None:
                return _StashTake(stash, which)
            return None
        return None

    def _find_which_before_from(self, tokens, line_start: int, from_at: int) -> str | None:
        for i in range(from_at - 1, line_start - 1, -1):
            token = tokens[i]
            if token.is_(TokenType.STRING):
                return token.value
            if token.is_(TokenType.NUMBER):
                return self._parser.normalise(token.value)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if StashSpots.is_stash_spot(word):
                return word
        return None

    def _find_dog_on_line(self) -> str | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_dog_subject_reference(word, token.value):
                return word
            offset += 1
        return None

    def _find_dog_speaker_on_line(self) -> str | None:
        return self._find_dog_on_line()

    def _line_has_print_verb(self) -> bool:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in Keywords.PRINT_KEYWORDS:
                return True
            offset += 1
        return False

    def _find_print_style_on_line(self) -> PrintStyle | None:
        offset = 0
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER):
                word = self._parser.normalise(token.value)
                if word in Keywords.PRINT_KEYWORDS:
                    return Keywords.PRINT_STYLES.get(word, PrintStyle.BARK)
            offset += 1
        return None
