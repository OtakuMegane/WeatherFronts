package com.minefit.XerxesTireIron.WeatherFronts.NMSBullshit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Horse;

import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;

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
            } else if (this.nmsVersion.equals("v1_9_R1") || this.nmsVersion.equals("v1_9_R2")) {
                entity = location.getWorld().spawnEntity(location, EntityType.HORSE);
                Horse e2 = (Horse) entity;
                e2.setVariant(org.bukkit.entity.Horse.Variant.SKELETON_HORSE);
                fieldName = "x";
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

            if (this.nmsVersion.equals("v1_9_R1")) {
                fieldName = "av";
            } else if (this.nmsVersion.equals("v1_9_R2")) {
                fieldName = "aw";
            } else if (this.nmsVersion.equals("v1_10_R1")) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
