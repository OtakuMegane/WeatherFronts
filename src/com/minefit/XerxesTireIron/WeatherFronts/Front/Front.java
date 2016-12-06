package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.DynmapFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;

public class Front {

    private final World world;
    private final WeatherFronts plugin;
    private final YamlConfiguration data;
    private String name;
    private final Simulator simulator;
    private final LightningGen lightning;
    private final DynmapFunctions dynmap;
    private boolean hostileSpawn;
    private WeatherSystem system;
    private Point2D[] boundaries;
    private Set<Chunk> frontChunks;
    private final ChunkTick chunkTick;
    private final FrontListener listener;

    public Front(WeatherFronts instance, Simulator simulator, YamlConfiguration data, String name) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.data = data;
        this.name = data.getString("name");
        this.lightning = new LightningGen(instance, simulator.getSimulatorConfig(), this);
        this.hostileSpawn = data.getInt("lightning-per-minute") > 0;
        this.dynmap = this.plugin.getDynmap();
        this.dynmap.addMarker(this.world.getName(), this.name, getFrontBoundaries());
        this.boundaries = new Point2D[4];
        this.frontChunks = new HashSet<Chunk>();
        this.chunkTick = new ChunkTick(instance, this);
        this.listener = new FrontListener(instance, this);
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        updateFrontBoundaries();
        updateFrontChunks();
    }

    public YamlConfiguration getData() {
        return this.data;
    }

    public FrontLocation getFrontLocation() {
        return new FrontLocation(this.simulator, this.data.getInt("center-x"), this.data.getInt("center-z"));
    }

    public void updatePosition(int x, int z) {
        this.data.set("center-x", x);
        this.data.set("center-z", z);
    }

    public String getName() {
        return this.name;
    }

    public World getWorld() {
        return this.world;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public int ageLimit() {
        return this.data.getInt("age-limit");
    }

    public int currentAge() {
        return this.data.getInt("age");
    }

    public void changeAge(int age) {
        this.data.set("age", age);
    }

    public boolean isPermanent() {
        return this.data.getInt("age-limit") == 0;
    }

    public boolean spawnHostile() {
        return this.hostileSpawn;
    }

    public void update() {
        updateFrontBoundaries();
        updateFrontChunks();
        this.dynmap.moveMarker(this.world.getName(), this.name, this.boundaries);
        this.hostileSpawn = data.getInt("lightning-per-minute") > 0;
    }

    public void genLightning() {
        this.lightning.lightningGen(world);
    }

    /*public void precipitationEffects() {
        this.precipitation.hydrateFarmland();
        this.precipitation.precipitationBlockEffects();
    }*/

    public String changeName(String newName) {

        if (newName != null) {
            this.name = newName;
        }

        return this.name;
    }

    public Point2D[] getFrontBoundaries() {
        return this.boundaries;
    }

    private void updateFrontBoundaries() {
        this.boundaries[0] = new Point2D.Double(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        this.boundaries[1] = new Point2D.Double(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        this.boundaries[2] = new Point2D.Double(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
        this.boundaries[3] = new Point2D.Double(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
    }

    public int getFrontArea() {
        return (this.data.getInt("radius-x") * 2) * (this.data.getInt("radius-z") * 2);
    }

    public boolean isInFront(int x, int z) {
        Point2D[] boundaries = getFrontBoundaries();
        return x > boundaries[0].getX() && x < boundaries[1].getX() && z > boundaries[1].getY()
                && z < boundaries[2].getY();
    }

    public boolean isInFront(FrontLocation location) {
        return isInFront(location.getBlockX(), location.getBlockZ());
    }

    public int getPrecipitationIntensity() {
        return this.data.getInt("precipitation-intensity");
    }

    public Set<Chunk> getFrontChunks() {
        return this.frontChunks;
    }

    public void updateFrontChunks() {
        Chunk[] loadedChunks = this.world.getLoadedChunks();

        for (Chunk chunk : loadedChunks) {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            int blockX = chunkX * 16;
            int blockZ = chunkZ * 16;
            FrontLocation[] locations = new FrontLocation[5];
            locations[0] = new FrontLocation(this.simulator, blockX + 8, blockZ + 8); // Center
            locations[1] = new FrontLocation(this.simulator, blockX, blockZ); // NW
            locations[2] = new FrontLocation(this.simulator, blockX + 15, blockZ); // SW
            locations[3] = new FrontLocation(this.simulator, blockX + 15, blockZ + 15); // SE
            locations[4] = new FrontLocation(this.simulator, blockX, blockZ + 15); // NE

            for (FrontLocation location : locations) {
                if (isInFront(location)) {
                    this.frontChunks.add(chunk);
                    break;
                }
            }
        }
    }

    public void tickFrontChunks() {
        this.chunkTick.tickDispatch();
    }

}
