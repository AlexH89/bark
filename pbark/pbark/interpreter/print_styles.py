from __future__ import annotations

from pbark.interpreter import bark_value as bv
from pbark.print_style import PrintStyle


class PrintStyles:
    @staticmethod
    def format(style: PrintStyle | None, text: str | None) -> str:
        resolved = style if style is not None else PrintStyle.BARK
        safe = text if text is not None else ""
        if resolved in (PrintStyle.BARK,):
            return safe
        if resolved in (PrintStyle.GROWL, PrintStyle.WOOF):
            return safe.upper()
        if resolved is PrintStyle.HOWL:
            return safe if not safe else safe[0] * 4 + safe
        if resolved in (PrintStyle.WHIMPER, PrintStyle.WHINE):
            return safe.lower()
        if resolved is PrintStyle.YAPPING:
            return ((safe + " ") * 2).strip() if safe else ""
        if resolved is PrintStyle.WHINING:
            return " ".join(list(safe))
        return safe

    @staticmethod
    def bare_text(style: PrintStyle | None) -> str:
        resolved = style if style is not None else PrintStyle.BARK
        mapping = {
            PrintStyle.BARK: "woof",
            PrintStyle.WOOF: "woof",
            PrintStyle.GROWL: "growl",
            PrintStyle.HOWL: "howl",
            PrintStyle.WHIMPER: "whimper",
            PrintStyle.WHINE: "whine",
            PrintStyle.WHINING: "whine",
            PrintStyle.YAPPING: "yap",
        }
        return mapping.get(resolved, "woof")
