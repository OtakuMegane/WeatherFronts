package com.minefit.xerxestireiron.weatherfronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public interface WeatherSystem {
    public YamlConfiguration loadSystemParameters();

    boolean updateFront(Storm storm);

    public void moveFront(Storm storm);

    public boolean shouldDie(Storm storm, Simulator simulator);

    public void ageFront(Storm storm);

    public Storm createFront(YamlConfiguration config);

    public YamlConfiguration getConfig();

    public Simulator getSimulator();
}
