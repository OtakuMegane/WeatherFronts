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

public class FunctionsAndTests
{
    private XORShiftRandom random = new XORShiftRandom();
    private WeatherFronts plugin;
    private Logger logger = Logger.getLogger("Minecraft");

    public FunctionsAndTests(WeatherFronts instance)
    {
        plugin = instance;
    }

    public Boolean worldExists(String worldName)
    {
        if(Bukkit.getWorld(worldName) != null)
        {
            return true;
        }

        return false;
    }

    public Boolean biomeIsDry(Location location)
    {
        Biome blockBiome = location.getBlock().getBiome();

        if (blockBiome == Biome.DESERT || blockBiome == Biome.DESERT_HILLS || blockBiome == Biome.DESERT_MOUNTAINS
                || blockBiome == Biome.SAVANNA || blockBiome == Biome.SAVANNA_MOUNTAINS || blockBiome == Biome.SAVANNA_PLATEAU
                || blockBiome == Biome.SAVANNA_PLATEAU_MOUNTAINS)
        {
            return true;
        }

        return false;
    }

    public Boolean biomeIsCold(Location location)
    {
        Biome blockBiome = location.getBlock().getBiome();

        if (blockBiome == Biome.COLD_BEACH || blockBiome == Biome.COLD_TAIGA || blockBiome == Biome.COLD_TAIGA_HILLS
                || blockBiome == Biome.COLD_TAIGA_MOUNTAINS || blockBiome == Biome.FROZEN_OCEAN || blockBiome == Biome.FROZEN_RIVER
                || blockBiome == Biome.ICE_MOUNTAINS || blockBiome == Biome.ICE_PLAINS || blockBiome == Biome.ICE_PLAINS_SPIKES)
        {
            return true;
        }

        return false;
    }

    public Boolean blockIsCold(Location location)
    {
        return calculateBlockTemp(location) < 0.15;
    }

    public boolean blockIsDry(Location location)
    {
        return calculateBlockTemp(location) > 1.0;
    }

    public boolean locationIsInRain(Location location)
    {
        if(!locationIsInFront(location) || !locationIsAboveground(location) || biomeIsDry(location) || biomeIsCold(location) || blockIsCold(location))
        {
            return false;
        }

        return true;
    }

    public boolean locationIsInSnow(Location location)
    {
        if(!locationIsInFront(location) || !locationIsAboveground(location) || biomeIsDry(location) || !biomeIsCold(location) || !blockIsCold(location))
        {
            return false;
        }

        return true;
    }

    public boolean locationIsInStorm(Location location)
    {
        if(!locationIsInRain(location) && !locationIsInSnow(location))
        {
            return false;
        }

        if(locationInWhichFront(location, true, true)[3].equals("thunder"))
        {
            return true;
        }

        return false;
    }

    public String[] locationInWhichFront(Location location, boolean multi, boolean thunder)
    {
        World world = location.getWorld();
        String[] returnFront = new String[4];
        returnFront[0] = null;
        returnFront[1] = null;
        returnFront[2] = "0";
        returnFront[3] = "none";

        if(!worldIsEnabled(world))
        {
            return returnFront;
        }

        for(String simulator : Configuration.fronts_config.getConfigurationSection(world.getName()).getKeys(false))
        {
            for(String frontName : Configuration.fronts_config.getConfigurationSection(world.getName() + "." + simulator).getKeys(false))
            {
                Map<String, Integer> frontMap = mapFront(simulator, frontName);

                if(frontMap.get("shape") == 1)
                {
                    int frontXRangePos = frontMap.get("center-x") + frontMap.get("radius-x");
                    int frontXRangeNeg = frontMap.get("center-x") - frontMap.get("radius-x");
                    int frontZRangePos = frontMap.get("center-z") + frontMap.get("radius-z");
                    int frontZRangeNeg = frontMap.get("center-z") - frontMap.get("radius-z");
                    int locX = location.getBlockX();
                    int locZ = location.getBlockZ();

                    if(locX < frontXRangePos && locX > frontXRangeNeg && locZ < frontZRangePos && locZ > frontZRangeNeg)
                    {
                        // If 2 or more fronts overlap the most intense one should take precedence
                        if(Integer.parseInt(returnFront[2]) < frontMap.get("intensity"))
                        {
                            returnFront[0] = simulator;
                            returnFront[1] = frontName;
                            returnFront[2] = Integer.toString(frontMap.get("intensity"));

                            if(frontMap.get("lightning-per-minute") > 0)
                            {
                                returnFront[3] = "thunder";

                                if(thunder)
                                {
                                    return returnFront;
                                }
                            }
                            else
                            {
                                returnFront[3] = "rain";
                            }
                        }

                        if(!multi)
                        {
                            return returnFront;
                        }
                    }
                }
            }

            if(returnFront[0] != null)
            {
                return returnFront;
            }
        }

        return returnFront;
    }

