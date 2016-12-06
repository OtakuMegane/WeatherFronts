package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontLocation extends Location {
    private Simulator simulator;
    private World world;
    private int x;
    private int y;
    private int z;
    private final BlockTests blocktest;
    private final BiomeData biomeData;

    public FrontLocation(Simulator simulator, double x, double y, double z) {
        super(simulator.getWorld(), x, y, z);
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
        this.blocktest = new BlockTests(simulator.getPlugin(), simulator);
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

    public int getFrontX() {
        return this.x;
    }

    public int getFrontY() {
        return this.y;
    }

    public int getFrontZ() {
        return this.z;
    }

    public boolean isLoaded() {
        return this.simulator.getWorld().isChunkLoaded(this.x >> 4, this.z >> 4);
    }

    public boolean isInFront() {
        return inWhichFront() != null;
    }

    public String inWhichFront() {
        return this.simulator.locationInWhichFront(this.x, this.z);
    }

    public boolean canSpawnHostile() {
        return isInWeather() && this.simulator.getFront(inWhichFront()).spawnHostile();
    }

    public boolean isAboveground() {
        return isLoaded() && this.y >= this.blocktest.getTopBlockY(getBukkitLocation());
    }

    public boolean isInWeather() {
        return isInFront() && isAboveground();
    }

    public boolean isInRain() {
        Location location = getBukkitLocation();
        Block block = getBlock();
        return isInWeather() && !this.biomeData.isDry(block) && !this.biomeData.isFrozen(block)
                && !this.blocktest.blockIsCold(location);
    }

    public boolean isInSnow() {
        Location location = getBukkitLocation();
        Block block = getBlock();
        return isInWeather() && (this.biomeData.isCold(block) || this.blocktest.blockIsCold(location));
    }

    public Location getBukkitLocation() {
        return new Location(this.world, this.x, this.y, this.z);
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

    public void updatePosition(int x, int z) {
        updatePositionX(x);
        updatePositionZ(z);
    }

    public void updatePositionX(int x) {
        this.x = x;
    }

    public void updatePositionZ(int z) {
        this.z = z;
    }

    public int getPositionX() {
        return this.x;
    }

    public int getPositionZ() {
        return this.z;
    }

    public int[] getPosition() {
        return new int[] { x, z };
    }

    public boolean inSpawnChunk() {
        Location spawn = this.world.getSpawnLocation();
        return this.x < (spawn.getX() + 128 + 8) && this.x > (spawn.getX() - 128 - 8)
                && this.z < (spawn.getZ() + 128 + 8) && this.z > (spawn.getZ() - 128 - 8);
    }
}
