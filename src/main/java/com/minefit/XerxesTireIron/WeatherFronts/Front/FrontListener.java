package com.minefit.XerxesTireIron.WeatherFronts.Front;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class FrontListener implements Listener {
    private final Front front;
    private final Simulator simulator;
    private final WeatherFronts plugin;
    private final BlockFunctions blockFunction;

    public FrontListener(WeatherFronts instance, Front front)
    {
        this.front = front;
        this.simulator = front.getSimulator();
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, this.simulator);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();

        if (this.blockFunction.isInRain(block) && block.getType() == Material.SOIL) {
            event.setCancelled(true);
            block.setData((byte) 6);
        }
    }
}
