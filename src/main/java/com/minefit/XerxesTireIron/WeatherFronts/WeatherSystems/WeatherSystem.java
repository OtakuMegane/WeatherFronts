package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public interface WeatherSystem {
    public YamlConfiguration loadSystemParameters();

    boolean updateFront(Front front);

    public void moveFront(Front front);

    public boolean shouldDie(Front front, Simulator simulator);

    public void ageFront(Front front);

    public Front createFront(YamlConfiguration config);

    public YamlConfiguration getConfig();

    public Simulator getSimulator();
}
