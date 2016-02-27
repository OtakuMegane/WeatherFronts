package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldListener implements Listener {
    private WeatherFronts plugin;
    private Logger logger = Logger.getLogger("Minecraft");

    public WorldListener(WeatherFronts instance) {
        plugin = instance;
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        plugin.config.loadWorld(world.getName());
    }
}
