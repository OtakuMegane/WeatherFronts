package com.minefit.xerxestireiron.weatherfronts.NMSBullshit;

import java.lang.reflect.Method;

import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;

public class NMS_API {
    private final WeatherFronts plugin;

    public NMS_API(WeatherFronts instance) {
        this.plugin = instance;
    }

    public Class<?> getCraftClass(Object object) {
        return object.getClass();
    }

    public Object getNMSHandle(Class<?> craftClass, Object object) {
        Object nmsHandle = null;

        try {
            Method getHandle = craftClass.getMethod("getHandle");
            nmsHandle = getHandle.invoke(object);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return nmsHandle;
    }

    public Class<?> getNMSClass(Object object) {
        return object.getClass();
    }

    public Object bukkitToNMS(Object object) {
        Class<?> craftClass = getCraftClass(object);
        Object nmsHandle = getNMSHandle(craftClass, object);
        return nmsHandle;
    }
}
