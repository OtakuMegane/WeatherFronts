package com.minefit.XerxesTireIron.WeatherFronts.Storm;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.ChunkFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

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
            for (Entry<Chunk, Boolean> chunk : this.stormChunks.entrySet()) {
                if (!chunk.getKey().isLoaded() || this.random.nextInt(16) != 0) {
                    continue;
                }

                FrontLocation location = this.chunkFunction.randomLocationInChunk(this.simulator, chunk.getKey(), true);

                if (!location.isLoaded() || !this.storm.isInStorm(location)) {
                    continue;
                }

                Block block = this.blockFunction.getTopBlock(location);

                if (this.blockFunction.isDry(block)) {
                    if (block.getType() == Material.CAULDRON) {
                        fillCauldron(block);
                    } else if (block.getType() == Material.SOIL) {
                        hydrateFarmland(block);
                    }

                    continue;
                }

                if (this.blockFunction.isInSnow(block)) {
                    formSnow(block);
                    continue;
                }
            }

            this.tickDelay -= 1.0;
        }
    }

    private void fillCauldron(Block block) {
        if (block.getData() < 3) {
            block.setData((byte) (block.getData() + 1));
        }
    }

    private void hydrateFarmland(Block block) {
        block.setData((byte) 6);
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
