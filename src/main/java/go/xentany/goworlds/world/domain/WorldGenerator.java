package go.xentany.goworlds.world.domain;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum WorldGenerator {

  NORMAL,
  FLAT;

  public static @Nullable WorldGenerator fromName(final @Nullable String input) {
    if (input == null) {
      return null;
    }

    try {
      return WorldGenerator.valueOf(input.toUpperCase(Locale.ROOT));
    } catch (final IllegalArgumentException exception) {
      return null;
    }
  }
}