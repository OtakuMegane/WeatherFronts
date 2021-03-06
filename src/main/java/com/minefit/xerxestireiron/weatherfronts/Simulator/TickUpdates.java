package com.minefit.xerxestireiron.weatherfronts.Simulator;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;

public class TickUpdates extends BukkitRunnable {
    private final WeatherFronts plugin;
    private final Simulator simulator;
    private int genDelay;

    public TickUpdates(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.simulator = simulator;
        this.genDelay = 0;
    }

    @Override
    public void run() {
        if (this.genDelay >= 30) {
            this.simulator.createStorm(new YamlConfiguration(), false, true);
            this.genDelay = 0;
        }

        this.simulator.updateStorms();
        ++this.genDelay;
    }
}
