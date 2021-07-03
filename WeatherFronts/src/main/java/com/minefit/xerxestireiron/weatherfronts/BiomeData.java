package com.minefit.xerxestireiron.weatherfronts;

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
        return biome == Biome.FROZEN_OCEAN || biome == Biome.FROZEN_RIVER || biome == Biome.SNOWY_TUNDRA
                || biome == Biome.SNOWY_MOUNTAINS || biome == Biome.SNOWY_BEACH || biome == Biome.SNOWY_TAIGA
                || biome == Biome.SNOWY_TAIGA_MOUNTAINS || biome == Biome.ICE_SPIKES
                || biome == Biome.SNOWY_TAIGA_MOUNTAINS;
    }

    public boolean isFrozen(Block block) {
        return isFrozen(block.getBiome());
    }

    public boolean isCold(Biome biome) {
        return biome == Biome.MOUNTAINS || biome == Biome.TAIGA || biome == Biome.TAIGA_HILLS
                || biome == Biome.MOUNTAIN_EDGE || biome == Biome.STONE_SHORE || biome == Biome.GIANT_TREE_TAIGA
                || biome == Biome.GIANT_TREE_TAIGA_HILLS || biome == Biome.WOODED_MOUNTAINS
                || biome == Biome.GRAVELLY_MOUNTAINS || biome == Biome.TAIGA_MOUNTAINS
                || biome == Biome.GIANT_SPRUCE_TAIGA || biome == Biome.GIANT_SPRUCE_TAIGA_HILLS
                || biome == Biome.MODIFIED_GRAVELLY_MOUNTAINS;
    }

    public boolean isCold(Block block) {
        return isCold(block.getBiome());
    }

    public boolean isWarm(Biome biome) {
        return biome == Biome.OCEAN || biome == Biome.PLAINS || biome == Biome.FOREST || biome == Biome.SWAMP
                || biome == Biome.RIVER || biome == Biome.MUSHROOM_FIELDS || biome == Biome.MUSHROOM_FIELD_SHORE
                || biome == Biome.BEACH || biome == Biome.WOODED_HILLS || biome == Biome.JUNGLE
                || biome == Biome.JUNGLE_HILLS || biome == Biome.JUNGLE_EDGE || biome == Biome.DEEP_OCEAN
                || biome == Biome.BIRCH_FOREST || biome == Biome.BIRCH_FOREST_HILLS || biome == Biome.DARK_FOREST
                || biome == Biome.SUNFLOWER_PLAINS || biome == Biome.FLOWER_FOREST || biome == Biome.SWAMP_HILLS
                || biome == Biome.MODIFIED_JUNGLE || biome == Biome.MODIFIED_JUNGLE_EDGE
                || biome == Biome.TALL_BIRCH_FOREST || biome == Biome.TALL_BIRCH_HILLS
                || biome == Biome.DARK_FOREST_HILLS;
    }

    public boolean isWarm(Block block) {
        return isWarm(block.getBiome());
    }

    public boolean isDry(Biome biome) {
        return biome == Biome.DESERT || biome == Biome.DESERT_HILLS || biome == Biome.SAVANNA
                || biome == Biome.SAVANNA_PLATEAU || biome == Biome.BADLANDS || biome == Biome.WOODED_BADLANDS_PLATEAU
                || biome == Biome.BADLANDS_PLATEAU || biome == Biome.DESERT_LAKES || biome == Biome.SHATTERED_SAVANNA
                || biome == Biome.SHATTERED_SAVANNA_PLATEAU|| biome == Biome.ERODED_BADLANDS
                || biome == Biome.MODIFIED_WOODED_BADLANDS_PLATEAU || biome == Biome.MODIFIED_BADLANDS_PLATEAU;
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
        return biome == Biome.MUSHROOM_FIELDS || biome == Biome.MUSHROOM_FIELD_SHORE;
    }
}
