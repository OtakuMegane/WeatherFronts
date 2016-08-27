package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

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

    public Front(WeatherFronts instance, Simulator simulator, YamlConfiguration data, String name) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.data = data;
        this.name = name;
        this.lightning = new LightningGen(instance, this.data, simulator.getSimulatorConfig());
        this.precipitation = new PrecipitationEffects(instance, this.data, this.world);
        this.plugin.getServer().getPluginManager().registerEvents(precipitation, plugin);
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
        if (this.data.getInt("age-limit") == 0) {
            return true;
        }

        return false;
    }

    public boolean update() {
        move();
        age();
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
        int velocityX = this.data.getInt("velocity-x");
        int velocityZ = this.data.getInt("velocity-z");
        int centerX = this.data.getInt("center-x");
        int centerZ = this.data.getInt("center-z");
        this.data.set("center-x", centerX + velocityX);
        this.data.set("center-z", centerZ + velocityZ);
        // this.plugin.dynmapFunctions.moveMarker(world,
        // this.simulator.getName(), this.name);
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
