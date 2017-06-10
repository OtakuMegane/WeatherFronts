package com.minefit.XerxesTireIron.WeatherFronts;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

public class SaveData {
    private final WeatherFronts plugin;

    public SaveData(WeatherFronts instance) {
        this.plugin = instance;
    }

    public void saveToYamlFile(String fileDirectory, String fileName, YamlConfiguration yamlConfig) {
        try {
            File yamlFile = new File(
                    this.plugin.getDataFolder() + File.separator + fileDirectory + File.separator + fileName);

            if (!yamlFile.exists()) {
                new File(this.plugin.getDataFolder() + File.separator + fileDirectory).mkdirs();
                yamlFile.createNewFile();
            }

            yamlConfig.save(yamlFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
