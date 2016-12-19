package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Chunk;
import org.bukkit.World;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class ChunkFunctions {
    private final WeatherFronts plugin;
    private final XORShiftRandom random;

    public ChunkFunctions(WeatherFronts instance) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
    }

    public FrontLocation randomLocationInChunk(Simulator simulator, Chunk chunk, boolean buffer) {

        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int x = chunkX << 4;
        int z = chunkZ << 4;
        int x2 = x + 16;
        int z2 = z + 16;

        // Making block changes at the edge of a chunk apparently causes the adjacent chunk to load/generate
        // Unsure if a Minecraft bug, some oddity of CraftBukkit/Spigot or what
        // This gives a slight buffer that seems to avoid the problem
        if (buffer) {
            World world = chunk.getWorld();

            if (!world.isChunkLoaded(chunkX + 1, chunkZ)) {
                x2 -= 1;
            }

            if (!world.isChunkLoaded(chunkX - 1, chunkZ)) {
                x += 1;
            }

            if (!world.isChunkLoaded(chunkX, chunkZ + 1)) {
                z2 -= 1;
            }

            if (!world.isChunkLoaded(chunkX, chunkZ - 1)) {
                z += 1;
            }
        }
        int x3 = this.random.nextIntRange(x, x2);
        int z3 = this.random.nextIntRange(z, z2);
        return new FrontLocation(simulator, x3, z3);
    }
}
