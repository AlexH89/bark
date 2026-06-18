package dev.klomptech.jbark.interpreter;

import dev.klomptech.jbark.print.PrintStyle;

// Formats output differently per print verb
public final class PrintStyles {

  private PrintStyles() {}

  public static String format(final PrintStyle style, final String text) {
    PrintStyle resolvedStyle = style != null ? style : PrintStyle.BARK;
    String safeText = text != null ? text : "";
    return switch (resolvedStyle) {
      case BARK -> safeText;
      case GROWL, WOOF -> safeText.toUpperCase();
      case HOWL -> safeText.isEmpty() ? safeText : (safeText.charAt(0) + "").repeat(4) + safeText;
      case WHIMPER, WHINE -> safeText.toLowerCase();
      case YAPPING -> (safeText + " ").repeat(2).trim();
      case WHINING -> String.join(" ", safeText.split(""));
    };
  }

  // Default word when a print verb has no value. Formatted by format().
  public static String bareText(final PrintStyle style) {
    PrintStyle resolvedStyle = style != null ? style : PrintStyle.BARK;
    return switch (resolvedStyle) {
      case BARK, WOOF -> "woof";
      case GROWL -> "growl";
      case HOWL -> "howl";
      case WHIMPER -> "whimper";
      case WHINE, WHINING -> "whine";
      case YAPPING -> "yap";
    };
  }
}
