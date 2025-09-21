package go.xentany.goworlds.world.domain;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public enum WorldEnvironment {

  NORMAL,
  NETHER,
  THE_END;

  public static @NotNull WorldEnvironment fromName(final @Nullable String input) {
    return input == null ? NORMAL : switch (input.toUpperCase(Locale.ROOT)) {
      case "NETHER" -> NETHER;
      case "END", "THE_END" -> THE_END;
      default -> NORMAL;
    };
  }
}