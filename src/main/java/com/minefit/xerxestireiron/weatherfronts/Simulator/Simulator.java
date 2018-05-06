package com.minefit.xerxestireiron.weatherfronts.Simulator;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.minefit.xerxestireiron.weatherfronts.DynmapFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontLocation;
import com.minefit.xerxestireiron.weatherfronts.LoadData;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;
import com.minefit.xerxestireiron.weatherfronts.WeatherSystems.WeatherSystem;
import com.minefit.xerxestireiron.weatherfronts.WeatherSystems.RandomBasic.RandomBasic;

public class Simulator {
    private final World world;
    private final YamlConfiguration simulatorConfig;
    private final WeatherFronts plugin;
    private String name;
    private final String id;
    private final ConcurrentMap<String, Storm> storms = new ConcurrentHashMap<>();
    private final LoadData loadData;
    private final DynmapFunctions dynmap;
    private final WeatherSystem system;
    private final BukkitTask mainTickCycle;
    private final BukkitTask tickUpdates;

    public Simulator(World world, WeatherFronts instance, YamlConfiguration config, String id) {
        this.simulatorConfig = config;
        this.plugin = instance;
        this.world = world;
        this.name = config.getString("name", "");
        this.loadData = new LoadData(instance);
        this.dynmap = this.plugin.getDynmap();
        this.system = new RandomBasic(instance, this);
        this.id = id;
        loadStorms();
        this.mainTickCycle = new MainTickCycle(instance, this).runTaskTimer(instance, 0, 1);
        this.tickUpdates = new TickUpdates(instance, this).runTaskTimer(instance, 0, 20);
    }

    public World getWorld() {
        return this.world;
    }

    public String getID() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Storm> getStorms() {
        return this.storms;
    }

    public WeatherFronts getPlugin() {
        return this.plugin;
    }

    public WeatherSystem getWeatherSystem() {
        return this.system;
    }

    public FrontLocation newFrontLocation(double x, double y, double z) {
        return new FrontLocation(this, x, y, z);
    }

    public FrontLocation newFrontLocation(Location location) {
        return new FrontLocation(this, location);
    }

    public FrontLocation newFrontLocation(Block block) {
        return new FrontLocation(this, block);
    }

    public String locationInWhichStorm(int x, int z) {
        if (isInSimulator(x, z)) {
            for (Entry<String, Storm> entry : this.storms.entrySet()) {
                if (entry.getValue().isInStorm(x, z)) {
                    return entry.getValue().getName();
                }
            }
        }

        return null;
    }

    public String locationInWhichStorm(FrontLocation location) {
        return locationInWhichStorm(location.getBlockX(), location.getBlockZ());
    }

    public void updateStorms() {
        for (Entry<String, Storm> storm : this.storms.entrySet()) {
            storm.getValue().update();
            boolean dead = this.system.updateFront(storm.getValue());

            if (dead) {
                removeStorm(storm.getKey());
            }
        }
    }

    private void loadStorms() {
        File[] stormFiles = this.loadData.getListOfStormFiles(this.world.getName(), this.id);

        for (File stormFile : stormFiles) {
            YamlConfiguration stormData = YamlConfiguration.loadConfiguration(stormFile);
            this.storms.put(stormData.getString("id"), new Storm(this.plugin, this, stormData));
        }
    }

    public void saveStorms() {
        for (Entry<String, Storm> storm : this.storms.entrySet()) {
            storm.getValue().save();
        }
    }

    public void addStorm(Storm storm) {
        this.storms.put(storm.getName(), storm);
    }

    public Storm createStorm(YamlConfiguration config, boolean command, boolean autogen) {
        if (canCreateStorm(command, autogen)) {
            Storm front = this.system.createFront(config);
            addStorm(front);
            return front;
        }

        return null;
    }

    public void removeStorm(String stormName) {
        this.storms.get(stormName).die();
        this.storms.remove(stormName);
        this.dynmap.deleteMarker(world, this.id, stormName);
    }

    private boolean canCreateStorm(boolean command, boolean autogen) {
        int frontsMax = this.simulatorConfig.getInt("maximum-fronts", 5);
        boolean autogenActive = this.system.getConfig().getBoolean("generate-fronts", true);

        if (autogen && !autogenActive) {
            return false;
        }

        if (this.storms.size() >= frontsMax && !command) {
            if (this.simulatorConfig.getBoolean("unending-does-not-count", true)) {
                int permanent = 0;

                for (Entry<String, Storm> entry : this.storms.entrySet()) {
                    if (entry.getValue().isPermanent()) {
                        permanent += 1;
                    }
                }

                if (this.storms.size() - permanent < frontsMax) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    public boolean simulatorHasFront(String frontName) {
        return this.storms.containsKey(frontName);
    }

    public Storm getFront(String frontName) {
        return this.storms.get(frontName);
    }

    public boolean renameFront(String originalName, String newName) {
        if (simulatorHasFront(originalName) && newName != null) {
            Storm front = this.storms.get(originalName);
            front.changeName(newName);
            this.storms.put(front.getName(), front);
            return true;
        }

        return false;
    }

    public YamlConfiguration getSimulatorConfig() {
        return this.simulatorConfig;
    }

    public YamlConfiguration getFrontData(String frontName) {
        YamlConfiguration data = new YamlConfiguration();

        if (frontName != null && this.storms.containsKey(frontName)) {
            data = this.storms.get(frontName).getData();
        }

        return data;
    }

    public boolean isInSimulator(int x, int z) {
        int sx = this.simulatorConfig.getInt("simulation-center-x");
        int sz = this.simulatorConfig.getInt("simulation-center-z");
        int sr = this.simulatorConfig.getInt("simulation-radius");

        if (x > sx + sr || x < sx - sr || z > sz + sr || z < sz - sr) {
            return false;
        }

        return true;
    }

    public boolean isInSimulator(FrontLocation location) {
        return isInSimulator(location.getBlockX(), location.getBlockZ());
    }

    public void shutdown() {
        this.mainTickCycle.cancel();
        this.tickUpdates.cancel();
    }
}
