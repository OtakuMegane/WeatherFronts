package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;
import com.minefit.XerxesTireIron.WeatherFronts.Storm.Storm;

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
