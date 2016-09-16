package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

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

import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class EntityHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final LocationTests locationtest;
    private final ConcurrentHashMap<Wolf,Boolean> wolvesInRain = new ConcurrentHashMap<Wolf, Boolean>();
    private final FrontsWorld frontsWorld;
    private final World world;
    private Logger logger = Logger.getLogger("Minecraft");

    public EntityHandler(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.locationtest = new LocationTests(instance);
        this.frontsWorld = frontsWorld;
        this.world = frontsWorld.getWorld();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChangeWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (world != this.world) {
            if (world.hasStorm()) {
                player.setPlayerWeather(WeatherType.DOWNFALL);
            } else {
                player.setPlayerWeather(WeatherType.CLEAR);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityBurn(EntityDamageEvent event) {
        if (event.isCancelled() || event.getCause() != DamageCause.FIRE_TICK) {
            return;
        }

        Entity entity = event.getEntity();
        Location entityLoc = entity.getLocation();

        if (this.locationtest.locationIsInFront(entityLoc) && this.locationtest.locationIsInRain(entityLoc)) {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        Location entityLoc = entity.getLocation();

        if (this.locationtest.locationIsInRain(entityLoc) && !(event instanceof EntityCombustByEntityEvent)
                && !(event instanceof EntityCombustByBlockEvent)) {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        changePlayerWeather();
    }

    public void changePlayerWeather() {
        for (Player player : this.world.getPlayers()) {
            Location playerLoc = player.getLocation();

            if (!this.locationtest.locationIsInFront(playerLoc)) {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.CLEAR)) {
                    player.setPlayerWeather(WeatherType.CLEAR);
                }
            } else {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.DOWNFALL)) {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                }

                String front = this.frontsWorld.locationInWhichFront(playerLoc.getBlockX(), playerLoc.getBlockZ());
                this.plugin.getPacketHandler().changeWeather(player, front);
            }
        }
    }

    public void affectArrows() {
        Collection<Arrow> allArrows = this.world.getEntitiesByClass(Arrow.class);

        for (Arrow arrow : allArrows) {
            Location arrowLoc = arrow.getLocation();

            if (this.locationtest.locationIsInRain(arrowLoc) && this.locationtest.locationIsLoaded(arrowLoc)) {
                arrow.setFireTicks(0);
            }
        }
    }

    public void affectBlazes() {
        Collection<Blaze> allBlazes = this.world.getEntitiesByClass(Blaze.class);

        for (Blaze blaze : allBlazes) {
            Location blazeLoc = blaze.getLocation();

            if (this.locationtest.locationIsInRain(blazeLoc) && this.locationtest.locationIsLoaded(blazeLoc)) {
                blaze.damage(1.0);
            }
        }
    }

    public void affectSnowmen() {
        Collection<Snowman> allSnowmen = this.world.getEntitiesByClass(Snowman.class);

        for (Snowman snowman : allSnowmen) {
            Location snowmanLoc = snowman.getLocation();

            if (this.locationtest.locationIsInRain(snowmanLoc) && this.locationtest.locationIsLoaded(snowmanLoc)) {
                snowman.damage(1.0);
            }
        }
    }

    public void affectEndermen() {
        Collection<Enderman> allEndermen = this.world.getEntitiesByClass(Enderman.class);

        for (Enderman enderman : allEndermen) {
            Location endermanLoc = enderman.getLocation();

            if (!this.locationtest.locationIsInRain(endermanLoc) || !this.locationtest.locationIsLoaded(endermanLoc)) {
                continue;
            }

            enderman.damage(1.0);

            boolean flag = false;
            int i = 0;

            while (!flag && i < 64) {
                int x = endermanLoc.getBlockX() + this.random.nextInt(64) - 32;
                int y = endermanLoc.getBlockY() - this.random.nextInt(32);
                int z = endermanLoc.getBlockZ() + this.random.nextInt(64) - 32;
                Location newLoc = new Location(world, x, y, z);

                if (!this.locationtest.locationIsLoaded(newLoc)) {
                    continue;
                }

                Block block = newLoc.getBlock();
                boolean flag2 = false;

                while (!flag2 && y > 0) {
                    if (block.getRelative(BlockFace.DOWN).getType().isSolid() && block.isEmpty()
                            && block.getRelative(BlockFace.UP).isEmpty()
                            && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty()) {
                        if (enderman.isInsideVehicle() && enderman.getVehicle().getType() == EntityType.MINECART) {
                            world.playSound(endermanLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                            break;
                        }

                        world.playSound(endermanLoc, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                        enderman.teleport(new Location(world, x, y, z));
                        flag2 = true;
                        flag = true;
                    } else {
                        --y;
                    }
                }

                ++i;
            }
        }
    }

    public void affectWolves() {
        Collection<Wolf> allWolves = this.world.getEntitiesByClass(Wolf.class);

        for (Wolf wolf : allWolves) {
            Location wolfLoc = wolf.getLocation();

            if (this.locationtest.locationIsInRain(wolfLoc) && !wolvesInRain.contains(wolf)) {
                wolvesInRain.put(wolf, true);
            }

            if (!this.locationtest.locationIsInRain(wolfLoc) && wolvesInRain.contains(wolf)) {
                wolvesInRain.remove(wolf);
                wolf.playEffect(EntityEffect.WOLF_SHAKE);
                world.playSound(wolfLoc, Sound.ENTITY_WOLF_SHAKE, 0.4F, 1.0F);
            }
        }
    }
}