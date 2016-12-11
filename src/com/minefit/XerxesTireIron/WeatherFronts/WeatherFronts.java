package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.comphenix.protocol.PacketType.Play;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld.FrontsWorld;

public class WeatherFronts extends JavaPlugin {
    private final WeatherListener weatherListener = new WeatherListener(this);
    private final Commands commands = new Commands(this);
    private final PacketHandler packetHandler = new PacketHandler(this);
    private final DynmapFunctions dynmap = new DynmapFunctions(this);
    private final WorldListener worldListener = new WorldListener(this);
    private final ServerVersion serverVersion = new ServerVersion(this);
    public final Logger logger = Logger.getLogger("Minecraft");
    private final boolean oldPacket;
    private final Map<String, FrontsWorld> worlds = new HashMap<String, FrontsWorld>();
    private YamlConfiguration mainConfig;
    private final LoadData load = new LoadData(this);
    private final List<String> compatibleVersions;
    private ProtocolManager protocolManager;

    public WeatherFronts() {
        this.compatibleVersions = Arrays.asList("v1_7_R1", "v1_7_R2", "v1_7_R3", "v1_7_R4", "v1_8_R1", "V1_8_R2",
                "v1_8_R3", "v1_9_R1", "v1_9_R2", "v1_10_R1", "v1_11_R1");

        if (!this.serverVersion.compatibleVersion(this.compatibleVersions)) {
            this.logger.info(
                    "[" + this.getName() + " Error] This version of Minecraft is not supported. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        if (this.serverVersion.getMajor().equals("7") || this.serverVersion.getMajor().equals("8")) {
            this.oldPacket = true;
        } else {
            this.oldPacket = false;
        }
    }

    @Override
    public void onEnable() {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
        this.mainConfig = this.load.loadMainConfig();
        this.getServer().getPluginManager().registerEvents(this.dynmap, this);
        this.getServer().getPluginManager().registerEvents(this.weatherListener, this);
        this.getServer().getPluginManager().registerEvents(this.worldListener, this);

        this.protocolManager.addPacketListener(new PacketAdapter(this, Play.Server.NAMED_SOUND_EFFECT) {
            @Override
            public void onPacketSending(PacketEvent event) {
                packetHandler.onSoundPacket(event);
            }
        });

        this.protocolManager.addPacketListener(new PacketAdapter(this, Play.Server.SPAWN_ENTITY_WEATHER) {
            @Override
            public void onPacketSending(PacketEvent event) {
                packetHandler.onLightningPacket(event);
            }
        });

        for (String worldName : this.mainConfig.getConfigurationSection("worlds-enabled").getKeys(false)) {
            addWorld(worldName);
        }

        getCommand("fronts").setExecutor(this.commands);

        this.logger.info(this.getName() + " WeatherFronts has loaded successfully!");
    }

    @Override
    public void onDisable() {
        for (Entry<String, FrontsWorld> entry : this.worlds.entrySet()) {
            entry.getValue().shutdown();
        }
    }

    public DynmapFunctions getDynmap() {
        return this.dynmap;
    }

    public PacketHandler getPacketHandler() {
        return this.packetHandler;
    }

    public ProtocolManager getProtocolManager() {
        return this.protocolManager;
    }

    public boolean useOldPacket() {
        return this.oldPacket;
    }

    public FrontsWorld addWorld(World world) {
        if (worldEnabled(world)) {
            this.worlds.put(world.getName(), new FrontsWorld(this, world));
            return this.worlds.get(world.getName());
        }

        return null;
    }

    public FrontsWorld addWorld(String worldName) {
        if (Bukkit.getServer().getWorld(worldName) != null) {
            return addWorld(Bukkit.getServer().getWorld(worldName));
        }

        return null;
    }

    public void removeWorld(String worldName) {
        this.worlds.remove(worldName);
    }

    public void removeWorld(World world) {
        removeWorld(world.getName());
    }

    public boolean worldEnabled(String worldName) {
        return this.mainConfig.getConfigurationSection("worlds-enabled").contains(worldName);
    }

    public boolean worldEnabled(World world) {
        return worldEnabled(world.getName());
    }

    public FrontsWorld getWorldHandle(String worldName) {
        return this.worlds.get(worldName);
    }

    public FrontsWorld getWorldHandle(World world) {
        return getWorldHandle(world.getName());
    }

    public Map<String, FrontsWorld> getAllFrontsWorlds() {
        return this.worlds;
    }

    public boolean useDynmap() {
        return this.mainConfig.getBoolean("use-dynmap");
    }
}
