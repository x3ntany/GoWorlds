package go.xentany.goworlds.world.domain;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class WorldRecord {

  private final String name;
  private final WorldEnvironment environment;
  private final String generator;

  private WorldRecord(final @NotNull String name,
                      final @NotNull WorldEnvironment environment,
                      final @NotNull String generator) {
    this.name = Objects.requireNonNull(name, "name");
    this.environment = Objects.requireNonNull(environment, "environment");
    this.generator = Objects.requireNonNull(generator, "generator").trim();
  }

  public static @NotNull WorldRecord of(final @NotNull String name,
                                        final @NotNull WorldEnvironment environment,
                                        final @NotNull String generator) {
    return new WorldRecord(name, environment, generator);
  }

  public @NotNull String name() {
    return name;
  }

  public @NotNull WorldEnvironment environment() {
    return environment;
  }

  public @NotNull String generator() {
    return generator;
  }
}