from __future__ import annotations

from pbark.print_style import PrintStyle


class PrintVerb:
    def __init__(self, style: PrintStyle | None = None, offset: int = -1) -> None:
        self.style = style
        self.offset = offset
