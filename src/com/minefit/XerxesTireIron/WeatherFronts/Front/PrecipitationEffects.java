package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
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
import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class PrecipitationEffects implements Listener {
    private final WeatherFronts plugin;
    private final Front front;
    private final Functions functions;
    private final LocationTests locationtest;
    private final BlockTests blocktest;
    private final YamlConfiguration frontConfig;
    private final XORShiftRandom random = new XORShiftRandom();
    private final ConcurrentHashMap<Block, Boolean> farmland = new ConcurrentHashMap<Block, Boolean>();
    private final World world;

    public PrecipitationEffects(WeatherFronts instance, Front front) {
        this.plugin = instance;
        this.front = front;
        this.functions = new Functions(instance);
        this.locationtest = new LocationTests(instance);
        this.blocktest = new BlockTests(instance);
        this.frontConfig = front.getData();
        this.world = front.getWorld();
    }

    public void precipitationBlockEffects() {
        int loopLimit = (int) Math.ceil(((this.frontConfig.getInt("radius-x") + this.frontConfig.getInt("radius-z"))
                * this.frontConfig.getInt("precipitation-intensity")) / 100);

        for (int i = 0; i < loopLimit; i++) {
            alterBlock();
        }
    }

    private void alterBlock() {
        FrontLocation location = this.functions.randomXYInFront(this.front.getSimulator(),
                this.front.getFrontBoundaries());

        if (!location.isLoaded()) {
            return;
        }

        Block lowBlock = this.blocktest
                .getTopBlock(new Location(this.world, location.getPositionX(), 0, location.getPositionZ()));

        if (this.locationtest.locationIsInRain(lowBlock.getLocation())) {
            if (lowBlock.getType() == Material.CAULDRON) {
                if (this.random.nextInt(20) == 0 && lowBlock.getData() < 3) {
                    lowBlock.setData((byte) (lowBlock.getData() + 1));
                }
            } else if (lowBlock.getType() == Material.SOIL) {
                this.farmland.put(lowBlock, true);
            }

            return;
        }

        Block highBlock = lowBlock.getRelative(BlockFace.UP);

        if (blockCanHaveSnow(highBlock)) {
            highBlock.setType(Material.SNOW);
            return;
        }
    }

    private Boolean blockCanHaveSnow(Block block) {
        Location location = block.getLocation();

        if (!this.blocktest.blockIsCold(location) || !this.locationtest.locationIsAboveground(location)
                || block.getType() != Material.AIR) {
            return false;
        }

        Block block2 = block.getRelative(BlockFace.DOWN);

        return this.blocktest.blockTypeCanFormSnow(block2.getType()) && block.getLightFromBlocks() < 10;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        if (block.getType() == Material.SOIL && this.locationtest.locationIsInRain(blockLoc)) {
            event.setCancelled(true);
            this.farmland.put(block, true);
        }
    }

    public void hydrateFarmland() {
        for (Entry<Block, Boolean> entry : this.farmland.entrySet()) {
            Block block = entry.getKey();
            if (block.getType() == Material.SOIL && this.locationtest.locationIsInRain(block.getLocation())) {
                block.setData((byte) 6);
            } else {
                this.farmland.remove(block);
            }
        }
    }
}
