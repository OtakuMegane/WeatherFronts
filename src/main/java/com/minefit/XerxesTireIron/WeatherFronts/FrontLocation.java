package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontLocation extends Location {
    private Simulator simulator;
    private World world;

    public FrontLocation(Simulator simulator, double x, double y, double z) {
        super(simulator.getWorld(), x, y, z);
        this.simulator = simulator;
        this.world = simulator.getWorld();
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
        return this.getBlockX() < (spawn.getX() + 128 + 8) && this.getBlockX() > (spawn.getX() - 128 - 8)
                && this.getBlockZ() < (spawn.getZ() + 128 + 8) && this.getBlockZ() > (spawn.getZ() - 128 - 8);
    }

    public Block getBlock() {
        return getBukkitLocation().getBlock();
    }
}
