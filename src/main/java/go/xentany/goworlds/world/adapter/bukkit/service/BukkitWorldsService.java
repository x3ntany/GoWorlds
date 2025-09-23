package go.xentany.goworlds.world.adapter.bukkit.service;

import go.xentany.goworlds.world.adapter.bukkit.mapping.BukkitWorldEnvironmentMapper;
import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.domain.WorldRecord;
import go.xentany.goworlds.world.port.WorldsDirectory;
import go.xentany.goworlds.world.port.WorldsRepository;
import go.xentany.goworlds.world.port.WorldsService;
import go.xentany.goworlds.world.port.generation.WorldGenerationApplier;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.nio.file.Files;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public final class BukkitWorldsService implements WorldsService {

  private final WorldsDirectory worldsDirectory;
  private final Logger logger;
  private final WorldsRepository repository;
  private final WorldGenerationApplier generationApplier;

  public BukkitWorldsService(final @NotNull WorldsDirectory directory,
                             final @NotNull Logger logger,
                             final @NotNull WorldsRepository repository,
                             final @NotNull WorldGenerationApplier generationApplier) {
    this.worldsDirectory = Objects.requireNonNull(directory, "directory");
    this.logger = Objects.requireNonNull(logger, "logger");
    this.repository = Objects.requireNonNull(repository, "repository");
    this.generationApplier = Objects.requireNonNull(generationApplier, "generationApplier");
  }

  @Override
  public @NotNull Optional<World> createWorld(final @NotNull String name,
                                              final @NotNull WorldEnvironment environment,
                                              final @NotNull String generator) {
    final var record = WorldRecord.of(name, environment, generator);

    return createAndRegisterWorld(record);
  }

  @Override
  public @NotNull Optional<World> importWorld(final @NotNull String name,
                                              final @NotNull WorldEnvironment environment,
                                              final @NotNull String generator) {
    final var directory = worldsDirectory.pathOf(name);

    if (!worldsDirectory.looksLikeWorld(directory)) {
      logger.warn("Import failed: {} (no level.dat)", name);

      return Optional.empty();
    }

    final var record = WorldRecord.of(name, environment, generator);

    return createAndRegisterWorld(record);
  }

  @Override
  public boolean loadWorld(final @NotNull WorldRecord record) {
    if (Bukkit.getWorld(record.name()) != null) {
      return true;
    }

    final var directory = worldsDirectory.pathOf(record.name());

    if (!Files.isDirectory(directory)) {
      logger.warn("Load failed: folder not found {}", directory);

      return false;
    }

    return createAndRegisterWorld(record).isPresent();
  }

  @Override
  public boolean unloadWorld(final @NotNull String name, final boolean save) {
    final var world = Bukkit.getWorld(name);

    if (world == null) {
      return true;
    }

    final var fallback = Bukkit.getWorlds().stream()
        .filter(otherWorld -> !otherWorld.equals(world))
        .findFirst()
        .orElse(null);

    if (fallback == null) {
      logger.warn("There is no other world for players to teleport to. Cancel upload {}", name);

      return false;
    }

    final var target = fallback.getSpawnLocation();

    for (final var player : List.copyOf(world.getPlayers())) {
      if (player.isInsideVehicle()) {
        player.leaveVehicle();
      }

      player.teleportAsync(target, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }

    final var ok = Bukkit.unloadWorld(world, save);

    if (!ok) {
      logger.warn("Unload failed: {}", name);
    }

    return ok;
  }

  @Override
  public boolean deleteWorld(final @NotNull String name) {
    if (Bukkit.getWorld(name) != null) {
      logger.warn("Delete refused: world is loaded ({})", name);
      return false;
    }

    if (!repository.isManaged(name)) {
      logger.warn("Delete refused: world is not managed by GoWorlds (safe delete): {}", name);
      return false;
    }

    final var directory = worldsDirectory.pathOf(name);

    if (!worldsDirectory.deleteRecursively(directory)) {
      logger.warn("Delete failed (file system error): {}", directory);
      return false;
    }

    if (repository.remove(name)) {
      repository.save();
    }

    return true;
  }

  @Override
  public @NotNull WorldsRepository repository() {
    return repository;
  }

  private Optional<World> createAndRegisterWorld(final @NotNull WorldRecord record) {
    final var existing = Bukkit.getWorld(record.name());

    if (existing != null) {
      return Optional.of(existing);
    }

    final var creator = new WorldCreator(record.name()).environment(BukkitWorldEnvironmentMapper.toBukkit(record.environment()));

    generationApplier.apply(creator, record.environment(), record.generator());

    final var world = Bukkit.createWorld(creator);

    if (world == null) {
      return Optional.empty();
    }

    repository.upsert(record);
    repository.save();

    return Optional.of(world);
  }
}