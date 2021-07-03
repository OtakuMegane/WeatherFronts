package com.minefit.xerxestireiron.weatherfronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public interface WeatherSystem {
    public YamlConfiguration loadSystemParameters();

    boolean updateStorm(Storm storm);

    public void moveStorm(Storm storm);

    public boolean shouldDie(Storm storm, Simulator simulator);

    public void ageStorm(Storm storm);

    public Storm createStorm(YamlConfiguration config);

    public YamlConfiguration getConfig();

    public Simulator getSimulator();
}
