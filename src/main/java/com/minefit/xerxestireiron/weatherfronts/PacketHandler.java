package com.minefit.xerxestireiron.weatherfronts;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.minefit.xerxestireiron.weatherfronts.FrontsWorld.FrontsWorld;

public class PacketHandler {
    private final WeatherFronts plugin;

    public PacketHandler(WeatherFronts instance) {
        this.plugin = instance;
    }

    public void onSoundPacket(PacketEvent event) {
        event.setCancelled(true);
        World world = event.getPlayer().getWorld();
        boolean isThunder = event.getPacket().getSoundEffects().read(0) == Sound.ENTITY_LIGHTNING_THUNDER;

        if (!this.plugin.worldEnabled(world) || !isThunder) {
            event.setCancelled(false);
            return;
        }

        double x = (event.getPacket().getIntegers().read(0) / 8.0);
        double y = (event.getPacket().getIntegers().read(1) / 8.0);
        double z = (event.getPacket().getIntegers().read(2) / 8.0);
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world);
        String stormName = frontsWorld.locationInWhichStorm((int) x, (int) z);

        if (stormName == null) {
            return;
        }

        YamlConfiguration simConfig = frontsWorld.getSimulatorByStorm(stormName).getSimulatorConfig();
        int volume = simConfig.getInt("thunder-volume", 192);
        int hearOutside = simConfig.getInt("thunder-distance-outside", 90);
        Location playerLoc = event.getPlayer().getLocation();
        int playerX = playerLoc.getBlockX();
        int playerZ = playerLoc.getBlockZ();
        YamlConfiguration stormConfig = frontsWorld.getSimulatorByStorm(stormName).getStormData(stormName);
        int stormRadiusX = stormConfig.getInt("radius-x");
        int stormRadiusZ = stormConfig.getInt("radius-z");
        int stormX = stormConfig.getInt("center-x");
        int stormZ = stormConfig.getInt("center-z");

        int x1 = stormX + stormRadiusX;
        int x2 = stormX - stormRadiusX;
        int z1 = stormZ + stormRadiusZ;
        int z2 = stormZ - stormRadiusZ;

        if (x1 + hearOutside > playerX && x2 - hearOutside < playerX && z1 + hearOutside > playerZ
                && z2 - hearOutside < playerZ) {
            event.setCancelled(false);
            event.getPacket().getFloat().write(0, (float) volume / 16);
        }
    }

    public void onLightningPacket(PacketEvent event) {
        event.setCancelled(true);
        World world = event.getPlayer().getWorld();

        if (!this.plugin.worldEnabled(world)) {
            event.setCancelled(false);
            return;
        }

        double x = (event.getPacket().getDoubles().read(0));
        double y = (event.getPacket().getDoubles().read(1));
        double z = (event.getPacket().getDoubles().read(2));
        Player player = event.getPlayer();
        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world.getName());
        String stormName = frontsWorld.locationInWhichStorm((int) x, (int) z);

        if (stormName == null) {
            return;
        }

        YamlConfiguration stormConfig = frontsWorld.getSimulatorByStorm(stormName).getStormData(stormName);
        int stormRadiusX = stormConfig.getInt("radius-x");
        int stormRadiusZ = stormConfig.getInt("radius-z");
        int stormX = stormConfig.getInt("center-x");
        int stormZ = stormConfig.getInt("center-z");
        int seeOutside = frontsWorld.getSimulatorByStorm(stormName).getSimulatorConfig()
                .getInt("lightning-distance-outside", 160);

        if (stormX + stormRadiusX + seeOutside > playerX && stormX - stormRadiusX - seeOutside < playerX
                && stormZ + stormRadiusZ + seeOutside > playerZ && stormZ - stormRadiusZ - seeOutside < playerZ) {
            event.setCancelled(false);
        }
    }

    public void changeWeather(Player player, String storm) {
        World world = player.getWorld();

        if (!this.plugin.worldEnabled(world)) {
            return;
        }

        PacketContainer packet1 = this.plugin.getProtocolManager()
                .createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);

        if (storm == null) {
            packet1.getIntegers().write(0, 1);
            packet1.getFloat().write(0, 0.0F);
        } else {
            FrontsWorld worldHandle = this.plugin.getWorldHandle(world);
            YamlConfiguration simConfig = worldHandle.getSimulatorByStorm(storm).getSimulatorConfig();
            YamlConfiguration stormConfig = worldHandle.getSimulatorByStorm(storm).getStormData(storm);
            int intensity = stormConfig.getInt("precipitation-intensity");

            if (stormConfig.getInt("lightning-per-minute") == 0) {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 0.0F);

            } else {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 1.0F);
            }

            if (simConfig.getBoolean("use-intensity-for-light-level", true)) {
                int maxIntensity = worldHandle.getSimulatorByStorm(storm).getWeatherSystem().getConfig()
                        .getInt("maximum-precipitation-intensity", 30);

                if (maxIntensity > 100) {
                    maxIntensity = 100;
                }

                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, (float) (intensity * (1.0 / maxIntensity)));
            }
        }

        sendPacket(packet1, player);
    }

    public void sendPacket(PacketContainer packet, Player player) {
        try {
            this.plugin.getProtocolManager().sendServerPacket(player, packet);
        } catch (Exception e) {
            throw new RuntimeException("Cannot send packet " + packet, e);
        }
    }

    public void onStateChangePacket(PacketEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!this.plugin.worldEnabled(world)) {
            return;
        }

        event.getPacket().getIntegers().write(0, 7);
        event.getPacket().getFloat().write(0, 1.0F);

    }
}
