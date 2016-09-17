package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapFunctions {
    private final WeatherFronts plugin;
    private DynmapAPI dynmapAPI;
    private Boolean dynmapEnabled = false;
    private MarkerAPI markerAPI;
    private MarkerSet frontMarkers;

    public DynmapFunctions(WeatherFronts instance) {
        this.plugin = instance;
    }

    public boolean initDynmap() {
        if (this.plugin.getServer().getPluginManager().getPlugin("dynmap") == null) {
            this.plugin.logger.info(
                    "[WeatherFronts] Dynmap not detected or did not properly initialize. Not enabled for WeatherFronts.");
            return false;
        }

        this.dynmapAPI = (DynmapAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");

        if (this.dynmapAPI.markerAPIInitialized()) {
            this.markerAPI = this.dynmapAPI.getMarkerAPI();
            this.dynmapEnabled = true;
            this.plugin.logger.info("[WeatherFronts] Dynmap detected and enabled for WeatherFronts");

            if (this.markerAPI.getMarkerSet("Weather") != null) {
                this.frontMarkers = this.markerAPI.getMarkerSet("Weather");
            } else {
                this.frontMarkers = this.markerAPI.createMarkerSet("Weather", "Weather", null, false);
            }
        }

        return true;

    }

    public void checkDynmapSetting() {
        if (this.plugin.useDynmap() && this.plugin.getServer().getPluginManager().getPlugin("dynmap") != null) {
            this.dynmapAPI = (DynmapAPI) this.plugin.getServer().getPluginManager().getPlugin("dynmap");

            if (this.dynmapAPI.markerAPIInitialized()) {
                this.markerAPI = this.dynmapAPI.getMarkerAPI();
                this.dynmapEnabled = true;
                this.plugin.logger.info("[WeatherFronts] Dynmap detected and enabled for WeatherFronts");

                if (this.markerAPI.getMarkerSet("Weather") != null) {
                    this.frontMarkers = this.markerAPI.getMarkerSet("Weather");
                } else {
                    this.frontMarkers = this.markerAPI.createMarkerSet("Weather", "Weather", null, false);
                }
            }
        } else {
            this.dynmapEnabled = false;
            this.plugin.logger.info(
                    "[WeatherFronts] Dynmap not detected or did not properly initialize. Not enabled for WeatherFronts.");
        }
    }

    public void addMarker(String worldName, String frontName, int[] dimSpeed) {
        if (!this.dynmapEnabled) {
            return;
        }

        double[] x = new double[4];
        double[] z = new double[4];
        x[0] = dimSpeed[0] + dimSpeed[2];
        x[1] = dimSpeed[0] + dimSpeed[2];
        x[2] = dimSpeed[0] - dimSpeed[2];
        x[3] = dimSpeed[0] - dimSpeed[2];
        z[0] = dimSpeed[1] + dimSpeed[3];
        z[1] = dimSpeed[1] - dimSpeed[3];
        z[2] = dimSpeed[1] - dimSpeed[3];
        z[3] = dimSpeed[1] + dimSpeed[3];
        AreaMarker newMarker = this.frontMarkers.createAreaMarker(frontName, frontName, false, worldName, x, z, false);

        if (newMarker == null) {
            this.plugin.logger.info("[WeatherFronts] An error occurred while creating the marker for " + frontName);
            return;
        }
        newMarker.setLineStyle(1, 1.0D, 0xffffff);
        newMarker.setFillStyle(0.40000000000000002D, 0xffffff);
    }

    public void moveMarker(String frontName, int[] dimSpeed) {
        if (!this.dynmapEnabled) {
            return;
        }

        if (this.frontMarkers.findAreaMarker(frontName) != null) {
            double[] x = new double[4];
            double[] z = new double[4];
            x[0] = dimSpeed[0] + dimSpeed[2];
            x[1] = dimSpeed[0] + dimSpeed[2];
            x[2] = dimSpeed[0] - dimSpeed[2];
            x[3] = dimSpeed[0] - dimSpeed[2];
            z[0] = dimSpeed[1] + dimSpeed[3];
            z[1] = dimSpeed[1] - dimSpeed[3];
            z[2] = dimSpeed[1] - dimSpeed[3];
            z[3] = dimSpeed[1] + dimSpeed[3];

            this.frontMarkers.findAreaMarker(frontName).setCornerLocations(x, z);
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
