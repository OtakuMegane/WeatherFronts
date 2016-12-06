package com.minefit.XerxesTireIron.WeatherFronts;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

public class LoadData {
    private final WeatherFronts plugin;

    public LoadData(WeatherFronts instance) {
        this.plugin = instance;

        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdir();
        }
    }

    public YamlConfiguration loadMainConfig() {
        YamlConfiguration main_config = new YamlConfiguration();

        try {
            File configFile = new File(this.plugin.getDataFolder() + File.separator + "config.yml");

            if (!configFile.exists()) {
                configFile.mkdirs();
                copy(this.plugin.getResource("config.yml"), configFile);
            }

            main_config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return main_config;
    }

    public YamlConfiguration loadConfigForWorld(String worldName, String fileName, boolean copyFile) {
        YamlConfiguration config = new YamlConfiguration();

        try {
            File configFile = new File(this.plugin.getDataFolder() + File.separator + worldName + File.separator + fileName);

            if (!configFile.exists()) {
//                configFile.mkdirs();

                if (copyFile) {
                    copy(this.plugin.getResource(fileName), configFile);
                }
                else
                {
                    configFile.createNewFile();
                }
            }

            config.load(configFile);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return config;
    }

    public YamlConfiguration combineConfigDefaults(String path, YamlConfiguration defaults, YamlConfiguration config) {

        YamlConfiguration combined = new YamlConfiguration();
        // Loop through default settings
        for (String key : defaults.getKeys(false)) {
            // Loop through each simulator for the current setting
            ConfigurationSection config2 = config.getConfigurationSection(path);
            for (String key2 : config2.getKeys(false)) {
                if (!config2.contains(key)) {
                    combined.set(key, defaults.get(key));
                } else {
                    combined.set(key, config2.get(key));
                }
            }
        }

        return combined;
    }

    public YamlConfiguration loadSimulatorFrontsData(String simulatorName, YamlConfiguration worldFrontsData) {
        YamlConfiguration simulatorFronts = (YamlConfiguration) worldFrontsData.getConfigurationSection(simulatorName);
        return simulatorFronts;
    }

    public YamlConfiguration loadFrontData(String frontName, YamlConfiguration simulatorFrontsData) {
        YamlConfiguration frontData = new YamlConfiguration();
        ConfigurationSection frontSection = simulatorFrontsData.getConfigurationSection(frontName);
        for (String key : frontSection.getKeys(true)) {
            frontData.set(key, frontSection.get(key));
        }

        return frontData;
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
