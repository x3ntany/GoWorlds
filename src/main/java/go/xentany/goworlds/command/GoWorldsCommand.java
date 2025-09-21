package go.xentany.goworlds.command;

import go.xentany.goworlds.command.engine.CommandAction;
import go.xentany.goworlds.command.engine.CommandContext;
import go.xentany.goworlds.command.engine.CommandRouter;
import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.domain.WorldGenerator;
import go.xentany.goworlds.world.port.WorldsService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

public final class GoWorldsCommand implements CommandExecutor, TabCompleter {

  private final WorldsService service;
  private final CommandRouter router;

  public GoWorldsCommand(final @NotNull WorldsService service) {
    this.service = service;
    this.router = build();
  }

  private CommandRouter build() {
    final var repository = service.repository();
    final var defaultGenerator = WorldGenerator.NORMAL.name();

    return new CommandRouter("goworlds")
        .register(new CommandAction("list", "list", context -> {
          final var records = new ArrayList<>(repository.worlds());
          final var sender = context.sender();

          sender.sendMessage("Managed worlds: " + records.size());

          for (final var record : records) {
            final var loaded = Bukkit.getWorld(record.name()) != null;

            sender.sendMessage("- " + record.name()
                + " environment=" + record.environment().name()
                + " generator=" + record.generator()
                + " loaded=" + loaded);
          }
        }, context -> List.of(), 0))
        .register(new CommandAction("create", "create (name) (environment) [generator]", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var environment = WorldEnvironment.fromName(context.argument(1));
          final var generator = Optional.ofNullable(context.argument(2))
              .filter(input -> !input.isBlank())
              .orElse(defaultGenerator);
          final var created = service.createWorld(name, environment, generator);

          sender.sendMessage(created.isPresent()
              ? "World created: " + name + " (" + environment.name() + ", " + generator + ")"
              : "Failed to create world: " + name);
        }, this::options, 2))
        .register(new CommandAction("import", "import (name) (environment) [generator]", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var environment = WorldEnvironment.fromName(context.argument(1));
          final var generator = Optional.ofNullable(context.argument(2))
              .filter(input -> !input.isBlank())
              .orElse(defaultGenerator);
          final var imported = service.importWorld(name, environment, generator);

          sender.sendMessage(imported.isPresent()
              ? "World imported: " + name + " (" + environment.name() + ", " + generator + ")"
              : "Failed to import world: " + name);
        }, this::options, 2))
        .register(new CommandAction("load", "load (name)", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var record = repository.optionalWorld(name);

          if (record.isEmpty()) {
            sender.sendMessage("Unknown world: " + name + " (not managed)");

            return;
          }

          final var loaded = service.loadWorld(record.get());

          sender.sendMessage(loaded ? "World loaded: " + name : "Failed to load: " + name);
        }, this::names, 1))
        .register(new CommandAction("unload", "unload (name) [save=true|false]", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var save = context.arity() < 2 || Boolean.parseBoolean(context.argument(1));
          final var unloaded = service.unloadWorld(name, save);

          sender.sendMessage(unloaded ? "World unloaded: " + name : "Failed to unload: " + name);
        }, this::names, 1))
        .register(new CommandAction("delete", "delete (name)", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var deleted = service.deleteWorld(name);

          sender.sendMessage(deleted ? "World deleted: " + name : "Failed to delete: " + name);
        }, this::names, 1))
        .register(new CommandAction("info", "info (name)", context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            sender.sendMessage("World name is required");

            return;
          }

          final var optionalRecord = repository.optionalWorld(name);

          if (optionalRecord.isEmpty()) {
            sender.sendMessage("Unknown world: " + name + " (not managed)");

            return;
          }

          final var record = optionalRecord.get();
          final var world = Bukkit.getWorld(name);
          final var loaded = world != null;

          sender.sendMessage("World: " + record.name());
          sender.sendMessage("  environment: " + record.environment().name());
          sender.sendMessage("  generator: " + record.generator());
          sender.sendMessage("  loaded: " + loaded);
        }, this::names, 1))
        .register(new CommandAction("teleport", "teleport (world) [player]", context -> {
          final var sender = context.sender();
          final var count = context.arity();

          if (count == 0) {
            sender.sendMessage("Usage: /" + context.label() + " teleport (world) [player]");

            return;
          }

          String name;
          Player target;
          final var first = context.argument(0);

          if (count == 1) {
            if (first == null) {
              sender.sendMessage("Usage: /" + context.label() + " teleport (world) [player]");

              return;
            }

            name = first;

            if (!(sender instanceof final Player player)) {
              sender.sendMessage("Only players can use: /" + context.label() + " teleport (world)");

              return;
            }

            target = player;
          } else {
            final var second = context.argument(1);

            if (first == null || second == null) {
              sender.sendMessage("Usage: /" + context.label() + " teleport (world) [player]");

              return;
            }

            final var firstPlayer = Bukkit.getPlayerExact(first);

            if (firstPlayer != null) {
              target = firstPlayer;
              name = second;
            } else {
              name = first;

              final var secondPlayer = Bukkit.getPlayerExact(second);

              if (secondPlayer == null) {
                sender.sendMessage("Player not online: " + second);

                return;
              }

              target = secondPlayer;
            }
          }

          var world = Bukkit.getWorld(name);

          if (world == null) {
            final var record = repository.optionalWorld(name);

            if (record.isPresent() && service.loadWorld(record.get())) {
              world = Bukkit.getWorld(name);
            }
          }

          if (world == null) {
            sender.sendMessage("World is not loaded: " + name);

            return;
          }

          final var teleported = target.teleport(world.getSpawnLocation());

          if (!teleported) {
            sender.sendMessage("Teleport failed.");

            return;
          }

          if (sender == target) {
            sender.sendMessage("Teleported " + target.getName() + " to " + name);
          }
        }, context -> {
          final var count = context.arity();

          if (count == 1) {
            final var names = new LinkedHashSet<String>();

            for (final var record : repository.worlds()) {
              names.add(record.name());
            }

            for (final var word : Bukkit.getWorlds()) {
              names.add(word.getName());
            }

            return new ArrayList<>(names);
          }

          if (count == 2) {
            final var players = new ArrayList<String>();

            for (final var player : Bukkit.getOnlinePlayers()) {
              players.add(player.getName());
            }

            return players;
          }

          return List.of();
        }, 1));
  }

  @Override
  public boolean onCommand(final @NotNull CommandSender sender,
                           final @NotNull Command command,
                           final @NotNull String label,
                           final @NotNull String @NotNull [] arguments) {
    return router.onCommand(sender, command, label, arguments);
  }

  @Override
  public List<String> onTabComplete(final @NotNull CommandSender sender,
                                    final @NotNull Command command,
                                    final @NotNull String alias,
                                    final @NotNull String @NotNull [] arguments) {
    return router.onTabComplete(sender, command, alias, arguments);
  }

  private @NotNull List<String> options(final @NotNull CommandContext context) {
    final var length = context.arity();

    if (length == 1) {
      return List.of();
    }

    if (length == 2) {
      final var environments = new ArrayList<String>();

      for (final var environment : WorldEnvironment.values()) {
        environments.add(environment.name());
      }

      return environments;
    }

    if (length == 3) {
      final var generators = new ArrayList<String>();

      for (final var generator : WorldGenerator.values()) {
        generators.add(generator.name());
      }

      return generators;
    }

    return List.of();
  }

  private @NotNull List<String> names(final @NotNull CommandContext context) {
    if (context.arity() == 1) {
      final var names = new ArrayList<String>();

      for (final var record : service.repository().worlds()) {
        names.add(record.name());
      }

      return names;
    }

    return List.of();
  }
}