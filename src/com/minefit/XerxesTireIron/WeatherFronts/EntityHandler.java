package com.minefit.XerxesTireIron.WeatherFronts;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustByEntityEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class EntityHandler implements Listener
{
    private XORShiftRandom random = new XORShiftRandom();
    private WeatherFronts plugin;
    private Logger logger = Logger.getLogger("Minecraft");
    private Set<Wolf> wolvesInRain;
    private Map<UUID,Integer> endermanAttempts;

    EntityHandler(WeatherFronts instance)
    {
        plugin = instance;
        wolvesInRain = new HashSet<Wolf>();
        endermanAttempts = new HashMap<UUID,Integer>();
    }

    private FunctionsAndTests test = new FunctionsAndTests(plugin);

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event)
    {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if(!test.worldIsEnabled(world))
        {
            if(world.hasStorm())
            {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            }
            else
            {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityBurn(EntityDamageEvent event)
    {
        Entity entity = event.getEntity();
        Location entityLoc = entity.getLocation();
        DamageCause reason = event.getCause();

        if(reason == DamageCause.FIRE_TICK && test.locationIsInFront(entityLoc) && !test.locationIsInRain(entityLoc))
        {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombust(EntityCombustEvent event)
    {
        Entity entity = event.getEntity();
        Location entityLoc = entity.getLocation();

        if(test.locationIsInRain(entityLoc) && !(event instanceof EntityCombustByEntityEvent) && !(event instanceof EntityCombustByBlockEvent))
        {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event)
    {
        changePlayerWeather(event.getPlayer().getWorld());
    }

    public void changePlayerWeather(World world)
    {
        if(!test.worldIsEnabled(world))
        {
            return;
        }

        Iterator<Player> iterator = world.getPlayers().iterator();

        while(iterator.hasNext())
        {
            Player player = iterator.next();
            Location playerLoc = player.getLocation();

            if(!test.locationIsInFront(playerLoc))
            {
                if(player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.CLEAR))
                {
                    player.setPlayerWeather(WeatherType.CLEAR);
                }
            }
            else
            {
                if(player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.DOWNFALL))
                {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                }

                String[] front = test.locationInWhichFront(player.getLocation(), true, false);
                plugin.packetHandler.changeWeather(player, front);
            }
        }
    }
    
    public void affectArrows(World world)
    {
        Collection<Arrow> allArrows = world.getEntitiesByClass(Arrow.class);
        
        for(Arrow arrow : allArrows)
        {
            Location arrowLoc = arrow.getLocation();

            if(test.locationIsInRain(arrowLoc) && test.locationIsLoaded(arrowLoc))
            {
                arrow.setFireTicks(0);
            }
        }
    }

    public void affectBlazes(World world)
    {
        Collection<Blaze> allBlazes = world.getEntitiesByClass(Blaze.class);
        
        for(Blaze blaze : allBlazes)
        {
            Location blazeLoc = blaze.getLocation();

            if(test.locationIsInRain(blazeLoc) && test.locationIsLoaded(blazeLoc))
            {
                blaze.damage(1.0);
            }
        }
    }
    
    public void affectSnowmen(World world)
    {
        Collection<Snowman> allSnowmen = world.getEntitiesByClass(Snowman.class);
        
        for(Snowman snowman : allSnowmen)
        {
            Location snowmanLoc = snowman.getLocation();

            if(test.locationIsInRain(snowmanLoc) && test.locationIsLoaded(snowmanLoc))
            {
                snowman.damage(1.0);
            }
        }
    }

    public void affectEndermen(World world)
    {
        Collection<Enderman> allEndermen = world.getEntitiesByClass(Enderman.class);

        for(Enderman enderman : allEndermen)
        {
            Location endermanLoc = enderman.getLocation();

            if(!test.locationIsInRain(endermanLoc) || !test.locationIsLoaded(endermanLoc))
            {
                continue;
            }

            enderman.damage(1.0);

            boolean flag = false;
            int i = 0;

            while(!flag && i < 64)
            {
                int x = endermanLoc.getBlockX() + random.nextInt(64) - 32;
                int y = endermanLoc.getBlockY() - random.nextInt(32);
                int z = endermanLoc.getBlockZ() + random.nextInt(64) - 32;
                Location newLoc = new Location(world, x, y, z);

                if(!test.locationIsLoaded(newLoc))
                {
                    continue;
                }

                Block block = newLoc.getBlock();
                boolean flag2 = false;

                while(!flag2 && y > 0)
                {
                    if(block.getRelative(BlockFace.DOWN).getType().isSolid()
                            && block.isEmpty()
                            && block.getRelative(BlockFace.UP).isEmpty()
                            && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty())
                    {
                        if(enderman.isInsideVehicle() && enderman.getVehicle().getType() == EntityType.MINECART)
                        {
                            world.playSound(endermanLoc, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                            break;
                        }

                        world.playSound(endermanLoc, Sound.ENDERMAN_TELEPORT, 1.0F, 1.0F);
                        enderman.teleport(new Location(world, x, y, z));
                        flag2 = true;
                        flag = true;
                    }
                    else
                    {
                        --y;
                    }
                }

                ++i;
            }
        }
    }

    public void affectWolves(World world)
    {
        Collection<Wolf> allWolves = world.getEntitiesByClass(Wolf.class);

        for(Wolf wolf : allWolves)
        {
            Location wolfLoc = wolf.getLocation();

            if(test.locationIsInRain(wolfLoc) && !wolvesInRain.contains(wolf))
            {
                wolvesInRain.add(wolf);
            }

            if(!test.locationIsInRain(wolfLoc) && wolvesInRain.contains(wolf))
            {
                wolvesInRain.remove(wolf);
                wolf.playEffect(EntityEffect.WOLF_SHAKE);
                world.playSound(wolfLoc, Sound.WOLF_SHAKE, 0.4F, 1.0F);
            }
        }
    }

    public void spawnMobs(World world)
    {
        for(Chunk chunk : world.getLoadedChunks())
        {
            if(random.nextInt(50) == 0)
            {
                boolean result = attemptSpawn(chunk);
            }
        }
    }

    public boolean attemptSpawn(Chunk chunk)
    {
        World world = chunk.getWorld();

        if((world.getTime() > 13187 && world.getTime() < 22812)
                || world.getDifficulty() == Difficulty.PEACEFUL || !chunk.isLoaded())
        {
            return false;
        }

        int baseX = chunk.getX() << 4;
        int baseZ = chunk.getZ() << 4;
        int x = random.nextIntRange(baseX, baseX + 15);
        int z = random.nextIntRange(baseZ, baseZ + 15);
        Block block = test.getTopSolidBlock(new Location(world, x, 0, z));
        
        if(!block.getType().isSolid() || !test.locationIsInStorm(block.getLocation()))
        {
            return false;
        }
        
        Location location = new Location(world, x, block.getY() + 1, z);
        int hostileCount = 0;

        for(Entity entity : chunk.getEntities())
        {
            if(entity instanceof Monster)
            {
                ++hostileCount;

                if(world.getHighestBlockYAt(entity.getLocation()) >= entity.getLocation().getBlockY())
                {
                    return false;
                }
            }
            else if(entity.getType() == EntityType.PLAYER)
            {
                if(location.distance(location) < 24)
                {
                    return false;
                }
            }
        }
        
        int height = 2;
        int width = 1;
        EntityType mob = randomHostile();
        
        if(mob == EntityType.SPIDER || mob == EntityType.SKELETON)
        {
            width = 2;
        }
        
        if(mob == EntityType.ENDERMAN)
        {
            height = 3;
        }

        if(hostileCount >= world.getMonsterSpawnLimit() || location.getBlock().getLightFromBlocks() > 7 || !mobCanSpawn(location, width, height))
        {
            return false;
        }

        Entity newEntity;
        newEntity = world.spawnEntity(location, randomHostile());
        return true;
    }

    public boolean mobCanSpawn(Location location, int width, int height)
    {
        World world = location.getWorld();
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        int xx = 0;
        int yy = 0;
        int zz = 0;

        for(;xx < width; ++xx)
        {
            for(;zz < width; ++zz)
            {
                for(;yy < height; ++yy)
                {
                    Block block = world.getBlockAt(x + width, y + height, z + width);

                    if(block.getType().isSolid() || block.isLiquid())
                    {
                        return false;
                    }
                }
            }
        }


        return true;
    }

    public EntityType randomHostile()
    {
        // From BiomeBase.java, hostile mob weights (minus slime for now) totals 415
        int roll = random.nextInt(415);

        if(roll < 100)
        {
            return EntityType.SPIDER;
        }
        else if(roll < 200)
        {
            return EntityType.ZOMBIE;
        }
        else if(roll < 300)
        {
            return EntityType.SKELETON;
        }
        else if(roll < 400)
        {
            return EntityType.CREEPER;
        }
        else if(roll < 410)
        {
            return EntityType.ENDERMAN;
        }
        else
        {
            return EntityType.WITCH;
        }
    }
}