package com.minefit.XerxesTireIron.WeatherFronts.Front;

import java.awt.geom.Point2D;

import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.DynmapFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems.WeatherSystem;

public class Front {

    private final World world;
    private final WeatherFronts plugin;
    private final YamlConfiguration data;
    private String name;
    private final Simulator simulator;
    private final LightningGen lightning;
    private final PrecipitationEffects precipitation;
    private final DynmapFunctions dynmap;
    private WeatherSystem system;

    public Front(WeatherFronts instance, Simulator simulator, YamlConfiguration data, String name) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.data = data;
        this.name = name;
        this.lightning = new LightningGen(instance, simulator.getSimulatorConfig(), this);
        this.precipitation = new PrecipitationEffects(instance, this);
        this.plugin.getServer().getPluginManager().registerEvents(precipitation, plugin);
        this.dynmap = this.plugin.getDynmap();
        this.dynmap.addMarker(this.world.getName(), this.name, getFrontBoundaries());
    }

    public YamlConfiguration getData() {
        return this.data;
    }

    public FrontLocation getFrontLocation() {
        return new FrontLocation(this.simulator, this.data.getInt("center-x"), this.data.getInt("center-z"));
    }

    public void updatePosition(int x, int z) {
        this.data.set("center-x", x);
        this.data.set("center-z", z);
    }

    public String getName() {
        return this.name;
    }

    public World getWorld() {
        return this.world;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public int ageLimit() {
        return this.data.getInt("age-limit");
    }

    public int currentAge() {
        return this.data.getInt("age");
    }

    public void changeAge(int age) {
        this.data.set("age", age);
    }

    public boolean isPermanent() {
        return this.data.getInt("age-limit") == 0;
    }

    public void update() {
        this.dynmap.moveMarker(this.world.getName(), this.name, getFrontBoundaries());
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

        return this.name;
    }

    public Point2D[] getFrontBoundaries() {
        Point2D[] boundaries = new Point2D[4];
        boundaries[0].setLocation(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        boundaries[1].setLocation(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") - this.data.getInt("radius-z"));
        boundaries[2].setLocation(this.data.getInt("center-x") + this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
        boundaries[3].setLocation(this.data.getInt("center-x") - this.data.getInt("radius-x"),
                this.data.getInt("center-z") + this.data.getInt("radius-z"));
        return boundaries;
    }

    public boolean isInFront(int x, int z) {
        Point2D[] boundaries = getFrontBoundaries();
        return x > boundaries[0].getX() || x < boundaries[1].getX() || z > boundaries[1].getY()
                || z < boundaries[2].getY();
    }

}
