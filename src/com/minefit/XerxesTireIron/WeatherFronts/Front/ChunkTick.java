package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class ChunkTick {
    private final WeatherFronts plugin;
    private final Front front;
    private Set<Chunk> frontChunks;
    private final XORShiftRandom random;
    private final Simulator simulator;
    private final BlockFunctions blockFunction;
    private double tickDelay;
    private int intensity;

    public ChunkTick(WeatherFronts instance, Front front) {
        this.plugin = instance;
        this.front = front;
        this.random = new XORShiftRandom();
        this.simulator = front.getSimulator();
        this.blockFunction = new BlockFunctions(instance, this.simulator);
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
                if (!chunk.isLoaded() || this.random.nextInt(16) != 0) {
                    continue;
                }

                FrontLocation location = randomLocationInChunk(this.simulator, chunk);

                if (!this.front.isInFront(location) || location.isDry()) {
                    continue;
                }

                Block block = this.blockFunction.getTopBlock(location);
                FrontLocation location2 = this.simulator.newFrontLocation(block);

                if (location2.isInRain()) {
                    if (block.getType() == Material.CAULDRON) {
                        fillCauldron(block);
                    } else if (block.getType() == Material.SOIL) {
                        hydrateFarmland(block);
                    }

                    continue;
                }

                if (location2.isInSnow()) {
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
        return upperBlock.getType() == Material.AIR
                && this.blockFunction.canFormSnow(block.getType()) && upperBlock.getLightFromBlocks() < 10;

    }

    private FrontLocation randomLocationInChunk(Simulator simulator, Chunk chunk) {
        int x = chunk.getX() * 16;
        int z = chunk.getZ() * 16;
        int x2 = this.random.nextIntRange(x, x + 15);
        int z2 = this.random.nextIntRange(z, z + 15);
        return new FrontLocation(simulator, x2, z2);
    }
}
