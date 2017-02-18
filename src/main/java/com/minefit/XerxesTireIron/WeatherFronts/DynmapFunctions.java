package com.minefit.XerxesTireIron.WeatherFronts;

import java.awt.geom.Point2D;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapFunctions implements Listener {
    private final WeatherFronts plugin;
    private DynmapAPI dynmapAPI;
    private Boolean dynmapEnabled = false;
    private MarkerAPI markerAPI;
    private MarkerSet frontMarkers;

    public DynmapFunctions(WeatherFronts instance) {
        this.plugin = instance;
        this.dynmapAPI = (DynmapAPI) this.plugin.getServer().getPluginManager().getPlugin("dynmap");
    }

    /*@EventHandler(priority = EventPriority.NORMAL)
    public void onPluginEnable(PluginEnableEvent event) {
        if (event.getPlugin().getName().equals("dynmap")) {
            this.dynmapEnabled = initDynmap();
        }
    }*/

    public boolean initDynmap() {
        if (this.dynmapAPI == null) {
            this.plugin.logger.info(
                    "[WeatherFronts] Dynmap not detected or did not properly initialize. Not enabled for WeatherFronts.");
            return false;
        }

        if (this.dynmapAPI.markerAPIInitialized()) {
            this.markerAPI = this.dynmapAPI.getMarkerAPI();
            this.plugin.logger.info("[WeatherFronts] Dynmap detected and enabled for WeatherFronts");

            if (this.markerAPI.getMarkerSet("Weather") != null) {
                this.frontMarkers = this.markerAPI.getMarkerSet("Weather");
            } else {
                this.frontMarkers = this.markerAPI.createMarkerSet("Weather", "Weather", null, false);
            }

            this.dynmapEnabled = true;
        }

        return true;

    }

    public void addMarker(String worldName, String frontName, Point2D[] boundaries) {
        if (!this.dynmapEnabled) {
            return;
        }

        double[] x = new double[4];
        double[] z = new double[4];

        for (int i = 0; i < boundaries.length; ++i) {
            x[i] = boundaries[i].getX();
            z[i] = boundaries[i].getY();
        }

        AreaMarker newMarker = this.frontMarkers.createAreaMarker(frontName, frontName, false, worldName, x, z, false);

        if (newMarker == null) {
            this.plugin.logger.info("[WeatherFronts] An error occurred while creating the marker for " + frontName);
            return;
        }

        newMarker.setLineStyle(1, 1.0D, 0xffffff);
        newMarker.setFillStyle(0.40000000000000002D, 0xffffff);
    }

    public void moveMarker(String worldName, String frontName, Point2D[] boundaries) {
        if (!this.dynmapEnabled) {
            return;
        }

        if (this.frontMarkers.findAreaMarker(frontName) != null) {
            double[] x = new double[4];
            double[] z = new double[4];

            for (int i = 0; i < boundaries.length; ++i) {
                x[i] = boundaries[i].getX();
                z[i] = boundaries[i].getY();
            }

            this.frontMarkers.findAreaMarker(frontName).setCornerLocations(x, z);
        } else {
            addMarker(worldName, frontName, boundaries);
        }
    }

    public void deleteMarker(World world, String simulator, String frontId) {
        if (!this.dynmapEnabled) {
            return;
        }

        if (this.frontMarkers.findAreaMarker(frontId) != null) {
            this.frontMarkers.findAreaMarker(frontId).deleteMarker();
        }
    }
}
