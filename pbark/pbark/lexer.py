from __future__ import annotations

from dataclasses import dataclass
from enum import Enum, auto

from pbark.errors import BarkError


class TokenType(Enum):
    EOF = auto()
    PERIOD = auto()
    COMMA = auto()
    NEWLINE = auto()
    STRING = auto()
    NUMBER = auto()
    IDENTIFIER = auto()
    PLUS = auto()
    MINUS = auto()
    STAR = auto()
    SLASH = auto()


@dataclass(frozen=True)
class Token:
    type: TokenType
    value: str
    line: int

    def is_(self, token_type: TokenType) -> bool:
        return self.type is token_type



class Lexer:
    def __init__(self, source: str) -> None:
        self._source = source
        self._tokens: list[Token] = []
        self._current = 0
        self._line = 1

    def tokenise(self) -> list[Token]:
        while not self._is_at_end():
            self._scan_token()
        self._tokens.append(Token(TokenType.EOF, "", self._line))
        return self._tokens

    def _scan_token(self) -> None:
        c = self._advance()
        if c in " \r\t:":
            return
        if c == "\n":
            self._newline()
            return
        if c == ".":
            if self._is_digit(self._peek()):
                self._number()
            else:
                self._tokens.append(Token(TokenType.PERIOD, ".", self._line))
            return
        if c == ",":
            self._tokens.append(Token(TokenType.COMMA, ",", self._line))
            return
        if c == "+":
            self._tokens.append(Token(TokenType.PLUS, "+", self._line))
            return
        if c == "*":
            self._tokens.append(Token(TokenType.STAR, "*", self._line))
            return
        if c == "-":
            if self._is_digit(self._peek()) or (
                self._peek() == "." and self._is_digit(self._peek_next())
            ):
                self._number()
            else:
                self._tokens.append(Token(TokenType.MINUS, "-", self._line))
            return
        if c == "/":
            if self._match("/"):
                self._skip_to_end_of_line()
            else:
                self._tokens.append(Token(TokenType.SLASH, "/", self._line))
            return
        if c == "#":
            self._skip_to_end_of_line()
            return
        if c == '"':
            self._string()
            return
        if self._is_digit(c):
            self._number()
            return
        if self._is_alpha(c):
            self._word()
            return
        raise BarkError.error(
            self._line,
            f"Sniffed a '{c}'. Dogs don't bury symbols like that.",
        )

    def _newline(self) -> None:
        self._tokens.append(Token(TokenType.NEWLINE, "\n", self._line))
        self._line += 1

    def _skip_to_end_of_line(self) -> None:
        while self._peek() != "\n" and not self._is_at_end():
            self._advance()

    def _string(self) -> None:
        start = self._current
        while not self._is_at_end():
            if self._peek() == '"':
                self._advance()
                value = self._source[start : self._current - 1]
                self._tokens.append(Token(TokenType.STRING, value, self._line))
                return
            if self._peek() == "\n":
                raise self._error(
                    "Unfinished commands, you have some confused dog faces starting at you!"
                )
            self._advance()
        raise self._error('You started a howl with " but it never finished?')

    def _number(self) -> None:
        start = self._current - 1
        while self._is_digit(self._peek()):
            self._advance()
        if self._peek() == "." and self._is_digit(self._peek_next()):
            self._advance()
            while self._is_digit(self._peek()):
                self._advance()
        value = self._source[start : self._current]
        self._tokens.append(Token(TokenType.NUMBER, value, self._line))

    def _word(self) -> None:
        start = self._current - 1
        while self._is_alpha_numeric(self._peek()):
            self._advance()
        end = self._current
        self._consume_possessive()
        text = self._source[start:end]
        self._tokens.append(Token(TokenType.IDENTIFIER, text, self._line))

    def _consume_possessive(self) -> None:
        if self._peek() == "'" and self._peek_next().lower() == "s":
            self._advance()
            self._advance()

    def _advance(self) -> str:
        c = self._source[self._current]
        self._current += 1
        return c

    def _match(self, expected: str) -> bool:
        if self._is_at_end() or self._source[self._current] != expected:
            return False
        self._current += 1
        return True

    def _peek(self) -> str:
        if self._is_at_end():
            return "\0"
        return self._source[self._current]

    def _peek_next(self) -> str:
        if self._current + 1 >= len(self._source):
            return "\0"
        return self._source[self._current + 1]

    def _is_at_end(self) -> bool:
        return self._current >= len(self._source)

    @staticmethod
    def _is_digit(c: str) -> bool:
        return "0" <= c <= "9"

    @staticmethod
    def _is_alpha(c: str) -> bool:
        return ("a" <= c <= "z") or ("A" <= c <= "Z") or c in "_\\"

    def _is_alpha_numeric(self, c: str) -> bool:
        return self._is_alpha(c) or self._is_digit(c)

    def _error(self, message: str) -> BarkError:
        return BarkError(self._line, message)
