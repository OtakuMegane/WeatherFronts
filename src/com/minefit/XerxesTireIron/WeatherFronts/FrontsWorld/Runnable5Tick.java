package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.Map.Entry;

import org.bukkit.scheduler.BukkitRunnable;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class Runnable5Tick extends BukkitRunnable {

    private final WeatherFronts plugin;
    private final FrontsWorld frontsWorld;
    private final EntityHandler entityHandler;
    private final FireHandler fireHandler;

    public Runnable5Tick(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.frontsWorld= frontsWorld;
        this.entityHandler = new EntityHandler(instance, frontsWorld);
        this.fireHandler = new FireHandler(instance);
        this.plugin.getServer().getPluginManager().registerEvents(this.entityHandler, instance);
        this.plugin.getServer().getPluginManager().registerEvents(this.fireHandler, instance);
    }

    @Override
    public void run() {
        this.entityHandler.changePlayerWeather();
        this.entityHandler.affectEndermen();
        this.entityHandler.affectWolves();
        this.entityHandler.affectBlazes();
        this.entityHandler.affectSnowmen();
        this.entityHandler.affectArrows();
        this.fireHandler.extinguishFire();

        for (Entry<String, Simulator> entry : this.frontsWorld.getSimulatorList().entrySet()) {
            for (Entry<String, Front> entry2 : entry.getValue().getFronts().entrySet()) {
                entry2.getValue().genLightning();
                entry2.getValue().precipitationEffects();
            }
        }
    }
}
