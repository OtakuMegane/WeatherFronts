package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.util.HashSet;
import java.util.Set;

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
import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class PrecipitationEffects implements Listener {
    private final WeatherFronts plugin;
    private final Functions functions;
    private final LocationTests locationtest;
    private final BlockTests blocktest;
    private final YamlConfiguration frontConfig;
    private final XORShiftRandom random = new XORShiftRandom();
    private final Set<Block> farmland = new HashSet<Block>();
    private final World world;

    public PrecipitationEffects(WeatherFronts instance, YamlConfiguration config, World world) {
        this.plugin = instance;
        this.functions = new Functions(instance);
        this.locationtest = new LocationTests(instance);
        this.blocktest = new BlockTests(instance);
        this.frontConfig = config;
        this.world = world;
    }

    public void precipitationBlockEffects() {
        int loopLimit = (int) Math.ceil(((this.frontConfig.getInt("radius-x") + this.frontConfig.getInt("radius-z"))
                * this.frontConfig.getInt("intensity")) / 100);

        for (int i = 0; i < loopLimit; i++) {
            alterBlock();
        }
    }

    private void alterBlock() {
        int[] xz = this.functions.randomXYInFront(this.frontConfig);

        if (!this.locationtest.locationIsLoaded(this.world, xz[0], xz[1])) {
            return;
        }

        Block highBlock = this.blocktest.getTopEmptyBlock(new Location(this.world, xz[0], 0, xz[1]));
        Block lowBlock = this.blocktest.getTopBlock(new Location(this.world, xz[0], 0, xz[1]));

        if (blockCanHaveSnow(highBlock.getLocation())) {
            highBlock.setType(Material.SNOW);
        }

        if (this.locationtest.locationIsInRain(lowBlock.getLocation())) {
            if (lowBlock.getType() == Material.CAULDRON && this.random.nextInt(20) == 0 && lowBlock.getData() < 3) {
                lowBlock.setData((byte) (lowBlock.getData() + 1));
            }

            if (lowBlock.getType() == Material.SOIL) {
                this.farmland.add(lowBlock);
            }
        }
    }

    private Boolean blockCanHaveSnow(Location location) {
        Block block = location.getBlock();

        if (!this.blocktest.blockIsCold(location) || !this.locationtest.locationIsAboveground(location)
                || block.getType() != Material.AIR) {
            return false;
        }

        Block block2 = block.getRelative(BlockFace.DOWN);
        return this.blocktest.blockTypeCanFormSnow(block2.getRelative(BlockFace.DOWN).getType())
                && block.getLightFromBlocks() < 10;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        if (block.getType() == Material.SOIL && this.locationtest.locationIsInRain(blockLoc)) {
            event.setCancelled(true);
            this.farmland.add(block);
        }
    }

    public void hydrateFarmland() {
        for (Block block : this.farmland) {
            if (block.getType() == Material.SOIL && this.locationtest.locationIsInRain(block.getLocation())) {
                block.setData((byte) 6);
            } else {
                this.farmland.remove(block);
            }
        }
    }
}
