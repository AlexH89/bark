from __future__ import annotations

from pbark.config.config_loader import ConfigLoader
from pbark.config.dog_topics import DogTopics
from pbark.errors import BarkError
from pbark.interpreter import bark_value as bv


class Dog:
    def __init__(self) -> None:
        self.food = 0.0
        self.items = 0.0
        self.inventory: list[str] = []
        self.age = 0.0
        self.name = ""
        self.description = ""
        self.color = ""
        self.happy = False
        self.fed = False

    @staticmethod
    def fresh() -> Dog:
        return Dog()

    def get(self, topic: str, line: int) -> bv.BarkValue:
        field = DogTopics.resolve(topic)
        if field is None:
            raise BarkError(
                line,
                f'This dog doesn\'t track "{topic}". Try one of: {DogTopics.CANONICAL}.',
            )
        if field == DogTopics.FOOD:
            return bv.of(self.food)
        if field == DogTopics.ITEMS:
            return bv.of(self.items)
        if field == DogTopics.INVENTORY:
            return bv.of(self._format_inventory())
        if field == DogTopics.AGE:
            return bv.of(self.age)
        if field == DogTopics.NAME:
            return bv.of(self.name)
        if field == DogTopics.DESCRIPTION:
            return bv.of(self.description)
        if field == DogTopics.COLOR:
            return bv.of(self.color)
        if field == DogTopics.HAPPY:
            return bv.of(self.happy)
        if field == DogTopics.FED:
            return bv.of(self.fed)
        raise BarkError(line, f'Unknown dog field "{field}".')

    def set(self, topic: str, value: bv.BarkValue, line: int) -> None:
        field = DogTopics.resolve(topic)
        if field is None:
            raise BarkError(
                line,
                f'This dog doesn\'t track "{topic}". Try one of: {DogTopics.CANONICAL}.',
            )
        if field == DogTopics.FOOD:
            self.food = self._as_number(value, line)
        elif field == DogTopics.ITEMS:
            self.items = self._as_number(value, line)
        elif field == DogTopics.INVENTORY:
            self._set_inventory(value, line)
        elif field == DogTopics.AGE:
            self.age = self._as_number(value, line)
        elif field == DogTopics.NAME:
            self.name = self._as_string(value, line)
        elif field == DogTopics.DESCRIPTION:
            self.description = self._as_string(value, line)
        elif field == DogTopics.COLOR:
            self.color = self._as_string(value, line)
        elif field == DogTopics.HAPPY:
            self.happy = self._as_bool(value, line)
        elif field == DogTopics.FED:
            self.fed = self._as_bool(value, line)
        else:
            raise BarkError(line, f'Unknown dog field "{field}".')

    def count_object(self, object_name: str) -> int:
        key = ConfigLoader.normalise(object_name)
        return sum(1 for held in self.inventory if held == key)

    def add_held_object(self, object_name: str) -> None:
        self.inventory.append(ConfigLoader.normalise(object_name))

    def remove_held_object(self, object_name: str) -> None:
        key = ConfigLoader.normalise(object_name)
        for i, held in enumerate(self.inventory):
            if held == key:
                del self.inventory[i]
                return

    def remove_any_held_object(self) -> None:
        if self.inventory:
            del self.inventory[-1]

    def _format_inventory(self) -> str:
        if not self.inventory:
            return ""
        return ", ".join(h.replace("_", " ") for h in self.inventory)

    def _set_inventory(self, value: bv.BarkValue, line: int) -> None:
        self.inventory.clear()
        if isinstance(value, bv.BarkNull):
            return
        if not isinstance(value, bv.BarkString):
            raise BarkError(
                line,
                f"She can't carry {bv.describe(value)} in her mouth. "
                "List things like ball, stick (comma-separated words).",
            )
        if not value.value.strip():
            return
        for part in value.value.split(","):
            trimmed = part.strip()
            if trimmed:
                self.inventory.append(ConfigLoader.normalise(trimmed.replace(" ", "_")))

    @staticmethod
    def _as_number(value: bv.BarkValue, line: int) -> float:
        if isinstance(value, bv.BarkNumber):
            return value.value
        raise BarkError(line, f"That needs a number, not {bv.describe(value)}.")

    @staticmethod
    def _as_string(value: bv.BarkValue, line: int) -> str:
        if isinstance(value, bv.BarkString):
            return value.value
        raise BarkError(line, f"That needs words on her tag, not {bv.describe(value)}.")

    @staticmethod
    def _as_bool(value: bv.BarkValue, line: int) -> bool:
        if isinstance(value, bv.BarkBoolean):
            return value.value
        raise BarkError(line, f"Wag yes or naw no. Not {bv.describe(value)}.")
