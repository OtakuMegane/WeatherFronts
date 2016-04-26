package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;

public class PacketHandler {
    final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
    private WeatherFronts plugin;
    private Logger logger = Logger.getLogger("Minecraft");

    public PacketHandler(WeatherFronts instance) {
        plugin = instance;
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    public void onSoundPacket(PacketEvent event) {
        event.setCancelled(true);

        World world = event.getPlayer().getWorld();
        boolean isThunder = false;

        if(this.plugin.serverVersionMajor.equals("8"))
        {
            isThunder = event.getPacket().getStrings().read(0).equals("ambient.weather.thunder");
        }
        else if(this.plugin.serverVersionMajor.equals("9"))
        {
            isThunder = event.getPacket().getSoundEffects().read(0) == Sound.ENTITY_LIGHTNING_THUNDER;
        }

        if (!test.worldIsEnabled(world) || !isThunder) {
            event.setCancelled(false);
            return;
        }

        for (String key : Configuration.fronts_config.getConfigurationSection(world.getName()).getKeys(false)) {
            String simConfig = "worlds." + world.getName() + "." + key + ".";
            int volume = Configuration.main_config.getInt(simConfig + "thunder-volume");

            if (volume == 0) {
                return;
            }

            int hearOutside = Configuration.main_config.getInt(simConfig + "thunder-distance-outside");

            for (String key2 : Configuration.fronts_config.getConfigurationSection(world.getName() + "." + key).getKeys(false)) {
                String frontConfig = world.getName() + "." + key + "." + key2 + ".";

                double x = (event.getPacket().getIntegers().read(0) / 8.0);
                double y = (event.getPacket().getIntegers().read(1) / 8.0);
                double z = (event.getPacket().getIntegers().read(2) / 8.0);
                Location eventLoc = new Location(world, x, y, z);
                String[] front = test.locationInWhichFront(eventLoc, true, false);

                if (front[1] == null) {
                    continue;
                }

                Player player = event.getPlayer();
                Location playerLoc = player.getLocation();
                int playerX = playerLoc.getBlockX();
                int playerY = playerLoc.getBlockY();
                int playerZ = playerLoc.getBlockZ();
                int frontRadiusX = Configuration.fronts_config.getInt(frontConfig + "radius-x");
                int frontRadiusZ = Configuration.fronts_config.getInt(frontConfig + "radius-z");
                int frontX = Configuration.fronts_config.getInt(frontConfig + "center-x");
                int frontZ = Configuration.fronts_config.getInt(frontConfig + "center-z");
                boolean hearThunder = false;

                int x1 = frontX + frontRadiusX;
                int x2 = frontX - frontRadiusX;
                int z1 = frontZ + frontRadiusZ;
                int z2 = frontZ - frontRadiusZ;

                if (x1 + hearOutside > playerX && x2 - hearOutside < playerX && z1 + hearOutside > playerZ
                        && z2 - hearOutside < playerZ) {
                    hearThunder = true;
                    event.getPacket().getFloat().write(0, (float) volume / 16);
                }

                if (hearThunder) {
                    event.setCancelled(false);
                    break;
                }
            }
        }
    }

    public void onLightningPacket(PacketEvent event) {
        event.setCancelled(true);
        World world = event.getPlayer().getWorld();

        if (!test.worldIsEnabled(world)) {
            event.setCancelled(false);
            return;
        }

        for (String key : Configuration.fronts_config.getConfigurationSection(world.getName()).getKeys(false)) {
            String simConfig = "worlds." + world.getName() + "." + key + ".";

            for (String key2 : Configuration.fronts_config.getConfigurationSection(world.getName() + "." + key).getKeys(false)) {
                String frontConfig = world.getName() + "." + key + "." + key2 + ".";
                double x = 0.0;
                double y = 0.0;
                double z = 0.0;

                if(this.plugin.serverVersionMajor.equals("8"))
                {
                    x = (event.getPacket().getIntegers().read(1) / 32.0);
                    y = (event.getPacket().getIntegers().read(2) / 32.0);
                    z = (event.getPacket().getIntegers().read(3) / 32.0);
                }
                else if(this.plugin.serverVersionMajor.equals("9"))
                {
                    x = (event.getPacket().getDoubles().read(0));
                    y = (event.getPacket().getDoubles().read(1));
                    z = (event.getPacket().getDoubles().read(2));
                }

                Player player = event.getPlayer();
                double playerX = player.getLocation().getX();
                double playerZ = player.getLocation().getZ();
                Location testEventLoc = new Location(world, x, 0, z);
                int frontRadiusX = Configuration.fronts_config.getInt(frontConfig + "radius-x");
                int frontRadiusZ = Configuration.fronts_config.getInt(frontConfig + "radius-z");
                int frontX = Configuration.fronts_config.getInt(frontConfig + "center-x");
                int frontZ = Configuration.fronts_config.getInt(frontConfig + "center-z");
                int seeOutside = Configuration.main_config.getInt(simConfig + "lightning-distance-outside");
                String[] front = test.locationInWhichFront(testEventLoc, false, false);

                if (front[1] == null) {
                    continue;
                }

                if (frontX + frontRadiusX + seeOutside > playerX && frontX - frontRadiusX - seeOutside < playerX
                        && frontZ + frontRadiusZ + seeOutside > playerZ && frontZ - frontRadiusZ - seeOutside < playerZ) {
                    event.setCancelled(false);
                    break;
                }
            }
        }
    }

    public void changeWeather(Player player, String[] front) {
        World world = player.getWorld();

        if (!test.worldIsEnabled(world)) {
            return;
        }

        PacketContainer packet1 = protocolManager.createPacket(PacketType.Play.Server.GAME_STATE_CHANGE);

        if (front[1] == null) {
            packet1.getIntegers().write(0, 1);
            packet1.getFloat().write(0, 0.0F);
        } else {
            String frontConfig = world.getName() + "." + front[0] + "." + front[1] + ".";
            String simConfig = "worlds." + world.getName() + "." + front[0] + ".";
            int intensity = Integer.parseInt(front[2]);

            if (Configuration.fronts_config.getInt(frontConfig + "lightning-per-minute") == 0) {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 0.0F);

            } else if (Configuration.fronts_config.getInt(frontConfig + "lightning-per-minute") != 0) {
                packet1.getIntegers().write(0, 8);
                packet1.getFloat().write(0, 1.0F);
            }

            if (Configuration.main_config.getBoolean(simConfig + "use-intensity-for-light-level")) {
                int maxIntensity = Configuration.main_config.getInt(simConfig + "maximum-intensity");

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
            protocolManager.sendServerPacket(player, packet);
        } catch (Exception e) {
            throw new RuntimeException("Cannot send packet " + packet, e);
        }
    }

    public void onStateChangePacket(PacketEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!test.worldIsEnabled(world)) {
            return;
        }

        event.getPacket().getIntegers().write(0, 7);
        event.getPacket().getFloat().write(0, 1.0F);

    }
}
