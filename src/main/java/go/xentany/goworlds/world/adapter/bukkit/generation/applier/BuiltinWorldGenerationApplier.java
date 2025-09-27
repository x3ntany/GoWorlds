package go.xentany.goworlds.world.adapter.bukkit.generation.applier;

import go.xentany.goworlds.world.domain.WorldGenerator;
import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.port.generation.WorldGenerationApplier;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

public final class BuiltinWorldGenerationApplier implements WorldGenerationApplier {

  private final Logger logger;

  public BuiltinWorldGenerationApplier(final @NotNull Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @Override
  public boolean apply(final @NotNull WorldCreator creator,
                       final @NotNull WorldEnvironment environment,
                       final @NotNull String generator) {
    final var builtin = WorldGenerator.fromName(generator);

    if (builtin == null) {
      return false;
    }

    switch (builtin) {
      case NORMAL -> {
        return true;
      }

      case FLAT -> {
        if (environment == WorldEnvironment.NORMAL) {
          creator.type(WorldType.FLAT);
        } else {
          logger.info("FLAT applies only to NORMAL environment; using default generator.");
        }

        return true;
      }
    }

    return false;
  }
}