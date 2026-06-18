from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.config.variable_type import VariableType


class DogTopics:
    FOOD = "food"
    ITEMS = "items"
    INVENTORY = "inventory"
    AGE = "age"
    NAME = "name"
    DESCRIPTION = "description"
    COLOR = "color"
    HAPPY = "happy"
    FED = "fed"

    CANONICAL = [
        FOOD,
        ITEMS,
        INVENTORY,
        AGE,
        NAME,
        DESCRIPTION,
        COLOR,
        HAPPY,
        FED,
    ]

    INT_WORDS = [
        "age",
        "food",
        "items",
        "toys",
        "treats",
        "puppies",
        "beds",
        "squirrels",
        "friends",
        "walks",
        "naps",
        "baths",
        "zoomies",
        "tricks",
    ]

    NAME_WORDS = ["name", "nickname"]
    DESCRIPTION_WORDS = ["description", "collar", "tag", "owner", "family"]
    COLOR_WORDS = ["color", "species", "gender"]
    STRING_WORDS = NAME_WORDS + DESCRIPTION_WORDS + COLOR_WORDS + ["inventory"]
    NICKNAME_WORDS = ["nickname"]
    BOOLEAN_WORDS = [
        "goodboy",
        "goodgirl",
        "gooddog",
        "happy",
        "trained",
        "fed",
        "groomed",
        "washed",
        "vetted",
        "sleepy",
        "tired",
        "excited",
        "muddy",
        "wet",
        "dirty",
        "clean",
        "sick",
        "healthy",
        "rescued",
        "adopted",
        "loyal",
        "stubborn",
        "clever",
        "silly",
        "calm",
        "noisy",
        "quiet",
        "lonely",
    ]

    _FED_WORDS = frozenset({"fed", "groomed", "washed", "vetted"})
    _HAPPY_WORDS = frozenset(
        {
            "goodboy",
            "goodgirl",
            "gooddog",
            "happy",
            "trained",
            "sleepy",
            "tired",
            "excited",
            "muddy",
            "wet",
            "dirty",
            "clean",
            "sick",
            "healthy",
            "rescued",
            "adopted",
            "loyal",
            "stubborn",
            "clever",
            "silly",
            "calm",
            "noisy",
            "quiet",
            "lonely",
        }
    )
    _FOOD_WORDS = frozenset(
        {
            "food",
            "treat",
            "treats",
            "snack",
            "snacks",
            "cookie",
            "cookies",
            "biscuit",
            "biscuits",
        }
    )
    _ITEM_WORDS = frozenset({"item", "items", "toy", "toys"})
    _CANONICAL_SET = frozenset(CANONICAL)
    _ALIAS = None

    @classmethod
    def _aliases(cls) -> dict[str, str]:
        if cls._ALIAS is None:
            cls._ALIAS = cls._build_aliases()
        return cls._ALIAS

    @staticmethod
    def default_assign_topic() -> str:
        return DogTopics.AGE

    @staticmethod
    def default_adjust_topic() -> str:
        return DogTopics.ITEMS

    @staticmethod
    def default_food_adjust_topic() -> str:
        return DogTopics.FOOD

    @classmethod
    def resolve(cls, word: str | None) -> str | None:
        if word is None:
            return None
        normalized = ConfigLoader.normalise(word)
        mapped = cls._aliases().get(normalized)
        if mapped is not None:
            return mapped
        if ConfigLoader.type_of(normalized) == VariableType.OBJECT:
            return None
        if normalized in cls._CANONICAL_SET:
            return normalized
        return None

    @classmethod
    def is_field_word(cls, word: str) -> bool:
        return cls.resolve(word) is not None

    @staticmethod
    def is_food_field(field: str) -> bool:
        return DogTopics.FOOD == field

    @staticmethod
    def is_items_field(field: str) -> bool:
        return DogTopics.ITEMS == field

    @staticmethod
    def is_numeric_field(field: str) -> bool:
        return field in (DogTopics.FOOD, DogTopics.ITEMS, DogTopics.AGE)

    @classmethod
    def _build_aliases(cls) -> dict[str, str]:
        m: dict[str, str] = {}
        for word in cls._FOOD_WORDS:
            m[word] = cls.FOOD
        for word in cls._ITEM_WORDS:
            m[word] = cls.ITEMS
        for extra in ("puppy", "bed", "walk", "nap", "trick", "friend", "squirrel"):
            m[extra] = cls.ITEMS
        for word in cls.INT_WORDS:
            if word not in (cls.AGE, cls.FOOD, cls.ITEMS):
                m.setdefault(word, cls.ITEMS)
        for word in cls.NAME_WORDS:
            m.setdefault(word, cls.NAME)
        for word in cls.DESCRIPTION_WORDS:
            m.setdefault(word, cls.DESCRIPTION)
        for word in cls.COLOR_WORDS:
            if word != cls.COLOR:
                m.setdefault(word, cls.COLOR)
        for word in cls.BOOLEAN_WORDS:
            if word in cls._FED_WORDS:
                m.setdefault(word, cls.FED)
            elif word in cls._HAPPY_WORDS:
                m.setdefault(word, cls.HAPPY)
        m["inventory"] = cls.INVENTORY
        m["belongings"] = cls.INVENTORY
        return dict(m)
