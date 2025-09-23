package go.xentany.goworlds.command;

import go.xentany.goworlds.command.engine.CommandAction;
import go.xentany.goworlds.command.engine.CommandContext;
import go.xentany.goworlds.command.engine.CommandRouter;
import go.xentany.goworlds.locale.MessageKey;
import go.xentany.goworlds.locale.Messages;
import go.xentany.goworlds.world.domain.WorldEnvironment;
import go.xentany.goworlds.world.domain.WorldGenerator;
import go.xentany.goworlds.world.domain.WorldRecord;
import go.xentany.goworlds.world.port.WorldsService;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

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

    return new CommandRouter()
        .register(new CommandAction("list", Messages.get(MessageKey.USAGE_LIST), context -> {
          final var records = new ArrayList<>(repository.worlds(true, true));
          final var sender = context.sender();

          Messages.send(sender, MessageKey.LIST_HEADER, Messages.vars("count", records.size()));

          for (final var record : records) {
            final var loaded = Bukkit.getWorld(record.name()) != null;

            Messages.send(sender, MessageKey.LIST_ENTRY, Messages.vars(
                "name", record.name(),
                "environment", record.environment().name(),
                "generator", record.generator(),
                "loaded", loaded
            ));
          }
        }, context -> List.of(), 0))
        .register(new CommandAction("create", Messages.get(MessageKey.USAGE_CREATE), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var environment = WorldEnvironment.fromName(context.argument(1));
          final var generator = Optional.ofNullable(context.argument(2))
              .filter(input -> !input.isBlank())
              .orElse(defaultGenerator);
          final var created = service.createWorld(name, environment, generator);

          if (created.isPresent()) {
            Messages.send(sender, MessageKey.CREATE_SUCCESS, Messages.vars(
                "name", name,
                "environment", environment.name(),
                "generator", generator
            ));
          } else {
            Messages.send(sender, MessageKey.CREATE_FAIL, Messages.vars("name", name));
          }
        }, this::options, 2))
        .register(new CommandAction("import", Messages.get(MessageKey.USAGE_IMPORT), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var environment = WorldEnvironment.fromName(context.argument(1));
          final var generator = Optional.ofNullable(context.argument(2))
              .filter(input -> !input.isBlank())
              .orElse(defaultGenerator);
          final var imported = service.importWorld(name, environment, generator);

          if (imported.isPresent()) {
            Messages.send(sender, MessageKey.IMPORT_SUCCESS, Messages.vars(
                "name", name,
                "environment", environment.name(),
                "generator", generator
            ));
          } else {
            Messages.send(sender, MessageKey.IMPORT_FAIL, Messages.vars("name", name));
          }
        }, this::options, 2))
        .register(new CommandAction("load", Messages.get(MessageKey.USAGE_LOAD), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var record = repository.optionalWorld(name);

          if (record.isEmpty()) {
            Messages.send(sender, MessageKey.COMMON_WORLD_UNKNOWN, Messages.vars("name", name));

            return;
          }

          final var loaded = service.loadWorld(record.get());

          if (loaded) {
            Messages.send(sender, MessageKey.LOAD_SUCCESS, Messages.vars("name", name));
          } else {
            Messages.send(sender, MessageKey.LOAD_FAIL, Messages.vars("name", name));
          }
        }, context -> worlds(context, false, true), 1))
        .register(new CommandAction("unload", Messages.get(MessageKey.USAGE_UNLOAD), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var save = context.arity() < 2 || Boolean.parseBoolean(context.argument(1));
          final var unloaded = service.unloadWorld(name, save);

          if (unloaded) {
            Messages.send(sender, MessageKey.UNLOAD_SUCCESS, Messages.vars("name", name, "save", save));
          } else {
            Messages.send(sender, MessageKey.UNLOAD_FAIL, Messages.vars("name", name));
          }
        }, context -> worlds(context, true, false), 1))
        .register(new CommandAction("delete", Messages.get(MessageKey.USAGE_DELETE), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var deleted = service.deleteWorld(name);

          if (deleted) {
            Messages.send(sender, MessageKey.DELETE_SUCCESS, Messages.vars("name", name));
          } else {
            Messages.send(sender, MessageKey.DELETE_FAIL, Messages.vars("name", name));
          }
        }, context -> worlds(context, true, true), 1))
        .register(new CommandAction("info", Messages.get(MessageKey.USAGE_INFO), context -> {
          final var sender = context.sender();
          final var name = context.argument(0);

          if (name == null) {
            Messages.send(sender, MessageKey.COMMON_WORLD_REQUIRED);

            return;
          }

          final var optionalRecord = repository.optionalWorld(name);

          if (optionalRecord.isEmpty()) {
            Messages.send(sender, MessageKey.COMMON_WORLD_UNKNOWN, Messages.vars("name", name));

            return;
          }

          final var record = optionalRecord.get();
          final var world = Bukkit.getWorld(name);
          final var loaded = world != null;

          Messages.send(sender, MessageKey.INFO_MESSAGE, Messages.vars(
              "name", record.name(),
              "environment", record.environment().name(),
              "generator", record.generator(),
              "loaded", loaded
          ));
        }, context -> worlds(context, true, true), 1))
        .register(new CommandAction("teleport", Messages.get(MessageKey.USAGE_TELEPORT), context -> {
          final var sender = context.sender();
          final var count = context.arity();

          if (count == 0) {
            Messages.send(sender, MessageKey.TELEPORT_USAGE, Messages.vars("command", context.label()));

            return;
          }

          String name;
          Player target;
          final var first = context.argument(0);

          if (count == 1) {
            if (first == null) {
              Messages.send(sender, MessageKey.TELEPORT_USAGE, Messages.vars("command", context.label()));

              return;
            }

            name = first;

            if (!(sender instanceof final Player player)) {
              Messages.send(sender, MessageKey.TELEPORT_ONLY_PLAYER, Messages.vars("command", context.label()));

              return;
            }

            target = player;
          } else {
            final var second = context.argument(1);

            if (first == null || second == null) {
              Messages.send(sender, MessageKey.TELEPORT_USAGE, Messages.vars("command", context.label()));

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
                Messages.send(sender, MessageKey.TELEPORT_PLAYER_NOT_ONLINE, Messages.vars("player", second));

                return;
              }

              target = secondPlayer;
            }
          }

          var world = Bukkit.getWorld(name);

          if (world == null) {
            final var record = repository.optionalWorld(name);

            if (record.isPresent()) {
              world = Bukkit.getWorld(name);
            }
          }

          if (world == null) {
            Messages.send(sender, MessageKey.TELEPORT_WORLD_NOT_LOADED, Messages.vars("name", name));

            return;
          }

          final var teleported = target.teleport(world.getSpawnLocation());

          if (!teleported) {
            Messages.send(sender, MessageKey.TELEPORT_FAILED);

            return;
          }

          if (sender == target) {
            Messages.send(sender, MessageKey.TELEPORT_SUCCESS_SELF, Messages.vars("name", name));
          } else {
            Messages.send(sender, MessageKey.TELEPORT_SUCCESS_OTHER, Messages.vars(
                "player", target.getName(),
                "name", name
            ));
          }
        }, context -> {
          final var count = context.arity();

          if (count == 1) {
            final var names = new LinkedHashSet<>(worlds(context, true, false));

            for (final var world : Bukkit.getWorlds()) {
              names.add(world.getName());
            }

            return completions(context.argument(0), names, name -> name);
          }

          if (count == 2) {
            final var input = context.argument(1);

            return input == null || input.isEmpty()
                ? List.of("{player}")
                : completions(input, Bukkit.getOnlinePlayers(), Player::getName);
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
      final var input = context.argument(0);

      return input == null || input.isEmpty() ? List.of("{name}") : List.of();
    }

    if (length == 2) {
      return completions(context.argument(1), Arrays.asList(WorldEnvironment.values()), Enum::name);
    }

    if (length == 3) {
      return completions(context.argument(2), Arrays.asList(WorldGenerator.values()), Enum::name);
    }

    return List.of();
  }

  private @NotNull List<String> worlds(final @NotNull CommandContext context,
                                       final boolean requireLoaded,
                                       final boolean requireUnloaded) {
    return context.arity() == 1
        ? completions(context.argument(0), service.repository().worlds(requireLoaded, requireUnloaded), WorldRecord::name)
        : List.of();
  }

  private <T> @NotNull List<String> completions(final @Nullable String input,
                                                final @NotNull Collection<T> source,
                                                final @NotNull Function<T, String> extractor) {
    final var prefix = input == null ? "" : input.toLowerCase(Locale.ROOT);
    final var completions = new ArrayList<String>();

    for (final var item : source) {
      final var name = extractor.apply(item);

      if (name.toLowerCase(Locale.ROOT).startsWith(prefix)) {
        completions.add(name);
      }
    }

    return completions;
  }
}