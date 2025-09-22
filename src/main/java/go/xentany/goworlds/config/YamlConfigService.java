package go.xentany.goworlds.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Supplier;

public final class YamlConfigService {

  private final Logger logger;

  public YamlConfigService(final @NotNull Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");
  }

  public <T> T load(final @NotNull Path file,
                    final @NotNull Class<T> type,
                    final @NotNull Supplier<T> defaults) {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(defaults, "defaults");

    try {
      if (!Files.exists(file)) {
        final var def = defaults.get();

        save(file, def);

        return def;
      }

      final var yaml = YamlKit.yaml(type);

      try (final var in = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
        final var object = yaml.loadAs(in, type);

        return object != null ? object : defaults.get();
      }
    } catch (final Exception exception) {
      logger.error("Failed to load yaml: {}", file, exception);

      return defaults.get();
    }
  }

  public void save(final @NotNull Path file, final @NotNull Object object) {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(object, "object");

    try {
      final var parent = file.getParent();

      if (parent != null) {
        Files.createDirectories(parent);
      }

      final var tmp = parent != null
          ? Files.createTempFile(parent, "yaml", ".tmp")
          : Files.createTempFile("ytaml", ".tmp");
      final var yaml = YamlKit.yaml(object.getClass());
      final var dump = yaml.dumpAsMap(object);

      try (final var out = new OutputStreamWriter(
          Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8)) {
        out.write(dump);
      }

      try {
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
      } catch (AtomicMoveNotSupportedException ex) {
        Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (final Exception exception) {
      logger.error("Failed to save yaml: {}", file, exception);
    }
  }
}