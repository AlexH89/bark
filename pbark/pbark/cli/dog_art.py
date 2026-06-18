from __future__ import annotations

import random

from pbark.config._resources import resources_dir

EMOJI = "\U0001f415"
PAW = "\U0001f43e"
FACT_PREFIX = "Funny fact: "

_EMOJIS = [EMOJI, "\U0001f436", "woof."]

_BANNERS = [
    """
      ___    __
    /(. .)\\    )
     (*)_____/|
     /        |
    /    |--\\ |
   (_)(_)   (_)
""",
    """
    / \\__
   (    @\\___
   /         O
  /   (_____/
 /_____/   U
    woof!
""",
    """
                __
   (\\,--------'()'--o
    (_    ___    /~"
     (_)_)  (_)_)
""",
]


def _load_goodbyes() -> list[str]:
    lines = [
        "The dogs are now chilling on your bed.",
        "After dragging the bed through the house, no more energy to play.",
        "That was a good walk, time to relax.",
        "The dogs had enough of playing, they went upstairs.",
    ]
    path = resources_dir() / "dogfacts.txt"
    if path.is_file():
        for line in path.read_text(encoding="utf-8").splitlines():
            stripped = line.strip()
            if stripped:
                lines.append(FACT_PREFIX + stripped)
    return lines


_GOODBYES = _load_goodbyes()


def print_banner() -> None:
    print(random.choice(_BANNERS))


def random_bare_bark() -> str:
    return random.choice(_EMOJIS)


def print_goodbye() -> None:
    print(random.choice(_GOODBYES))


def paws_art() -> str:
    return f"{PAW}   {PAW}   {PAW}"


def print_paws() -> None:
    print(paws_art())
