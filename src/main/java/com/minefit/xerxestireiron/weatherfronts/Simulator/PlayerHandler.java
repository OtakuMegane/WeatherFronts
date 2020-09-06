package com.minefit.xerxestireiron.weatherfronts.Simulator;

import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.NMSBullshit.NMSHandler;

public class PlayerHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final Simulator simulator;
    private final World world;
    private final NMSHandler nmsHandler;

    public PlayerHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.nmsHandler = new NMSHandler(this.plugin);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world != this.world) {
            if (world.hasStorm()) {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            } else {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();

        if (world != this.world) {
            return;
        }

        changePlayerWeather();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled() || event.getState() != PlayerFishEvent.State.FISHING
                || !this.plugin.worldEnabled(player.getWorld())) {
            return;
        }

        FishHook hook = event.getHook();
        FrontsLocation hookLocation = new FrontsLocation(this.simulator, hook.getLocation());

        if (!hookLocation.isInWeather()) {
            return;
        }

        int hookTime = this.random.nextIntRange(100, 600);

        if (!hookLocation.isExposedToSky()) {
            hookTime *= 1.5; // If the bobber location is sheltered we increase time by 50%
        }

        double reductionPercentage = this.simulator.getSimulatorConfig().getDouble("fishing-time-reduction", 20);
        double reductionTime = (reductionPercentage / 100) * hookTime;
        hookTime = (int) Math.round(hookTime - reductionTime);
        hookTime -= this.nmsHandler.getRodLureLevel(hook) * 20 * 5;

        if (hookTime <= 0) {
            hookTime = 1;
        }

        this.nmsHandler.fishingTime(hook, hookTime);
    }

    public void changePlayerWeather() {
        for (Player player : this.world.getPlayers()) {
            FrontsLocation location = new FrontsLocation(this.simulator, player.getLocation());

            if (!location.isInStorm()) {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.CLEAR)) {
                    player.setPlayerWeather(WeatherType.CLEAR);
                }
            } else {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.DOWNFALL)) {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                }

                //this.plugin.getPacketHandler().changeWeather(player, location.inWhichStorm());
            }
        }
    }
}