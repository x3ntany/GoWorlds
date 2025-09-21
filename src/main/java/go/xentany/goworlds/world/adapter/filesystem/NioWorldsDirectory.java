package go.xentany.goworlds.world.adapter.filesystem;

import go.xentany.goworlds.world.port.WorldsDirectory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

public final class NioWorldsDirectory implements WorldsDirectory {

  private final Path container;

  public NioWorldsDirectory(final @NotNull Path container) {
    this.container = Objects.requireNonNull(container, "container");
  }

  @Override
  public @NotNull Path pathOf(final @NotNull String name) {
    return container.resolve(Objects.requireNonNull(name, "name"));
  }

  @Override
  public boolean looksLikeWorld(final @NotNull Path worldDirectory) {
    final var directory = Objects.requireNonNull(worldDirectory, "worldDirectory");
    final var levelDat = directory.resolve("level.dat");

    return Files.isDirectory(directory) && Files.exists(levelDat);
  }

  @Override
  public boolean deleteRecursively(final @NotNull Path rootDirectory) {
    final var root = Objects.requireNonNull(rootDirectory, "rootDirectory");

    if (Files.exists(root)) {
      final var fileTreeVisitor = new SimpleFileVisitor<Path>() {

        @Override
        public @NotNull FileVisitResult visitFile(final @NotNull Path file,
                                                  final @NotNull BasicFileAttributes attributes) throws IOException {
          Files.deleteIfExists(file);

          return FileVisitResult.CONTINUE;
        }

        @Override
        public @NotNull FileVisitResult postVisitDirectory(final @NotNull Path directory,
                                                           final @Nullable IOException exception) throws IOException {
          Files.deleteIfExists(directory);

          return FileVisitResult.CONTINUE;
        }
      };

      try {
        Files.walkFileTree(root, fileTreeVisitor);

        return true;
      } catch (final IOException exception) {
        return false;
      }
    }

    return true;
  }
}