package go.xentany.goworlds.command.engine;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import go.xentany.goworlds.locale.MessageKey;
import go.xentany.goworlds.locale.Messages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

public final class CommandRouter implements CommandExecutor, TabCompleter {

  private final List<CommandAction> catalog = new ArrayList<>();
  private final Map<String, CommandAction> index = new LinkedHashMap<>();

  public CommandRouter register(final @NotNull CommandAction action) {
    Objects.requireNonNull(action, "subcommand");

    catalog.add(action);
    index.put(action.name().toLowerCase(Locale.ROOT), action);

    return this;
  }

  @Override
  public boolean onCommand(final @NotNull CommandSender sender,
                           final @NotNull Command command,
                           final @NotNull String label,
                           final @NotNull String @NotNull [] arguments) {
    if (arguments.length == 0) {
      sendUsage(sender, label);

      return true;
    }

    final var token = arguments[0].toLowerCase(Locale.ROOT);
    final var subcommand = index.get(token);

    if (subcommand == null) {
      sendUsage(sender, label);

      return true;
    }

    final int arity = arguments.length - 1;

    if (arity < subcommand.arity()) {
      Messages.send(sender, MessageKey.USAGE_PREFIX, Messages.vars(
          "command", label,
          "usage", subcommand.usage())
      );

      return true;
    }

    final var context = new CommandContext(sender, label, arguments, 1);

    subcommand.invoker().invoke(context);

    return true;
  }

  @Override
  public List<String> onTabComplete(final @NotNull CommandSender sender,
                                    final @NotNull Command command,
                                    final @NotNull String alias,
                                    final @NotNull String @NotNull [] arguments) {
    if (arguments.length == 1) {
      final var names = new ArrayList<String>(catalog.size());

      for (final var subcommand : catalog) {
        names.add(subcommand.name());
      }

      return completions(arguments[0], names);
    }

    final var subcommand = index.get(arguments[0].toLowerCase(Locale.ROOT));

    if (subcommand == null) {
      return List.of();
    }

    final var context = new CommandContext(sender, alias, arguments, 1);

    return subcommand.completer().suggest(context);
  }

  private void sendUsage(final @NotNull CommandSender sender, final @NotNull String label) {
    Messages.send(sender, MessageKey.ROUTER_USAGE_HEADER);

    for (final var sub : catalog) {
      Messages.send(sender, MessageKey.ROUTER_USAGE_LINE, Messages.vars(
          "command", label,
          "usage", sub.usage())
      );
    }
  }

  private @NotNull List<String> completions(@NotNull String prefix, final @NotNull List<String> options) {
    prefix = prefix.toLowerCase(Locale.ROOT);

    final var result = new ArrayList<String>();

    for (final var option : options) {
      if (option.toLowerCase(Locale.ROOT).startsWith(prefix)) {
        result.add(option);
      }
    }

    return result;
  }
}