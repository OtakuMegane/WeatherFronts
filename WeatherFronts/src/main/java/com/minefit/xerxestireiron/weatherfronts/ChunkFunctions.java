package com.minefit.xerxestireiron.weatherfronts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;

import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;

public class ChunkFunctions {
    private final WeatherFronts plugin;
    private final XORShiftRandom random;

    public ChunkFunctions(WeatherFronts instance) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
    }

    public FrontsLocation randomLocationInChunk(Simulator simulator, Chunk chunk, boolean buffer) {
        int chunkX = chunk.getX();
        int chunkZ = chunk.getZ();
        int x = chunkX << 4;
        int z = chunkZ << 4;
        int x2 = x + 16;
        int z2 = z + 16;
        FrontsLocation location = new FrontsLocation(simulator, this.random.nextIntRange(x, x2),
                this.random.nextIntRange(z, z2));

        if (buffer) {
            location = adjustForUnloadedChunks(location);
        }

        return location;
    }

    // Making block changes at the edge of a chunk apparently causes the adjacent chunk to load/generate
    // This gives a slight buffer that seems to avoid the problem when needed
    public FrontsLocation adjustForUnloadedChunks(FrontsLocation location) {
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

    public List<Chunk> chunksContainingBlock(List<Chunk> chunks, Block block) {
        List<Chunk> returnChunks = new ArrayList<>();
        BlockData blockData = block.getBlockData();

        for (Chunk chunk : chunks) {
            if (chunk.contains(blockData)) {
                returnChunks.add(chunk);
            }
        }

        return returnChunks;
    }

    public Set<Chunk> chunksInRadiusBySpiral(Location location, int radius, boolean loadedOnly) {
        Set<Chunk> collectedChunks = new LinkedHashSet<>();
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockX() >> 4;
        World world = location.getWorld();
        int x = 0;
        int z = 0;
        int max = radius * radius;
        int iterations = 0;

        while (collectedChunks.size() < max) {

        }

        for (int level = 0; level <= radius; level++) {

            for (int i = -radius; i <= radius; i++) {
                for (int j = -radius; j <= radius; j++) {
                    if (loadedOnly && !world.isChunkLoaded(centerChunkX + i, centerChunkZ + j)) {
                        continue;
                    }

                    collectedChunks.add(world.getChunkAt(centerChunkX + i, centerChunkZ + j));
                }
            }
        }

        for (int i = 0; i <= radius; i++) {
            collectedChunks.addAll(this.chunksInRadius(location, radius, loadedOnly));
        }

        return collectedChunks;
    }

    public Set<Chunk> chunksInRadiusByLevel(Location location, int radius, boolean loadedOnly) {
        Set<Chunk> collectedChunks = new LinkedHashSet<>();
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockX() >> 4;
        World world = location.getWorld();
        int level = 0;

        while (level <= radius) {
            // HashSet will deal with the corners being collected twice

            // Collect left and right sides
            for (int j = -level; j <= level; j++) {
                if (!world.isChunkGenerated(centerChunkX - level, centerChunkZ + j)
                        || (loadedOnly && !world.isChunkLoaded(centerChunkX - level, centerChunkZ + j))) {
                    continue;
                }

                collectedChunks.add(world.getChunkAt(centerChunkX - level, centerChunkZ + j));

                if (!world.isChunkGenerated(centerChunkX - level, centerChunkZ + j)
                        || (loadedOnly && !world.isChunkLoaded(centerChunkX + level, centerChunkZ + j))) {
                    continue;
                }

                collectedChunks.add(world.getChunkAt(centerChunkX + level, centerChunkZ + j));
            }

            // Collect top and bottom
            for (int i = -level; i <= level; i++) {
                if (!world.isChunkGenerated(centerChunkX + i, centerChunkZ - level)
                        || (loadedOnly && !world.isChunkLoaded(centerChunkX + i, centerChunkZ - level))) {
                    continue;
                }

                collectedChunks.add(world.getChunkAt(centerChunkX + i, centerChunkZ - level));

                if (!world.isChunkGenerated(centerChunkX + i, centerChunkZ - level)
                        || (loadedOnly && !world.isChunkLoaded(centerChunkX + i, centerChunkZ - level))) {
                    continue;
                }

                collectedChunks.add(world.getChunkAt(centerChunkX + i, centerChunkZ + level));
            }

            level++;
        }

        return collectedChunks;
    }

    public Set<Chunk> chunksInRadius(Location location, int radius, boolean loadedOnly) {
        Set<Chunk> collectedChunks = new LinkedHashSet<>();
        int centerChunkX = location.getBlockX() >> 4;
        int centerChunkZ = location.getBlockX() >> 4;
        World world = location.getWorld();

        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {

                if (loadedOnly && !world.isChunkLoaded(centerChunkX + i, centerChunkZ + j)) {
                    continue;
                }

                collectedChunks.add(world.getChunkAt(centerChunkX + i, centerChunkZ + j));
            }
        }

        return collectedChunks;
    }
}
