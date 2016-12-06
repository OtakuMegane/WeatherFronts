package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Map.Entry;

import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import com.minefit.XerxesTireIron.WeatherFronts.Front.Front;
import com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld.FrontsWorld;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

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
                player.sendMessage("You do not have permission to add random weather fronts.");
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

            Front newFront = frontsWorld.randomSimulator().createFront(new YamlConfiguration(), true);

            if (newFront != null) {
                player.sendMessage("New front named " + newFront.getName() + " has randomly formed in world "
                        + newFront.getWorld().getName() + " at X: " + newFront.getData().getInt("center-x") + " Z: "
                        + newFront.getData().getInt("center-z"));
                return true;
            } else {
                player.sendMessage("Front was not able to form in world " + newFront.getWorld().getName());
                return true;
            }
        }

        if (arguments[0].equalsIgnoreCase("add")) {
            if (!player.hasPermission("weatherfronts.add")) {
                player.sendMessage("You do not have permission to add weather fronts.");
                return true;
            }

            if (arguments.length == 1) {
                player.sendMessage("No identifiable parameters given. Try /fronts help");
                return true;
            }

            int argLength = arguments.length - 1;
            YamlConfiguration newFrontConfig = new YamlConfiguration();
            String newFrontWorldName = frontsWorld.getWorld().getName();
            String newFrontSimulatorName = frontsWorld.randomSimulator().getName();

            for (int i = 1; i < argLength; i++) {
                String currentArgument = arguments[i].trim();
                String currentValue = arguments[i + 1].trim();

                if (currentArgument.equalsIgnoreCase("-w")) {
                    newFrontWorldName = currentValue;
                } else if (currentArgument.equalsIgnoreCase("-n")) {
                    newFrontConfig.set("name", currentValue);
                } else if (currentArgument.equalsIgnoreCase("-x")) {
                    newFrontConfig.set("center-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-z")) {
                    newFrontConfig.set("center-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-rx")) {
                    newFrontConfig.set("radius-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-rz")) {
                    newFrontConfig.set("radius-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-vx")) {
                    newFrontConfig.set("velocity-x", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-vz")) {
                    newFrontConfig.set("velocity-z", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-i")) {
                    newFrontConfig.set("precipitation-intensity", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-ro")) {
                    newFrontConfig.set("lightning-per-minute", 0.0);
                    continue;
                } else if (currentArgument.equalsIgnoreCase("-lpm")) {
                    newFrontConfig.set("lightning-per-minute", Double.parseDouble(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-a")) {
                    newFrontConfig.set("age-limit", Integer.parseInt(currentValue));
                } else if (currentArgument.equalsIgnoreCase("-shp")) {
                    newFrontConfig.set("shape", currentValue);
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

            if (newFrontConfig.isSet("name") && frontsWorld.hasFront(newFrontConfig.getString("name"))) {
                player.sendMessage("A front named " + newFrontConfig.getString("name") + " already exists");
                player.sendMessage("in world " + newFrontWorldName);
                return true;
            }

            Front newFront = simulator.createFront(newFrontConfig, true);
            YamlConfiguration frontData = newFront.getData();

            player.sendMessage("New front named " + newFront.getName() + " has formed in world " + newFrontWorldName
                    + " at " + "X: " + frontData.getInt("center-x") + " Z: " + frontData.getInt("center-z"));

            return true;
        }

        if (arguments[0].equalsIgnoreCase("remove")) {
            if (!player.hasPermission("weatherfronts.remove")) {
                player.sendMessage("You do not have permission to remove weather fronts.");
                return true;
            }

            String frontId = null;
            String frontWorldName = frontsWorld.getWorld().getName();

            if (arguments.length > 1) {
                int argLength = arguments.length - 1;

                for (int i = 1; i < argLength; i++) {
                    String currentArgument = arguments[i].trim();
                    String currentValue = arguments[i + 1].trim();

                    if (currentArgument.equalsIgnoreCase("-w")) {
                        frontWorldName = currentValue;
                    } else if (currentArgument.equalsIgnoreCase("-f")) {
                        frontId = currentValue;
                    }
                }

                frontsWorld = this.plugin.getWorldHandle(frontWorldName);

                if (frontId != null) {
                    if (frontsWorld != null) {
                        frontsWorld.getSimulatorByFront(frontId).removeFront(frontId);
                        player.sendMessage("The front " + frontId + " in world " + frontWorldName + " has dissipated.");
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
                player.sendMessage("You do not have permission to list weather fronts.");
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

            player.sendMessage("All weather fronts in world " + world.getName());

            for (Entry<String, Simulator> simulator : frontsWorld.getSimulatorList().entrySet()) {
                for (Entry<String, Front> entry : simulator.getValue().getFronts().entrySet()) {
                    Front front = entry.getValue();
                    YamlConfiguration frontConfig = front.getData();
                    String heading = "Heading: ";
                    String type = "Type: ";

                    if (frontConfig.getInt("lightning-per-minute") == 0) {
                        type += " Rain";
                    } else {
                        type += " Thunder";
                    }

                    int velocityX = frontConfig.getInt("velocity-x");
                    int velocityZ = frontConfig.getInt("velocity-z");
                    double frontAngle = 0;

                    if (velocityX == 0 && velocityZ == 0) {
                        heading += "(stationary)";
                    } else {
                        double frontSpeed = Math.sqrt(Math.pow(velocityX, 2) + Math.pow(velocityZ, 2));
                        String headingSpeed = Double.toString(Math.round(frontSpeed * 10) / 10);
                        frontAngle = Math.toDegrees(Math.atan2(velocityX, velocityZ));
                        if (frontAngle < 0) {
                            frontAngle = (frontAngle * -1) + 180;
                        }

                        String cardinals[] = { "N", "NE", "E", "SE", "S", "SW", "W", "NW", "N" };
                        String frontDirection = cardinals[Math.abs((int) Math.round(((frontAngle % 360) / 45)))];
                        heading += frontDirection + " @ " + headingSpeed + " m/s";
                    }

                    int frontX = frontConfig.getInt("center-x");
                    int frontZ = frontConfig.getInt("center-z");
                    player.sendMessage(front.getName() + " @ X: " + frontX + "  Z: " + frontZ + "  " + heading);
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
                } else {
                    for (Entry<String, FrontsWorld> entry : this.plugin.getAllFrontsWorlds().entrySet()) {
                        entry.getValue().saveFronts();
                    }
                }
            }

            player.sendMessage("Fronts saved.");
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
            player.sendMessage("Requires at least one parameter. Possible parameters");
            player.sendMessage("-w [world name]    -n [front name]");
            player.sendMessage("-x [front location (x-axis)]   -z [front location (z-axis)]");
            player.sendMessage("-rx [front radius (x-axis)]   -rz [front radius (z-axis)]");
            player.sendMessage("-vx [front velocity (x-axis)]   -vz [front velocity (z-axis)]");
            player.sendMessage("-i [intensity]    -ro [rain only]");
            player.sendMessage("-lpm [lightning per minute]    -a [maximum age of front]");
            player.sendMessage("-shp [front shape]");
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
