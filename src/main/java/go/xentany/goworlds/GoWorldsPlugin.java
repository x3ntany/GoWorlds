package go.xentany.goworlds;

import go.xentany.goworlds.command.GoWorldsCommand;
import go.xentany.goworlds.config.YamlConfigService;
import go.xentany.goworlds.locale.Messages;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.BuiltinWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.CompositeWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.generation.applier.PluginWorldGenerationApplier;
import go.xentany.goworlds.world.adapter.bukkit.service.BukkitWorldsService;
import go.xentany.goworlds.world.adapter.filesystem.NioWorldsDirectory;
import go.xentany.goworlds.world.adapter.storage.YamlWorldsRepository;
import go.xentany.goworlds.world.port.WorldsRepository;
import go.xentany.goworlds.world.port.WorldsService;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class GoWorldsPlugin extends JavaPlugin implements Listener {

  private final CompletableFuture<Void> serverReady = new CompletableFuture<>();
  private BukkitTask readinessPoller;
  private Logger logger;
  private WorldsRepository repository;
  private WorldsService service;

  @Override
  public void onEnable() {
    logger = getSLF4JLogger();

    try {
      Files.createDirectories(getDataFolder().toPath());

      final var directory = new NioWorldsDirectory(Bukkit.getWorldContainer().toPath());
      final var generatorApplier = new CompositeWorldGenerationApplier(
          List.of(
              new BuiltinWorldGenerationApplier(logger),
              new PluginWorldGenerationApplier()
          )
      );

      final var file = getDataFolder().toPath().resolve("worlds.yml");
      final var yaml = new YamlConfigService(logger);

      Messages.initialize(this, yaml);

      repository = new YamlWorldsRepository(file, yaml);
      repository.load();

      service = new BukkitWorldsService(directory, logger, repository, generatorApplier);

      final var root = getCommand("goworlds");

      if (root != null) {
        final var executor = new GoWorldsCommand(service);

        root.setExecutor(executor);
        root.setTabCompleter(executor);
      } else {
        logger.warn("Command 'goworlds' is not defined in plugin.yml");
      }

      serverReady.thenRun(() -> {
        var records = new ArrayList<>(repository.worlds(true, true));
        int ok = 0;

        for (final var record : records) {
          try {
            if (service.loadWorld(record)) {
              ok++;
            }
          } catch (final Throwable t) {
            logger.warn("Autoload failed: {}", record.name(), t);
          }
        }

        logger.info("Autoloaded worlds: {}/{}", ok, records.size());
      });

      if (!tryCompleteWorldLoadReadiness()) {
        readinessPoller = Bukkit.getScheduler().runTaskTimer(this, () -> {
          if (tryCompleteWorldLoadReadiness()) {
            if (readinessPoller != null) {
              readinessPoller.cancel();
            }
          }
        }, 1L, 1L);
      }
    } catch (final Exception exception) {
      logger.error("Failed to enable plugin: {}", exception.getMessage(), exception);

      Bukkit.getPluginManager().disablePlugin(this);
    }
  }

  private boolean tryCompleteWorldLoadReadiness() {
    if (!isEnabled()) {
      return false;
    }

    if (Bukkit.isStopping()) {
      return false;
    }

    if (repository == null || service == null) {
      return false;
    }

    if (Bukkit.getWorlds().isEmpty()) {
      return false;
    }

    if (!serverReady.isDone()) {
      serverReady.complete(null);
    }

    return true;
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
}