package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class LightningGen {
    private final WeatherFronts plugin;
    private final Front front;
    private final YamlConfiguration frontConfig;
    private final YamlConfiguration simulatorConfig;
    private final YamlConfiguration systemConfig;
    private final BlockFunctions blockFunction;
    private double accumulator = 0.0;
    private double weightedLPM;
    private double lightningPerCheck;
    private boolean weighted = false;
    private final XORShiftRandom random;
    private int randomDelay = 0;

    public LightningGen(WeatherFronts instance, YamlConfiguration config, Front front) {
        this.plugin = instance;
        this.front = front;
        this.frontConfig = front.getData();
        this.simulatorConfig = config;
        this.systemConfig = front.getSimulator().getWeatherSystem().getConfig();
        this.blockFunction = new BlockFunctions(instance, front.getSimulator());
        this.random = new XORShiftRandom();
        this.weightedLPM = this.frontConfig.getInt("lightning-per-minute");
        this.randomDelay = this.frontConfig.getInt("lightning-per-minute") * 8;

        if (this.systemConfig.getBoolean("use-weighted-lightning")) {
            weight(this.systemConfig.getInt("weight-radius-threshold"));
        }

        this.lightningPerCheck = this.weightedLPM / (60 * 20);

    }

    private void weight(int threshold) {
        if (this.frontConfig.getInt("radius-x") > threshold) {
            this.weightedLPM *= (this.frontConfig.getInt("radius-x") * 2) / threshold;
            this.weighted = true;
        }

        if (this.frontConfig.getInt("radius-z") > threshold) {
            this.weightedLPM *= (this.frontConfig.getInt("radius-z") * 2) / threshold;
            this.weighted = true;
        }
    }

    public void lightningGen(World world) {
        this.accumulator += this.lightningPerCheck;

        // Add some randomness to small fronts so strikes aren't quite so predictable for players
        // Larger weighted fronts have an inherently more random experience
        if (!this.weighted) {
            if (this.random.nextBoolean()) {
                this.accumulator += this.lightningPerCheck + (this.random.nextDouble() / this.randomDelay);
            } else {
                this.accumulator -= this.lightningPerCheck + (this.random.nextDouble() / this.randomDelay);
            }
        }

        if (this.accumulator < 1.0) {
            return;
        }

        if (this.accumulator < 0) {
            this.accumulator = 0;
        }

        while (this.accumulator >= 1.0) {
            randomStrike(world);
            this.accumulator -= 1.0;
        }
    }

    private void randomStrike(World world) {
        FrontLocation location = this.front.randomLocationInFront();

        if (!location.isLoaded()) {
            return;
        }

        boolean lightningDry = this.simulatorConfig.getBoolean("lightning-in-dry-biomes");
        boolean lightningCold = this.simulatorConfig.getBoolean("lightning-in-cold-biomes");
        Block block = this.blockFunction.getTopBlockLightningValid(location);
        FrontLocation location2 = this.front.getSimulator().newFrontLocation(block);

        if ((!location2.isDry() && !location2.isCold()) || (location2.isDry() && lightningDry)
                || (location2.isCold() && lightningCold)) {
            world.strikeLightning(block.getLocation());
        }
    }
}
