package go.xentany.goworlds.world.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum WorldGenerator {

  NORMAL,
  FLAT,
  VOID;

  public static @NotNull WorldGenerator fromName(final @Nullable String input) {
    return input == null ? NORMAL : switch (input.toUpperCase(Locale.ROOT)) {
      case "FLAT" -> FLAT;
      case "VOID" -> VOID;
      default -> NORMAL;
    };
  }
}