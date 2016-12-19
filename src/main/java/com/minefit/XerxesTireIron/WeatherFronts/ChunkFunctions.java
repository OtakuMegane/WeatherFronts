package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Chunk;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class ChunkFunctions {
    private final WeatherFronts plugin;
    private final XORShiftRandom random;

    public ChunkFunctions(WeatherFronts instance) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
    }

    public FrontLocation randomLocationInChunk(Simulator simulator, Chunk chunk) {
        int x = chunk.getX() * 16;
        int z = chunk.getZ() * 16;
        int x2 = this.random.nextIntRange(x, x + 16);
        int z2 = this.random.nextIntRange(z, z + 16);
        return new FrontLocation(simulator, x2, z2);
    }
}
