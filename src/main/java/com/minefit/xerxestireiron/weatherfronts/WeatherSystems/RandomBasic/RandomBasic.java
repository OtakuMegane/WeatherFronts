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
    public boolean updateFront(Storm front)
    {
        moveFront(front);
        ageFront(front);
        return shouldDie(front, this.simulator);
    }

    @Override
    public void moveFront(Storm front) {
        YamlConfiguration frontData = front.getData();
        front.updatePosition(frontData.getInt("center-x") + frontData.getInt("velocity-x"),
                frontData.getInt("center-z") + frontData.getInt("velocity-z"));
    }

    @Override
    public boolean shouldDie(Storm front, Simulator simulator) {
        int ageLimit = front.ageLimit();
        int age = front.currentAge();

        if (age > ageLimit && ageLimit != 0) {
            return true;
        }

        if (!simulator.isInSimulator(front.getFrontLocation())) {
            return true;
        }

        return false;
    }

    @Override
    public void ageFront(Storm front) {
        int age = front.currentAge() + 1;
        front.changeAge(age);
    }

    @Override
    public Storm createFront(YamlConfiguration config) {
        GenerateStormData generate = new GenerateStormData(this.plugin, this, config);
        Storm front = new Storm(this.plugin, this.simulator, generate.generateValues());
        return front;
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
