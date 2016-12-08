package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

public class TickFrontUpdates extends BukkitRunnable {
    private final WeatherFronts plugin;
    private final FrontsWorld frontsWorld;
    private int genDelay;

    public TickFrontUpdates(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.frontsWorld = frontsWorld;
        this.genDelay = 0;
    }

    @Override
    public void run() {
        if (this.genDelay >= 30) {
            this.frontsWorld.autoGenFronts();
            this.genDelay = 0;
        }

        this.frontsWorld.updateSimulators();
        ++this.genDelay;
    }
}
