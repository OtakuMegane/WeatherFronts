package com.minefit.XerxesTireIron.WeatherFronts;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontLocation {
    private Simulator simulator;
    private int x;
    private int z;

    public FrontLocation(Simulator simulator, int x, int z) {
        this.simulator = simulator;
        this.x = x;
        this.z = z;
    }

    public FrontLocation(Simulator simulator, double x, double z) {
        this(simulator, (int) x, (int) z);
    }

    public boolean isLoaded() {
        return this.simulator.getWorld().isChunkLoaded(this.x >> 4, this.z >> 4);
    }

    public void changeSimulator(Simulator simulator) {
        this.simulator = simulator;
    }

    public Simulator getSimulator() {
        return this.simulator;
    }

    public void updatePosition(int x, int z) {
        updatePositionX(x);
        updatePositionZ(z);
    }

    public void updatePositionX(int x) {
        this.x = x;
    }

    public void updatePositionZ(int z) {
        this.z = z;
    }

    public int getPositionX() {
        return this.x;
    }

    public int getPositionZ() {
        return this.z;
    }

    public int[] getPosition() {
        return new int[] { x, z };
    }
}
