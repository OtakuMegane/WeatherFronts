package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class LightningGen {
    private final WeatherFronts plugin;
    private final Front front;
    private final YamlConfiguration frontConfig;
    private final YamlConfiguration simulatorConfig;
    private final Functions functions;
    private final BlockTests blocktest;
    private double accumulator = 0.0;
    private double weightedLPM;
    private double lightningPerCheck;
    private boolean hasLightning;
    private final XORShiftRandom random;

    public LightningGen(WeatherFronts instance, YamlConfiguration config, Front front) {
        this.plugin = instance;
        this.front = front;
        this.frontConfig = front.getData();
        this.simulatorConfig = config;
        this.functions = new Functions(instance);
        this.blocktest = new BlockTests(instance, front.getSimulator());
        this.random = new XORShiftRandom();

        if (this.frontConfig.getInt("lightning-per-minute") <= 0) {
            this.hasLightning = false;
        } else {
            this.hasLightning = true;
            this.weightedLPM = this.frontConfig.getInt("lightning-per-minute");

            if (this.simulatorConfig.getBoolean("use-weighted-lightning")) {
                weight(this.simulatorConfig.getInt("weight-radius-threshold"));
            }

            this.lightningPerCheck = this.weightedLPM / (60 * 20);
        }
    }

    private void weight(int threshold) {
        if (this.frontConfig.getInt("radius-x") > threshold) {
            this.weightedLPM *= this.frontConfig.getInt("radius-x") / threshold;
        }

        if (this.frontConfig.getInt("radius-z") > threshold) {
            this.weightedLPM *= this.frontConfig.getInt("radius-z") / threshold;
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

        while (accumulator >= 1.0 && this.random.nextBoolean()) {
            randomStrike(world);
            this.accumulator -= 1.0;
        }
    }

    private void randomStrike(World world) {
        FrontLocation frontLocation = this.functions.randomXYInFront(this.front.getSimulator(),
                this.front.getFrontBoundaries());

        if (!frontLocation.isLoaded()) {
            return;
        }

        boolean lightningDry = this.simulatorConfig.getBoolean("lightning-in-dry-biomes");
        boolean lightningCold = this.simulatorConfig.getBoolean("lightning-in-cold-biomes");
        Block highBlock = this.blocktest.getTopLightningBlock(
                new Location(world, frontLocation.getPositionX(), 0, frontLocation.getPositionZ()));

        if (highBlock == null) {
            return;
        }

        Biome biome = highBlock.getBiome();

        if ((!this.functions.biomeIsDry(biome) && !this.functions.biomeIsCold(biome))
                || (this.functions.biomeIsDry(biome) && lightningDry)
                || (this.functions.biomeIsCold(biome) && lightningCold)) {
            world.strikeLightning(highBlock.getLocation());
        }
    }
}
