package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.HashMap;
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
    final WeatherListener weatherListener;
    Commands commands;
    public final PacketHandler packetHandler;
    //final DynmapFunctions dynmapFunctions;
    private final WorldListener worldListener;
    private final ServerVersion serverVersion = new ServerVersion(this);
    private Logger logger = Logger.getLogger("Minecraft");
    public boolean oldPacket;
    private final Map<String, FrontsWorld> worlds = new HashMap<String, FrontsWorld>();
    private YamlConfiguration mainConfig;
    private final LoadData load = new LoadData(this);
    private final SaveData save = new SaveData(this);
    public ProtocolManager protocolManager;

    public WeatherFronts() {
        this.worldListener = new WorldListener(this);
        this.weatherListener = new WeatherListener(this);
        this.packetHandler = new PacketHandler(this);
        //this.dynmapFunctions = new DynmapFunctions(this);
        this.commands = new Commands(this);
    }

    @Override
    public void onEnable() {
        this.mainConfig = this.load.loadMainConfig();
        this.protocolManager = ProtocolLibrary.getProtocolManager();

        if (this.serverVersion.major.equals("7") || serverVersion.major.equals("8")) {
            oldPacket = true;
        } else {
            oldPacket = false;
        }

        this.getServer().getPluginManager().registerEvents(weatherListener, this);
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

        for (String worldName : this.mainConfig.getConfigurationSection("worlds-enabled").getKeys(false)) {
            addWorld(worldName);
        }

        getCommand("fronts").setExecutor(this.commands);
    }

    @Override
    public void onDisable() {
        for (Entry<String, FrontsWorld> entry : this.worlds.entrySet()) {
            entry.getValue().shutdown();
        }
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