    public Boolean locationIsInFront(Location location)
    {
        return locationInWhichFront(location, false, false)[1] != null;
    }

    public Boolean locationIsAboveground(Location location)
    {
        return locationIsLoaded(location, false) && location.getBlockY() >= getTopBlockY(location);
    }
    
    public Block getTopBlock(Location location)
    {
        return findHighestBlock(location, 255);
    }

    public int getTopBlockY(Location location)
    {
        return findHighestBlock(location, 255).getY();
    }

    public Block getTopEmptyBlock(Location location)
    {
        return findHighestBlock(location, 255).getRelative(BlockFace.UP);
    }
    
    public Block getTopLightningBlock(Location location)
    {
        Block block = findHighestBlock(location, 255);

        while(!block.getType().isSolid() && !block.isLiquid())
        {
            if(block.getY() == 0)
            {
                return null;
            }

            block = findHighestBlock(location, block.getY() - 1);
        }
        
        return block;
    }
    
    public Block getTopSolidBlock(Location location)
    {
        Block block = findHighestBlock(location, 255);

        while(!block.getType().isSolid())
        {
            if(block.getY() == 0)
            {
                return null;
            }

            block = findHighestBlock(location, block.getY() - 1);
        }
        
        return block;
    }
    
    public Block getTopLiquidBlock(Location location)
    {
        Block block = findHighestBlock(location, 255);

        while(!block.isLiquid())
        {
            if(block.getY() == 0)
            {
                return null;
            }

            block = findHighestBlock(location, block.getY() - 1);
        }
        
        return block;
    }

    public Block findHighestBlock(Location location, int start)
    {
        World world = location.getWorld();
        int i = 255;

        if(start < 256 && start > 0)
        {
            i = start;
        }

        for(; i > 0; --i)
        {
            Block testBlock = world.getBlockAt(location.getBlockX(), i, location.getBlockZ());

            if(!testBlock.isEmpty())
            {
                return testBlock;
            }
        }

        return world.getBlockAt(location.getBlockX(), 0, location.getBlockZ());
    }

    public boolean adjacentBlockExposed(Block block)
    {
        Location eastLoc = block.getRelative(BlockFace.EAST).getLocation();
        Location westLoc = block.getRelative(BlockFace.WEST).getLocation();
        Location northLoc = block.getRelative(BlockFace.NORTH).getLocation();
        Location southLoc = block.getRelative(BlockFace.SOUTH).getLocation();

        if(getTopBlockY(northLoc) >= northLoc.getY() && getTopBlockY(southLoc) >= southLoc.getY()
                && getTopBlockY(eastLoc) >= eastLoc.getY() && getTopBlockY(westLoc) >= westLoc.getY())
        {
            return false;
        }

        return true;
    }

    public Boolean worldIsEnabled(World world)
    {
        return Configuration.main_config.getBoolean("worlds-enabled." + world.getName()) && worldExists(world.getName());
    }

    public double calculateBlockTemp(Location location)
    {
        double blockTemp = location.getBlock().getTemperature();

        // Taken from the NMS BiomeBase calculation
        if(location.getY() > 64)
        {
            blockTemp = blockTemp - (location.getY() - 64) * 0.05F / 30.0F;
        }

        return blockTemp;
    }

