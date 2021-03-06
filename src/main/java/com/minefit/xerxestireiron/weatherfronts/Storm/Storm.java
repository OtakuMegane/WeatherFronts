package com.minefit.xerxestireiron.weatherfronts.Storm;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.DynmapFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.SaveData;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;

public class Storm {

    private final World world;
    private final WeatherFronts plugin;
    private final YamlConfiguration data;
    private String name;
    private final String id;
    private final Simulator simulator;
    private final LightningGen lightning;
    private final DynmapFunctions dynmap;
    private boolean hostileSpawn;
    private final Point2D[] boundaries;
    private Map<Chunk, Boolean> stormChunks;
    private final ChunkTick chunkTick;
    private final StormListener listener;
    private final XORShiftRandom random;
    private boolean hasLightning;
    private final SaveData save;
    private int intracloudPercentage;

    public Storm(WeatherFronts instance, Simulator simulator, YamlConfiguration data) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.data = data;
        this.id = data.getString("id");
        this.name = data.getString("name");
        this.lightning = new LightningGen(instance, simulator.getSimulatorConfig(), this);
        this.hostileSpawn = data.getInt("lightning-per-minute") > 0;
        this.dynmap = this.plugin.getDynmap();
        this.boundaries = new Point2D[4];
        this.stormChunks = new HashMap<>();
        this.chunkTick = new ChunkTick(instance, this);
        this.listener = new StormListener(instance, this);
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        this.random = new XORShiftRandom();
        this.hasLightning = this.data.getInt("lightning-per-minute") >= 0;
        this.intracloudPercentage = this.data.getInt("intracloud-percentage");
        updateStormBoundaries();
        this.dynmap.addMarker(this.world.getName(), this.name, getStormBoundaries());
        updateStormChunks();
        this.save = new SaveData(instance);
    }

    public YamlConfiguration getData() {
        return this.data;
    }

    public void save() {
        String file_separator = System.getProperty("file.separator");
        this.save.saveToYamlFile(
                this.world.getName() + file_separator + this.simulator.getID() + file_separator + "storms",
                this.id + ".yml", this.data);
    }

    public void die() {
        String file_separator = System.getProperty("file.separator");
        this.save.removeFile(this.world.getName() + file_separator + this.simulator.getID() + file_separator + "storms",
                this.id + ".yml");
    }

    public FrontsLocation getFrontsLocation() {
        return new FrontsLocation(this.simulator, this.data.getInt("center-x"), this.data.getInt("center-z"));
    }

    public void updatePosition(int x, int z) {
        this.data.set("center-x", x);
        this.data.set("center-z", z);
    }

    public String getID() {
        return this.id;
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

    public int intracloudPecentage() {
        return this.intracloudPercentage;
    }

    public void update() {
        updateStormBoundaries();
        updateStormChunks();
        this.dynmap.moveMarker(this.world.getName(), this.name, this.boundaries);
        this.hostileSpawn = data.getInt("lightning-per-minute") > 0;
    }

    public void genLightning() {
        if (this.hasLightning) {
            this.lightning.lightningGen(world);
        }
    }

    public boolean hasLightning() {
        return this.hasLightning;
    }

    public String changeName(String newName) {

        if (newName != null) {
            this.name = newName;
        }

        return this.name;
    }

    public Point2D[] getStormBoundaries() {
        return this.boundaries;
    }

    private void updateStormBoundaries() {
        // Low X, Low Z
        this.boundaries[0] = new Point2D.Double(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        // High X, Low Z
        this.boundaries[1] = new Point2D.Double(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        // High X, High Z
        this.boundaries[2] = new Point2D.Double(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
        // Low X, High Z
        this.boundaries[3] = new Point2D.Double(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
    }

    public int getStormArea() {
        return (this.data.getInt("radius-x") * 2) * (this.data.getInt("radius-z") * 2);
    }

    public boolean isInStorm(int x, int z) {
        Point2D[] boundaries = getStormBoundaries();
        return x > boundaries[0].getX() && x < boundaries[1].getX() && z > boundaries[1].getY()
                && z < boundaries[2].getY();
    }

    public boolean isInStorm(double x, double z) {
        return isInStorm((int) x, (int) z);
    }

    public boolean isInStorm(FrontsLocation location) {
        return isInStorm(location.getBlockX(), location.getBlockZ());
    }

    public boolean isInRangeOf(int x, int z, int range) {
        Point2D[] boundaries = getStormBoundaries();
        return x > boundaries[0].getX() - range && x < boundaries[1].getX() + range && z > boundaries[1].getY() - range
                && z < boundaries[2].getY() + range;
    }

    public boolean isInRangeOf(double x, double z, int range) {
        return isInRangeOf((int) x, (int) z, range);
    }

    public int getPrecipitationIntensity() {
        return this.data.getInt("precipitation-intensity");
    }

    public Map<Chunk, Boolean> getStormChunks() {
        return this.stormChunks;
    }

    public void updateStormChunks() {
        Point2D[] boundaries = getStormBoundaries();
        double frontLowX = boundaries[0].getX();
        double frontHighX = boundaries[2].getX();
        double frontLowZ = boundaries[0].getY();
        double frontHighZ = boundaries[2].getY();
        this.stormChunks.clear();

        for (Chunk chunk : this.world.getLoadedChunks()) {
            int chunkX = chunk.getX();
            int chunkZ = chunk.getZ();
            int chunkLowX = chunkX << 4;
            int chunkHighX = chunkLowX + 15;
            int chunkLowZ = chunkZ << 4;
            int chunkHighZ = chunkLowX + 15;

            if (chunkLowX < frontHighX && chunkHighX > frontLowX && chunkLowZ < frontHighZ && chunkHighZ > frontLowZ) {
                boolean isPlayerChunk = false;

                if (this.world.isChunkInUse(chunkX, chunkZ)) {
                    isPlayerChunk = true;
                }

                this.stormChunks.put(chunk, isPlayerChunk);
            }
        }
    }

    public void tickStormChunks() {
        this.chunkTick.tickDispatch();
    }

    public FrontsLocation randomLocationInStorm() {
        double x = this.random.nextIntRangeInclusive(boundaries[0].getX(), boundaries[1].getX());
        double z = this.random.nextIntRangeInclusive(boundaries[1].getY(), boundaries[2].getY());
        return new FrontsLocation(simulator, x, z);
    }
}
