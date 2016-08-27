package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld.FrontsWorld;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class PacketHandler {
    private final WeatherFronts plugin;

    public PacketHandler(WeatherFronts instance) {
        this.plugin = instance;
    }

    public void onSoundPacket(PacketEvent event) {
        event.setCancelled(true);
        World world = event.getPlayer().getWorld();
        boolean isThunder = false;

        if (this.plugin.oldPacket) {
            isThunder = event.getPacket().getStrings().read(0).equals("ambient.weather.thunder");
        } else {
            isThunder = event.getPacket().getSoundEffects().read(0) == Sound.ENTITY_LIGHTNING_THUNDER;
        }

        if (!this.plugin.worldEnabled(world) || !isThunder) {
            event.setCancelled(false);
            return;
        }

        double x = (event.getPacket().getIntegers().read(0) / 8.0);
        double y = (event.getPacket().getIntegers().read(1) / 8.0);
        double z = (event.getPacket().getIntegers().read(2) / 8.0);
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world);
        String frontName = frontsWorld.locationInWhichFront((int) x, (int) z);

        if (frontName == null) {
            return;
        }

        YamlConfiguration simConfig = frontsWorld.getSimulatorByFront(frontName).getSimulatorConfig();
        int volume = simConfig.getInt("thunder-volume");
        int hearOutside = simConfig.getInt("thunder-distance-outside");
        Location playerLoc = event.getPlayer().getLocation();
        int playerX = playerLoc.getBlockX();
        int playerZ = playerLoc.getBlockZ();
        YamlConfiguration frontConfig = frontsWorld.getSimulatorByFront(frontName).getFrontData(frontName);
        int frontRadiusX = frontConfig.getInt("radius-x");
        int frontRadiusZ = frontConfig.getInt("radius-z");
        int frontX = frontConfig.getInt("center-x");
        int frontZ = frontConfig.getInt("center-z");

        int x1 = frontX + frontRadiusX;
        int x2 = frontX - frontRadiusX;
        int z1 = frontZ + frontRadiusZ;
        int z2 = frontZ - frontRadiusZ;

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

        double x = 0.0;
        double y = 0.0;
        double z = 0.0;

        if (this.plugin.oldPacket) {
            x = (event.getPacket().getIntegers().read(1) / 32.0);
            y = (event.getPacket().getIntegers().read(2) / 32.0);
            z = (event.getPacket().getIntegers().read(3) / 32.0);
        } else {
            x = (event.getPacket().getDoubles().read(0));
            y = (event.getPacket().getDoubles().read(1));
            z = (event.getPacket().getDoubles().read(2));
        }

        Player player = event.getPlayer();
        double playerX = player.getLocation().getX();
        double playerZ = player.getLocation().getZ();
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world.getName());
        String frontName = frontsWorld.locationInWhichFront((int) x, (int) z);

        if (frontName == null) {
            return;
        }

        YamlConfiguration frontConfig = frontsWorld.getSimulatorByFront(frontName).getFrontData(frontName);
        int frontRadiusX = frontConfig.getInt("radius-x");
        int frontRadiusZ = frontConfig.getInt("radius-z");
        int frontX = frontConfig.getInt("center-x");
        int frontZ = frontConfig.getInt("center-z");
        int seeOutside = frontsWorld.getSimulatorByFront(frontName).getSimulatorConfig()
                .getInt("lightning-distance-outside");

        if (frontX + frontRadiusX + seeOutside > playerX && frontX - frontRadiusX - seeOutside < playerX
                && frontZ + frontRadiusZ + seeOutside > playerZ && frontZ - frontRadiusZ - seeOutside < playerZ) {
            event.setCancelled(false);
        }
    }

    public void changeWeather(Player player, String front) {
        World world = player.getWorld();

        if (!this.plugin.worldEnabled(world)) {
            return;
        }

        PacketContainer packet1 = this.plugin.protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);

        if (front == null) {
            packet1.getIntegers().write(0, 1);
            packet1.getFloat().write(0, 0.0F);
        } else {
            FrontsWorld worldHandle = this.plugin.getWorldHandle(world);
            YamlConfiguration simConfig = worldHandle.getSimulatorByFront(front).getSimulatorConfig();
            YamlConfiguration frontConfig = worldHandle.getSimulatorByFront(front).getFrontData(front);
            int intensity = frontConfig.getInt("intensity");

            if (frontConfig.getInt("lightning-per-minute") == 0) {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 0.0F);

            } else {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 1.0F);
            }

            if (simConfig.getBoolean("use-intensity-for-light-level")) {
                int maxIntensity = simConfig.getInt("maximum-intensity");

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
            this.plugin.protocolManager.sendServerPacket(player, packet);
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
