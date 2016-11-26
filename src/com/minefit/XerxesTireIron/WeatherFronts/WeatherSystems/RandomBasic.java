package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems;

import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class RandomBasic implements WeatherSystem {

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

        FrontLocation frontLocation = front.getLocation();

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

}
