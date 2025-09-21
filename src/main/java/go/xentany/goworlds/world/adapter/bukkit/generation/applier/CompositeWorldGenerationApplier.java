package go.xentany.goworlds.world.adapter.bukkit.generation.applier;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.port.generation.WorldGenerationApplier;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public final class CompositeWorldGenerationApplier implements WorldGenerationApplier {

  private final List<WorldGenerationApplier> delegates;

  public CompositeWorldGenerationApplier(final @NotNull List<WorldGenerationApplier> delegates) {
    this.delegates = List.copyOf(Objects.requireNonNull(delegates, "delegates"));
  }

  @Override
  public boolean apply(final @NotNull WorldCreator creator,
                       final @NotNull WorldEnvironment environment,
                       final @NotNull String generator) {
    for (final var delegate : delegates) {
      if (delegate.apply(creator, environment, generator)) {
        return true;
      }
    }

    return false;
  }
}