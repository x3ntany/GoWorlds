package go.xentany.goworlds.locale;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum MessageKey {

  ROUTER_USAGE_HEADER(path("router", "usage-header"), "&7Использование:"),
  ROUTER_USAGE_LINE(path("router", "usage-line"), "&e/{command} {usage}"),

  USAGE_PREFIX(path("usage", "prefix"), "&#00ffac▶ &#e0e1e1Используйте &#00ffac/{command} {usage}"),
  USAGE_LIST (path("usage", "list"), "list"),
  USAGE_CREATE(path("usage", "create"), "create (name) (environment) [generator]"),
  USAGE_IMPORT(path("usage", "import"), "import (name) (environment) [generator]"),
  USAGE_LOAD(path("usage", "load"), "load (name)"),
  USAGE_UNLOAD(path("usage", "unload"), "unload (name) [save=true|false]"),
  USAGE_DELETE(path("usage", "delete"), "delete (name)"),
  USAGE_INFO(path("usage", "info"), "info (name)"),
  USAGE_TELEPORT(path("usage", "teleport"), "teleport (world) [player]"),

  COMMON_WORLD_NAME_INVALID(path("common", "world-name-invalid"), "&cНекорректное имя мира: &e{name}&7. Допускаются символы: &f{pattern}"),
  COMMON_WORLD_REQUIRED(path("common", "world-required"), "&cНужно указать название мира."),
  COMMON_WORLD_UNKNOWN(path("common", "world-unknown"), "&cНеизвестный мир: &e{name} &7(не управляется)"),

  LIST_HEADER(path("list", "header"), "&7Управляемых миров: &e{count}"),
  LIST_ENTRY(path("list", "entry"), "&7- &e{name}&7 env=&f{environment}&7 gen=&f{generator}&7 loaded=&f{loaded}"),

  CREATE_SUCCESS(path("create", "success"), "&aМир создан: &e{name}&7 (&f{environment}&7, &f{generator}&7)"),
  CREATE_FAIL(path("create", "fail"), "&cНе удалось создать мир: &e{name}"),

  IMPORT_SUCCESS(path("import", "success"), "&aМир импортирован: &e{name}&7 (&f{environment}&7, &f{generator}&7)"),
  IMPORT_FAIL(path("import", "fail"), "&cНе удалось импортировать мир: &e{name}"),

  LOAD_SUCCESS(path("load", "success"), "&aМир загружен: &e{name}"),
  LOAD_FAIL(path("load", "fail"), "&cНе удалось загрузить: &e{name}"),

  UNLOAD_SUCCESS(path("unload", "success"), "&aМир выгружен: &e{name} &7(сохранение: &f{save}&7)"),
  UNLOAD_FAIL(path("unload", "fail"), "&cНе удалось выгрузить: &e{name}"),

  DELETE_SUCCESS(path("delete", "success"), "&aМир удалён: &e{name}"),
  DELETE_FAIL(path("delete", "fail"), "&cНе удалось удалить: &e{name}"),

  INFO_MESSAGE(path("info", "message"), "&7Мир: &e{name}<br>&7 environment: &f{environment}<br>&7 generator: &f{generator}<br>&7 loaded: &f{loaded}"),

  TELEPORT_PLAYER_NOT_ONLINE(path("teleport", "player-not-online"), "&cИгрок оффлайн: &e{player}"),
  TELEPORT_FAILED(path("teleport", "teleport-failed"), "&cТелепорт не удался."),
  TELEPORT_SUCCESS_SELF(path("teleport", "success-self"), "&aТелепортирован в &e{name}"),
  TELEPORT_SUCCESS_OTHER(path("teleport", "success-other"), "&aТелепортирован &e{player}&a в &e{name}"),
  TELEPORT_WORLD_NOT_LOADED(path("teleport", "world-not-loaded"), "&cМир не загружен: &e{name}"),
  TELEPORT_ONLY_PLAYER(path("teleport", "only-player"), "&cТолько игрок может: &e/{command} teleport (world)");

  private final String[] segments;
  private final String defaultText;

  MessageKey(final @NotNull String[] segments, final @NotNull String defaultText) {
    this.segments = Objects.requireNonNull(segments, "segments");
    this.defaultText = Objects.requireNonNull(defaultText, "defaultText");
  }

  public @NotNull String[] segments() {
    return segments;
  }

  public @NotNull String defaultText() {
    return defaultText;
  }

  private static @NotNull String @NotNull [] path(final @NotNull String @NotNull ... segments) {
    Objects.requireNonNull(segments, "segments is null");

    for (int index = 0; index < segments.length; index++) {
      Objects.requireNonNull(segments[index], "segments[" + index + "] is null");
    }

    return segments.clone();
  }
}