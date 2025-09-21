package go.xentany.goworlds.world.adapter.bukkit.mapping;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class BukkitWorldEnvironmentMapper {

  private BukkitWorldEnvironmentMapper() {}

  public static World.Environment toBukkit(final @NotNull WorldEnvironment environment) {
    return switch (Objects.requireNonNull(environment, "environment")) {
      case NETHER -> World.Environment.NETHER;
      case THE_END -> World.Environment.THE_END;
      default -> World.Environment.NORMAL;
    };
  }
}