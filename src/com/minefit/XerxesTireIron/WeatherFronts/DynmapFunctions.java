package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.logging.Logger;

import org.bukkit.World;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

public class DynmapFunctions {
    private WeatherFronts plugin;
    private DynmapAPI dynmapAPI;
    private Boolean dynmapEnabled = false;
    private MarkerAPI markerAPI;
    private MarkerSet frontMarkers;
    private Logger logger = Logger.getLogger("Minecraft");

    public DynmapFunctions(WeatherFronts instance) {
        plugin = instance;
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    public void checkDynmapSetting() {
        if (Configuration.main_config.getBoolean("use-dynmap") && plugin.getServer().getPluginManager().getPlugin("dynmap") != null) {
            dynmapAPI = (DynmapAPI) plugin.getServer().getPluginManager().getPlugin("dynmap");

            if (dynmapAPI.markerAPIInitialized()) {
                markerAPI = dynmapAPI.getMarkerAPI();
                dynmapEnabled = true;
                logger.info("[WeatherFronts] Dynmap detected and enabled for WeatherFronts");

                if (markerAPI.getMarkerSet("Weather") != null) {
                    frontMarkers = markerAPI.getMarkerSet("Weather");
                } else {
                    frontMarkers = markerAPI.createMarkerSet("Weather", "Weather", null, false);
                }
            }
        } else {
            dynmapEnabled = false;
            logger.info("[WeatherFronts] Dynmap not detected or did not properly initialize. Not enabled for WeatherFronts.");
        }
    }

    public void addMarker(World world, String simulator, String frontId) {
        if (!dynmapEnabled) {
            return;
        }

        String frontConfig = world.getName() + "." + simulator + "." + frontId + ".";
        int markerShape = Configuration.fronts_config.getInt(frontConfig + "shape");
        int frontRadiusX = Configuration.fronts_config.getInt(frontConfig + "radius-x");
        int frontRadiusZ = Configuration.fronts_config.getInt(frontConfig + "radius-z");
        Double frontX = Configuration.fronts_config.getDouble(frontConfig + "center-x");
        Double frontZ = Configuration.fronts_config.getDouble(frontConfig + "center-z");

        if (markerShape == 1) {
            double[] x = new double[4];
            double[] z = new double[4];
            x[0] = frontX + frontRadiusX;
            x[1] = frontX + frontRadiusX;
            x[2] = frontX - frontRadiusX;
            x[3] = frontX - frontRadiusX;
            z[0] = frontZ + frontRadiusZ;
            z[1] = frontZ - frontRadiusZ;
            z[2] = frontZ - frontRadiusZ;
            z[3] = frontZ + frontRadiusZ;
            AreaMarker newMarker = frontMarkers.createAreaMarker(frontId, frontId, false, world.getName(), x, z, false);

            if (newMarker == null) {
                logger.info("[WeatherFronts] An error occurred while creating the marker for " + frontId);
                return;
            }
            newMarker.setLineStyle(1, 1.0D, 0xffffff);
            newMarker.setFillStyle(0.40000000000000002D, 0xffffff);
        }
    }

    public void moveMarker(World world, String simulator, String frontId) {
        if (!dynmapEnabled) {
            return;
        }

        String frontConfig = world.getName() + "." + simulator + "." + frontId + ".";
        int markerShape = Configuration.fronts_config.getInt(frontConfig + "shape");
        int frontRadiusX = Configuration.fronts_config.getInt(frontConfig + "radius-x");
        int frontRadiusZ = Configuration.fronts_config.getInt(frontConfig + "radius-z");
        Double frontX = Configuration.fronts_config.getDouble(frontConfig + "center-x");
        Double frontZ = Configuration.fronts_config.getDouble(frontConfig + "center-z");

        if (markerShape == 1 && frontMarkers.findAreaMarker(frontId) != null) {
            double[] x = new double[4];
            double[] z = new double[4];
            x[0] = frontX + frontRadiusX;
            x[1] = frontX + frontRadiusX;
            x[2] = frontX - frontRadiusX;
            x[3] = frontX - frontRadiusX;
            z[0] = frontZ + frontRadiusZ;
            z[1] = frontZ - frontRadiusZ;
            z[2] = frontZ - frontRadiusZ;
            z[3] = frontZ + frontRadiusZ;

            frontMarkers.findAreaMarker(frontId).setCornerLocations(x, z);
        }
    }

    public void deleteMarker(World world, String simulator, String frontId) {
        if (!dynmapEnabled) {
            return;
        }

        String frontConfig = world.getName() + "." + simulator + "." + frontId + ".";
        int markerShape = Configuration.fronts_config.getInt(frontConfig + "shape");

        if ((markerShape == 1) && frontMarkers.findAreaMarker(frontId) != null) {
            frontMarkers.findAreaMarker(frontId).deleteMarker();
        }
    }
}
