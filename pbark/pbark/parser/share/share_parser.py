from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.lexer import TokenType
from pbark.parser import ast_node as AstNode
from pbark.parser import keywords as Keywords


class ShareParser:
    def __init__(self, parser) -> None:
        self._parser = parser

    def parse_pass_line(self):
        line = self._parser.peek().line
        pass_at = self._find_word_after(-1, Keywords.PASSES_STATEMENT_KEYWORDS)
        if pass_at < 0:
            return None
        to_at = self._find_word_after(pass_at, Keywords.TO_WORDS)
        if to_at < 0:
            return None
        giver = self._find_dog_before(pass_at)
        if giver is None:
            raise BarkError(line, "Only dogs can pass toys, name who is giving first")
        recipients = self._find_dogs_after(to_at + 1)
        if not recipients:
            raise BarkError(line, "Passing needs one friend, give the name of who gets the item")
        if len(recipients) > 1:
            raise BarkError(line, "One pass, one friend. Use shares when more dogs get a slice")
        self._parser.consume_until_line_end()
        return AstNode.Pass(giver, recipients[0], line)

    def parse_share_line(self):
        line = self._parser.peek().line
        shares_at = self._find_word_after(-1, Keywords.SHARES_STATEMENT_KEYWORDS)
        if shares_at < 0:
            return None
        with_at = self._find_word_after(shares_at, Keywords.WITH_WORDS)
        if with_at < 0:
            return None
        giver = self._find_dog_before(shares_at)
        if giver is None:
            raise BarkError(line, "Only dogs can share. Name who is giving first")
        stash = None
        if with_at > shares_at + 1:
            line_start = self._parser.cursor_index()
            stash = ConfigLoader.resolve_collection_from_tokens(
                self._parser.all_tokens(), line_start + shares_at + 1, line_start + with_at
            )
        recipients = self._find_dogs_after(with_at + 1)
        if not recipients:
            raise BarkError(
                line, "The dogs need a friend to share with. Name another dog after with"
            )
        self._parser.consume_until_line_end()
        return AstNode.Share(giver, stash, recipients, line)

    def _find_word_after(self, after_offset: int, words: list[str]) -> int:
        start = after_offset + 1
        offset = start
        while not self._parser.is_at_end_or_line_end_offset(offset):
            token = self._parser.peek_at(offset)
            if token.is_(TokenType.IDENTIFIER) and self._parser.normalise(token.value) in words:
                return offset
            offset += 1
        return -1

    def _find_dog_before(self, before_offset: int) -> str | None:
        for offset in range(before_offset):
            token = self._parser.peek_at(offset)
            if not token.is_(TokenType.IDENTIFIER):
                continue
            word = self._parser.normalise(token.value)
            if Keywords.is_dog_subject_reference(word, token.value):
                return word
        return None

    def _find_dogs_after(self, start_offset: int) -> list[str]:
        line = self._parser.peek().line
        line_start = self._parser.cursor_index()
        line_end = self._parser.line_end_index()
        dogs: list[str] = []
        offset = start_offset
        while offset < line_end - line_start:
            abs_idx = line_start + offset
            token = self._parser.all_tokens()[abs_idx]
            if token.is_(TokenType.COMMA):
                offset += 1
                continue
            if not token.is_(TokenType.IDENTIFIER):
                offset += 1
                continue
            word = self._parser.normalise(token.value)
            if (
                word in Keywords.THE_WORDS
                or word in Keywords.ARTICLE_WORDS
                or word in Keywords.LIST_AND_WORDS
                or word in Keywords.WITH_WORDS
                or word in Keywords.SHARES_STATEMENT_KEYWORDS
            ):
                offset += 1
                continue
            breed = ConfigLoader.resolve_name_from_tokens(
                self._parser.all_tokens(), abs_idx, line_end
            )
            if breed is not None and (
                ConfigLoader.is_breed(breed)
                or Keywords.is_breed_pronoun_word(breed)
                or Keywords.is_pet_name_word(breed, token.value)
            ):
                dogs.append(breed)
                offset = self._advance_past_name_phrase(offset, breed)
                continue
            if (
                ConfigLoader.is_breed(word)
                or Keywords.is_breed_pronoun_word(word)
                or Keywords.is_pet_name_word(word, token.value)
            ):
                dogs.append(word)
                offset += 1
                continue
            if not Keywords.is_ignored(word):
                raise BarkError(line, f'"{word}" is not a dog the pack can share with.')
            offset += 1
        return dogs

    def _advance_past_name_phrase(self, offset_from_line_start: int, name: str) -> int:
        line_start = self._parser.cursor_index()
        line_end = self._parser.line_end_index()
        words: list[str] = []
        positions: list[int] = []
        for abs_idx in range(line_start + offset_from_line_start, line_end):
            token = self._parser.all_tokens()[abs_idx]
            if token.is_(TokenType.IDENTIFIER):
                words.append(self._parser.normalise(token.value))
                positions.append(abs_idx)
        for length in range(min(4, len(words)), 0, -1):
            for start in range(len(words) - length + 1):
                phrase = " ".join(words[start : start + length])
                if name == ConfigLoader.resolve_name_phrase(phrase):
                    return positions[start + length - 1] + 1 - line_start
        return offset_from_line_start + 1
