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
import com.minefit.XerxesTireIron.WeatherFronts.Storm.Storm;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.RandomBasic.RandomBasic;

public class Simulator {
    private final World world;
    private final YamlConfiguration simulatorConfig;
    private final WeatherFronts plugin;
    private String name;
    private final ConcurrentMap<String, Storm> storms = new ConcurrentHashMap<>();
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
        loadStorms();
        this.mainTickCycle = new MainTickCycle(instance, this).runTaskTimer(instance, 0, 1);
        this.tickUpdates = new TickUpdates(instance, this).runTaskTimer(instance, 0, 20);

    }

    public World getWorld() {
        return this.world;
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
        for (Entry<String, Storm> entry : this.storms.entrySet()) {
            Storm storm = entry.getValue();
            storm.update();
            boolean dead = this.system.updateFront(storm);

            if (dead) {
                removeStorm(entry.getKey());
            }
        }
    }

    private void loadStorms() {
        YamlConfiguration storms = this.loadData.loadConfigForWorld(world.getName(), "fronts.yml", false);
        YamlConfiguration simulatorStorms = this.loadData.getSectionAsConfig(storms, getName());

        if (storms.getKeys(false).size() == 0) {
            return;
        }

        for (String stormName : simulatorStorms.getKeys(false)) {
            YamlConfiguration frontData = this.loadData.getSectionAsConfig(simulatorStorms, stormName);
            this.storms.put(stormName, new Storm(this.plugin, this, frontData));
        }
    }

    public void addStorm(Storm front) {
        this.storms.put(front.getName(), front);
    }

    public Storm createStorm(YamlConfiguration config, boolean command, boolean autogen) {
        if (canCreateStorm(command, autogen)) {
            Storm front = this.system.createFront(config);
            addStorm(front);
            return front;
        }

        return null;
    }

    public void removeStorm(String frontName) {
        this.storms.remove(frontName);
        this.dynmap.deleteMarker(world, this.name, frontName);
    }

    private boolean canCreateStorm(boolean command, boolean autogen) {
        int frontsMax = this.simulatorConfig.getInt("maximum-fronts");
        boolean autogenActive = this.system.getConfig().getBoolean("generate-fronts");

        if (autogen && !autogenActive) {
            return false;
        }

        if (this.storms.size() >= frontsMax && !command) {
            if (this.simulatorConfig.getBoolean("unending-does-not-count")) {
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

    public YamlConfiguration allFrontsData() {
        YamlConfiguration allFronts = new YamlConfiguration();

        for (Entry<String, Storm> entry : this.storms.entrySet()) {
            if (!entry.getKey().isEmpty()) {
                allFronts.set(entry.getKey(), getFrontData(entry.getKey()));
            }
        }

        return allFronts;
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
