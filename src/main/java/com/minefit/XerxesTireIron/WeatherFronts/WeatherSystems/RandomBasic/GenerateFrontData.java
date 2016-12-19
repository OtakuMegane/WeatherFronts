package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.RandomBasic;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;

public class GenerateFrontData {
    private final WeatherFronts plugin;
    private final YamlConfiguration frontValues;
    private final XORShiftRandom random;
    private final WeatherSystem system;
    private final YamlConfiguration systemConfig;
    private final YamlConfiguration simulatorConfig;

    public GenerateFrontData(WeatherFronts instance, WeatherSystem system, YamlConfiguration config) {
        this.plugin = instance;
        this.random = new XORShiftRandom();
        this.frontValues = config;
        this.system = system;
        this.systemConfig = system.getConfig();
        this.simulatorConfig = system.getSimulator().getSimulatorConfig();
    }

    public YamlConfiguration generateValues() {
        frontName();
        frontLocation();
        frontSize();
        frontVelocity();
        frontIntensity();
        frontShape();
        lightningRate();
        frontAgeLimit();
        this.frontValues.set("age", 0);
        return this.frontValues;
    }

    private void frontName() {
        String name = "";

        if (!this.frontValues.contains("name")) {
            boolean validId = false;

            while (!validId) {
                name = "front" + this.random.nextInt(1000);

                if (!this.system.getSimulator().simulatorHasFront(name)) {
                    validId = true;
                }
            }

            this.frontValues.set("name", name);
        }
    }

    private void frontLocation() {
        int simRadius = this.simulatorConfig.getInt("simulation-radius");

        if (!this.frontValues.contains("center-x")) {
            int minX = this.simulatorConfig.getInt("simulation-center-x") - simRadius;
            int maxX = this.simulatorConfig.getInt("simulation-center-x") + simRadius;
            this.frontValues.set("center-x", this.random.nextIntRangeInclusive(minX, maxX));
        }

        if (!this.frontValues.contains("center-z")) {
            int minZ = this.simulatorConfig.getInt("simulation-center-z") - simRadius;
            int maxZ = this.simulatorConfig.getInt("simulation-center-z") + simRadius;
            this.frontValues.set("center-z", this.random.nextIntRangeInclusive(minZ, maxZ));
        }
    }

    private void frontSize() {
        if (!this.frontValues.contains("radius-x")) {
            this.frontValues.set("radius-x", intFromMinMax("radius-x"));
        }

        if (!this.frontValues.contains("radius-z")) {
            this.frontValues.set("radius-z", intFromMinMax("radius-z"));
        }
    }

    private void frontVelocity() {
        if (!this.frontValues.contains("velocity-x")) {
            this.frontValues.set("velocity-x", intFromMinMax("velocity-x"));
        }

        if (!this.frontValues.contains("velocity-z")) {
            this.frontValues.set("velocity-z", intFromMinMax("velocity-z"));
        }
    }

    private void frontIntensity() {
        if (!this.frontValues.contains("intensity")) {
            this.frontValues.set("precipitation-intensity", intFromMinMax("precipitation-intensity"));
        }
    }

    private void frontShape() {
        if (!this.frontValues.contains("shape")) {
            this.frontValues.set("shape", "rectangle"); // Only shape right now is rectangle
        }
    }

    private void lightningRate() {
        if (!this.frontValues.contains("lightning-per-minute")) {
            this.frontValues.set("lightning-per-minute", intFromMinMax("lightning-per-minute"));
        }
    }

    private void frontAgeLimit() {
        if (!this.frontValues.contains("age-limit")) {
            this.frontValues.set("age-limit", intFromMinMax("age-limit"));
        }
    }

    private int intFromMinMax(String setting) {
        int min = this.systemConfig.getInt("minimum-" + setting);
        int max = this.systemConfig.getInt("maximum-" + setting);
        return this.random.nextIntRangeInclusive(min, max);
    }
}
