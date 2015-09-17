package com.minefit.XerxesTireIron.WeatherFronts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

public class Configuration
{
    static YamlConfiguration default_config = new YamlConfiguration();
    static YamlConfiguration worlds_config = new YamlConfiguration();
    static YamlConfiguration main_config = new YamlConfiguration();
    static YamlConfiguration fronts_config = new YamlConfiguration();
    private WeatherFronts plugin;

    public Configuration(WeatherFronts instance)
    {
        plugin = instance;
    }

    public void loadMainConfig()
    {

        if(!plugin.getDataFolder().exists())
        {
            plugin.getDataFolder().mkdir();
        }

        try
        {
            File configFile = new File(plugin.getDataFolder() + File.separator + "config.yml");

            if(!configFile.exists())
            {
                copy(plugin.getResource("config.yml"), configFile);
            }

            main_config.load(configFile);
            main_config.createSection("worlds");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadWorlds()
    {
        Iterator<World> iterator = Bukkit.getServer().getWorlds().iterator();

        while (iterator.hasNext())
        {
            loadWorld(iterator.next().getName());
        }
    }


    public void loadWorld(String world)
    {
        try
        {
            if(main_config.getBoolean("worlds-enabled." + world) && !main_config.contains("worlds." + world))
            {
                File defaultsFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "defaults.yml");

                if(!defaultsFile.exists())
                {
                    new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                    copy(plugin.getResource("defaults.yml"), defaultsFile);
                }

                YamlConfiguration world_defaults = new YamlConfiguration();
                world_defaults.load(defaultsFile);

                File simulatorFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "simulators.yml");

                if(!simulatorFile.exists())
                {
                    new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                    copy(plugin.getResource("simulators.yml"), simulatorFile);
                }

                YamlConfiguration world_temp = new YamlConfiguration();
                world_temp.load(simulatorFile);

                // Loop through default settings
                for(String key : world_defaults.getKeys(false))
                {
                    // Loop through each simulator for the current setting
                    for(String key2 : world_temp.getKeys(false))
                    {
                        if(!world_temp.contains(key2 + "." + key))
                        {
                            world_temp.set(key2 + "." + key, world_defaults.get(key));
                        }
                    }

                    main_config.createSection("worlds." + world, world_temp.getValues(true));
                }
            }
            else
            {
                File defaultsFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "defaults.yml");
                File simulatorFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "simulators.yml");

                if(!defaultsFile.exists())
                {
                    new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                    defaultsFile.createNewFile();
                }

                if(!simulatorFile.exists())
                {
                    new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                    simulatorFile.createNewFile();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void loadAllFronts()
    {
        loadFronts("", true);
    }

    public void loadFronts(String world, boolean allFronts)
    {
        try
        {
            if(allFronts)
            {
                Iterator<World> iterator = Bukkit.getServer().getWorlds().iterator();

                while (iterator.hasNext())
                {
                    String worldName = iterator.next().getName();

                    if(main_config.getBoolean("worlds-enabled." + worldName))
                    {
                        YamlConfiguration simulators_temp = new YamlConfiguration();
                        File simulatorsFile = new File(plugin.getDataFolder() + File.separator + worldName + File.separator + "simulators.yml");
                        simulators_temp.load(simulatorsFile);

                        YamlConfiguration fronts_temp = new YamlConfiguration();
                        File frontsFile = new File(plugin.getDataFolder() + File.separator + worldName + File.separator + "fronts.yml");

                        if(!frontsFile.exists())
                        {
                            new File(plugin.getDataFolder() + File.separator + worldName).mkdirs();
                            frontsFile.createNewFile();
                        }

                        fronts_temp.load(frontsFile);

                        if(fronts_temp.getValues(true).isEmpty())
                        {
                            fronts_config.createSection(worldName);
                        }
                        else
                        {
                            fronts_config.set(worldName, fronts_temp.getConfigurationSection(worldName));
                        }

                        for(String key2 : simulators_temp.getKeys(false))
                        {
                            if(!fronts_config.getConfigurationSection(worldName).contains(key2))
                            {
                                fronts_config.createSection(worldName + "." + key2);
                            }
                        }
                    }
                }
            }
            else
            {
                if(main_config.getBoolean("worlds-enabled." + world))
                {
                    YamlConfiguration fronts_temp = new YamlConfiguration();
                    File frontsFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "fronts.yml");

                    if(!frontsFile.exists())
                    {
                        new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                        frontsFile.createNewFile();
                    }

                    fronts_temp.load(frontsFile);

                    if(fronts_temp.getValues(true).isEmpty())
                    {
                        fronts_config.createSection(world);
                        fronts_config.createSection(world + ".simulator1");
                    }
                    else
                    {
                        fronts_config.set(world, fronts_temp.getConfigurationSection(world));
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void saveFronts(String world)
    {
        try
        {
            if(world.equalsIgnoreCase("all"))
            {
                Iterator<World> iterator = Bukkit.getServer().getWorlds().iterator();

                while (iterator.hasNext())
                {
                    String worldName = iterator.next().getName();

                    File frontsFile = new File(plugin.getDataFolder() + File.separator + worldName + File.separator + "fronts.yml");

                    if(!frontsFile.exists())
                    {
                        new File(plugin.getDataFolder() + File.separator + worldName).mkdirs();
                        frontsFile.createNewFile();
                    }

                    if(fronts_config.contains(worldName))
                    {
                        YamlConfiguration temp_config = new YamlConfiguration();
                        temp_config.set(worldName, fronts_config.getConfigurationSection(worldName));
                        temp_config.save(frontsFile);
                    }
                }
            }
            else
            {
                File frontsFile = new File(plugin.getDataFolder() + File.separator + world + File.separator + "fronts.yml");

                if(!frontsFile.exists())
                {
                    new File(plugin.getDataFolder() + File.separator + world).mkdirs();
                    frontsFile.createNewFile();
                }

                if(fronts_config.contains(world))
                {
                    YamlConfiguration temp_config = new YamlConfiguration();
                    temp_config.set(world, fronts_config.getConfigurationSection(world));
                    temp_config.save(frontsFile);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void renameFront(String world, String original, String newname)
    {
        fronts_config.createSection(world + "." + newname,
                fronts_config.getConfigurationSection(world + "." + original).getValues(true));
        fronts_config.getConfigurationSection(world).set(original, null);
    }

    private void copy(InputStream in, File file)
    {
        try
        {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf)) > 0)
            {
                out.write(buf,0,len);
            }
            out.close();
            in.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
