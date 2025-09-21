package go.xentany.goworlds.world.port;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public interface WorldsDirectory {

  @NotNull Path pathOf(final @NotNull String name);

  boolean looksLikeWorld(final @NotNull Path folder);

  boolean deleteRecursively(final @NotNull Path root);
}