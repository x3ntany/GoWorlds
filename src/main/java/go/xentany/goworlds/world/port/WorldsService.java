package go.xentany.goworlds.world.port;

import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.domain.WorldRecord;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface WorldsService {

  @NotNull Optional<World> createWorld(final @NotNull String name,
                                       final @NotNull WorldEnvironment environment,
                                       final @NotNull String generator);

  @NotNull Optional<World> importWorld(final @NotNull String name,
                                       final @NotNull WorldEnvironment environment,
                                       final @NotNull String generator);

  boolean loadWorld(final @NotNull WorldRecord record);

  boolean unloadWorld(final @NotNull String name, final boolean save);

  boolean deleteWorld(final @NotNull String name);

  @NotNull WorldsRepository repository();
}