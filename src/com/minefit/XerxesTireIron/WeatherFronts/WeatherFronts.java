package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;

public class WeatherFronts extends JavaPlugin {
    Configuration config;
    FireHandler fireHandler;
    FrontEngine frontsHandler;
    WeatherListener weatherListener;
    CommandHandler commands;
    EntityHandler entityHandler;
    PacketHandler packetHandler;
    DynmapFunctions dynmapFunctions;
    FrontGenerator frontGenerator;
    WorldListener worldListener;
    String serverVersionMajor;
    String serverVersionMinor;

    private int fiveTickTask;
    private int twentyTickTask;
    private HashMap<String, Integer> taskDelay;

    private Logger logger = Logger.getLogger("Minecraft");
    private BukkitScheduler scheduler = Bukkit.getServer().getScheduler();

    @Override
    public void onEnable() {
        config = new Configuration(this);
        config.loadMainConfig();

        fireHandler = new FireHandler(this);
        frontsHandler = new FrontEngine(this);
        weatherListener = new WeatherListener(this);
        entityHandler = new EntityHandler(this);
        packetHandler = new PacketHandler(this);
        dynmapFunctions = new DynmapFunctions(this);
        frontGenerator = new FrontGenerator(this);
        worldListener = new WorldListener(this);
        commands = new CommandHandler(this);

        String name = getServer().getClass().getPackage().getName();
        String v1 = name.substring(name.lastIndexOf(".") + 1);
        String[] vn = v1.split("_");
        this.serverVersionMajor = vn[1];
        this.serverVersionMinor = vn[2];

        taskDelay = new HashMap<String, Integer>();
        taskDelay.put("40t", 1);
        taskDelay.put("600t", 1);

        this.getServer().getPluginManager().registerEvents(fireHandler, this);
        this.getServer().getPluginManager().registerEvents(frontsHandler, this);
        this.getServer().getPluginManager().registerEvents(weatherListener, this);
        this.getServer().getPluginManager().registerEvents(entityHandler, this);
        this.getServer().getPluginManager().registerEvents(worldListener, this);

        final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

        protocolManager.addPacketListener(new PacketAdapter(this, Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                packetHandler.onSoundPacket(event);
            }
        });

        protocolManager.addPacketListener(new PacketAdapter(this, Play.Server.SPAWN_ENTITY_WEATHER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                packetHandler.onLightningPacket(event);
            }
        });

        scheduler.scheduleSyncDelayedTask(this, new Runnable() {
            @Override
            public void run() {
                dynmapFunctions.checkDynmapSetting();
                config.loadWorlds();
                config.loadAllFronts();

                for (String worldName : Configuration.main_config.getConfigurationSection("worlds-enabled").getKeys(false)) {
                    if (Bukkit.getServer().getWorld(worldName) != null) {
                        World world = Bukkit.getServer().getWorld(worldName);

                        for (String simulator : Configuration.fronts_config.getConfigurationSection(worldName).getKeys(false)) {
                            for (String front : Configuration.fronts_config.getConfigurationSection(worldName + "." + simulator)
                                    .getKeys(false)) {
                                dynmapFunctions.addMarker(world, simulator, front);
                            }
                        }
                    }
                }
            }
        }, 0L);

        fiveTickTask = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (String worldName : Configuration.main_config.getConfigurationSection("worlds-enabled").getKeys(false)) {
                    if (Bukkit.getServer().getWorld(worldName) != null) {
                        World world = Bukkit.getServer().getWorld(worldName);
                        entityHandler.changePlayerWeather(world);
                        entityHandler.affectEndermen(world);
                        entityHandler.affectWolves(world);
                        entityHandler.affectBlazes(world);
                        entityHandler.affectSnowmen(world);
                        entityHandler.affectArrows(world);

                        for (String simulator : Configuration.fronts_config.getConfigurationSection(worldName).getKeys(false)) {
                            for (String front : Configuration.fronts_config.getConfigurationSection(worldName + "." + simulator)
                                    .getKeys(false)) {
                                frontsHandler.lightningGen(world, simulator, front);
                                frontsHandler.precipitationBlockEffects(world, simulator, front);
                            }
                        }
                    }
                }
            }
        }, 1L, 5L);

        twentyTickTask = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                fireHandler.extinguishFire();

                if (taskDelay.get("40t") >= 2) {
                    frontsHandler.hydrateFarmland();
                    taskDelay.put("40t", 1);
                } else {
                    taskDelay.put("40t", taskDelay.get("40t") + 1);
                }

                for (String worldName : Configuration.main_config.getConfigurationSection("worlds-enabled").getKeys(false)) {
                    if (Bukkit.getServer().getWorld(worldName) != null) {
                        World world = Bukkit.getServer().getWorld(worldName);

                        entityHandler.spawnMobs(world);

                        for (String simulator : Configuration.fronts_config.getConfigurationSection(worldName).getKeys(false)) {
                            if (taskDelay.get("600t") >= 600) {
                                if (!Configuration.main_config.getBoolean("worlds." + worldName + "." + simulator
                                        + ".generate-fronts")) {
                                    continue;
                                }

                                Map<String, String> frontParams = new HashMap<String, String>();
                                frontParams.put("random", "true");
                                frontParams.put("command", "false");
                                frontGenerator.generateNewFront(world, simulator, frontParams);
                            } else {
                                taskDelay.put("600t", taskDelay.get("600t") + 1);
                            }

                            for (String front : Configuration.fronts_config.getConfigurationSection(worldName + "." + simulator)
                                    .getKeys(false)) {
                                frontsHandler.moveFront(world, simulator, front);
                                frontsHandler.ageFront(world, simulator, front);
                            }
                        }
                    }
                }
            }
        }, 1L, 20L);

        getCommand("fronts").setExecutor(commands);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(fiveTickTask);
        getServer().getScheduler().cancelTask(twentyTickTask);
        config.saveFronts("all");
        Configuration.default_config = null;
        Configuration.main_config = null;
        Configuration.worlds_config = null;
        Configuration.fronts_config = null;
        config = null;
    }
}
