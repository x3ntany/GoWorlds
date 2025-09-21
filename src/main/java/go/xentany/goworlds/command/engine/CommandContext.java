package go.xentany.goworlds.command.engine;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record CommandContext(@NotNull CommandSender sender,
                             @NotNull String label,
                             @NotNull String[] arguments,
                             int offset) {

  public CommandContext(final @NotNull CommandSender sender,
                        final @NotNull String label,
                        final @NotNull String[] arguments,
                        final int offset) {
    this.sender = Objects.requireNonNull(sender, "sender");
    this.label = Objects.requireNonNull(label, "label");
    this.arguments = Objects.requireNonNull(arguments, "arguments");
    this.offset = Math.max(0, offset);
  }

  public int arity() {
    return Math.max(0, arguments.length - offset);
  }

  public @Nullable String argument(final int index) {
    final int absolute = offset + index;

    return absolute < 0 || absolute >= arguments.length ? null : arguments[absolute];
  }
}