package com.minefit.xerxestireiron.weatherfronts.Simulator;

import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.LightningStrikeEvent;

import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.NMSBullshit.NMSHandler;

public class LightningHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final Simulator simulator;
    private final NMSHandler nmsHandler;

    public LightningHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.simulator = simulator;
        this.nmsHandler = new NMSHandler(this.plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (event.isCancelled() || !this.plugin.worldEnabled(world)) {
            return;
        }

        Block block = event.getLightning().getLocation().getBlock();
        FrontsLocation location = new FrontsLocation(this.simulator, block.getLocation());

        if (!this.simulator.isInSimulator(location)) {
            return;
        }

        /*YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (event.getWorld().getGameRuleValue(GameRule.DO_MOB_SPAWNING) && simulatorConfig.getBoolean("spawn-skeleton-traps", true)) {
            if (!location.inSpawnChunk() || simulatorConfig.getBoolean("skeleton-traps-in-spawn-chunk", false)) {

                Difficulty difficulty = world.getDifficulty();
                double chanceLimit = 0.0;

                if (difficulty == Difficulty.EASY) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.0075) + 0.0075;
                } else if (difficulty == Difficulty.NORMAL) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.025) + 0.015;
                } else if (difficulty == Difficulty.HARD) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.039375) + 0.028125;
                }

                double spawnChance = this.random.nextDouble();
                boolean doSpawn = spawnChance > 0.0 && spawnChance <= chanceLimit;

                if (doSpawn) {
                    this.nmsHandler.createHorseTrap(location);
                }
            }
        }

        if (simulatorConfig.getBoolean("create-fulgurites", false)) {
            if (!location.inSpawnChunk() || simulatorConfig.getBoolean("fulgurite-in-spawn-chunk", false)) {

                double fulguriteChance = simulatorConfig.getDouble("fulgurite-chance", 0.10D) / 100;

                if (this.random.nextDouble() <= fulguriteChance) {
                    new Fulgurite(this.simulator, block);
                }
            }
        }*/
    }
}
