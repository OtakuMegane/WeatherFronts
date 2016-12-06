package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class ChunkTick {
    private final WeatherFronts plugin;
    private final Front front;
    private Set<Chunk> frontChunks;
    private final XORShiftRandom random;
    private final Functions functions;
    private final Simulator simulator;
    private final BlockTests blocktest;
    private double tickDelay;
    private int intensity;

    public ChunkTick(WeatherFronts instance, Front front) {
        this.plugin = instance;
        this.front = front;
        this.random = new XORShiftRandom();
        this.functions = new Functions(instance);
        this.simulator = front.getSimulator();
        this.blocktest = new BlockTests(instance, this.simulator);
        this.intensity = this.front.getPrecipitationIntensity();

        if (this.intensity > 100) {
            this.intensity = 100;
        }
    }

    public void tickDispatch() {
        this.frontChunks = this.front.getFrontChunks();
        this.intensity = this.front.getPrecipitationIntensity();
        precipitationEffects();
    }

    private void precipitationEffects() {
        this.tickDelay += (this.intensity) / 10;

        if (this.tickDelay < 1.0) {
            return;
        }

        while (this.tickDelay >= 1.0) {
            for (Chunk chunk : this.frontChunks) {
                FrontLocation location = this.functions.randomXYInFrontChunk(this.simulator, chunk);

                if (!location.isLoaded()) {
                    return;
                }

                Block lowBlock = this.blocktest.getTopBlock(location);
                FrontLocation location2 = this.simulator.newFrontLocation(lowBlock);

                if (location2.isInRain()) {
                    fillCauldron(lowBlock);
                    hydrateFarmland(lowBlock);
                    continue;
                }

                if (location2.isInSnow()) {
                    formSnow(lowBlock);
                    continue;
                }
            }

            this.tickDelay -= 1.0;
        }
    }

    private void fillCauldron(Block block) {
        if (this.random.nextInt(16) != 0) {
            return;
        }
        if (block.getType() == Material.CAULDRON && block.getData() < 3) {
            block.setData((byte) (block.getData() + 1));
        }

    }

    private void hydrateFarmland(Block block) {
        if (this.random.nextInt(48) != 0) {
            return;
        }
        if (block.getType() == Material.SOIL) {
            block.setData((byte) 6);
        }
    }

    private void formSnow(Block block) {
        if (this.random.nextInt(16) != 0) {
            return;
        }

        Block upperBlock = block.getRelative(BlockFace.UP);

        if (blockCanHaveSnow(block)) {
            upperBlock.setType(Material.SNOW);
        }

    }

    private Boolean blockCanHaveSnow(Block block) {
        return block.getRelative(BlockFace.UP).getType() == Material.AIR
                && this.blocktest.blockTypeCanFormSnow(block.getType()) && block.getLightFromBlocks() < 10;
    }
}
