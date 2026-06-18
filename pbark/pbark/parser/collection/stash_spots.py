from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.errors import BarkError
from pbark.parser.collection.smart_ordinal_parser import parse_word_to_number

_LAST = "last"
_FIRST = "first"


def is_stash_spot(word: str) -> bool:
    normalized = ConfigLoader.normalise(word)
    if normalized in (_LAST, _FIRST):
        return True
    return parse_word_to_number(normalized) > 0


def resolve_index(size: int, which: str, line: int) -> int:
    normalized = ConfigLoader.normalise(which)
    if normalized == _LAST:
        if size == 0:
            raise BarkError(line, "The stash is empty, there is no last bone to dig up.")
        return size - 1
    if normalized == _FIRST:
        return 0
    one_based = parse_word_to_number(normalized)
    if one_based <= 0:
        raise BarkError(line, f'"{which}" is not a stash spot the dogs understand.')
    if one_based > size:
        raise BarkError(
            line,
            f"That stash only has {size} items, the dog checked twice looking for spot {which}.",
        )
    return one_based - 1
