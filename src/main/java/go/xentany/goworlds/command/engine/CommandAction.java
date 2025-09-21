package go.xentany.goworlds.command.engine;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public record CommandAction(@NotNull String name,
                            @NotNull String usage,
                            @NotNull Invoker invoker,
                            @NotNull Completer completer,
                            int arity) {

  @FunctionalInterface
  public interface Invoker {

    void invoke(final @NotNull CommandContext context);
  }

  @FunctionalInterface
  public interface Completer {

    @NotNull List<String> suggest(final @NotNull CommandContext context);
  }

  public CommandAction(final @NotNull String name,
                       final @NotNull String usage,
                       final @NotNull Invoker invoker,
                       final @NotNull Completer completer,
                       final int arity) {
    this.name = Objects.requireNonNull(name, "name");
    this.usage = Objects.requireNonNull(usage, "usage");
    this.invoker = Objects.requireNonNull(invoker, "invoker");
    this.completer = Objects.requireNonNull(completer, "completer");
    this.arity = Math.max(0, arity);
  }
}