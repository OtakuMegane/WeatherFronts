package com.minefit.xerxestireiron.weatherfronts.NMSBullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
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

            if (this.nmsVersion.equals("v1_11_R1") || this.nmsVersion.equals("v1_12_R1")) {
                entity = location.getWorld().spawnEntity(location, EntityType.SKELETON_HORSE);
                fieldName = "p";
            } else if (this.nmsVersion.equals("v1_10_R1")) {
                entity = location.getWorld().spawnEntity(location, EntityType.HORSE);
                Horse e2 = (Horse) entity;
                e2.setVariant(org.bukkit.entity.Horse.Variant.SKELETON_HORSE);
                fieldName = "y";
            }

            Object nmsHorse = this.nmsAPI.bukkitToNMS(entity);
            Method isTrap = nmsHorse.getClass().getMethod(fieldName, boolean.class);
            isTrap.invoke(nmsHorse, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fishingTime(Fish hook, int time) {

        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_10_R1")) {
                fieldName = "av";
            } else if (this.nmsVersion.equals("v1_11_R1")) {
                fieldName = "h";
            } else if (this.nmsVersion.equals("v1_12_R1")) {
                fieldName = "h";
            } else {
                return;
            }

            Field fishTime = nmsHook.getClass().getDeclaredField(fieldName);
            fishTime.setAccessible(true);
            fishTime.setInt(nmsHook, time);
            fishTime.setAccessible(false);

            Field en = nmsHook.getClass().getDeclaredField("ax");
            this.plugin.logger.info("enchant " + en.get(nmsHook));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getRodLureLevel(Fish hook) {

        try {
            Object nmsHook = this.nmsAPI.bukkitToNMS(hook);
            String fieldName = null;

            if (this.nmsVersion.equals("v1_11_R1")) {
                fieldName = "ax";
            } else if (this.nmsVersion.equals("v1_12_R1")) {
                fieldName = "ax";
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
