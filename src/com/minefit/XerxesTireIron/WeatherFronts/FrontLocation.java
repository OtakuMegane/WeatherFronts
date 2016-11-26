package com.minefit.XerxesTireIron.WeatherFronts;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontLocation {
    private Simulator simulator;
    private int x;
    private int z;
    private int velocityX;
    private int velocityZ;

    public FrontLocation(Simulator simulator, int x, int z, int vx, int vz)
    {
        this.simulator = simulator;
        this.x = x;
        this.z = z;
        this.velocityX = vx;
        this.velocityZ = vz;
    }

    public FrontLocation(Simulator simulator)
    {
        this(simulator, 0, 0, 0, 0);
    }

    public void changeSimulator(Simulator simulator)
    {
        this.simulator = simulator;
    }

    public Simulator getSimulator()
    {
        return this.simulator;
    }

    public void updatePosition(int x, int z)
    {
        updatePositionX(x);
        updatePositionZ(z);
    }

    public void updatePositionX(int x)
    {
        this.x = x;
    }

    public void updatePositionZ(int z)
    {
        this.z = z;
    }

    public int getPositionX()
    {
        return this.x;
    }

    public int getPositionZ()
    {
        return this.z;
    }

    public int[] getPosition()
    {
        return new int[] {x, z};
    }

    public void updateVelocity(int x, int z)
    {
        updateVelocityX(x);
        updateVelocityZ(z);
    }

    public void updateVelocityX(int x)
    {
        this.velocityX = x;
    }

    public void updateVelocityZ(int z)
    {
        this.velocityZ = z;
    }

    public int getVelocityX()
    {
        return this.velocityX;
    }

    public int getVelocityZ()
    {
        return this.velocityZ;
    }

    public int[] getVelocity()
    {
        return new int[] {velocityX, velocityZ};
    }

}
