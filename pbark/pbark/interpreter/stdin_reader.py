from __future__ import annotations

import sys

from pbark.errors import BarkError
from pbark.interpreter import bark_value as bv


class StdinReader:
    @staticmethod
    def read(line: int) -> bv.BarkValue:
        try:
            input_line = sys.stdin.readline()
            if input_line == "":
                return bv.BarkNull()
            return bv.of(input_line.rstrip("\n"))
        except OSError as exc:
            raise BarkError(line, "The human went quiet. Couldn't catch anything from stdin.") from exc
