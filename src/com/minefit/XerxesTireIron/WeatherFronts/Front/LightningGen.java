package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class LightningGen {
    private final WeatherFronts plugin;
    private final YamlConfiguration frontConfig;
    private final YamlConfiguration simulatorConfig;
    private final Functions functions;
    private final BlockTests blocktest;
    private final LocationTests locationtest;
    private double accumulator = 0.0;
    private double weightedLPM;
    private double lightningPerCheck;
    private boolean hasLightning;

    public LightningGen(WeatherFronts instance, YamlConfiguration config, YamlConfiguration config2) {
        this.plugin = instance;
        this.frontConfig = config;
        this.simulatorConfig = config2;
        this.functions = new Functions(instance);
        this.blocktest = new BlockTests(instance);
        this.locationtest = new LocationTests(instance);

        if (this.frontConfig.getDouble("lightning-per-minute") <= 0.0) {
            this.hasLightning = false;
        } else {
            this.hasLightning = true;
            this.weightedLPM = this.frontConfig.getDouble("lightning-per-minute");
            weight();
            this.lightningPerCheck = this.weightedLPM / 240;
        }
    }

    private void weight() {
        if (this.frontConfig.getInt("radius-x") > 160) {
            this.weightedLPM *= this.frontConfig.getInt("radius-x") / 160;
        }

        if (this.frontConfig.getInt("radius-z") > 160) {
            this.weightedLPM *= this.frontConfig.getInt("radius-z") / 160;
        }
    }

    public void lightningGen(World world) {
        if (!this.hasLightning) {
            return;
        }

        this.accumulator += this.lightningPerCheck;

        if (this.accumulator < 1.0) {
            return;
        }

        Chunk[] validChunks = world.getLoadedChunks();

        while (accumulator >= 1.0) {
            randomStrike(world, validChunks);
            this.accumulator -= 1.0;
        }
    }

    public void randomStrike(World world, Chunk[] validChunks) {
        int[] xz = this.functions.randomXYInFront(this.frontConfig);

        if (!this.locationtest.locationIsLoaded(world, xz[0], xz[1])) {
            return;
        }

        int x = xz[0];
        int z = xz[1];
        boolean lightningDry = this.simulatorConfig.getBoolean("lightning-in-dry-biomes");
        boolean lightningCold = this.simulatorConfig.getBoolean("lightning-in-cold-biomes");
        Block highBlock = this.blocktest.getTopLightningBlock(new Location(world, x, 0, z));

        if (highBlock == null) {
            return;
        }

        Location highLoc = highBlock.getRelative(BlockFace.UP).getLocation();
        Biome biome = highBlock.getBiome();

        if ((!this.functions.biomeIsDry(biome) && !this.functions.biomeIsCold(biome)) || (this.functions.biomeIsDry(biome) && lightningDry)
                || (this.functions.biomeIsCold(biome) && lightningCold)) {
            world.strikeLightning(highLoc);
        }
    }
}
