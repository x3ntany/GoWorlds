package go.xentany.goworlds.locale;

import go.xentany.goworlds.config.YamlConfigService;
import go.xentany.goworlds.config.YamlKit;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Files;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class Messages {

  private static final String FILE_NAME = "messages.yml";
  private static final String HEADER = "Auto-generated. Edit texts; new keys are added automatically.";
  private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder()
      .character('&')
      .hexColors()
      .build();

  private static Map<String, Object> root = new LinkedHashMap<>();

  private Messages() {}

  public static void initialize(final @NotNull JavaPlugin plugin,
                                final @NotNull YamlConfigService service) {
    Objects.requireNonNull(plugin, "plugin");

    try {
      final var file = plugin.getDataFolder().toPath().resolve(FILE_NAME);

      root = YamlKit.deepStringObjectMap(service.load(file, LinkedHashMap.class, LinkedHashMap::new));

      var changed = false;

      for (final var key : MessageKey.values()) {
        if (get(root, key.segments()) == null) {
          set(root, key.defaultText(), key.segments());
          changed = true;
        }
      }

      if (changed || !Files.exists(file)) {
        YamlKit.writeWithHeader(root, file, HEADER);
      }
    } catch (final Exception exception) {
      plugin.getSLF4JLogger().warn("Failed to initialize messages: {}", exception.getMessage(), exception);
    }
  }

  public static @NotNull String get(final @NotNull MessageKey key) {
    return get(key, Collections.emptyMap());
  }

  public static @NotNull String get(final @NotNull MessageKey key, final @NotNull Map<String, Object> vars) {
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(vars, "vars");

    return apply(stringAt(key.segments(), key.defaultText()), vars);
  }

  public static @NotNull Component format(final @NotNull MessageKey key) {
    return format(get(key));
  }

  public static @NotNull Component format(final @NotNull String message) {
    return SERIALIZER.deserialize(apply(message, Collections.emptyMap()));
  }

  public static @NotNull Component format(final @NotNull MessageKey key, final @NotNull Map<String, Object> vars) {
    return format(get(key), vars);
  }

  public static @NotNull Component format(final @NotNull String message, final @NotNull Map<String, Object> vars) {
    Objects.requireNonNull(message, "key");
    Objects.requireNonNull(vars, "vars");

    return SERIALIZER.deserialize(apply(message, vars));
  }

  public static void send(final @NotNull CommandSender to,
                          final @NotNull MessageKey key) {
    send(to, key, Collections.emptyMap());
  }

  public static void send(final @NotNull CommandSender to,
                          final @NotNull MessageKey key,
                          final @NotNull Map<String, Object> vars) {
    Objects.requireNonNull(to, "to");
    Objects.requireNonNull(key, "key");
    Objects.requireNonNull(vars, "vars");

    to.sendMessage(format(key, vars));
  }

  public static @NotNull Map<String, Object> vars(final @NotNull Object @NotNull ... keyValues) {
    Objects.requireNonNull(keyValues, "keyValues is null");

    if ((keyValues.length & 1) != 0) {
      throw new IllegalArgumentException("vars requires even number of arguments: key, value, ...");
    }

    final var placeholders = new LinkedHashMap<String, Object>(keyValues.length / 2);

    for (int index = 0; index < keyValues.length; index += 2) {
      final var key = Objects.requireNonNull(keyValues[index], "placeholder key at index " + index + " is null");

      placeholders.put(String.valueOf(key), keyValues[index + 1]);
    }

    return placeholders;
  }

  private static @Nullable Object get(final @NotNull Map<String, Object> tree,
                                      final @NotNull String @NotNull ... segments) {
    Objects.requireNonNull(tree, "tree is null");
    Objects.requireNonNull(segments, "segments is null");
    Object cursor = tree;

    for (int index = 0; index < segments.length; index++) {
      final var segment = Objects.requireNonNull(segments[index], "segments[" + index + "] is null");

      if (cursor instanceof final Map<?, ?> map) {
        final var next = map.get(segment);

        if (next == null) {
          return null;
        }

        cursor = next;
      } else {
        return null;
      }
    }

    return cursor;
  }

  @SuppressWarnings("unchecked")
  private static void set(final @NotNull Map<String, Object> tree,
                          final @Nullable Object value,
                          final @NotNull String @NotNull ... segments) {
    Objects.requireNonNull(tree, "tree is null");
    Objects.requireNonNull(segments, "segments is null");

    if (segments.length == 0) {
      throw new IllegalArgumentException("segments is empty");
    }

    var cursor = tree;

    for (int index = 0; index < segments.length - 1; index++) {
      final var segment = Objects.requireNonNull(segments[index], "segments[" + index + "] is null");
      final var next = cursor.get(segment);

      if (next instanceof Map<?, ?>) {
        cursor = (Map<String, Object>) next;
      } else {
        final var created = new LinkedHashMap<String, Object>();

        cursor.put(segment, created);
        cursor = created;
      }
    }

    final var last = Objects.requireNonNull(segments[segments.length - 1], "segments[last] is null");

    cursor.put(last, value);
  }

  private static String stringAt(final @NotNull String @NotNull [] segments, final @NotNull String defaultText) {
    Objects.requireNonNull(segments, "segments is null");
    Objects.requireNonNull(defaultText, "defaultText");

    final var object = get(root, segments);

    return object == null ? defaultText : String.valueOf(object);
  }

  private static String apply(final @NotNull String input, final @NotNull Map<String, Object> vars) {
    Objects.requireNonNull(input, "input is null");
    Objects.requireNonNull(vars, "vars is null");


    String result;

    if (!vars.isEmpty() && input.indexOf('{') >= 0) {
      final var out = new StringBuilder(input.length() + Math.min(64, 16 * vars.size()));
      int index = 0, length = input.length();

      while (index < length) {
        final var currentChar = input.charAt(index);

        if (currentChar == '{') {
          if (index + 1 < length && input.charAt(index + 1) == '{') {
            out.append('{');
            index += 2;

            continue;
          }

          final int close = input.indexOf('}', index + 1);

          if (close < 0) {
            out.append(input, index, length);

            break;
          }

          final var key = input.substring(index + 1, close);
          final var value = vars.get(key);

          if (value != null) {
            out.append(value);
          } else {
            out.append(input, index, close + 1);
          }

          index = close + 1;

          continue;
        }

        if (currentChar == '}' && index + 1 < length && input.charAt(index + 1) == '}') {
          out.append('}');
          index += 2;

          continue;
        }

        out.append(currentChar);
        index++;
      }

      result = out.toString();
    } else {
      result = input;
    }

    return result.replace("<br>", "\n");
  }
}