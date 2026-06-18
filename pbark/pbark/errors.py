from __future__ import annotations

from enum import IntEnum


class ExitCode(IntEnum):
    OK = 0
    INVALID_ARGUMENTS = 1
    USAGE = 64
    DATA_ERROR = 65


class BarkError(Exception):
    def __init__(self, line: int, message: str) -> None:
        super().__init__(message)
        self.line = line

    @staticmethod
    def error(line: int, message: str) -> BarkError:
        return BarkError(line, message)

    def __str__(self) -> str:
        if self.line > 0:
            return f"Line {self.line}: {self.args[0]}"
        return str(self.args[0])


class BarktimeError(BarkError):
    pass
