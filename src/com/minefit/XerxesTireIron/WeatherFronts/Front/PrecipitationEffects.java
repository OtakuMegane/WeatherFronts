package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class PrecipitationEffects implements Listener {
    private final WeatherFronts plugin;
    private final Front front;
    private final Functions functions;
    private final BlockTests blocktest;
    private final YamlConfiguration frontConfig;
    private final XORShiftRandom random = new XORShiftRandom();
    private final ConcurrentHashMap<Block, Boolean> farmland = new ConcurrentHashMap<Block, Boolean>();
    private final World world;
    private final Simulator simulator;

    public PrecipitationEffects(WeatherFronts instance, Front front) {
        this.plugin = instance;
        this.front = front;
        this.functions = new Functions(instance);
        this.frontConfig = front.getData();
        this.world = front.getWorld();
        this.simulator = front.getSimulator();
        this.blocktest = new BlockTests(instance, this.simulator);
    }

    public void precipitationBlockEffects() {
        Set<Chunk> frontChunks = this.front.getFrontChunks();
        int loopLimit = (int) Math.ceil(this.frontConfig.getInt("precipitation-intensity") / 10);

        for (int i = 0; i < loopLimit; i++) {
            alterBlock(frontChunks);
        }
    }

    private void alterBlock(Set<Chunk> frontChunks) {

        for (Chunk chunk : frontChunks) {
            if (this.random.nextInt(16) != 0) {
                continue;
            }

            FrontLocation location = this.functions.randomXYInFrontChunk(this.simulator, chunk);

            if (!location.isLoaded()) {
                return;
            }

            Block lowBlock = this.blocktest.getTopBlock(location);
            FrontLocation location2 = this.simulator.newFrontLocation(lowBlock);

            if (location2.isInRain()) {
                if (lowBlock.getType() == Material.CAULDRON) {
                    if (lowBlock.getData() < 3) {
                        lowBlock.setData((byte) (lowBlock.getData() + 1));
                    }
                } else if (lowBlock.getType() == Material.SOIL) {
                    this.farmland.put(lowBlock, true);
                }

                continue;
            }

            Block highBlock = lowBlock.getRelative(BlockFace.UP);

            if (blockCanHaveSnow(highBlock)) {
                highBlock.setType(Material.SNOW);
                continue;
            }
        }
    }

    private Boolean blockCanHaveSnow(Block block) {
        FrontLocation location = this.simulator.newFrontLocation(block);

        if (!location.isInSnow() || block.getType() != Material.AIR) {
            return false;
        }

        Block block2 = block.getRelative(BlockFace.DOWN);

        return this.blocktest.blockTypeCanFormSnow(block2.getType()) && block.getLightFromBlocks() < 10;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();
        FrontLocation location = this.simulator.newFrontLocation(block);

        if (location.isInRain() && block.getType() == Material.SOIL) {
            event.setCancelled(true);
            this.farmland.put(block, true);
        }
    }

    public void hydrateFarmland() {
        for (Entry<Block, Boolean> entry : this.farmland.entrySet()) {
            Block block = entry.getKey();
            FrontLocation location = this.simulator.newFrontLocation(block);

            if (location.isInRain() && block.getType() == Material.SOIL) {
                block.setData((byte) 6);
            } else {
                this.farmland.remove(block);
            }
        }
    }
}
