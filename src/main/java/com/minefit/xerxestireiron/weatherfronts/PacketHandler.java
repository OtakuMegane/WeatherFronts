package com.minefit.xerxestireiron.weatherfronts;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.minefit.xerxestireiron.weatherfronts.FrontsWorld.FrontsWorld;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public class PacketHandler {
    private final WeatherFronts plugin;

    public PacketHandler(WeatherFronts instance) {
        this.plugin = instance;
    }

    public void onSoundPacket(PacketEvent event) {
        World world = event.getPlayer().getWorld();
        boolean isThunder = soundIsThunder(event.getPacket().getSoundEffects().read(0));

        if (!this.plugin.worldEnabled(world) || !isThunder) {
            return;
        }

        double x = (event.getPacket().getIntegers().read(0) / 8.0);
        double y = (event.getPacket().getIntegers().read(1) / 8.0);
        double z = (event.getPacket().getIntegers().read(2) / 8.0);
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world.getName());
        String stormName = frontsWorld.locationInWhichStorm((int) x, (int) z);

        if (stormName == null) {
            return;
        }

        Simulator simulator = frontsWorld.getSimulatorByStorm(stormName);

        if (simulator == null) {
            return;
        }

        YamlConfiguration simConfig = simulator.getSimulatorConfig();
        int volume = simConfig.getInt("thunder-volume", 300);

        // Make sure intracloud lightning isn't too quiet
        if (y > 255) {
            volume += y - 255;
        }


        Storm storm = simulator.getStorm(stormName);
        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        double playerX = playerLoc.getX();
        double playerZ = playerLoc.getZ();
        int hearOutside = simConfig.getInt("thunder-distance-outside", 140);

        if(storm.isInStorm(playerX, playerZ)) {
            event.getPacket().getFloat().write(0, (float) volume / 16);
            return;
        }

        if(storm.isInRangeOf(playerX, playerZ, hearOutside)) {
            event.getPacket().getFloat().write(0, (float) volume / 16);
            return;
        }

        event.setCancelled(true);
    }

    public void onLightningPacket(PacketEvent event) {
        World world = event.getPlayer().getWorld();

        if (!this.plugin.worldEnabled(world)) {
            return;
        }

        EntityType packetEntity = event.getPacket().getEntityTypeModifier().getValues().get(0);

        if(packetEntity != EntityType.LIGHTNING) {
            return;
        }

        double x = (event.getPacket().getDoubles().read(0));
        double y = (event.getPacket().getDoubles().read(1));
        double z = (event.getPacket().getDoubles().read(2));
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world.getName());
        String stormName = frontsWorld.locationInWhichStorm((int) x, (int) z);

        if (stormName == null) {
            return;
        }

        Simulator simulator = frontsWorld.getSimulatorByStorm(stormName);

        if (simulator == null) {
            return;
        }

        Storm storm = simulator.getStorm(stormName);
        Player player = event.getPlayer();
        Location playerLoc = player.getLocation();
        double playerX = playerLoc.getX();
        double playerZ = playerLoc.getZ();
        int seeOutside = frontsWorld.getSimulatorByStorm(stormName).getSimulatorConfig()
                .getInt("lightning-distance-outside", 180);

        if(storm.isInStorm(playerX, playerZ)) {
            return;
        }

        if(storm.isInRangeOf(playerX, playerZ, seeOutside)) {
            return;
        }

        event.setCancelled(true);
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

    public boolean soundIsThunder(Sound sound) {
        return sound == Sound.ENTITY_LIGHTNING_BOLT_IMPACT || sound == Sound.ENTITY_LIGHTNING_BOLT_THUNDER;
    }
}
