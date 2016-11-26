package com.minefit.XerxesTireIron.WeatherFronts.Front;

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
    private final int[] dimSpeed;
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
        this.dimSpeed = new int[6];
        updateDimSpeed();
        this.dynmap = this.plugin.getDynmap();
        this.dynmap.addMarker(this.world.getName(), name, this.dimSpeed);
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

    public FrontLocation getLocation()
    {
        FrontLocation location = new FrontLocation(this.simulator);
        location.updatePosition(this.data.getInt("center-x"), this.data.getInt("center-z"));
        location.updateVelocity(this.data.getInt("velocity-x"), this.data.getInt("velocity-z"));
        return location;
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

    public int ageLimit()
    {
        return this.data.getInt("age-limit");
    }

    public int currentAge()
    {
        return this.data.getInt("age");
    }

    public void changeAge(int age)
    {
        this.data.set("age", age);
    }

    public boolean isPermanent() {
        return this.data.getInt("age-limit") == 0;
    }

    public void update() {
        updateDimSpeed();
        this.dynmap.moveMarker(this.world.getName(), this.name, this.dimSpeed);
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
