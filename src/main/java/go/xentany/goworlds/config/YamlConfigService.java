package go.xentany.goworlds.config;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Objects;
import java.util.function.Supplier;

public final class YamlConfigService {

  private final Logger logger;
  private final LoaderOptions loaderOptions;
  private final DumperOptions dumperOptions;
  private final PropertyUtils propertyUtils;

  public YamlConfigService(final @NotNull Logger logger) {
    this.logger = Objects.requireNonNull(logger, "logger");

    loaderOptions = createLoaderOptions();
    dumperOptions = createDumperOptions();
    propertyUtils = createPropertyUtils();
  }

  public <T> T load(final @NotNull Path file,
                    final @NotNull Class<T> rootType,
                    final @NotNull Supplier<T> defaults) {
    Objects.requireNonNull(file, "file");
    Objects.requireNonNull(rootType, "rootType");
    Objects.requireNonNull(defaults, "defaults");

    try {
      if (!Files.exists(file)) {
        final var def = defaults.get();

        save(file, def);

        return def;
      }

      final var yaml = createYamlFor(rootType, loaderOptions);

      try (final var reader = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {
        final var object = yaml.loadAs(reader, rootType);

        return object == null ? defaults.get() : object;
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
      ensureParentDirectory(file);

      final var parent = file.getParent();
      final var base = file.getFileName().toString();
      final var tmp = Files.createTempFile(parent, base, ".tmp");
      final var yaml = createYamlFor(object.getClass(), loaderOptions);
      final var dump = yaml.dumpAsMap(object);

      try (final var writer = new OutputStreamWriter(
          Files.newOutputStream(tmp, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8)) {
        writer.write(dump);
      }

      Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (final IOException exception) {
      logger.error("Failed to save yaml: {}", file, exception);
    }
  }

  private @NotNull Yaml createYamlFor(final @NotNull Class<?> rootType,
                                      final @NotNull LoaderOptions options) {
    final var constructor = new Constructor(rootType, options);
    final var representer = new Representer(dumperOptions);

    constructor.setPropertyUtils(propertyUtils);
    representer.setPropertyUtils(propertyUtils);

    return new Yaml(constructor, representer, dumperOptions, options);
  }

  private @NotNull LoaderOptions createLoaderOptions() {
    final var options = new LoaderOptions();

    options.setAllowDuplicateKeys(false);

    return options;
  }

  private @NotNull DumperOptions createDumperOptions() {
    final var options = new DumperOptions();

    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    options.setIndent(2);
    options.setPrettyFlow(true);

    return options;
  }

  private @NotNull PropertyUtils createPropertyUtils() {
    final var properties = new PropertyUtils();

    properties.setBeanAccess(BeanAccess.FIELD);

    return properties;
  }

  private void ensureParentDirectory(final @NotNull Path file) throws IOException {
    final var parent = file.getParent();

    if (parent != null) {
      Files.createDirectories(parent);
    }
  }
}