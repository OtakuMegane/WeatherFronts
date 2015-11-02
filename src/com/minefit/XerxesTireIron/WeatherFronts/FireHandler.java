package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

public class FireHandler implements Listener
{
    private Random random = new XORShiftRandom();
    private WeatherFronts plugin;
    private ConcurrentHashMap<Block,Integer> fireBlocks;
    private Logger logger = Logger.getLogger("Minecraft");

    public FireHandler(WeatherFronts instance)
    {
        plugin = instance;
        fireBlocks = new ConcurrentHashMap<Block,Integer>();
    }

    FunctionsAndTests test = new FunctionsAndTests(plugin);

    public void extinguishFire()
    {
        for(Block block : fireBlocks.keySet())
        {
            Location location = block.getLocation();

            if(!test.locationIsLoaded(location))
            {
                fireBlocks.remove(block);
            }

            if(!test.locationIsLoaded(location) || block.getType() != Material.FIRE || block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK)
            {
                fireBlocks.remove(block);
                continue;
            }

            if(test.locationIsInRain(location) || test.adjacentBlockExposed(block))
            {

                if(fireBlocks.get(block) < 2)
                {
                    if(block.getData() < 15)
                    {
                        block.setData((byte) 15);
                    }

                    fireBlocks.put(block, fireBlocks.get(block) + 1);
                }
                else
                {
                    block.setType(Material.AIR);
                    fireBlocks.remove(block);
                }
            }
            else
            {
                if(fireBlocks.get(block) > 0)
                {
                    block.setType(Material.AIR);
                    fireBlocks.remove(block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockIgnite(BlockIgniteEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }

        Block block = event.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();

        if(test.worldIsEnabled(world) && test.locationIsInFront(location) && (test.locationIsInRain(location) || test.adjacentBlockExposed(block)))
        {
            addFireBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBurn(BlockBurnEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }

        Block block = event.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();

        if(test.worldIsEnabled(world) && test.locationIsInFront(location) && (test.locationIsInRain(location) || test.adjacentBlockExposed(block)))
        {
            event.setCancelled(true);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockSpread(BlockSpreadEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }

        Block block = event.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();
        Block source = event.getSource();

        if(test.worldIsEnabled(world) && test.locationIsInFront(location) && (test.locationIsInRain(location) || test.adjacentBlockExposed(block)))
        {
            event.setCancelled(true);
            addFireBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onLightningStrike(LightningStrikeEvent event)
    {
        if(event.isCancelled())
        {
            return;
        }

        Block block = event.getLightning().getLocation().getBlock().getRelative(BlockFace.DOWN);
        World world = event.getWorld();

        if(test.createFulgurite(block.getLocation()))
        {
            fulgurite(world, block);
        }
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onClickEvent(PlayerInteractEvent event)
    {
        logger.info("BLOCK: " + event.getClickedBlock().getType() + "  LIGHT: " + event.getClickedBlock().getRelative(BlockFace.UP).getLightFromBlocks());
    }

    public void addAdjacentFire(Block block)
    {
        Location location = block.getLocation();

        if(block.getRelative(BlockFace.UP).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.UP), 0);
        }

        if(block.getRelative(BlockFace.DOWN).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.DOWN), 0);
        }

        if(block.getRelative(BlockFace.NORTH).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.NORTH), 0);
        }

        if(block.getRelative(BlockFace.SOUTH).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.SOUTH), 0);
        }

        if(block.getRelative(BlockFace.EAST).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.EAST), 0);
        }

        if(block.getRelative(BlockFace.WEST).getType() == Material.FIRE)
        {
            fireBlocks.put(block.getRelative(BlockFace.WEST), 0);
        }
    }

    public void addFireBlock(Block block, Boolean addNearby)
    {
        if(!fireBlocks.containsKey(block))
        {
            fireBlocks.put(block, 0);
        }

        if(addNearby)
        {
            addAdjacentFire(block);
        }
    }

    public void fulgurite(World world, Block block)
    {
        Material blockType = block.getType();
        Material newType = convertBlock(blockType);
        String worldName = world.getName();
        String simulator = test.getSimulator(block.getLocation());
        String simConfig = "worlds." + worldName + "." + simulator + ".";

        if(newType != null)
        {
            block.setType(newType);
            int limit = random.nextInt(Configuration.main_config.getInt(simConfig + "fulgurite-max-size"));
            Block baseBlock = block;

            int i = 0;

            while(i < limit)
            {
                if(limit > 3 && i == 0)
                {
                    newType = convertBlock(block.getRelative(BlockFace.DOWN).getType());

                    if(newType != null)
                    {
                        baseBlock = block.getRelative(BlockFace.DOWN);
                        baseBlock.setType(newType);
                        i++;
                        continue;
                    }
                }

                BlockFace[] faces = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN};
                Block block2 = baseBlock.getRelative(faces[random.nextInt(5)]);
                newType = convertBlock(block2.getType());

                if(newType != null)
                {
                    block2.setType(newType);

                    if(random.nextInt(2) == 0)
                    {
                        baseBlock = block2;
                    }
                }

                i++;
            }
        }
    }

    public Material convertBlock(Material blockType)
    {
        if(blockType == Material.SAND)
        {
            return Material.GLASS;
        }
        else if(blockType == Material.CLAY)
        {
            return Material.HARD_CLAY;
        }

        return null;
    }
}
