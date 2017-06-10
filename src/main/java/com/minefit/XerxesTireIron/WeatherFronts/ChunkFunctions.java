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
        FrontLocation location = new FrontLocation(simulator, this.random.nextIntRange(x, x2),
                this.random.nextIntRange(z, z2));

        if (buffer) {
            location = adjustForUnloadedChunks(location);
        }

        return location;
    }

    // Making block changes at the edge of a chunk apparently causes the adjacent chunk to load/generate
    // This gives a slight buffer that seems to avoid the problem when needed
    public FrontLocation adjustForUnloadedChunks(FrontLocation location) {
        int chunkX = location.getChunk().getX();
        int chunkZ = location.getChunk().getZ();
        World world = location.getWorld();

        if (!world.isChunkLoaded(chunkX + 1, chunkZ)) {
            location.setX(location.getX() - 1);
        }

        if (!world.isChunkLoaded(chunkX - 1, chunkZ)) {
            location.setX(location.getX() + 1);
        }

        if (!world.isChunkLoaded(chunkX, chunkZ + 1)) {
            location.setZ(location.getZ() - 1);
        }

        if (!world.isChunkLoaded(chunkX, chunkZ - 1)) {
            location.setZ(location.getZ() + 1);
        }

        return location;
    }
}
