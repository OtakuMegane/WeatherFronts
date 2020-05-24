package com.minefit.xerxestireiron.weatherfronts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public class FrontsLocation extends Location {
    private Simulator simulator;
    private World world;
    private BlockFunctions blockFunction;

    public FrontsLocation(Simulator simulator, double x, double y, double z) {
        super(simulator.getWorld(), x, y, z);
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.blockFunction = new BlockFunctions(simulator.getPlugin(), simulator);
    }

    public FrontsLocation(Simulator simulator, double x, double z) {
        this(simulator, x, 0, z);
    }

    public FrontsLocation(Simulator simulator, Location location) {
        this(simulator, location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public FrontsLocation(Simulator simulator, Block block) {
        this(simulator, block.getLocation());
    }

    public boolean isLoaded() {
        return this.simulator.getWorld().isChunkLoaded(this.getBlockX() >> 4, this.getBlockZ() >> 4);
    }

    public boolean isInWeather() {
        if (this.isLoaded()) {
            return this.blockFunction.isInWeather(this.getBlock());
        }

        return false;
    }

    public boolean isInRain() {
        if (this.isLoaded()) {
            return this.blockFunction.isInRain(this.getBlock());
        }

        return false;
    }

    public boolean isExposedToSky() {
        if (this.isLoaded()) {
            return this.blockFunction.isExposedToSky(this.getBlock());
        }

        return false;
    }

    public boolean isInStorm() {
        return inWhichStorm() != null;
    }

    public String inWhichStorm() {
        return this.simulator.locationInWhichStorm(this.getBlockX(), this.getBlockZ());
    }

    public Storm getStorm() {
        if (this.isInStorm()) {
            return this.simulator.getStorm(inWhichStorm());
        }

        return null;
    }

    public Location getBukkitLocation() {
        return new Location(this.world, this.getBlockX(), this.getBlockY(), this.getBlockZ());
    }

    public void changeSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public boolean inSpawnChunk() {
        Location spawn = this.world.getSpawnLocation();
        // There is a radius of 22 chunks (352 blocks) from the spawn chunk that are always loaded
        int spawnChunkBlockRadius = 352;
        return this.getBlockX() < (spawn.getX() + spawnChunkBlockRadius + 8) && this.getBlockX() > (spawn.getX() - spawnChunkBlockRadius - 8)
                && this.getBlockZ() < (spawn.getZ() + spawnChunkBlockRadius + 8) && this.getBlockZ() > (spawn.getZ() - spawnChunkBlockRadius - 8);
    }

    public Block getBlock() {
        return getBukkitLocation().getBlock();
    }
}
