package com.minefit.XerxesTireIron.WeatherFronts.WeatherSystems;

import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public interface WeatherSystem {

    public void moveFront(Front front);

    public boolean shouldDie(Front front, Simulator simulator);

    public void ageFront(Front front);

}
