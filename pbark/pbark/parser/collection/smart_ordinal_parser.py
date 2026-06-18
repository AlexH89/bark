from __future__ import annotations

_VOCAB: dict[str, int] = {}

_ONES = [
    "zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine",
    "ten", "eleven", "twelve", "thirteen", "fourteen", "fifteen", "sixteen",
    "seventeen", "eighteen", "nineteen",
]
_ORDINALS = [
    "zeroth", "first", "second", "third", "fourth", "fifth", "sixth", "seventh",
    "eighth", "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth",
    "fifteenth", "sixteen-th", "seventeenth", "eighteenth", "nineteenth",
]
for i, (one, ord_) in enumerate(zip(_ONES, _ORDINALS)):
    _VOCAB[one] = i
    _VOCAB[ord_] = i

for word, val in [
    ("twenty", 20), ("twentieth", 20), ("thirty", 30), ("thirtieth", 30),
    ("forty", 40), ("fortieth", 40), ("fifty", 50), ("fiftieth", 50),
    ("sixty", 60), ("sixtieth", 60), ("seventy", 70), ("seventieth", 70),
    ("eighty", 80), ("eightieth", 80), ("ninety", 90), ("ninetieth", 90),
    ("hundred", 100), ("hundredth", 100), ("thousand", 1000), ("thousandth", 1000),
    ("million", 1_000_000), ("millionth", 1_000_000),
]:
    _VOCAB[word] = val


def parse_word_to_number(input_word: str) -> int:
    if input_word is None or not input_word.strip():
        return 0
    clean = input_word.strip().lower()
    if clean == "last":
        return -1
    tokens = clean.replace("-", " ").split()
    total_sum = 0
    current_section = 0
    for token in tokens:
        if token == "and":
            continue
        if token not in _VOCAB:
            continue
        value = _VOCAB[token]
        if value == 100:
            current_section *= value
        elif value >= 1000:
            current_section *= value
            total_sum += current_section
            current_section = 0
        else:
            current_section += value
    return total_sum + current_section
