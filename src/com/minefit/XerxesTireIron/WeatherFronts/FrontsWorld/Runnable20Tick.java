package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

public class Runnable20Tick extends BukkitRunnable {

    WeatherFronts plugin;
    FrontsWorld frontsWorld;

    public Runnable20Tick(WeatherFronts instance) {
        this.plugin = instance;
    }

    public Runnable20Tick(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.frontsWorld = frontsWorld;
    }

    @Override
    public void run() {
        this.frontsWorld.updateSimulators();
    }
}
