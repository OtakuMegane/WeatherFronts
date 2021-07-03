package com.minefit.xerxestireiron.weatherfronts;

import java.awt.geom.Point2D;

import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapFunctions {
    private final WeatherFronts plugin;
    private DynmapCommonAPI dynmapAPI;
    private Boolean dynmapEnabled = false;
    private MarkerAPI markerAPI;
    private MarkerSet stormMarkers;

    public DynmapFunctions(WeatherFronts instance) {
        this.plugin = instance;
    }

    public boolean initDynmap() {
        Plugin dynmapPlugin = this.plugin.getServer().getPluginManager().getPlugin("dynmap");

        if (dynmapPlugin == null) {
            return false;
        }

        this.dynmapAPI = (DynmapCommonAPI) dynmapPlugin;

        try {
            if (!dynmapAPI.markerAPIInitialized()) {
                return false;
            }
        } catch (NullPointerException e) {
            return false;
        }

        this.markerAPI = dynmapAPI.getMarkerAPI();

        if (this.markerAPI.getMarkerSet("Weather") != null) {
            this.stormMarkers = this.markerAPI.getMarkerSet("Weather");
        } else {
            this.stormMarkers = this.markerAPI.createMarkerSet("Weather", "Weather", null, false);
        }

        this.dynmapEnabled = true;

        return true;

    }

    public void addMarker(String worldName, String stormName, Point2D[] boundaries) {
        if (!this.dynmapEnabled) {
            return;
        }

        double[] x = new double[4];
        double[] z = new double[4];

        for (int i = 0; i < boundaries.length; ++i) {
            x[i] = boundaries[i].getX();
            z[i] = boundaries[i].getY();
        }

        AreaMarker newMarker = this.stormMarkers.createAreaMarker(stormName, stormName, false, worldName, x, z, false);

        if (newMarker == null) {
            this.plugin.logger.info("[WeatherFronts] An error occurred while creating the marker for " + stormName);
            return;
        }

        newMarker.setLineStyle(1, 1.0D, 0xffffff);
        newMarker.setFillStyle(0.40000000000000002D, 0xffffff);
    }

    public void moveMarker(String worldName, String stormName, Point2D[] boundaries) {
        if (!this.dynmapEnabled) {
            return;
        }

        if (this.stormMarkers.findAreaMarker(stormName) != null) {
            double[] x = new double[4];
            double[] z = new double[4];

            for (int i = 0; i < boundaries.length; ++i) {
                x[i] = boundaries[i].getX();
                z[i] = boundaries[i].getY();
            }

            this.stormMarkers.findAreaMarker(stormName).setCornerLocations(x, z);
        } else {
            addMarker(worldName, stormName, boundaries);
        }
    }

    public void deleteMarker(World world, String simulator, String stormId) {
        if (!this.dynmapEnabled) {
            return;
        }

        if (this.stormMarkers.findAreaMarker(stormId) != null) {
            this.stormMarkers.findAreaMarker(stormId).deleteMarker();
        }
    }
}
