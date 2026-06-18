package dev.klomptech.jbark.parser.printing;

import dev.klomptech.jbark.print.PrintStyle;

public final class PrintVerb {
  private PrintStyle style;
  private int offset;

  public PrintVerb(final PrintStyle style, final int offset) {
    this.style = style;
    this.offset = offset;
  }

  public PrintStyle getStyle() {
    return style;
  }

  public int getOffset() {
    return offset;
  }

  public void setStyle(final PrintStyle style) {
    this.style = style;
  }

  public void setOffset(final int offset) {
    this.offset = offset;
  }
}
