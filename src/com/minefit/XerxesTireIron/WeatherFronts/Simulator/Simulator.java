package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.DynmapFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.LoadData;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;

public class Simulator {
    private final World world;
    private final YamlConfiguration simulatorConfig;
    private final YamlConfiguration frontsData;
    private final WeatherFronts plugin;
    private String name;
    private final ConcurrentMap<String, Front> fronts = new ConcurrentHashMap<String, Front>();
    private final LoadData load;
    private final DynmapFunctions dynmap;
    private WeatherSystem system;

    public Simulator(World world, WeatherFronts instance, YamlConfiguration config, String name) {
        this.simulatorConfig = config;
        this.plugin = instance;
        this.world = world;
        this.name = name;
        this.load = new LoadData(instance);
        this.frontsData = this.load.loadConfigForWorld(world.getName(), "fronts.yml", false);
        this.dynmap = this.plugin.getDynmap();
        loadFronts();
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

    public void updateFronts() {
        for (Entry<String, Front> entry : this.fronts.entrySet()) {
            Front front = entry.getValue();
            this.system.moveFront(front);
            this.system.ageFront(front);

            if (this.system.shouldDie(front, this)) {
                removeFront(entry.getKey());
            }
        }
    }

    private void loadFronts() {
        for (String frontName : this.frontsData.getKeys(false)) {
            this.fronts.put(frontName,
                    new Front(this.plugin, this, this.load.loadFrontData(frontName, this.frontsData), frontName));
        }
    }

    public void addFront(Front front)
    {
        this.fronts.put(front.getName(), front);
    }

    public Front createFront(YamlConfiguration config, boolean command) {
        if (canCreateFront(command)) {
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

    private boolean canCreateFront(boolean command) {
        int frontsMax = this.simulatorConfig.getInt("maximum-fronts");
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
            allFronts.set(entry.getKey(), getFrontData(entry.getKey()));
        }

        return allFronts;
    }

    public YamlConfiguration getFrontData(String frontName) {
        YamlConfiguration data = new YamlConfiguration();

        if (frontName != null) {
            if (this.fronts.containsKey(frontName)) {
                data = this.fronts.get(frontName).getData();
            }
        } else {
            data = this.frontsData;
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
}