    public int limitCheckInt(int value, String simulator, String setting)
    {
        String worldName = getSimulatorWorldName(simulator);

        if(Configuration.main_config.getBoolean("worlds." + worldName + "." + simulator + "." + "manual-creation-limits"))
        {
            int minLimit = Configuration.main_config.getInt("worlds." + worldName + "." + simulator + "." + "minimum-" + setting);
            int maxLimit = Configuration.main_config.getInt("worlds." + worldName + "." + simulator + "." + "maximum-" + setting);

            if(value < minLimit)
            {
                value = minLimit;
            }

            if(value > maxLimit)
            {
                value = maxLimit;
            }
        }

        return value;
    }

    public String getSimulatorWorldName(String simulator)
    {
        for(String worldName : Configuration.main_config.getConfigurationSection("worlds-enabled").getKeys(false))
        {
            if(worldExists(worldName) && Configuration.main_config.getConfigurationSection("worlds." + worldName).contains(simulator))
            {
                return worldName;
            }
        }

        return null;
    }

    public int posNeg()
    {
        if(random.nextInt(100) < 50)
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }

    public int[] randomXYInFront(Map<String, Integer> frontMap)
    {
        int[] xz = new int[2];

        if(frontMap.get("shape") == 1)
        {
            xz[0] = random.nextInt(frontMap.get("radius-x") * 2) + (frontMap.get("center-x") - frontMap.get("radius-x"));
            xz[1] = random.nextInt(frontMap.get("radius-z") * 2) + (frontMap.get("center-z") - frontMap.get("radius-z"));
        }

        return xz;
    }

    public boolean createFulgurite(Location location)
    {
        String[] whichFront = locationInWhichFront(location, false, false);

        if(whichFront[0] != null)
        {
            World world = location.getWorld();

            if(Configuration.main_config.getBoolean("worlds." + world.getName() + "." + whichFront[0] + ".create-fulgurites")
                    && random.nextInt(100) + 1 < Configuration.main_config.getInt("worlds." + world.getName() + "." + whichFront[0] + ".fulgurite-chance"))
            {
                return true;
            }
        }

        return false;
    }

    public String randomSimulator(World world)
    {
        String[] sims = new String[999];
        int i = 0;
        sims[0] = "lol";

        for(String simulator : Configuration.fronts_config.getConfigurationSection(world.getName()).getKeys(false))
        {
            sims[i] = simulator;
            ++i;
        }

        return sims[random.nextInt(i)];
    }

    public String getSimulator(Location location)
    {
        String[] whichFront = locationInWhichFront(location, false, false);

        if(whichFront[0] == null)
        {
            return "";
        }

        return whichFront[0];
    }

    public boolean locationIsLoaded(Location location, boolean inUseCheck)
    {
        if(inUseCheck)
        {
            return location.getWorld().isChunkInUse(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }
        else
        {
            return location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4);
        }
    }

    public boolean locationIsLoaded(World world, int x, int z, boolean inUseCheck)
    {
        return locationIsLoaded(new Location(world, x, 0 , z), inUseCheck);
    }

    public Map<String, Integer> mapFront(String simulator, String frontName)
    {
        String frontConfig = getSimulatorWorldName(simulator) + "." + simulator  + "." + frontName + ".";
        Map<String, Integer> frontMap = new HashMap<String, Integer>();
        frontMap.put("shape", Configuration.fronts_config.getInt(frontConfig + "shape"));
        frontMap.put("radius-x", Configuration.fronts_config.getInt(frontConfig + "radius-x"));
        frontMap.put("radius-z", Configuration.fronts_config.getInt(frontConfig + "radius-z"));
        frontMap.put("velocity-x", Configuration.fronts_config.getInt(frontConfig + "velocity-x"));
        frontMap.put("velocity-z", Configuration.fronts_config.getInt(frontConfig + "velocity-z"));
        frontMap.put("center-x", Configuration.fronts_config.getInt(frontConfig + "center-x"));
        frontMap.put("center-z", Configuration.fronts_config.getInt(frontConfig + "center-z"));
        frontMap.put("intensity", Configuration.fronts_config.getInt(frontConfig + "intensity"));
        frontMap.put("lightning-per-minute", Configuration.fronts_config.getInt(frontConfig + "lightning-per-minute"));
        frontMap.put("age-limit", Configuration.fronts_config.getInt(frontConfig + "age-limit"));
        frontMap.put("age", Configuration.fronts_config.getInt(frontConfig + "age"));
        return frontMap;
    }
}
