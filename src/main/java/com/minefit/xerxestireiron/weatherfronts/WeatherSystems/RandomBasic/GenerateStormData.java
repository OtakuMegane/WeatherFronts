package com.minefit.xerxestireiron.weatherfronts.WeatherSystems.RandomBasic;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.WeatherSystems.WeatherSystem;

public class GenerateStormData {
    private final WeatherFronts plugin;
    private final YamlConfiguration stormValues;
    private final XORShiftRandom random;
    private final WeatherSystem system;
    private final YamlConfiguration systemConfig;
    private final YamlConfiguration simulatorConfig;

    public GenerateStormData(WeatherFronts instance, WeatherSystem system, YamlConfiguration config) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
        this.stormValues = config;
        this.system = system;
        this.systemConfig = system.getConfig();
        this.simulatorConfig = system.getSimulator().getSimulatorConfig();
    }

    public YamlConfiguration generateValues() {
        stormID();
        stormName();
        stormLocation();
        stormSize();
        stormVelocity();
        stormIntensity();
        stormShape();
        lightningRate();
        stormAgeLimit();
        this.stormValues.set("age", 0);
        return this.stormValues;
    }

    private void stormID() {
        String id = "";

        if (!this.stormValues.contains("id")) {
            boolean validId = false;

            while (!validId) {
                id = "storm" + this.random.nextInt(10000);

                if (!this.system.getSimulator().simulatorHasStorm(id)) {
                    validId = true;
                }
            }

            this.stormValues.set("id", id);
        }
    }

    private void stormName() {
        String name = "";

        if (this.stormValues.contains("name")) {
            name = this.stormValues.getString("name");
        } else {
            name = this.stormValues.getString("id");
        }

        this.stormValues.set("name", name);
    }

    private void stormLocation() {
        int simRadius = this.simulatorConfig.getInt("simulation-radius");

        if (!this.stormValues.contains("center-x")) {
            int minX = this.simulatorConfig.getInt("simulation-center-x") - simRadius;
            int maxX = this.simulatorConfig.getInt("simulation-center-x") + simRadius;
            this.stormValues.set("center-x", this.random.nextIntRangeInclusive(minX, maxX));
        }

        if (!this.stormValues.contains("center-z")) {
            int minZ = this.simulatorConfig.getInt("simulation-center-z") - simRadius;
            int maxZ = this.simulatorConfig.getInt("simulation-center-z") + simRadius;
            this.stormValues.set("center-z", this.random.nextIntRangeInclusive(minZ, maxZ));
        }
    }

    private void stormSize() {
        if (!this.stormValues.contains("radius-x")) {
            this.stormValues.set("radius-x", intFromMinMax("radius-x"));
        }

        if (!this.stormValues.contains("radius-z")) {
            this.stormValues.set("radius-z", intFromMinMax("radius-z"));
        }
    }

    private void stormVelocity() {
        if (!this.stormValues.contains("velocity-x")) {
            this.stormValues.set("velocity-x", intFromMinMax("velocity-x"));
        }

        if (!this.stormValues.contains("velocity-z")) {
            this.stormValues.set("velocity-z", intFromMinMax("velocity-z"));
        }
    }

    private void stormIntensity() {
        if (!this.stormValues.contains("intensity")) {
            this.stormValues.set("precipitation-intensity", intFromMinMax("precipitation-intensity"));
        }
    }

    private void stormShape() {
        if (!this.stormValues.contains("shape")) {
            this.stormValues.set("shape", "rectangle"); // Only shape right now is rectangle
        }
    }

    private void lightningRate() {
        if (!this.stormValues.contains("lightning-per-minute")) {
            if (this.random.nextInt(100) >= this.systemConfig.getInt("rain-only-chance", 80)) {
                this.stormValues.set("lightning-per-minute", intFromMinMax("lightning-per-minute"));
            } else {
                this.stormValues.set("lightning-per-minute", 0);
            }
        }
    }

    private void stormAgeLimit() {
        if (!this.stormValues.contains("age-limit")) {
            this.stormValues.set("age-limit", intFromMinMax("age-limit"));
        }
    }

    private int intFromMinMax(String setting) {
        int min = this.systemConfig.getInt("minimum-" + setting);
        int max = this.systemConfig.getInt("maximum-" + setting);
        return this.random.nextIntRangeInclusive(min, max);
    }
}
