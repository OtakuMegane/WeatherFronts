package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

public class Functions {
    private WeatherFronts plugin;
    private XORShiftRandom random;

    public Functions(WeatherFronts instance) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
    }

    public int[] randomXYInFront(YamlConfiguration config) {
        int[] xz = new int[2];
        xz[0] = random.nextIntRange(config.getInt("center-x") - config.getInt("radius-x"),
                config.getInt("center-x") + config.getInt("radius-x"));
        xz[1] = random.nextIntRange(config.getInt("center-z") - config.getInt("radius-z"),
                config.getInt("center-z") + config.getInt("radius-z"));
        return xz;
    }

    public Block findHighestBlock(Location location, int start, boolean isSolid, boolean isLiquid) {
        World world = location.getWorld();
        int i = 255;

        if (start < 256 && start > 0) {
            i = start;
        }

        for (; i > 0; --i) {
            Block block = world.getBlockAt(location.getBlockX(), i, location.getBlockZ());

            if (block.isEmpty()) {
                continue;
            }

            if (!isSolid && !isLiquid && !block.isEmpty()) {
                return block;
            }

            if (isSolid && isLiquid && block.getType().isSolid() && !block.isLiquid()) {
                return block;

            }

            if (isSolid && block.getType().isSolid()) {
                return block;

            }

            if (isLiquid && block.isLiquid()) {
                return block;
            }

        }

        return world.getBlockAt(location.getBlockX(), 0, location.getBlockZ());
    }

    public double calculateLocationTemp(Location location) {
        double temp = location.getBlock().getTemperature();

        // Taken from the NMS BiomeBase calculation
        if (location.getY() > 64) {
            temp = temp - (location.getY() - 64) * 0.05F / 30.0F;
        }

        return temp;
    }

    public Boolean biomeIsDry(Biome biome) {
        return biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.MUTATED_DESERT
                || biome == Biome.SAVANNA || biome == Biome.SAVANNA_ROCK || biome == Biome.MUTATED_SAVANNA
                || biome == Biome.MUTATED_SAVANNA_ROCK;
    }

    public Boolean biomeIsCold(Biome biome) {
        return biome == Biome.COLD_BEACH || biome == Biome.TAIGA_COLD || biome == Biome.TAIGA_COLD_HILLS
                || biome == Biome.MUTATED_TAIGA_COLD || biome == Biome.FROZEN_OCEAN || biome == Biome.FROZEN_RIVER
                || biome == Biome.ICE_MOUNTAINS || biome == Biome.ICE_FLATS || biome == Biome.MUTATED_ICE_FLATS;
    }
}
