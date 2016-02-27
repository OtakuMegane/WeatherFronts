package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Map;

import org.bukkit.World;

public class FrontGenerator {
    private XORShiftRandom random = new XORShiftRandom();
    private WeatherFronts plugin;

    public FrontGenerator(WeatherFronts instance) {
        plugin = instance;
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    public String[] generateNewFront(World world, String simulator, Map<String, String> frontParams) {
        String[] frontData = new String[10];
        String worldName = world.getName();

        if (!test.worldIsEnabled(world)) {
            frontData[1] = "worlddisabled";
            return frontData;
        }

        if (simulator.equalsIgnoreCase("")) {
            simulator = test.randomSimulator(world);
        }

        frontData[2] = simulator;
        String simConfig = "worlds." + world.getName() + "." + simulator + ".";
        int simRange = Configuration.main_config.getInt(simConfig + "simulation-radius");
        int simCenterX = Configuration.main_config.getInt(simConfig + "simulation-center-x");
        int simCenterZ = Configuration.main_config.getInt(simConfig + "simulation-center-z");

        boolean validId = false;

        while (!validId) {
            frontData[0] = "front" + Integer.toString(random.nextInt(1000));

            if (!Configuration.fronts_config.contains(worldName + "." + simulator + "." + frontData[0])) {
                validId = true;
            }
        }

        String frontConfig = world.getName() + "." + simulator + "." + frontData[0] + ".";
        int frontCount = 0;

        for (String key : Configuration.fronts_config.getConfigurationSection(worldName + "." + simulator).getKeys(false)) {
            int frontAgeLimit = Configuration.fronts_config.getInt(worldName + "." + simulator + "." + key + ".age-limit");

            if (frontAgeLimit != 0
                    || (frontAgeLimit == 0 && !Configuration.main_config.getBoolean(simConfig + "unending-does-not-count"))
                    || (frontAgeLimit == 0 && Configuration.main_config.getBoolean(simConfig + "unending-does-not-count") && Configuration.main_config
                            .getInt(simConfig + "maximum-age") == 0)) {
                frontCount += 1;
            }
        }

        int maxFronts = Configuration.main_config.getInt(simConfig + "maximum-fronts");
        int chanceNewFront = Configuration.main_config.getInt(simConfig + "chance-new-front");

        if (frontCount >= maxFronts && frontParams.get("command") == "false") {
            frontData[1] = "maxfronts";
            return frontData;
        } else if (random.nextInt(100) > chanceNewFront && frontParams.get("command") == "false") {
            frontData[1] = "nofront";
            return frontData;
        }

        Configuration.fronts_config.createSection(worldName + "." + simulator + "." + frontData[0]);

        if (frontParams.containsKey("name")) {
            frontData[0] = frontParams.get("name");
        }

        if (frontParams.containsKey("shape")) {
            if (frontParams.get("shape").equalsIgnoreCase("rectangle")) {
                Configuration.fronts_config.set(frontConfig + "shape", 1);
            } else {
                Configuration.fronts_config.set(frontConfig + "shape", 1);
            }
        } else {
            String shape = Configuration.main_config.getString(simConfig + "front-shape");

            if (shape.equalsIgnoreCase("rectangle")) {
                Configuration.fronts_config.set(frontConfig + "shape", 1);
            } else {
                Configuration.fronts_config.set(frontConfig + "shape", 1);
            }
        }

        if (frontParams.containsKey("radius-x")) {
            int radiusX = test.limitCheckInt(Integer.parseInt(frontParams.get("radius-x")), simulator, "radius-x");
            Configuration.fronts_config.set(frontConfig + "radius-x", radiusX);
        } else {
            Configuration.fronts_config.set(frontConfig + "radius-x", randomFrontValue(simulator, "radius-x"));
        }

        if (frontParams.containsKey("radius-z")) {
            int radiusZ = test.limitCheckInt(Integer.parseInt(frontParams.get("radius-z")), simulator, "radius-z");
            Configuration.fronts_config.set(frontConfig + "radius-z", radiusZ);
        } else {
            Configuration.fronts_config.set(frontConfig + "radius-z", randomFrontValue(simulator, "radius-z"));
        }

        if (frontParams.containsKey("velocity-x")) {
            int velocityX = test.limitCheckInt(Integer.parseInt(frontParams.get("velocity-x")), simulator, "velocity-x");
            Configuration.fronts_config.set(frontConfig + "velocity-x", velocityX);
        } else {
            Configuration.fronts_config.set(frontConfig + "velocity-x", randomFrontValue(simulator, "velocity-x") * test.posNeg());
        }

        if (frontParams.containsKey("velocity-z")) {
            int velocityZ = test.limitCheckInt(Integer.parseInt(frontParams.get("velocity-z")), simulator, "velocity-z");
            Configuration.fronts_config.set(frontConfig + "velocity-z", velocityZ);
        } else {
            Configuration.fronts_config.set(frontConfig + "velocity-z", randomFrontValue(simulator, "velocity-z") * test.posNeg());
        }

        if (frontParams.containsKey("center-x")) {
            int frontX = Integer.parseInt(frontParams.get("center-x")) + simCenterX;
            Configuration.fronts_config.set(frontConfig + "center-x", frontX);
        } else {
            int frontX = (int) ((Math.random() * ((simRange * 2) + 1)) - (simRange)) + simCenterX;
            Configuration.fronts_config.set(frontConfig + "center-x", frontX);
        }

        if (frontParams.containsKey("center-z")) {
            int frontZ = Integer.parseInt(frontParams.get("center-z")) + simCenterZ;
            Configuration.fronts_config.set(frontConfig + "center-z", frontZ);
        } else {
            int frontZ = (int) ((Math.random() * ((simRange * 2) + 1)) - (simRange)) + simCenterZ;
            Configuration.fronts_config.set(frontConfig + "center-z", frontZ);
        }

        if (frontParams.containsKey("intensity")) {
            int intensity = test.limitCheckInt(Integer.parseInt(frontParams.get("intensity")), simulator, "intensity");
            Configuration.fronts_config.set(frontConfig + "intensity", intensity);
        } else {
            Configuration.fronts_config.set(frontConfig + "intensity", randomFrontValue(simulator, "intensity"));
        }

        if (frontParams.containsKey("lightning-per-minute")) {
            int lightningPerMinute = test.limitCheckInt(Integer.parseInt(frontParams.get("lightning-per-minute")), simulator,
                    "lightning-per-minute");
            Configuration.fronts_config.set(frontConfig + "lightning-per-minute", lightningPerMinute);
        } else {
            if (random.nextInt(100) < Configuration.main_config.getInt(simConfig + "rain-only-chance")) {
                Configuration.fronts_config.set(frontConfig + "lightning-per-minute", 0);
            } else {
                int lightningPerMinute = randomFrontValue(simulator, "lightning-per-minute");

                if (lightningPerMinute < 1) {
                    lightningPerMinute = 1;
                }

                Configuration.fronts_config.set(frontConfig + "lightning-per-minute", lightningPerMinute);
            }
        }

        if (frontParams.containsKey("age-limit")) {
            int ageLimit = test.limitCheckInt(Integer.parseInt(frontParams.get("age-limit")), simulator, "age");
            Configuration.fronts_config.set(frontConfig + "age-limit", ageLimit);
        } else {
            Configuration.fronts_config.set(frontConfig + "age-limit", randomFrontValue(simulator, "age"));
        }

        Configuration.fronts_config.set(frontConfig + "age", 0);

        plugin.config.saveFronts(worldName);
        plugin.dynmapFunctions.addMarker(world, simulator, frontData[0]);
        return frontData;
    }

    public int randomFrontValue(String simulator, String setting) {
        String worldName = test.getSimulatorWorldName(simulator);
        int minLimit = Configuration.main_config.getInt("worlds." + worldName + "." + simulator + "." + "minimum-" + setting);
        int maxLimit = Configuration.main_config.getInt("worlds." + worldName + "." + simulator + "." + "maximum-" + setting);
        return random.nextIntRange(minLimit, maxLimit);
    }
}
