package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;
import com.minefit.XerxesTireIron.WeatherFronts.LoadData;
import com.minefit.XerxesTireIron.WeatherFronts.SaveData;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class FrontsWorld {
    private final WeatherFronts plugin;
    private final World world;
    private final LoadData load;
    private final Map<String, Simulator> simulators = new HashMap<String, Simulator>();
    private final SaveData save;
    private YamlConfiguration worldSimulatorConfigs;
    private final XORShiftRandom random = new XORShiftRandom();

    public FrontsWorld(WeatherFronts instance, World world) {
        this.plugin = instance;
        this.world = world;
        this.load = new LoadData(instance);
        this.save = new SaveData(instance);
        loadSimulators();
    }

    public World getWorld() {
        return this.world;
    }

    public Simulator randomSimulator() {
        int length = this.simulators.size();
        Object[] values = this.simulators.values().toArray();
        return (Simulator) values[random.nextInt(length)];
    }

    public boolean hasFront(String frontName) {
        return whichSimulator(frontName) != null;
    }

    private Simulator whichSimulator(String frontName) {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            if (entry.getValue().simulatorHasFront(frontName)) {
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

    public String locationInWhichFront(int x, int z) {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            String frontName = entry.getValue().locationInWhichFront(x, z);

            if (frontName != null) {
                return frontName;
            }
        }

        return null;
    }

    public Simulator getSimulatorByFront(String frontName) {
        if (hasFront(frontName)) {
            return whichSimulator(frontName);
        }

        return null;
    }

    public void loadSimulators() {
        String worldName = this.world.getName();
        YamlConfiguration simulatorDefaults = this.load.loadConfigForWorld(worldName, "simulator-defaults.yml", true);
        this.worldSimulatorConfigs = this.load.loadConfigForWorld(worldName, "simulators.yml", true);

        // Set up new simulator here
        for (String simulatorName : worldSimulatorConfigs.getKeys(false)) {
            YamlConfiguration config = this.load.combineConfigDefaults(simulatorName, simulatorDefaults,
                    this.worldSimulatorConfigs);
            this.simulators.put(simulatorName, new Simulator(this.world, this.plugin, config, simulatorName));
            /*this.save.saveToYamlFile(worldName, "simulators-mod.yml",
                    this.load.combineConfigDefaults(simulatorName, simulatorDefaults, this.worldSimulatorConfigs));*/
        }

    }

    public void saveSimulators() {
        // This will come later
        // For now only manual changes to the file
    }

    public void saveFronts() {
        YamlConfiguration allFronts = new YamlConfiguration();
        String worldName = world.getName();

        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            allFronts.set(entry.getKey(), entry.getValue().allFrontsData());
        }

        this.save.saveToYamlFile(worldName, "fronts.yml", allFronts);
    }

    public void shutdownSimulators() {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            entry.getValue().shutdown();
        }
    }

    public void shutdown() {
        saveSimulators();
        saveFronts();
        shutdownSimulators();
    }

}
