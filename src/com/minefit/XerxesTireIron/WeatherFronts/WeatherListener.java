package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WeatherListener implements Listener {
    private WeatherFronts plugin;

    public WeatherListener(WeatherFronts instance) {
        plugin = instance;
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    @EventHandler(priority = EventPriority.NORMAL)
    public void onThunderChange(ThunderChangeEvent thunderchange) {
        if (test.worldIsEnabled(thunderchange.getWorld()) && thunderchange.toThunderState()) {
            thunderchange.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onWeatherChange(WeatherChangeEvent weatherchange) {
        if (test.worldIsEnabled(weatherchange.getWorld()) && weatherchange.toWeatherState()) {
            weatherchange.setCancelled(true);
        }
    }
}
