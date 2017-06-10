package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

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
        this.simulator.updateStormChunks();
        ++this.genDelay;
    }
}
