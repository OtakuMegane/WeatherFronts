package com.minefit.xerxestireiron.weatherfronts.NMSBullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.SkeletonHorse;

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
            String methodName = null;
            Entity entity = null;

            if (this.nmsVersion.equals("v1_17_R1")) {
                entity = location.getWorld().spawnEntity(location, EntityType.SKELETON_HORSE);
                methodName = "v";
            } else {
                return;
            }

            Object nmsHorse = this.nmsAPI.bukkitToNMS(entity);
            Method isTrap = nmsHorse.getClass().getMethod(methodName, boolean.class);
            isTrap.invoke(nmsHorse, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fishingTime(FishHook hook, int time) {
        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_16_R1")) {
                fieldName = "ao";
            } else if (this.nmsVersion.equals("v1_16_R2") || this.nmsVersion.equals("v1_16_R3")) {
                fieldName = "ah";
            } else {
                return;
            }

            Field fishTime = nmsHook.getClass().getDeclaredField(fieldName);
            fishTime.setAccessible(true);
            fishTime.setInt(nmsHook, time);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRodLureLevel(FishHook hook) {
        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_16_R1")) {
                fieldName = "av";
            } else if (this.nmsVersion.equals("v1_16_R2") || this.nmsVersion.equals("v1_16_R3")) {
                fieldName = "ao";
            } else {
                return 0;
            }

            Field lureLevel = nmsHook.getClass().getDeclaredField(fieldName);
            lureLevel.setAccessible(true);
            return lureLevel.getInt(nmsHook);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }
}
