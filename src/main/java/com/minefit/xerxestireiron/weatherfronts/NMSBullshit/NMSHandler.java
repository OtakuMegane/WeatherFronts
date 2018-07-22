package com.minefit.xerxestireiron.weatherfronts.NMSBullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Horse;

import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;

public class NMSHandler {
    private final WeatherFronts plugin;
    private final String nmsVersion;
    private final NMS_API nmsAPI;

    public NMSHandler(WeatherFronts instance) {
        this.plugin = instance;
        this.nmsVersion = this.plugin.serverVersion.getNMSVersion();
        this.nmsAPI = new NMS_API(this.plugin);
    }

    public void createHorseTrap(Location location) {

        try {
            String fieldName = null;
            Entity entity = null;

            if (this.nmsVersion.equals("v1_13_R1")) {
                entity = location.getWorld().spawnEntity(location, EntityType.SKELETON_HORSE);
                fieldName = "s";
            }

            Object nmsHorse = this.nmsAPI.bukkitToNMS(entity);
            Method isTrap = nmsHorse.getClass().getMethod(fieldName, boolean.class);
            isTrap.invoke(nmsHorse, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Update this
    public void fishingTime(FishHook hook, int time) {

        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_13_R1")) {
                fieldName = "h";
            } else {
                return;
            }

            Field fishTime = nmsHook.getClass().getDeclaredField(fieldName);
            fishTime.setAccessible(true);
            fishTime.setInt(nmsHook, time);

            Field en = nmsHook.getClass().getDeclaredField("ax");
            this.plugin.logger.info("enchant " + en.get(nmsHook)); // TODO remove
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: Update this
    public int getRodLureLevel(FishHook hook) {

        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_13_R1")) {
                fieldName = "aA";
            } else {
                return 0;
            }

            Field en = nmsHook.getClass().getDeclaredField(fieldName);
            en.setAccessible(true);
            return en.getInt(nmsHook);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
