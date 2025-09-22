package go.xentany.goworlds.config;

import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.introspector.BeanAccess;
import org.yaml.snakeyaml.introspector.PropertyUtils;
import org.yaml.snakeyaml.representer.Representer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class YamlKit {

  private static final LoaderOptions LOADER;
  private static final DumperOptions DUMPER;
  private static final PropertyUtils PROPERTIES;

  static {
    LOADER = new LoaderOptions();
    LOADER.setAllowDuplicateKeys(false);

    DUMPER = new DumperOptions();
    DUMPER.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
    DUMPER.setPrettyFlow(true);
    DUMPER.setIndent(2);
    DUMPER.setIndicatorIndent(0);

    PROPERTIES = new PropertyUtils();
    PROPERTIES.setBeanAccess(BeanAccess.FIELD);
  }

  private YamlKit() {}

  public static @NotNull Yaml yaml(final @NotNull Class<?> type) {
    final var constructor = new Constructor(Objects.requireNonNull(type, "type"), LOADER);
    final var representer = new Representer(DUMPER);

    constructor.setPropertyUtils(PROPERTIES);
    representer.setPropertyUtils(PROPERTIES);

    return new Yaml(constructor, representer, DUMPER, LOADER);
  }

  public static @NotNull Map<String, Object> deepStringObjectMap(final @NotNull Object object) {
    final var out = new LinkedHashMap<String, Object>();

    if (Objects.requireNonNull(object, "object") instanceof final Map<?, ?> source) {
      for (final var entry : source.entrySet()) {
        final var key = String.valueOf(entry.getKey());
        final var value = entry.getValue();

        if (value instanceof final Map<?, ?> map) {
          out.put(key, deepStringObjectMap(map));
        } else {
          out.put(key, value);
        }
      }

      return out;
    }

    return out;
  }

  public static void writeWithHeader(final @NotNull Object data,
                                     final @NotNull Path path,
                                     final @NotNull String header) throws IOException {
    Objects.requireNonNull(data, "data");
    Objects.requireNonNull(header, "header");

    final var parent = Objects.requireNonNull(path, "path").getParent();

    if (parent != null) {
      Files.createDirectories(parent);
    }

    final var dumped = yaml(data.getClass()).dumpAsMap(data);
    final var builder = new StringBuilder();

    if (!header.isBlank()) {
      for (final var line : header.split("\n")) {
        builder.append("# ").append(line).append('\n');
      }
    }

    builder.append(dumped);

    final var tmp = parent != null
        ? Files.createTempFile(parent, "yamlkit", ".tmp")
        : Files.createTempFile("yamlkit", ".tmp");

    try (final var writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
      writer.write(builder.toString());
    }

    try {
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (final AtomicMoveNotSupportedException exception) {
      Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}