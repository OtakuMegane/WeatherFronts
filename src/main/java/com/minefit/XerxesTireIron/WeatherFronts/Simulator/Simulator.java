package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.minefit.XerxesTireIron.WeatherFronts.DynmapFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.LoadData;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.RandomBasic.RandomBasic;

public class Simulator {
    private final World world;
    private final YamlConfiguration simulatorConfig;
    private final WeatherFronts plugin;
    private String name;
    private final ConcurrentMap<String, Front> fronts = new ConcurrentHashMap<>();
    private final LoadData loadData;
    private final DynmapFunctions dynmap;
    private final WeatherSystem system;
    private final BukkitTask mainTickCycle;
    private final BukkitTask tickUpdates;

    public Simulator(World world, WeatherFronts instance, YamlConfiguration config, String name) {
        this.simulatorConfig = config;
        this.plugin = instance;
        this.world = world;
        this.name = name;
        this.loadData = new LoadData(instance);
        this.dynmap = this.plugin.getDynmap();
        this.system = new RandomBasic(instance, this);
        loadFronts();
        this.mainTickCycle = new MainTickCycle(instance, this).runTaskTimer(instance, 0, 1);
        this.tickUpdates = new TickUpdates(instance, this).runTaskTimer(instance, 0, 20);

    }

    public World getWorld() {
        return this.world;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, Front> getFronts() {
        return this.fronts;
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

    public String locationInWhichFront(int x, int z) {
        if (isInSimulator(x, z)) {
            for (Entry<String, Front> entry : this.fronts.entrySet()) {
                if (entry.getValue().isInFront(x, z)) {
                    return entry.getValue().getName();
                }
            }
        }

        return null;
    }

    public String locationInWhichFront(FrontLocation location) {
        return locationInWhichFront(location.getBlockX(), location.getBlockZ());
    }

    public void updateFronts() {
        for (Entry<String, Front> entry : this.fronts.entrySet()) {
            Front front = entry.getValue();
            front.update();
            boolean dead = this.system.updateFront(front);

            if (dead) {
                removeFront(entry.getKey());
            }
        }
    }

    private void loadFronts() {
        YamlConfiguration fronts = this.loadData.loadConfigForWorld(world.getName(), "fronts.yml", false);
        YamlConfiguration simulatorFronts = this.loadData.getSectionAsConfig(fronts, getName());

        if (fronts.getKeys(false).size() == 0) {
            return;
        }

        for (String frontName : simulatorFronts.getKeys(false)) {
            YamlConfiguration frontData = this.loadData.getSectionAsConfig(simulatorFronts, frontName);
            this.fronts.put(frontName, new Front(this.plugin, this, frontData));
        }
    }

    public void addFront(Front front) {
        this.fronts.put(front.getName(), front);
    }

    public Front createFront(YamlConfiguration config, boolean command, boolean autogen) {
        if (canCreateFront(command, autogen)) {
            Front front = this.system.createFront(config);
            addFront(front);
            return front;
        }

        return null;
    }

    public void removeFront(String frontName) {
        this.fronts.remove(frontName);
        this.dynmap.deleteMarker(world, this.name, frontName);
    }

    private boolean canCreateFront(boolean command, boolean autogen) {
        int frontsMax = this.simulatorConfig.getInt("maximum-fronts");
        boolean autogenActive = this.system.getConfig().getBoolean("generate-fronts");

        if (autogen && !autogenActive) {
            return false;
        }

        if (this.fronts.size() >= frontsMax && !command) {
            if (this.simulatorConfig.getBoolean("unending-does-not-count")) {
                int permanent = 0;

                for (Entry<String, Front> entry : this.fronts.entrySet()) {
                    if (entry.getValue().isPermanent()) {
                        permanent += 1;
                    }
                }

                if (this.fronts.size() - permanent < frontsMax) {
                    return true;
                }
            }
        } else {
            return true;
        }

        return false;
    }

    public boolean simulatorHasFront(String frontName) {
        return this.fronts.containsKey(frontName);
    }

    public Front getFront(String frontName) {
        return this.fronts.get(frontName);
    }

    public boolean renameFront(String originalName, String newName) {
        if (simulatorHasFront(originalName) && newName != null) {
            Front front = this.fronts.get(originalName);
            front.changeName(newName);
            this.fronts.put(front.getName(), front);
            return true;
        }

        return false;
    }

    public YamlConfiguration getSimulatorConfig() {
        return this.simulatorConfig;
    }

    public YamlConfiguration allFrontsData() {
        YamlConfiguration allFronts = new YamlConfiguration();

        for (Entry<String, Front> entry : this.fronts.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                allFronts.set(entry.getKey(), getFrontData(entry.getKey()));
            }
        }

        return allFronts;
    }

    public YamlConfiguration getFrontData(String frontName) {
        YamlConfiguration data = new YamlConfiguration();

        if (frontName != null && this.fronts.containsKey(frontName)) {
            data = this.fronts.get(frontName).getData();
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
