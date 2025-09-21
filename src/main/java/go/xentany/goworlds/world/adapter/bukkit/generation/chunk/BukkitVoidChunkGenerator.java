package go.xentany.goworlds.world.adapter.bukkit.generation.chunk;

import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public final class BukkitVoidChunkGenerator extends ChunkGenerator {

  public BukkitVoidChunkGenerator() {}

  @Override
  public @NotNull ChunkData generateChunkData(final @NotNull World world,
                                              final @Nullable Random random,
                                              final int x,
                                              final int z,
                                              final @Nullable BiomeGrid biome) {
    return createChunkData(world);
  }
}