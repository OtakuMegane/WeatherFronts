package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor
{
    private WeatherFronts plugin;
    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    public CommandHandler(WeatherFronts instance)
    {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] arguments)
    {
        if (cmd.getName().equalsIgnoreCase("fronts") && arguments.length > 0)
        {
            Player player = null;

            if (sender instanceof Player)
            {
                player = (Player) sender;
            }
            else
            {
                return false;
            }

            World world = player.getWorld();
            Map<String, String> frontParams = new HashMap<String, String>();

            if(arguments[0].equalsIgnoreCase("addrandom"))
            {
                if (!player.hasPermission("weatherfronts.add"))
                {
                    player.sendMessage("You do not have permission to add weather fronts.");
                    return true;
                }

                frontParams.put("random", "true");
                frontParams.put("command", "true");

                if(arguments.length > 1)
                {
                    if(arguments[1].equalsIgnoreCase("-w"))
                    {
                        world = Bukkit.getWorld(arguments[2]);
                    }
                }

                String worldName = world.getName();

                String[] frontResult = plugin.frontGenerator.generateNewFront(world, "", frontParams);

                if(frontResult[1] == "worlddisabled")
                {
                    player.sendMessage("Front failed to form. Fronts are disabled in world " + worldName + ".");
                }
                else
                {
                    String frontX = Integer.toString(Configuration.fronts_config.getInt(worldName + "." + frontResult[2] + "." + frontResult[0] + ".center-x"));
                    String frontZ = Integer.toString(Configuration.fronts_config.getInt(worldName + "." + frontResult[2] + "." + frontResult[0] + ".center-z"));
                    player.sendMessage("New front named " + frontResult[0] + " has randomly formed in world " + worldName + " at " +
                            "X: " + frontX + " Z: " + frontZ);
                }

                return true;
            }
            else if(arguments[0].equalsIgnoreCase("add"))
            {
                if (!player.hasPermission("weatherfronts.add"))
                {
                    player.sendMessage("You do not have permission to add weather fronts.");
                    return true;
                }

                if(arguments.length == 1)
                {
                    player.sendMessage("No identifiable parameters given. Try /fronts help");
                    return true;
                }

                int argLength = arguments.length - 1;
                frontParams.put("random","false");
                frontParams.put("command", "true");

                for(int i = 1;i < argLength; i++)
                {
                    String currentArgument = arguments[i].trim();
                    String currentValue = arguments[i + 1].trim();

                    if(currentArgument.equalsIgnoreCase("-w"))
                    {
                        world = Bukkit.getWorld(currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-n"))
                    {
                        String worldName = world.getName();

                        if(Configuration.fronts_config.contains("worlds." + worldName + "." + currentValue))
                        {
                            player.sendMessage("A front named " + currentValue + " already exists");
                            player.sendMessage("in world " + worldName);
                            return true;
                        }
                        else
                        {
                            frontParams.put("name", currentValue);
                        }
                    }
                    else if(currentArgument.equalsIgnoreCase("-x"))
                    {
                        frontParams.put("center-x", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-z"))
                    {
                        frontParams.put("center-z", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-rx"))
                    {
                        frontParams.put("radius-x", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-rz"))
                    {
                        frontParams.put("radius-z", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-vx"))
                    {
                        frontParams.put("velocity-x", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-vz"))
                    {
                        frontParams.put("velocity-z", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-i"))
                    {
                        frontParams.put("intensity", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-ro"))
                    {
                        frontParams.put("lightning-per-minute", "0");
                        continue;
                    }
                    else if(currentArgument.equalsIgnoreCase("-lpm"))
                    {
                        frontParams.put("lightning-per-minute", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-a"))
                    {
                        frontParams.put("age-limit", currentValue);
                    }
                    else if(currentArgument.equalsIgnoreCase("-shp"))
                    {
                        frontParams.put("shape", currentValue);
                    }
                    else
                    {
                        player.sendMessage("Unknown or mistyped parameter given: " + currentArgument);
                        return true;
                    }

                    ++i;
                }

                if(world != null)
                {
                    String worldName = world.getName();
                    String[] frontResult = plugin.frontGenerator.generateNewFront(world, "", frontParams);

                    if(frontResult[1] == "worlddisabled")
                    {
                        player.sendMessage("Front failed to form. Fronts are disabled in world " + worldName + ".");
                    }
                    else if(frontResult[1] == "invalidsim")
                    {
                        player.sendMessage("Front failed to form. The location given is not within a valid weather simulation.");
                    }
                    else
                    {
                        String frontX = Integer.toString(Configuration.fronts_config.getInt(worldName + "." + frontResult[2] + "." + frontResult[0] + ".center-x"));
                        String frontZ = Integer.toString(Configuration.fronts_config.getInt(worldName + "." + frontResult[2] + "." + frontResult[0] + ".center-z"));
                        player.sendMessage("New front named " + frontResult[0] + " has formed in world " + worldName + " at " +
                                "X: " + frontX + " Z: " + frontZ);
                    }

                    return true;
                }
                else
                {
                    player.sendMessage("No valid world specified for new front.");
                    return true;
                }
            }
            else if(arguments[0].equalsIgnoreCase("remove"))
            {
                if (!player.hasPermission("weatherfronts.remove"))
                {
                    player.sendMessage("You do not have permission to remove weather fronts.");
                    return true;
                }

                String frontId = null;

                if(arguments.length > 1)
                {
                    int argLength = arguments.length - 1;

                    for(int i = 1;i < argLength; i++)
                    {
                        String currentArgument = arguments[i].trim();
                        String currentValue = arguments[i + 1].trim();

                        if(currentArgument.equalsIgnoreCase("-w"))
                        {
                            world = Bukkit.getWorld(currentValue);
                        }
                        else if(currentArgument.equalsIgnoreCase("-f"))
                        {
                            frontId = currentValue;
                        }
                    }

                    if(frontId != null)
                    {
                        if(plugin.frontsHandler.removeFront(world, null, frontId))
                        {
                            player.sendMessage("The front " +  frontId + " in world " + world.getName() + " has dissipated.");
                        }
                        return true;
                    }
                }

                player.sendMessage("No identifiable parameters given. Try /fronts help");
                return true;
            }
            else if(arguments[0].equalsIgnoreCase("list"))
            {
                if (!player.hasPermission("weatherfronts.list"))
                {
                    player.sendMessage("You do not have permission to list weather fronts.");
                    return true;
                }

                if(arguments.length > 1)
                {
                    if(arguments[1].equalsIgnoreCase("-w"))
                    {
                        world = Bukkit.getWorld(arguments[2]);
                    }
                }

                player.sendMessage("All weather fronts in world " + world.getName());

                if(!test.worldIsEnabled(world))
                {
                    return true;
                }

                for(String key : Configuration.fronts_config.getConfigurationSection(world.getName()).getKeys(false))
                {
                    for(String key2 : Configuration.fronts_config.getConfigurationSection(world.getName() + "." + key).getKeys(false))
                    {
                        String frontConfig = world.getName() + "." + key + "." + key2 + ".";
                        String heading = "Heading: ";
                        String type = "Type: ";

                        int strikeRate = Configuration.fronts_config.getInt(frontConfig + "lightning-per-minute");

                        if(strikeRate == 0)
                        {
                            type += " Thunder";
                        }
                        else
                        {
                            type += " Rain";
                        }

                        int velocityX = Configuration.fronts_config.getInt(frontConfig + "velocity-x");
                        int velocityZ = Configuration.fronts_config.getInt(frontConfig + "velocity-z");
                        double frontAngle = 0;


                        if(velocityX == 0
                                && velocityZ == 0)
                        {
                            heading += "(stationary)";
                        }
                        else
                        {
                            double frontSpeed = Math.sqrt(Math.pow(velocityX, 2) + Math.pow(velocityZ, 2));
                            String headingSpeed = Double.toString(Math.round(frontSpeed * 10) / 10);
                            frontAngle = Math.toDegrees(Math.atan2(velocityX, velocityZ));
                            if(frontAngle < 0)
                            {
                                frontAngle = (frontAngle * -1) + 180;
                            }

                            String cardinals[] = {"N", "NE", "E", "SE", "S", "SW", "W", "NW", "N"};
                            String frontDirection = cardinals[ Math.abs((int)Math.round((  (frontAngle % 360) / 45))) ];
                            heading += frontDirection + " @ " + headingSpeed + " m/s";
                        }

                        int frontX = Configuration.fronts_config.getInt(frontConfig  + "center-x");
                        int frontZ = Configuration.fronts_config.getInt(frontConfig  + "center-z");
                        player.sendMessage(key2 + " @ X: " + frontX + "  Z: " + frontZ + "  " + heading);
                    }
                }

                return true;
            }
            else if(arguments[0].equalsIgnoreCase("save"))
            {
                if (!player.hasPermission("weatherfronts.save"))
                {
                    player.sendMessage("You do not have permission to save front data.");
                    return true;
                }

                if(arguments.length > 1)
                {
                    if(arguments[1].equalsIgnoreCase("-w"))
                    {
                        plugin.config.saveFronts(arguments[1]);
                    }
                    else
                    {
                        plugin.config.saveFronts("all");
                    }
                }

                player.sendMessage("Fronts saved.");
                return true;
            }
            else if(arguments[0].equalsIgnoreCase("rename"))
            {
                if (!player.hasPermission("weatherfronts.rename"))
                {
                    player.sendMessage("You do not have permission to rename weather fronts.");
                    return true;
                }
                rename(arguments, player, world);
                return true;
            }
            else if(arguments[0].equalsIgnoreCase("help"))
            {
                help(arguments, player);
                return true;
            }
            else
            {
                return false;
            }
        }
        else
        {
            return false;
        }
    }


    public void rename(String[] arguments, Player player, World world)
    {

        if(arguments.length > 1)
        {
            int argLength = arguments.length - 1;
            String original = null;
            String newname = null;

            for(int i = 1;i < argLength; i++)
            {
                String currentArgument = arguments[i].trim();
                String currentValue = arguments[i + 1].trim();

                if(currentArgument.equalsIgnoreCase("-w"))
                {
                    world = Bukkit.getWorld(currentValue);
                }
                else if(currentArgument.equalsIgnoreCase("-from"))
                {
                    original = currentValue;
                }
                else if(currentArgument.equalsIgnoreCase("-to"))
                {
                    newname = currentValue;
                }

                i++;
            }

            String worldName = world.getName();

            if(Configuration.fronts_config.contains("worlds." + worldName + "." + newname))
            {
                player.sendMessage("A front named " + newname + " already exists");
                player.sendMessage("in world " + worldName);
            }
            else
            {
                plugin.config.renameFront(worldName, original, newname);
                plugin.config.saveFronts(worldName);
                player.sendMessage("Front " + original + " in world " + worldName);
                player.sendMessage("has been renamed to " + newname);
            }
        }
    }

    public void help(String[] arguments, Player player)
    {
        if(arguments.length == 1)
        {
            player.sendMessage("Usage: /fronts <add/addrandom/remove/list/save/rename> [parameters]");
            player.sendMessage("Type /fronts help <command> for specific command help");
        }
        else if(arguments[1].equalsIgnoreCase("add"))
        {
            player.sendMessage("Usage: /fronts add [parameters]");
            player.sendMessage("Requires at least one parameter. Possible parameters");
            player.sendMessage("-w [world name]    -n [front name]");
            player.sendMessage("-x [front location (x-axis)]   -z [front location (z-axis)]");
            player.sendMessage("-rx [front radius (x-axis)]   -rz [front radius (z-axis)]");
            player.sendMessage("-vx [front velocity (x-axis)]   -vz [front velocity (z-axis)]");
            player.sendMessage("-i [intensity]    -ro [rain only]");
            player.sendMessage("-lpm [lightning per minute]    -a [maximum age of front]");
            player.sendMessage("-shp [front shape]");
        }
        else if(arguments[1].equalsIgnoreCase("addrandom"))
        {
            player.sendMessage("Usage: /fronts addrandom [-w world name]");
            player.sendMessage("Adds a random front in your current world");
            player.sendMessage("or the world specified with -w");
        }
        else if(arguments[1].equalsIgnoreCase("remove"))
        {
            player.sendMessage("Usage: /fronts remove -f <front name> [-w worldname]");
            player.sendMessage("Removes the specified front from your current world");
            player.sendMessage("or the world specified with -w");
        }
        else if(arguments[1].equalsIgnoreCase("list"))
        {
            player.sendMessage("Usage: /fronts list [-w worldname]");
            player.sendMessage("Lists fronts in your current world");
            player.sendMessage("or the world specified with -w");
        }
        else if(arguments[1].equalsIgnoreCase("save"))
        {
            player.sendMessage("Usage: /fronts save [-w world name]");
            player.sendMessage("Saves the existing fronts in all worlds");
            player.sendMessage("unless a specific world is given for -w");
        }
        else if(arguments[1].equalsIgnoreCase("rename"))
        {
            player.sendMessage("Usage: /fronts rename [-w world name] -from <front name> -to <new front name>");
            player.sendMessage("Renames the specified front in your current world");
            player.sendMessage("or the world specified with -w");
        }
    }
}
