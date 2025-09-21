package go.xentany.goworlds.world.port.generation;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

public interface WorldGenerationApplier {

  boolean apply(final @NotNull WorldCreator creator,
                final @NotNull WorldEnvironment environment,
                final @NotNull String generator);
}