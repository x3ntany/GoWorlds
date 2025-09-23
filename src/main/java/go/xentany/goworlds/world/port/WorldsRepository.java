package go.xentany.goworlds.world.port;

import go.xentany.goworlds.world.domain.WorldRecord;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public interface WorldsRepository {

  void load();

  void save();

  @NotNull Collection<WorldRecord> worlds(final boolean requireLoaded, final boolean requireUnloaded);

  @NotNull Optional<WorldRecord> optionalWorld(final @NotNull String name);

  void upsert(final @NotNull WorldRecord record);

  boolean remove(final @NotNull String name);

  boolean isManaged(final @NotNull String name);
}