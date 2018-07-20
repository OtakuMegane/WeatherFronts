package com.minefit.xerxestireiron.weatherfronts.Storm;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;

public class StormListener implements Listener {
    private final Storm storm;
    private final Simulator simulator;
    private final WeatherFronts plugin;
    private final BlockFunctions blockFunction;

    public StormListener(WeatherFronts instance, Storm storm) {
        this.storm = storm;
        this.simulator = storm.getSimulator();
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, this.simulator);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event) {
        Block block = event.getBlock();

        if (this.blockFunction.isInRain(block) && block.getType() == Material.FARMLAND) {
            event.setCancelled(true);
            Farmland farmland = (Farmland) block.getBlockData();

            if ( farmland.getMoisture() < farmland.getMaximumMoisture()) {
                farmland.setMoisture(6);
            }
        }
    }
}
