package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class FunctionsAndTests {
    private XORShiftRandom random = new XORShiftRandom();
    private WeatherFronts plugin;
    private Logger logger = Logger.getLogger("Minecraft");

    public FunctionsAndTests(WeatherFronts instance) {
        plugin = instance;
    }

    /*
     * public int posNeg() { if (random.nextInt(100) < 50) { return -1; } else {
     * return 1; } }
     */

    public boolean createFulgurite(Location location) {
        String[] whichFront = locationInWhichFront(location, false, false);

        if (whichFront[0] != null) {
            World world = location.getWorld();

            if (Configuration.main_config
                    .getBoolean("worlds." + world.getName() + "." + whichFront[0] + ".create-fulgurites")
                    && random.nextInt(100) + 1 < Configuration.main_config
                            .getInt("worlds." + world.getName() + "." + whichFront[0] + ".fulgurite-chance")) {
                return true;
            }
        }

        return false;
    }

    /*
     * public Map<String, Integer> mapFront(String simulator, String frontName)
     * { String frontConfig = getSimulatorWorldName(simulator) + "." + simulator
     * + "." + frontName + "."; Map<String, Integer> frontMap = new
     * HashMap<String, Integer>(); frontMap.put("shape",
     * Configuration.fronts_config.getInt(frontConfig + "shape"));
     * frontMap.put("radius-x", Configuration.fronts_config.getInt(frontConfig +
     * "radius-x")); frontMap.put("radius-z",
     * Configuration.fronts_config.getInt(frontConfig + "radius-z"));
     * frontMap.put("velocity-x", Configuration.fronts_config.getInt(frontConfig
     * + "velocity-x")); frontMap.put("velocity-z",
     * Configuration.fronts_config.getInt(frontConfig + "velocity-z"));
     * frontMap.put("center-x", Configuration.fronts_config.getInt(frontConfig +
     * "center-x")); frontMap.put("center-z",
     * Configuration.fronts_config.getInt(frontConfig + "center-z"));
     * frontMap.put("intensity", Configuration.fronts_config.getInt(frontConfig
     * + "intensity")); frontMap.put("lightning-per-minute",
     * Configuration.fronts_config.getInt(frontConfig +
     * "lightning-per-minute")); frontMap.put("age-limit",
     * Configuration.fronts_config.getInt(frontConfig + "age-limit"));
     * frontMap.put("age", Configuration.fronts_config.getInt(frontConfig +
     * "age")); return frontMap; }
     */
}
