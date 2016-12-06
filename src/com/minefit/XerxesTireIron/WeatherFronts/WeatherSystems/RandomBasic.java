package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.GenerateFrontData;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class RandomBasic implements WeatherSystem {
    private final WeatherFronts plugin;
    private final Simulator simulator;

    public RandomBasic(WeatherFronts instance, Simulator simulator)
    {
        this.plugin = instance;
        this.simulator = simulator;
    }

    @Override
    public void moveFront(Front front) {
        YamlConfiguration frontData = front.getData();
        front.updatePosition(frontData.getInt("center-x") + frontData.getInt("velocity-x"),
                frontData.getInt("center-z") + frontData.getInt("velocity-z"));
    }

    @Override
    public boolean shouldDie(Front front, Simulator simulator) {
        int ageLimit = front.ageLimit();
        int age = front.currentAge();

        if (age > ageLimit && ageLimit != 0) {
            return true;
        }

        FrontLocation frontLocation = front.getFrontLocation();

        if (!simulator.isInSimulator(frontLocation.getPositionX(), frontLocation.getPositionZ())) {
            return true;
        }

        return false;
    }

    @Override
    public void ageFront(Front front)
    {
        int age = front.currentAge() + 1;
        front.changeAge(age);
    }

    @Override
    public Front createFront(YamlConfiguration config)
    {
        GenerateFrontData generate = new GenerateFrontData(this.plugin, this.simulator, config);
        Front front = new Front(this.plugin, this.simulator, generate.generateValues(), "");
        return front;
    }

}
