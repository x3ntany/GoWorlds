package go.xentany.goworlds.world.adapter.bukkit.generation.applier;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.port.generation.WorldGenerationApplier;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.Objects;

public final class PluginWorldGenerationApplier implements WorldGenerationApplier {

  private final Logger logger;

  public PluginWorldGenerationApplier(final @NotNull Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  @Override
  public boolean apply(final @NotNull WorldCreator creator,
                       final @NotNull WorldEnvironment environment,
                       final @NotNull String generator) {
    final int colonIndex = generator.indexOf(':');

    if (colonIndex > 0) {
      final var pluginName = generator.substring(0, colonIndex);

      if (!Bukkit.getPluginManager().isPluginEnabled(pluginName)) {
        logger.warn("Generator plugin not found or disabled: {} (id: {})", pluginName, generator);
      }
    }

    creator.generator(generator);

    return true;
  }
}