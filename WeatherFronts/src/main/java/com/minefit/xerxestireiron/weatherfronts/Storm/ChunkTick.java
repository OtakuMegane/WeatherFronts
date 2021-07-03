package com.minefit.xerxestireiron.weatherfronts.Storm;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.block.CauldronLevelChangeEvent;
import org.bukkit.event.block.MoistureChangeEvent;

import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.ChunkFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;

public class ChunkTick {
    private final WeatherFronts plugin;
    private final Storm storm;
    private Map<Chunk, Boolean> stormChunks;
    private final XORShiftRandom random;
    private final Simulator simulator;
    private final BlockFunctions blockFunction;
    private final ChunkFunctions chunkFunction;
    private double tickDelay;
    private int intensity;

    public ChunkTick(WeatherFronts instance, Storm storm) {
        this.plugin = instance;
        this.storm = storm;
        this.random = new XORShiftRandom();
        this.simulator = storm.getSimulator();
        this.blockFunction = new BlockFunctions(instance, this.simulator);
        this.chunkFunction = new ChunkFunctions(instance);
        this.intensity = this.storm.getPrecipitationIntensity();
    }

    public void tickDispatch() {
        this.stormChunks = this.storm.getStormChunks();
        this.intensity = this.storm.getPrecipitationIntensity();

        if (this.intensity > 100) {
            this.intensity = 100;
        }

        precipitationEffects();
    }

    private void precipitationEffects() {
        this.tickDelay += (this.intensity) / 10;

        if (this.tickDelay < 1.0) {
            return;
        }

        while (this.tickDelay >= 1.0) {
            for (Entry<Chunk, Boolean> entry : this.stormChunks.entrySet()) {
                if (!entry.getKey().isLoaded() || this.random.nextInt(16) != 0) {
                    continue;
                }

                FrontsLocation location = this.chunkFunction.randomLocationInChunk(this.simulator, entry.getKey(),
                        true);

                if (!location.isLoaded() || !this.storm.isInStorm(location)) {
                    continue;
                }

                Block block = this.blockFunction.getTopBlock(location);
                Material blockType = block.getType();

                if (this.blockFunction.isDry(block)) {
                    continue;
                }

                if (this.blockFunction.isInSnow(block)) {
                    formSnow(block);
                    continue;
                }

                if (blockType == Material.WATER_CAULDRON || blockType == Material.CAULDRON) {
                    fillCauldron(block);
                } else if (block.getType() == Material.FARMLAND) {
                    hydrateFarmland(block);
                }
            }

            this.tickDelay -= 1.0;
        }
    }

    private void fillCauldron(Block block) {
        Material blockType = block.getType();
        boolean newWater = blockType == Material.CAULDRON;

        if (newWater) {
            block.setType(Material.WATER_CAULDRON);
        }

        Levelled cauldron = (Levelled) block.getBlockData();
        int oldLevel = cauldron.getLevel();

        if (!newWater && oldLevel < cauldron.getMaximumLevel()) {
            int newLevel = cauldron.getLevel() + 1;
            cauldron.setLevel(newLevel);
            block.setBlockData(cauldron);
        }

        CauldronLevelChangeEvent newEvent = new CauldronLevelChangeEvent(block, null,
                CauldronLevelChangeEvent.ChangeReason.NATURAL_FILL, block.getState());
        Bukkit.getServer().getPluginManager().callEvent(newEvent);
    }

    private void hydrateFarmland(Block block) {
        Farmland farmland = (Farmland) block.getBlockData();

        if (farmland.getMoisture() < farmland.getMaximumMoisture()) {
            BlockState newState = block.getState();
            farmland.setMoisture(7);
            newState.setBlockData((BlockData) farmland);
            MoistureChangeEvent newEvent = new MoistureChangeEvent(block, newState);
            Bukkit.getServer().getPluginManager().callEvent(newEvent);

            if (!newEvent.isCancelled()) {
                block.setBlockData(farmland);
            }
        }
    }

    private void formSnow(Block block) {
        Block upperBlock = block.getRelative(BlockFace.UP);

        if (blockCanHaveSnow(block, upperBlock)) {
            upperBlock.setType(Material.SNOW);
        }

    }

    private Boolean blockCanHaveSnow(Block block, Block upperBlock) {
        return upperBlock.getType() == Material.AIR && this.blockFunction.canFormSnow(block)
                && upperBlock.getLightFromBlocks() < 10;

    }
}
