package go.xentany.goworlds;

import go.xentany.goworlds.command.GoWorldsCommand;
import go.xentany.goworlds.config.YamlConfigService;
import go.xentany.goworlds.locale.Messages;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.BuiltinWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.CompositeWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.PluginWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.generation.chunk.BukkitVoidChunkGenerator;
import go.xentany.goworlds.world.adapter.bukkit.service.BukkitWorldsService;
import go.xentany.goworlds.world.adapter.filesystem.NioWorldsDirectory;
import go.xentany.goworlds.world.adapter.storage.YamlWorldsRepository;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class GoWorldsPlugin extends JavaPlugin {

  private final ChunkGenerator voidChunkGenerator = new BukkitVoidChunkGenerator();

  private Logger logger;
  private YamlWorldsRepository repository;

  @Override
  public void onEnable() {
    logger = getSLF4JLogger();

    try {
      Files.createDirectories(getDataFolder().toPath());

      final var directory = new NioWorldsDirectory(Bukkit.getWorldContainer().toPath());
      final var generatorApplier = new CompositeWorldGenerationApplier(
          List.of(
              new BuiltinWorldGenerationApplier(voidChunkGenerator, logger),
              new PluginWorldGenerationApplier(logger)
          )
      );

      final var file = getDataFolder().toPath().resolve("worlds.yml");
      final var yaml = new YamlConfigService(logger);

      Messages.initialize(this, yaml);

      repository = new YamlWorldsRepository(file, yaml);
      repository.load();

      final var worldsService = new BukkitWorldsService(directory, logger, repository, generatorApplier);
      final var root = getCommand("goworlds");

      if (root != null) {
        final var executor = new GoWorldsCommand(worldsService);

        root.setExecutor(executor);
        root.setTabCompleter(executor);
      } else {
        logger.warn("Command 'goworlds' is not defined in plugin.yml");
      }

      final var records = new ArrayList<>(repository.worlds(true, true));

      int ok = 0;

      for (final var record : records) {
        try {
          if (worldsService.loadWorld(record)) {
            ok++;
          }
        } catch (final Throwable throwable) {
          logger.warn("Autoload failed: {}", record.name(), throwable);
        }
      }

      logger.info("Autoloaded worlds: {}/{}", ok, records.size());
    } catch (final Exception exception) {
      logger.error("Failed to enable plugin: {}", exception.getMessage(), exception);

      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  @Override
  public void onDisable() {
    if (repository != null) {
      try {
        repository.save();
      } catch (final Exception exception) {
        if (logger != null) {
          logger.warn("Failed to save worlds.yml on shutdown", exception);
        }
      }
    }
  }

  @Override
  public @Nullable ChunkGenerator getDefaultWorldGenerator(final @NotNull String ignored,
                                                           final @Nullable String generator) {
    return (generator == null ? "" : generator.trim()).toLowerCase(Locale.ROOT).equals("void")
        ? voidChunkGenerator
        : null;
  }
}