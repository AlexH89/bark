from __future__ import annotations

import json

from pbark.config._resources import resources_dir
from pbark.config.config_loader import ConfigLoader


class TraitLoader:
    GREEDY_FEEDS_BEFORE_LOSS = 3

    _DEFAULT_TRAITS = None

    @classmethod
    def _load_traits(cls) -> dict[str, set[str]]:
        path = resources_dir() / "traits.json"
        if not path.is_file():
            return {}
        raw = json.loads(path.read_text(encoding="utf-8"))
        traits: dict[str, set[str]] = {}
        for breed, trait_list in raw.items():
            names = {ConfigLoader.normalise(t) for t in trait_list}
            traits[ConfigLoader.normalise(breed)] = names
        return traits

    @classmethod
    def default_traits(cls) -> dict[str, set[str]]:
        if cls._DEFAULT_TRAITS is None:
            cls._DEFAULT_TRAITS = cls._load_traits()
        return {k: set(v) for k, v in cls._DEFAULT_TRAITS.items()}
