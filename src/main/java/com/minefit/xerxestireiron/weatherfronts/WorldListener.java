package com.minefit.xerxestireiron.weatherfronts;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.minefit.xerxestireiron.weatherfronts.FrontsWorld.FrontsWorld;

public class WorldListener implements Listener {
    private WeatherFronts plugin;

    public WorldListener(WeatherFronts instance) {
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        this.plugin.addWorld(world);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldUnload(WorldUnloadEvent event) {
        World world = event.getWorld();

        if (!this.plugin.worldEnabled(world)) {
            return;
        }

        String worldName = world.getName();
        FrontsWorld handle = this.plugin.getWorldHandle(worldName);
        handle.saveStorms();
        handle.saveSimulators();
        this.plugin.removeWorld(worldName);
    }
}
