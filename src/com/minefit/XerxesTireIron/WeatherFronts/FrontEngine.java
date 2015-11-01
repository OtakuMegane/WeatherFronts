package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockFadeEvent;

public class FrontEngine implements Listener
{
    private Random random = new XORShiftRandom();
    private WeatherFronts plugin;
    private ConcurrentHashMap<Block,Block> farmland;
    private ConcurrentHashMap<String,Double> lightningAccumulator;

    public FrontEngine(WeatherFronts instance)
    {
        plugin = instance;
        farmland = new ConcurrentHashMap<Block,Block>();
        lightningAccumulator = new ConcurrentHashMap<String,Double>();
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    public void lightningGen(World world, String simulator, String frontName)
    {
        Map<String, Integer> frontMap = test.mapFront(simulator, frontName);

        if(lightningAccumulator.get(frontName) == null)
        {
            lightningAccumulator.put(frontName, 0.0);
        }

        int lightningPerMinute = frontMap.get("lightning-per-minute");

        if(lightningPerMinute == 0)
        {
            return;
        }

        double weightedLightningPerMinute = lightningPerMinute;

        if(frontMap.get("radius-x") > 192)
        {
            weightedLightningPerMinute *= frontMap.get("radius-x")/192;
        }

        if(frontMap.get("radius-z") > 192)
        {
            weightedLightningPerMinute *= frontMap.get("radius-z")/192;
        }

        double lightningPerCheck = weightedLightningPerMinute/240;	// We do a check every 5 Ticks (1/4 second)
        Chunk[] validChunks = world.getLoadedChunks();

        if(lightningPerCheck < 1.0)
        {
            lightningAccumulator.put(frontName, lightningAccumulator.get(frontName) + lightningPerCheck);
        }
        else if(lightningPerCheck >= 2.0)
        {
            for(int i = 1; i <= lightningPerCheck; i++)
            {
                lightningAccumulator.put(frontName, lightningPerCheck - i);
                randomStrike(world, simulator, frontMap, validChunks);
            }
        }

        if(lightningAccumulator.get(frontName) >= 1.0)
        {
            lightningAccumulator.put(frontName, lightningAccumulator.get(frontName) - 1.0);
            randomStrike(world, simulator, frontMap, validChunks);
        }
    }

    public void randomStrike(World world, String simulator, Map<String, Integer> frontMap, Chunk[] validChunks)
    {
        String simConfig = "worlds." + world.getName() + "." + simulator + ".";
        int[] xz = test.randomXYInFront(frontMap);

        if(!test.locationIsLoaded(world, xz[0], xz[1], false))
        {
            return;
        }

        int x = xz[0];
        int z = xz[1];
        boolean lightningDry = Configuration.main_config.getBoolean(simConfig + "lightning-in-dry-biomes");
        boolean lightningCold = Configuration.main_config.getBoolean(simConfig + "lightning-in-cold-biomes");
        Block highBlock = test.getTopLightningBlock(new Location(world, x, 0, z));

        if(highBlock == null)
        {
            return;
        }

        Material blockType = highBlock.getType();
        Location highLoc = highBlock.getRelative(BlockFace.UP).getLocation();

        if((!test.biomeIsDry(highLoc) && !test.biomeIsCold(highLoc)) || (test.biomeIsDry(highLoc) && lightningDry) || (test.biomeIsCold(highLoc) && lightningCold))
        {
            world.strikeLightning(highLoc);
        }
    }

    public void precipitationBlockEffects(World world, String simulator, String frontName)
    {
        Map<String, Integer> frontMap = test.mapFront(simulator, frontName);
        int loopLimit = (int) Math.ceil(((frontMap.get("radius-x") + frontMap.get("radius-z")) * frontMap.get("intensity")) / 100);

        for(int i = 0; i < loopLimit; i++)
        {
            alterBlock(world, simulator, frontMap);
        }
    }

    public void alterBlock(World world, String simulator, Map<String, Integer> frontMap)
    {
        int[] xz = test.randomXYInFront(frontMap);

        if(!test.locationIsLoaded(world, xz[0], xz[1], false))
        {
            return;
        }

        int x = xz[0];
        int z = xz[1];
        Block highBlock = test.getTopEmptyBlock(new Location(world, x, 0, z));
        Block lowBlock = test.getTopBlock(new Location(world, x, 0, z));
        Location highLoc = highBlock.getLocation();
        Location lowLoc = lowBlock.getLocation();

        if(blockCanHaveSnow(highLoc))
        {
            highBlock.setType(Material.SNOW);
        }

        if(test.locationIsInRain(lowLoc))
        {
            if(lowBlock.getType() == Material.CAULDRON && random.nextInt(20) == 0 && lowBlock.getData() < 3)
            {
                lowBlock.setData((byte) (lowBlock.getData() + 1));
            }

            if(lowBlock.getType() == Material.SOIL)
            {
                farmland.put(lowBlock, lowBlock);
            }
        }
    }

    public void moveFront(World world, String simulator, String frontName)
    {
        String frontConfig = world.getName() + "." + simulator + "." + frontName + ".";
        String simConfig = "worlds." + world.getName() + "." + simulator + ".";
        Map<String, Integer> frontMap = test.mapFront(simulator, frontName);

        if(frontMap.get("velocity-x") != 0 && frontMap.get("velocity-z") != 0)
        {
            int frontX = frontMap.get("center-x") + frontMap.get("velocity-x");
            int frontZ = frontMap.get("center-z") + frontMap.get("velocity-z");
            int simRange = Configuration.main_config.getInt(simConfig + "simulation-radius");
            int simCenterX = Configuration.main_config.getInt(simConfig + "simulation-center-x");
            int simCenterZ = Configuration.main_config.getInt(simConfig + "simulation-center-z");

            if(frontX > simCenterX + simRange || frontX < simCenterX - simRange
                    || frontZ > simCenterZ + simRange || frontZ < simCenterZ - simRange)
            {
                removeFront(world, simulator, frontName);
            }
            else
            {
                Configuration.fronts_config.set(frontConfig + "center-x", frontX);
                Configuration.fronts_config.set(frontConfig + "center-z", frontZ);
                plugin.dynmapFunctions.moveMarker(world, simulator, frontName);

            }
        }
    }

    public void ageFront(World world, String simulator, String frontName)
    {
        String frontConfig = world.getName() + "." + simulator + "." + frontName + ".";
        int frontAge = Configuration.fronts_config.getInt(frontConfig + "age");
        int ageLimit = Configuration.fronts_config.getInt(frontConfig + "age-limit");

        if(ageLimit != 0)
        {
            if(frontAge > ageLimit)
            {
                removeFront(world, simulator, frontName);
            }
            else
            {
                Configuration.fronts_config.set(frontConfig + "age", frontAge + 1);
            }
        }
    }

    public Boolean removeFront(World world, String simulator, String currentFront)
    {
        String worldName = world.getName();

        if(simulator == null)
        {
            for(String key2 : Configuration.fronts_config.getConfigurationSection(worldName).getKeys(false))
            {
                if(Configuration.fronts_config.getConfigurationSection(worldName + "." + key2).contains(currentFront))
                {
                    simulator = key2;
                    break;
                }
            }

            if(simulator == null)
            {
                return false;
            }
        }

        plugin.dynmapFunctions.deleteMarker(world, simulator, currentFront);
        Configuration.fronts_config.getConfigurationSection(worldName + "." + simulator).set(currentFront, null);
        plugin.config.saveFronts(worldName);

        return true;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onFarmlandDecay(BlockFadeEvent event)
    {
        Block block = event.getBlock();
        Location blockLoc = block.getLocation();

        if(block.getType() == Material.SOIL && test.locationIsInRain(blockLoc))
        {
            event.setCancelled(true);
            farmland.put(block, block);
        }
    }

    public void hydrateFarmland()
    {
        for(Block block : farmland.keySet())
        {
            if(test.worldIsEnabled(block.getWorld()) && block.getType() == Material.SOIL && test.locationIsInRain(block.getLocation()))
            {
                block.setData((byte) 7);
            }
            else
            {
                farmland.remove(block);
            }
        }
    }


    public Boolean blockCanHaveSnow(Location location)
    {
        Block block = location.getBlock();

        if(!test.blockIsCold(location) || !test.locationIsAboveground(location) || block.getType() != Material.AIR)
        {
            return false;
        }

        Block block2 = block.getRelative(BlockFace.DOWN);
        Material block2Type = block2.getType();

        if((block2Type.isOccluding() || block2Type == Material.TNT || block2Type == Material.REDSTONE_BLOCK
                || block2Type == Material.LEAVES || block2Type == Material.LEAVES_2 || block2Type == Material.CACTUS)
                && (block2Type != Material.ENDER_PORTAL_FRAME && block2Type != Material.MOB_SPAWNER
                && block2Type != Material.ICE && block2Type != Material.PACKED_ICE && block.getLightFromBlocks() < 10))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}