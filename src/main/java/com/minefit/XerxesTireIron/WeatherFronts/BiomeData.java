package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.block.Biome;
import org.bukkit.block.Block;

public class BiomeData {

    public BiomeData() {

    }

    public BiomeType getType(Biome biome) {
        if (isFrozen(biome)) {
            return BiomeType.FROZEN;
        }

        if (isCold(biome)) {
            return BiomeType.COOL;
        }

        if (isWarm(biome)) {
            return BiomeType.WARM;
        }

        if (isDry(biome)) {
            return BiomeType.DRY;
        }

        return BiomeType.UNKNOWN;
    }

    public BiomeType getType(Block block) {
        return getType(block.getBiome());
    }

    public boolean isFrozen(Biome biome) {
        return biome == Biome.FROZEN_OCEAN || biome == Biome.FROZEN_RIVER || biome == Biome.ICE_FLATS
                || biome == Biome.ICE_MOUNTAINS || biome == Biome.COLD_BEACH || biome == Biome.TAIGA_COLD
                || biome == Biome.TAIGA_COLD_HILLS || biome == Biome.MUTATED_ICE_FLATS
                || biome == Biome.MUTATED_TAIGA_COLD;
    }

    public boolean isFrozen(Block block) {
        return isFrozen(block.getBiome());
    }

    public boolean isCold(Biome biome) {
        return biome == Biome.EXTREME_HILLS || biome == Biome.TAIGA || biome == Biome.TAIGA_HILLS
                || biome == Biome.SMALLER_EXTREME_HILLS || biome == Biome.STONE_BEACH || biome == Biome.REDWOOD_TAIGA
                || biome == Biome.REDWOOD_TAIGA_HILLS || biome == Biome.EXTREME_HILLS_WITH_TREES
                || biome == Biome.MUTATED_EXTREME_HILLS || biome == Biome.MUTATED_TAIGA
                || biome == Biome.MUTATED_REDWOOD_TAIGA || biome == Biome.MUTATED_REDWOOD_TAIGA_HILLS
                || biome == Biome.MUTATED_EXTREME_HILLS_WITH_TREES;
    }

    public boolean isCold(Block block) {
        return isCold(block.getBiome());
    }

    public boolean isWarm(Biome biome) {
        return biome == Biome.OCEAN || biome == Biome.PLAINS || biome == Biome.FOREST || biome == Biome.SWAMPLAND
                || biome == Biome.RIVER || biome == Biome.MUSHROOM_ISLAND || biome == Biome.MUSHROOM_ISLAND_SHORE
                || biome == Biome.BEACHES || biome == Biome.FOREST_HILLS || biome == Biome.JUNGLE
                || biome == Biome.JUNGLE_HILLS || biome == Biome.JUNGLE_EDGE || biome == Biome.DEEP_OCEAN
                || biome == Biome.BIRCH_FOREST || biome == Biome.BIRCH_FOREST_HILLS || biome == Biome.ROOFED_FOREST
                || biome == Biome.MUTATED_PLAINS || biome == Biome.MUTATED_FOREST || biome == Biome.MUTATED_SWAMPLAND
                || biome == Biome.MUTATED_JUNGLE || biome == Biome.MUTATED_JUNGLE_EDGE
                || biome == Biome.MUTATED_BIRCH_FOREST || biome == Biome.MUTATED_BIRCH_FOREST_HILLS
                || biome == Biome.MUTATED_ROOFED_FOREST;
    }

    public boolean isWarm(Block block) {
        return isWarm(block.getBiome());
    }

    public boolean isDry(Biome biome) {
        return biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.SAVANNA
                || biome == Biome.SAVANNA_ROCK || biome == Biome.MESA || biome == Biome.MESA_ROCK
                || biome == Biome.MESA_CLEAR_ROCK || biome == Biome.MUTATED_DESERT || biome == Biome.MUTATED_SAVANNA
                || biome == Biome.MUTATED_SAVANNA_ROCK || biome == Biome.MUTATED_MESA
                || biome == Biome.MUTATED_MESA_ROCK || biome == Biome.MUTATED_MESA_CLEAR_ROCK;
    }

    public boolean isDry(Block block) {
        return isDry(block.getBiome());
    }

    public boolean isOcean(Biome biome) {
        return biome == Biome.OCEAN || biome == Biome.DEEP_OCEAN;
    }

    public boolean isOcean(Block block) {
        return isOcean(block.getBiome());
    }

    public boolean isTropical(Biome biome) {
        return biome == Biome.JUNGLE || biome == Biome.JUNGLE_HILLS || biome == Biome.JUNGLE_EDGE;
    }

    public boolean isTropical(Block block) {
        return isTropical(block.getBiome());
    }

    public boolean isSafe(Biome biome) {
        return biome == Biome.MUSHROOM_ISLAND || biome == Biome.MUSHROOM_ISLAND_SHORE;
    }
}
