package com.minefit.xerxestireiron.weatherfronts.FrontsWorld;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.LoadData;
import com.minefit.xerxestireiron.weatherfronts.SaveData;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public class FrontsWorld {
    private final WeatherFronts plugin;
    private final World world;
    private final LoadData load;
    private final Map<String, Simulator> simulators = new HashMap<>();
    private final SaveData save;
    private final XORShiftRandom random = new XORShiftRandom();
    private final boolean isSpigot;
    private final int mobSpawnRange;

    public FrontsWorld(WeatherFronts instance, World world) {
        this.plugin = instance;
        this.world = world;
        this.load = new LoadData(instance);
        this.save = new SaveData(instance);
        this.isSpigot = checkIsSpigot();
        int mobRange = 8;

        if (this.isSpigot) {
            SpigotHandler spigotHandler = new SpigotHandler(world);
            mobRange = spigotHandler.mobSpawnRange;
        }

        if (mobRange > Bukkit.getServer().getViewDistance()) {
            this.mobSpawnRange = Bukkit.getServer().getViewDistance();
        } else {
            this.mobSpawnRange = mobRange;
        }

        loadSimulators();
    }

    private boolean checkIsSpigot() {
        try {
            Class.forName("org.spigotmc.SpigotWorldConfig");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public int getMobSpawnRange() {
        return this.mobSpawnRange;
    }

    public World getWorld() {
        return this.world;
    }

    public Simulator randomSimulator() {
        int length = this.simulators.size();
        Object[] values = this.simulators.values().toArray();
        return (Simulator) values[random.nextInt(length)];
    }

    public boolean hasStorm(String stormName) {
        return whichSimulator(stormName) != null;
    }

    private Simulator whichSimulator(String stormName) {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            if (entry.getValue().simulatorHasFront(stormName)) {
                return entry.getValue();
            }
        }

        return null;
    }

    public Simulator getSimulator(String simulatorName) {
        return simulators.get(simulatorName);
    }

    public Map<String, Simulator> getSimulatorList() {
        return this.simulators;
    }

    public String locationInWhichStorm(int x, int z) {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            String stormName = entry.getValue().locationInWhichStorm(x, z);

            if (stormName != null) {
                return stormName;
            }
        }

        return null;
    }

    public Simulator getSimulatorByStorm(String stormName) {
        if (hasStorm(stormName)) {
            return whichSimulator(stormName);
        }

        return null;
    }

    public void loadSimulators() {
        String worldName = this.world.getName();
        YamlConfiguration simulatorDefaults = this.load.loadConfigForWorld(worldName, "simulator-defaults.yml", true);
        YamlConfiguration simulatorConfigs = this.load.loadConfigForWorld(worldName, "simulators.yml", true);

        // Set up new simulator here
        for (String simulatorName : simulatorConfigs.getKeys(false)) {
            YamlConfiguration config = this.load.combineConfigDefaults(simulatorName, simulatorDefaults,
                    simulatorConfigs);
            this.simulators.put(simulatorName, new Simulator(this.world, this.plugin, config, simulatorName));
            /*this.save.saveToYamlFile(worldName, "simulators-mod.yml",
                    this.load.combineConfigDefaults(simulatorName, simulatorDefaults, this.worldSimulatorConfigs));*/
        }

    }

    public void saveSimulators() {
        // This will come later
        // For now only manual changes to the file
    }

    public void saveStorms() {
        YamlConfiguration allStorms = new YamlConfiguration();
        YamlConfiguration simulatorStorms = new YamlConfiguration();
        String worldName = world.getName();

        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            allStorms.set(entry.getKey(), entry.getValue().allFrontsData());
        }

        for (Entry<String, Simulator> simulator : this.simulators.entrySet()) {
            ArrayList<String> stormList = new ArrayList<>();

            for (Entry<String, Storm> storm : simulator.getValue().getStorms().entrySet()) {
                stormList.add(storm.getKey());
                storm.getValue().save();
            }

            simulatorStorms.set(simulator.getKey(), stormList);
        }

        this.save.saveToYamlFile(worldName, "stormlist.yml", simulatorStorms);
        this.save.saveToYamlFile(worldName, "fronts.yml", allStorms);
    }

    public void shutdownSimulators() {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            entry.getValue().shutdown();
        }
    }

    public void shutdown() {
        saveSimulators();
        saveStorms();
        shutdownSimulators();
    }

}
