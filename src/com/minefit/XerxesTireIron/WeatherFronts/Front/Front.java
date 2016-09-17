package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.DynmapFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class Front {

    private final World world;
    private final WeatherFronts plugin;
    private final YamlConfiguration data;
    private String name;
    private final Simulator simulator;
    private final LightningGen lightning;
    private final PrecipitationEffects precipitation;
    private final int[] dimSpeed;
    private final DynmapFunctions dynmap;

    public Front(WeatherFronts instance, Simulator simulator, YamlConfiguration data, String name) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.data = data;
        this.name = name;
        this.lightning = new LightningGen(instance, simulator.getSimulatorConfig(), this);
        this.precipitation = new PrecipitationEffects(instance, this);
        this.plugin.getServer().getPluginManager().registerEvents(precipitation, plugin);
        this.dimSpeed = new int[6];
        updateDimSpeed();
        this.dynmap = new DynmapFunctions(instance);
    }

    // center, radius and velocity are very commonly accessed and used in calculations
    // This way we can pass and access them easier
    private void updateDimSpeed() {
        dimSpeed[0] = this.data.getInt("center-x");
        dimSpeed[1] = this.data.getInt("center-z");
        dimSpeed[2] = this.data.getInt("radius-x");
        dimSpeed[3] = this.data.getInt("radius-z");
        dimSpeed[4] = this.data.getInt("velocity-x");
        dimSpeed[5] = this.data.getInt("velocity-z");
    }

    public int[] getDimSpeed()
    {
        return this.dimSpeed;
    }

    public YamlConfiguration getData() {
        return this.data;
    }

    public String getName() {
        return this.name;
    }

    public World getWorld() {
        return this.world;
    }

    public boolean isPermanent() {
        return this.data.getInt("age-limit") == 0;
    }

    public boolean update() {
        move();
        age();
        updateDimSpeed();
        this.dynmap.moveMarker(this.name, this.dimSpeed);
        return shouldDie();
    }

    public void genLightning() {
        this.lightning.lightningGen(world);
    }

    public void precipitationEffects() {
        this.precipitation.hydrateFarmland();
        this.precipitation.precipitationBlockEffects();
    }

    public String changeName(String newName) {

        if (newName != null) {
            this.name = newName;
        }

        return newName;
    }

    private void move() {
        this.data.set("center-x", this.data.getInt("center-x") + this.data.getInt("velocity-x"));
        this.data.set("center-z", this.data.getInt("center-z") + this.data.getInt("velocity-z"));
    }

    private void age() {
        this.data.set("age", this.data.getInt("age") + 1);
    }

    private boolean shouldDie() {
        int ageLimit = this.data.getInt("age-limit");
        int age = this.data.getInt("age");

        if (age > ageLimit && ageLimit != 0) {
            return true;
        }

        if (!this.simulator.isInSimulator(this.data.getInt("center-x"), this.data.getInt("center-z"))) {
            return true;
        }

        return false;
    }

    public boolean isInFront(int x, int z) {
        int fx = this.data.getInt("center-x");
        int fz = this.data.getInt("center-z");
        int frx = this.data.getInt("radius-x");
        int frz = this.data.getInt("radius-z");

        if (x > fx + frx || x < fx - frx || z > fz + frz || z < fz - frz) {
            return false;
        }

        return true;
    }

}
