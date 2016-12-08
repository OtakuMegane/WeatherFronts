package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontLocation extends Location {
    private Simulator simulator;
    private World world;
    private final BlockFunctions blockFunction;
    private final BiomeData biomeData;

    public FrontLocation(Simulator simulator, double x, double y, double z) {
        super(simulator.getWorld(), x, y, z);
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.blockFunction = new BlockFunctions(simulator.getPlugin(), simulator);
        this.biomeData = new BiomeData();
    }

    public FrontLocation(Simulator simulator, double x, double z) {
        this(simulator, x, 0, z);
    }

    public FrontLocation(Simulator simulator, Location location) {
        this(simulator, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public FrontLocation(Simulator simulator, Block block) {
        this(simulator, block.getLocation());
    }

    public boolean isLoaded() {
        return this.simulator.getWorld().isChunkLoaded(this.getBlockX() >> 4, this.getBlockZ() >> 4);
    }

    public boolean isInFront() {
        return inWhichFront() != null;
    }

    public String inWhichFront() {
        return this.simulator.locationInWhichFront(this.getBlockX(), this.getBlockZ());
    }

    public boolean canSpawnHostile() {
        return isInWeather() && this.simulator.getFront(inWhichFront()).spawnHostile();
    }

    public boolean isInWeather() {
        return isInFront() && this.getBlockY() >= this.blockFunction.getTopShelterBlock(this).getY();
    }

    public boolean isInRain() {
        Block block = getBlock();
        return isInWeather() && !this.biomeData.isDry(block) && !this.biomeData.isFrozen(block) && !isCold();
    }

    public boolean isInSnow() {
        Block block = getBlock();
        return isInWeather() && !this.biomeData.isDry(block) && (this.biomeData.isCold(block) || isCold());
    }

    public boolean isDry() {
        return this.biomeData.isDry(getBlock());
    }

    public Location getBukkitLocation() {
        return new Location(this.world, this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    public Block getBlock() {
        return getBukkitLocation().getBlock();
    }

    public Biome getBiome() {
        return getBlock().getBiome();
    }

    public void changeSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public boolean inSpawnChunk() {
        Location spawn = this.world.getSpawnLocation();
        return this.getBlockX() < (spawn.getX() + 128 + 8) && this.getBlockX() > (spawn.getX() - 128 - 8)
                && this.getBlockZ() < (spawn.getZ() + 128 + 8) && this.getBlockZ() > (spawn.getZ() - 128 - 8);
    }

    public boolean isCold() {
        return getTemperature() < 0.15;
    }

    public double getTemperature() {
        double temp = getBlock().getTemperature();

        // Taken from the NMS BiomeBase calculation
        if (this.getY() > 64) {
            temp = temp - (this.getY() - 64) * 0.05F / 30.0F;
        }

        return temp;
    }
}
