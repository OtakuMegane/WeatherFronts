package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontListener implements Listener {
    private final Front front;
    private final Simulator simulator;
    private final WeatherFronts plugin;

    public FrontListener(WeatherFronts instance, Front front)
    {
        this.front = front;
        this.simulator = front.getSimulator();
        this.plugin = instance;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();
        FrontLocation location = this.simulator.newFrontLocation(block);

        if (location.isInRain() && block.getType() == Material.SOIL) {
            event.setCancelled(true);
            block.setData((byte) 6);
        }
    }
}
