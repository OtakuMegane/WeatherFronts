package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld.FrontsWorld;

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
        String worldName = event.getWorld().getName();
        FrontsWorld handle = this.plugin.getWorldHandle(worldName);
        handle.saveFronts();
        handle.saveSimulators();
        this.plugin.removeWorld(worldName);
    }
}
