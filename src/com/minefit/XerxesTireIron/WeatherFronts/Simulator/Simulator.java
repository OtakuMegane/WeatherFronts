package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.LoadData;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;

public class Simulator {

    private final World world;
    private final YamlConfiguration simulatorConfig;
    private final YamlConfiguration frontsData;
    private final WeatherFronts plugin;
    private String name;
    private final ConcurrentMap<String, Front> fronts = new ConcurrentHashMap<String, Front>();
    private final LoadData load;
    private Logger logger = Logger.getLogger("Minecraft");

    public Simulator(World world, WeatherFronts instance, YamlConfiguration config, String name) {
        this.simulatorConfig = config;
        this.plugin = instance;
        this.world = world;
        this.name = name;
        this.load = new LoadData(instance);
        this.frontsData = this.load.loadConfigForWorld(world.getName(), "fronts.yml", false);

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
            boolean shouldDie = entry.getValue().update();

            if (shouldDie) {
                this.fronts.remove(entry.getKey());
            }
        }
    }

    private void loadFronts() {
        for (String frontName : this.frontsData.getKeys(false)) {
            fronts.put(frontName,
                    new Front(this.plugin, this, this.load.loadFrontData(frontName, this.frontsData), frontName));
        }
    }

    public Front createFront(YamlConfiguration config, boolean command) {
        if (canCreateFront(command)) {
            GenerateFrontData generate = new GenerateFrontData(this.plugin, this, config);
            String name = generate.frontName();
            this.fronts.put(name, new Front(this.plugin, this, generate.generateValues(), name));
            return this.fronts.get(name);
        }

        return null;
    }

    public void removeFront(String frontName)
    {
        Front front = this.fronts.get(frontName);
        this.fronts.remove(frontName);
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
        if (this.fronts.containsKey(frontName)) {
            return true;
        }

        return false;
    }

    public boolean renameFront(String originalName, String newName) {
        if (simulatorHasFront(originalName) && newName != null) {
            this.fronts.get(originalName).changeName(newName);
            this.fronts.put(newName, this.fronts.get(originalName));
            return true;
        }

        return false;
    }

    public YamlConfiguration getSimulatorConfig() {
        return this.simulatorConfig;
    }

    public YamlConfiguration allFrontsData() {
        YamlConfiguration allFronts = new YamlConfiguration();
        // allFronts.createSection(this.name);

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
