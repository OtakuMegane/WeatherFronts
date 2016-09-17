package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.scheduler.BukkitTask;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;
import com.minefit.XerxesTireIron.WeatherFronts.LoadData;
import com.minefit.XerxesTireIron.WeatherFronts.SaveData;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;

public class FrontsWorld {
    private final WeatherFronts plugin;
    private final World world;
    private final LoadData load;
    private final Map<String, Simulator> simulators = new HashMap<String, Simulator>();
    private final SaveData save;
    private final BukkitTask oneTick;
    private final BukkitTask fiveTick;
    private final BukkitTask twentyTick;
    private final BukkitTask sixHundredTick;
    private YamlConfiguration worldSimulatorConfigs;
    private final XORShiftRandom random = new XORShiftRandom();

    public FrontsWorld(WeatherFronts instance, World world) {
        this.plugin = instance;
        this.world = world;
        this.load = new LoadData(instance);
        this.save = new SaveData(instance);
        loadSimulators();
        this.oneTick = new Runnable1Tick(instance, this).runTaskTimer(instance, 0, 1);
        this.fiveTick = new Runnable5Tick(instance, this).runTaskTimer(instance, 0, 5);
        this.twentyTick = new Runnable20Tick(instance, this).runTaskTimer(instance, 0, 20);
        this.sixHundredTick = new Runnable600Tick(instance, this).runTaskTimer(instance, 0, 600);
    }

    public void updateSimulators() {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            entry.getValue().updateFronts();
        }
    }

    public World getWorld() {
        return this.world;
    }

    public void autoGenFronts() {
        for (Entry<String, Simulator> entry : this.simulators.entrySet()) {
            entry.getValue().createFront(new YamlConfiguration(), false);
        }
    }

    public Simulator randomSimulator()
    {
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
            if (entry.getValue().isInSimulator(x, z)) {
                for (Entry<String, Front> entry2 : entry.getValue().getFronts().entrySet()) {
                    if (entry2.getValue().isInFront(x, z)) {
                        return entry2.getValue().getName();
                    }
                }
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

    public Simulator getSimulatorByLocation(Location location) {
        String frontName = locationInWhichFront(location.getBlockX(), location.getBlockZ());

        if (frontName != null && hasFront(frontName)) {
            return getSimulatorByFront(frontName);
        }

        return null;
    }

    public void loadSimulators() {
        String worldName = this.world.getName();
        YamlConfiguration worldDefaults = this.load.loadConfigForWorld(worldName, "defaults.yml", true);
        this.worldSimulatorConfigs = this.load.loadConfigForWorld(worldName, "simulators.yml", true);

        // Set up new simulator here
        for (String simulatorName : worldSimulatorConfigs.getKeys(false)) {
            this.simulators.put(simulatorName, new Simulator(this.world, this.plugin,
                    this.load.combineConfigDefaults(simulatorName, worldDefaults, this.worldSimulatorConfigs), simulatorName));
            this.save.saveToYamlFile(worldName, "simulators-mod.yml", this.load.combineConfigDefaults(simulatorName, worldDefaults, this.worldSimulatorConfigs));
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
            allFronts = entry.getValue().allFrontsData();
        }

        this.save.saveToYamlFile(worldName, "fronts.yml", allFronts);
    }

    public void shutdown() {
        saveSimulators();
        saveFronts();
        this.oneTick.cancel();
        this.fiveTick.cancel();
        this.twentyTick.cancel();
        this.sixHundredTick.cancel();
    }

}
