package go.xentany.goworlds.world.adapter.bukkit.generation.applier;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.port.generation.WorldGenerationApplier;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

public final class PluginWorldGenerationApplier implements WorldGenerationApplier {

  @Override
  public boolean apply(final @NotNull WorldCreator creator,
                       final @NotNull WorldEnvironment environment,
                       final @NotNull String generator) {
    creator.generator(generator);

    return true;
  }
}