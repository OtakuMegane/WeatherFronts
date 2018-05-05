package com.minefit.xerxestireiron.weatherfronts;

import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.minefit.xerxestireiron.weatherfronts.FrontsWorld.FrontsWorld;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Simulator;
import com.minefit.xerxestireiron.weatherfronts.Storm.Storm;

public class Commands implements CommandExecutor {
    private final WeatherFronts plugin;
    private final SaveData save;

    public Commands(WeatherFronts instance) {
        this.plugin = instance;
        this.save = new SaveData(instance);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arguments) {
        if (!cmd.getName().equalsIgnoreCase("fronts") || arguments.length == 0) {
            return false;
        }

        Player player = null;

        // Eventually you will be able to do stuff from console too
        if (sender instanceof Player) {
            player = (Player) sender;
        } else {
            return false;
        }

        World world = player.getWorld();
        FrontsWorld frontsWorld = this.plugin.getWorldHandle(world);

        if (arguments[0].equalsIgnoreCase("addrandom")) {
            if (!player.hasPermission("weatherfronts.add")) {
                player.sendMessage("You do not have permission to add random storms.");
                return true;
            }

            String newFrontWorldName = frontsWorld.getWorld().getName();

            if (arguments.length > 1) {
                if (arguments[1].equalsIgnoreCase("-w")) {
                    newFrontWorldName = arguments[2];
                }
            }

            if (!this.plugin.worldEnabled(newFrontWorldName)) {
                player.sendMessage("WeatherFronts is not enabled for this world.");
                return true;
            }

            Storm newStorm = frontsWorld.randomSimulator().createStorm(new YamlConfiguration(), true, false);

            if (newStorm != null) {
                player.sendMessage("New storm named " + newStorm.getName() + " has randomly formed in world "
                        + newStorm.getWorld().getName() + " at X: " + newStorm.getData().getInt("center-x") + " Z: "
                        + newStorm.getData().getInt("center-z"));
                return true;
            } else {
                player.sendMessage("Storm was not able to form in world " + newStorm.getWorld().getName());
                return true;
            }
        }

        if (arguments[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("weatherfronts.add")) {
                player.sendMessage("You do not have permission to add storms.");
                return true;
            }

            if (arguments.length == 1) {
                player.sendMessage("No identifiable parameters given. Try /fronts help");
                return true;
            }

            int argLength = arguments.length - 1;
            YamlConfiguration newStormConfig = new YamlConfiguration();
            String newFrontWorldName = frontsWorld.getWorld().getName();
            String newFrontSimulatorName = frontsWorld.randomSimulator().getName();

            for (int i = 1; i < argLength; i++) {
                String currentArgument = arguments[i].trim();
                String currentValue = arguments[i + 1].trim();

                if (currentArgument.equalsIgnoreCase("-w")) {
                    newFrontWorldName = currentValue;
                } else if (currentArgument.equalsIgnoreCase("-n")) {
                    newStormConfig.set("name", currentValue);
                } else if (currentArgument.equalsIgnoreCase("-x")) {
                    newStormConfig.set("center-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-z")) {
                    newStormConfig.set("center-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-xz")) {
                    newStormConfig.set("center-x", Integer.parseInt(currentValue));
                    newStormConfig.set("center-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-rx")) {
                    newStormConfig.set("radius-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-rz")) {
                    newStormConfig.set("radius-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-rxz")) {
                    newStormConfig.set("radius-x", Integer.parseInt(currentValue));
                    newStormConfig.set("radius-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-vx")) {
                    newStormConfig.set("velocity-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-vz")) {
                    newStormConfig.set("velocity-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-vxz")) {
                    newStormConfig.set("velocity-x", Integer.parseInt(currentValue));
                    newStormConfig.set("velocity-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-pi")) {
                    newStormConfig.set("precipitation-intensity", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-ro")) {
                    newStormConfig.set("lightning-per-minute", 0);
                    continue;
                } else if (currentArgument.equalsIgnoreCase("-lpm")) {
                    newStormConfig.set("lightning-per-minute", Double.parseDouble(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-al")) {
                    newStormConfig.set("age-limit", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-a")) {
                    newStormConfig.set("age", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-shp")) {
                    newStormConfig.set("shape", currentValue);
                } else if (currentArgument.equalsIgnoreCase("-sim")) {
                    newFrontSimulatorName = currentValue;
                } else {
                    player.sendMessage("Unknown or mistyped parameter given: " + currentArgument);
                    return true;
                }

                ++i;
            }

            if (!this.plugin.worldEnabled(newFrontWorldName)) {
                player.sendMessage("WeatherFronts is not enabled for this world.");
                return true;
            }

            frontsWorld = this.plugin.getWorldHandle(newFrontWorldName);

            if (frontsWorld.getSimulator(newFrontSimulatorName) == null) {
                player.sendMessage("The world " + newFrontWorldName + " does not have a");
                player.sendMessage("simulator named " + newFrontSimulatorName + ".");
                return true;
            }

            Simulator simulator = frontsWorld.getSimulator(newFrontSimulatorName);

            if (newStormConfig.isSet("name") && frontsWorld.hasStorm(newStormConfig.getString("name"))) {
                player.sendMessage("A storm named " + newStormConfig.getString("name") + " already exists");
                player.sendMessage("in world " + newFrontWorldName);
                return true;
            }

            Storm newStorm = simulator.createStorm(newStormConfig, true, false);
            YamlConfiguration stormData = newStorm.getData();

            player.sendMessage("New storm named " + newStorm.getName() + " has formed in world " + newFrontWorldName
                    + " at " + "X: " + stormData.getInt("center-x") + " Z: " + stormData.getInt("center-z"));

            return true;
        }

        if (arguments[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("weatherfronts.remove")) {
                player.sendMessage("You do not have permission to remove storms.");
                return true;
            }

            String stormId = null;
            String stormWorldName = frontsWorld.getWorld().getName();

            if (arguments.length > 1) {
                int argLength = arguments.length - 1;

                for (int i = 1; i < argLength; i++) {
                    String currentArgument = arguments[i].trim();
                    String currentValue = arguments[i + 1].trim();

                    if (currentArgument.equalsIgnoreCase("-w")) {
                        stormWorldName = currentValue;
                    } else if (currentArgument.equalsIgnoreCase("-f")) {
                        stormId = currentValue;
                    }
                }

                frontsWorld = this.plugin.getWorldHandle(stormWorldName);

                if (stormId != null) {
                    if (frontsWorld != null) {
                        frontsWorld.getSimulatorByStorm(stormId).removeStorm(stormId);
                        player.sendMessage("The storm " + stormId + " in world " + stormWorldName + " has dissipated.");
                    }
                    return true;
                } else {
                    player.sendMessage("Invalid world specified.");
                    return true;
                }
            }

            player.sendMessage("No identifiable parameters given. Try /fronts help");
            return true;
        }

        if (arguments[0].equalsIgnoreCase("list")) {
            if (!player.hasPermission("weatherfronts.list")) {
                player.sendMessage("You do not have permission to list storms.");
                return true;
            }

            String worldName = frontsWorld.getWorld().getName();

            if (arguments.length > 1) {
                if (arguments[1].equalsIgnoreCase("-w")) {
                    worldName = arguments[2];
                }
            }

            if (!this.plugin.worldEnabled(worldName)) {
                player.sendMessage("Specified world is not valid.");
                return true;
            }

            frontsWorld = this.plugin.getWorldHandle(worldName);

            player.sendMessage("All storms in world " + world.getName());

            for (Entry<String, Simulator> simulator : frontsWorld.getSimulatorList().entrySet()) {
                for (Entry<String, Storm> entry : simulator.getValue().getStorms().entrySet()) {
                    Storm storm = entry.getValue();
                    YamlConfiguration stormConfig = storm.getData();
                    String heading = "Heading: ";
                    String type = "Type: ";

                    if (stormConfig.getInt("lightning-per-minute") == 0) {
                        type += " Rain";
                    } else {
                        type += " Thunder";
                    }

                    int velocityX = stormConfig.getInt("velocity-x");
                    int velocityZ = stormConfig.getInt("velocity-z");
                    double stormAngle = 0;

                    if (velocityX == 0 && velocityZ == 0) {
                        heading += "(stationary)";
                    } else {
                        double stormSpeed = Math.sqrt(Math.pow(velocityX, 2) + Math.pow(velocityZ, 2));
                        String headingSpeed = Double.toString(Math.round(stormSpeed * 10) / 10);
                        stormAngle = Math.toDegrees(Math.atan2(velocityX, velocityZ));
                        if (stormAngle < 0) {
                            stormAngle = (stormAngle * -1) + 180;
                        }

                        String cardinals[] = { "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N" };
                        String stormDirection = cardinals[Math.abs((int) Math.round(((stormAngle % 360) / 45)))];
                        heading += stormDirection + " @ " + headingSpeed + " m/s";
                    }

                    int stormX = stormConfig.getInt("center-x");
                    int stormZ = stormConfig.getInt("center-z");
                    player.sendMessage(storm.getName() + " @ X: " + stormX + "  Z: " + stormZ + "  " + heading);
                }
            }

            return true;
        }

        if (arguments[0].equalsIgnoreCase("save")) {
            if (!player.hasPermission("weatherfronts.save")) {
                player.sendMessage("You do not have permission to save front data.");
                return true;
            }

            if (arguments.length > 1) {
                if (arguments[1].equalsIgnoreCase("-w")) {
                    this.plugin.getWorldHandle(arguments[2]);
                }
            } else {
                for (Entry<String, FrontsWorld> entry : this.plugin.getAllFrontsWorlds().entrySet()) {
                    entry.getValue().saveStorms();
                }
            }

            player.sendMessage("Storms saved.");
            return true;
        }

        if (arguments[0].equalsIgnoreCase("help")) {
            help(arguments, player);
            return true;
        }

        return false;

    }

    public void help(String[] arguments, Player player) {
        if (arguments.length == 1) {
            player.sendMessage("Usage: /fronts <add/addrandom/remove/list/save/rename> [parameters]");
            player.sendMessage("Type /fronts help <command> for specific command help");
        } else if (arguments[1].equalsIgnoreCase("add")) {
            player.sendMessage("Usage: /fronts add [parameters]");
            player.sendMessage("Requires at least one parameter. Possible parameters:");
            player.sendMessage("-w [world name]    -n [front name]");
            player.sendMessage("-x [front location (x-axis)]   -z [front location (z-axis)]");
            player.sendMessage("-rx [front radius (x-axis)]   -rz [front radius (z-axis)]");
            player.sendMessage("-vx [front velocity (x-axis)]   -vz [front velocity (z-axis)]");
            player.sendMessage("-pi [precipitation intensity]    -ro [rain only]");
            player.sendMessage("-lpm [lightning per minute]    -a [starting age of front]");
            player.sendMessage("-al [maximum age of front]    -shp [front shape]");
        } else if (arguments[1].equalsIgnoreCase("addrandom")) {
            player.sendMessage("Usage: /fronts addrandom [-w world name]");
            player.sendMessage("Adds a random front in your current world");
            player.sendMessage("or the world specified with -w");
        } else if (arguments[1].equalsIgnoreCase("remove")) {
            player.sendMessage("Usage: /fronts remove -f <front name> [-w worldname]");
            player.sendMessage("Removes the specified front from your current world");
            player.sendMessage("or the world specified with -w");
        } else if (arguments[1].equalsIgnoreCase("list")) {
            player.sendMessage("Usage: /fronts list [-w worldname]");
            player.sendMessage("Lists fronts in your current world");
            player.sendMessage("or the world specified with -w");
        } else if (arguments[1].equalsIgnoreCase("save")) {
            player.sendMessage("Usage: /fronts save [-w world name]");
            player.sendMessage("Saves the existing fronts in all worlds");
            player.sendMessage("unless a specific world is given for -w");
        } else if (arguments[1].equalsIgnoreCase("rename")) {
            player.sendMessage("Usage: /fronts rename [-w world name] -from <front name> -to <new front name>");
            player.sendMessage("Renames the specified front in your current world");
            player.sendMessage("or the world specified with -w");
        }
    }
}
