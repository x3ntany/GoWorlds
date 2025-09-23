package go.xentany.goworlds.world.adapter.storage;

import go.xentany.goworlds.config.YamlConfigService;
import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.domain.WorldRecord;
import go.xentany.goworlds.world.port.WorldsRepository;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

import java.nio.file.Path;
import java.util.*;

public final class YamlWorldsRepository implements WorldsRepository {

  private final Map<String, WorldRecord> byName = new LinkedHashMap<>();
  private final Path yamlFile;
  private final YamlConfigService yamlService;

  public YamlWorldsRepository(final @NotNull Path yamlFile,
                              final @NotNull YamlConfigService yamlService) {
    this.yamlFile = Objects.requireNonNull(yamlFile, "yamlFile");
    this.yamlService = Objects.requireNonNull(yamlService, "yamlService");
  }

  @Override
  public void load() {
    byName.clear();

    final var schema = yamlService.load(yamlFile, Schema.class, Schema::new);
    final var entries = schema.entries;

    for (final var entry : entries) {
      if (entry == null) {
        continue;
      }

      final var name = entry.name;

      if (name == null || name.isBlank()) {
        continue;
      }

      final var environment = entry.environment;
      final var generator = entry.generator;
      final var record = WorldRecord.of(name, WorldEnvironment.fromName(environment), generator);

      byName.put(key(name), record);
    }
  }

  @Override
  public void save() {
    final var schema = new Schema();
    final var entries = schema.entries;

    for (final var record : byName.values()) {
      final var entry = new Schema.Entry();

      entry.name = record.name();
      entry.environment = record.environment().name();
      entry.generator = record.generator();
      entries.add(entry);
    }

    yamlService.save(yamlFile, schema);
  }

  @Override
  public @NotNull @UnmodifiableView Collection<WorldRecord> worlds(final boolean requireLoaded,
                                                                   final boolean requireUnloaded) {
    if (requireLoaded == requireUnloaded) {
      return Collections.unmodifiableCollection(byName.values());
    }

    return byName.values().stream()
        .filter(record -> (Bukkit.getWorld(record.name()) != null) == requireLoaded)
        .toList();
  }

  @Override
  public @NotNull Optional<WorldRecord> optionalWorld(final @NotNull String name) {
    return Optional.ofNullable(byName.get(key(name)));
  }

  @Override
  public void upsert(final @NotNull WorldRecord record) {
    byName.put(key(record.name()), record);
  }

  @Override
  public boolean remove(final @NotNull String name) {
    return byName.remove(key(name)) != null;
  }

  @Override
  public boolean isManaged(final @NotNull String name) {
    return byName.containsKey(key(name));
  }

  private @NotNull String key(final @NotNull String name) {
    return Objects.requireNonNull(name, "name").toLowerCase(Locale.ROOT);
  }

  public static final class Schema {

    public final List<Entry> entries = new ArrayList<>();

    public static final class Entry {

      public String name;
      public String environment;
      public String generator;
    }
  }
}