package com.minefit.xerxestireiron.weatherfronts.WeatherSystems.RandomBasic;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.LoadData;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;
import com.minefit.xerxestireiron.weatherfronts.WeatherSystems.WeatherSystem;

public class RandomBasic implements WeatherSystem {
    private final WeatherFronts plugin;
    private final Simulator simulator;
    private final LoadData loadData;
    private final YamlConfiguration systemConfig;
    private final String systemId;

    public RandomBasic(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.simulator = simulator;
        this.loadData = new LoadData(instance);
        this.systemId = simulator.getSimulatorConfig().getString("weather-system");
        this.systemConfig = loadSystemParameters();
    }

    @Override
    public YamlConfiguration loadSystemParameters() {
        YamlConfiguration config = loadData.loadConfigForWorld(simulator.getWorld().getName(), "weather-systems.yml",
                true);
        YamlConfiguration defaults = loadData.loadConfigForWorld(simulator.getWorld().getName(),
                "random-basic-defaults.yml", true);
        return this.loadData.combineConfigDefaults(this.systemId, defaults, config);
    }

    @Override
    public boolean updateStorm(Storm storm)
    {
        moveStorm(storm);
        ageStorm(storm);
        return shouldDie(storm, this.simulator);
    }

    @Override
    public void moveStorm(Storm storm) {
        YamlConfiguration stormData = storm.getData();
        storm.updatePosition(stormData.getInt("center-x") + stormData.getInt("velocity-x"),
                stormData.getInt("center-z") + stormData.getInt("velocity-z"));
    }

    @Override
    public boolean shouldDie(Storm storm, Simulator simulator) {
        int ageLimit = storm.ageLimit();
        int age = storm.currentAge();

        if (age > ageLimit && ageLimit != 0) {
            return true;
        }

        if (!simulator.isInSimulator(storm.getFrontsLocation())) {
            return true;
        }

        return false;
    }

    @Override
    public void ageStorm(Storm storm) {
        int age = storm.currentAge() + 1;
        storm.changeAge(age);
    }

    @Override
    public Storm createStorm(YamlConfiguration config) {
        GenerateStormData generate = new GenerateStormData(this.plugin, this, config);
        Storm storm = new Storm(this.plugin, this.simulator, generate.generateValues());
        return storm;
    }

    @Override
    public YamlConfiguration getConfig() {
        return this.systemConfig;
    }

    @Override
    public Simulator getSimulator() {
        return this.simulator;
    }

}
