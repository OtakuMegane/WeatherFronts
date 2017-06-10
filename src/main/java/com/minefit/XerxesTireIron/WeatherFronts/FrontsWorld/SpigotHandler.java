package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import org.bukkit.World;
import org.spigotmc.SpigotWorldConfig;

public class SpigotHandler {
    private SpigotWorldConfig spigotWorldConfig;
    public final int mobSpawnRange;

    public SpigotHandler(World world) {
        this.spigotWorldConfig = new SpigotWorldConfig(world.getName());
        this.mobSpawnRange = this.spigotWorldConfig.mobSpawnRange;
    }

    public SpigotWorldConfig getSpigotWorldConfig() {
        return this.spigotWorldConfig;
    }
}
