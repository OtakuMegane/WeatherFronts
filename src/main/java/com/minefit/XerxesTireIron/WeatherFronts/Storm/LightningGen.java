package com.minefit.XerxesTireIron.WeatherFronts.Storm;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class LightningGen {
    private final WeatherFronts plugin;
    private final Storm storm;
    private final YamlConfiguration stormConfig;
    private final YamlConfiguration simulatorConfig;
    private final YamlConfiguration systemConfig;
    private final BlockFunctions blockFunction;
    private double accumulator = 0.0;
    private double baseLPM;
    private double weightedLPM;
    private double lightningPerCheck;
    private boolean weighted = false;
    private final XORShiftRandom random;

    public LightningGen(WeatherFronts instance, YamlConfiguration config, Storm storm) {
        this.plugin = instance;
        this.storm = storm;
        this.stormConfig = storm.getData();
        this.simulatorConfig = config;
        this.systemConfig = storm.getSimulator().getWeatherSystem().getConfig();
        this.blockFunction = new BlockFunctions(instance, storm.getSimulator());
        this.random = new XORShiftRandom();
        this.baseLPM = this.stormConfig.getInt("lightning-per-minute");
        this.weightedLPM = this.baseLPM;

        if (this.systemConfig.getBoolean("use-weighted-lightning", true)) {
            weight(this.systemConfig.getInt("weight-radius-threshold", 192));
        }

        this.lightningPerCheck = this.weightedLPM / (60 * 20);

    }

    private void weight(int threshold) {
        if (this.stormConfig.getInt("radius-x") > threshold) {
            this.weightedLPM *= this.stormConfig.getInt("radius-x") / threshold;
            this.weighted = true;
        }

        if (this.stormConfig.getInt("radius-z") > threshold) {
            this.weightedLPM *= this.stormConfig.getInt("radius-z") / threshold;
            this.weighted = true;
        }
    }

    public void lightningGen(World world) {
        this.accumulator += this.lightningPerCheck;

        // Add some randomness to small fronts so strikes aren't quite so predictable
        // Larger weighted fronts have an inherently more random experience
        if (!this.weighted) {
            int randomDelay = (int) (this.baseLPM * 8);

            if (this.random.nextBoolean()) {
                this.accumulator += this.lightningPerCheck + (this.random.nextDouble() / randomDelay);
            } else {
                this.accumulator -= this.lightningPerCheck + (this.random.nextDouble() / randomDelay);
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
        FrontLocation location = this.storm.randomLocationInFront();

        if (!location.isLoaded()) {
            return;
        }

        boolean lightningDry = this.simulatorConfig.getBoolean("lightning-in-dry-biomes", false);
        boolean lightningCold = this.simulatorConfig.getBoolean("lightning-in-cold-biomes", false);
        Block block = this.blockFunction.getTopBlockLightningValid(location);

        if ((!this.blockFunction.isDry(block) && !this.blockFunction.isCold(block))
                || (this.blockFunction.isDry(block) && lightningDry)
                || (this.blockFunction.isCold(block) && lightningCold)) {
            world.strikeLightning(block.getLocation());
        }
    }
}
