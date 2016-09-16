package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.World;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class LocationTests {
    private final WeatherFronts plugin;
    private final Functions functions;
    private final BlockTests blocktest;

    public LocationTests(WeatherFronts instance) {
        this.plugin = instance;
        this.functions = new Functions(instance);
        this.blocktest = new BlockTests(instance);
    }

    public Boolean locationInSpawnChunk(Location location) {
        Location spawn = location.getWorld().getSpawnLocation();
        return location.getX() < (spawn.getX() + 128 + 8) && location.getX() > (spawn.getX() - 128 - 8)
                && location.getZ() < (spawn.getZ() + 128 + 8) && location.getZ() > (spawn.getZ() - 128 - 8);
    }

    public boolean locationIsInRain(Location location) {
        return locationIsInWeather(location) && !this.functions.biomeIsDry(location.getBlock().getBiome())
                && !this.functions.biomeIsCold(location.getBlock().getBiome()) && !this.blocktest.blockIsCold(location);
    }

    public boolean locationIsInSnow(Location location) {
        return locationIsInWeather(location)
                && (this.functions.biomeIsCold(location.getBlock().getBiome()) || this.blocktest.blockIsCold(location));
    }

    public boolean locationIsInWeather(Location location) {
        return locationIsInFront(location) && locationIsAboveground(location);
    }

    public Boolean locationIsInFront(Location location) {
        return this.plugin.getWorldHandle(location.getWorld().getName()).locationInWhichFront(location.getBlockX(),
                location.getBlockZ()) != null;
    }

    public Boolean locationIsAboveground(Location location) {
        return locationIsLoaded(location) && location.getBlockY() >= this.blocktest.getTopBlockY(location);
    }

    public boolean locationChunkIsInUse(Location location) {
        return location.getWorld().isChunkInUse(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public boolean locationIsLoaded(Location location) {
        return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public boolean locationIsLoaded(World world, int x, int z) {
        return locationIsLoaded(new Location(world, x, 0, z));
    }
}
