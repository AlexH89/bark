from __future__ import annotations

import io
import sys
from contextlib import contextmanager

import pytest

from pbark.errors import BarkError
from pbark.interpreter.interpreter import Interpreter
from pbark.lexer import Lexer
from pbark.options import BarkOptions
from pbark.parser.parser import Parser

FETCHY_REPEAT = "*tail THUMP THUMP, same stick, still the best stick*"


def run_lines(source: str) -> list[str]:
    lexer = Lexer(source)
    parser = Parser(lexer.tokenise(), BarkOptions.defaults())
    program = parser.parse()
    buffer = io.StringIO()
    previous = sys.stdout
    sys.stdout = buffer
    try:
        Interpreter().run(program)
    finally:
        sys.stdout = previous
    return [line.strip() for line in buffer.getvalue().splitlines() if line.strip()]


def run_joined(source: str) -> str:
    return "\n".join(run_lines(source))


def parse_line(source: str):
    program = Parser(Lexer(source + "\n").tokenise(), BarkOptions.defaults()).parse()
    assert len(program.statements) == 1
    return program.statements[0]


def parse_program(source: str):
    return Parser(Lexer(source).tokenise(), BarkOptions.defaults()).parse().statements


@pytest.fixture
def examples_dir():
    from pathlib import Path

    return Path(__file__).resolve().parent.parent.parent / "examples" / "woof"
