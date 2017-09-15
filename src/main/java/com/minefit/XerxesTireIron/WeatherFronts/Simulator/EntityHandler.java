package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
import org.bukkit.util.Vector;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class EntityHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final Set<Wolf> wolvesInRain = new HashSet<>();
    private final Simulator simulator;
    private final World world;
    private final BlockFunctions blockFunction;

    public EntityHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.blockFunction = new BlockFunctions(instance, simulator);
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
        World world = event.getEntity().getWorld();

        if (world != this.world || event.isCancelled() || event.getCause() != DamageCause.FIRE_TICK) {
            return;
        }

        Entity entity = event.getEntity();
        FrontLocation location = this.simulator.newFrontLocation(entity.getLocation());

        if (!location.isLoaded() || !location.isInStorm()) {
            return;
        }

        if (this.blockFunction.isInRain(location)) {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityCombust(EntityCombustEvent event) {
        World world = event.getEntity().getWorld();

        if (world != this.world || event.isCancelled()) {
            return;
        }

        Entity entity = event.getEntity();
        FrontLocation location = this.simulator.newFrontLocation(entity.getLocation());

        if (!location.isLoaded() || !location.isInStorm()) {
            return;
        }

        if (this.blockFunction.isInWeather(location) && !(event instanceof EntityCombustByEntityEvent)
                && !(event instanceof EntityCombustByBlockEvent)) {
            entity.setFireTicks(0);
            event.setCancelled(true);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        World world = event.getPlayer().getWorld();

        if (world != this.world) {
            return;
        }

        changePlayerWeather();
    }

    public void changePlayerWeather() {
        for (Player player : this.world.getPlayers()) {
            FrontLocation location = this.simulator.newFrontLocation(player.getLocation());

            if (!location.isInStorm()) {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.CLEAR)) {
                    player.setPlayerWeather(WeatherType.CLEAR);
                }
            } else {
                if (player.getPlayerWeather() == null || !player.getPlayerWeather().equals(WeatherType.DOWNFALL)) {
                    player.setPlayerWeather(WeatherType.DOWNFALL);
                }

                this.plugin.getPacketHandler().changeWeather(player, location.inWhichStorm());
            }
        }
    }

    public void affectArrows() {
        Collection<Arrow> allArrows = this.world.getEntitiesByClass(Arrow.class);

        for (Arrow arrow : allArrows) {
            if (this.blockFunction.isInRain(arrow.getLocation())) {
                arrow.setFireTicks(0);
            }
        }
    }

    public void affectBlazes() {
        Collection<Blaze> allBlazes = this.world.getEntitiesByClass(Blaze.class);

        for (Blaze blaze : allBlazes) {
            if (this.blockFunction.isInRain(blaze.getLocation())) {
                blaze.damage(1.0);
            }
        }
    }

    public void affectSnowmen() {
        Collection<Snowman> allSnowmen = this.world.getEntitiesByClass(Snowman.class);

        for (Snowman snowman : allSnowmen) {
            if (this.blockFunction.isInRain(snowman.getLocation())) {
                snowman.damage(1.0);
            }
        }
    }

    public void affectEndermen() {
        Collection<Enderman> allEndermen = this.world.getEntitiesByClass(Enderman.class);

        for (Enderman enderman : allEndermen) {
            FrontLocation location = this.simulator.newFrontLocation(enderman.getLocation());

            if (!this.blockFunction.isInRain(location)) {
                continue;
            }

            enderman.damage(1.0);

            boolean flag = false;
            int i = 0;

            while (!flag && i < 64) {
                int x = location.getBlockX() + this.random.nextInt(64) - 32;
                int y = location.getBlockY() - this.random.nextInt(32);
                int z = location.getBlockZ() + this.random.nextInt(64) - 32;
                FrontLocation newLoc = this.simulator.newFrontLocation(x, y, z);

                if (!newLoc.isLoaded()) {
                    continue;
                }

                Block block = newLoc.getBlock();
                boolean flag2 = false;

                while (!flag2 && y > 0) {
                    if (block.getRelative(BlockFace.DOWN).getType().isSolid() && block.isEmpty()
                            && block.getRelative(BlockFace.UP).isEmpty()
                            && block.getRelative(BlockFace.UP).getRelative(BlockFace.UP).isEmpty()) {
                        if (enderman.isInsideVehicle() && enderman.getVehicle().getType() == EntityType.MINECART) {
                            world.playSound(location, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
                            break;
                        }

                        world.playSound(location, Sound.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
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
            FrontLocation location = this.simulator.newFrontLocation(wolf.getLocation());

            if (this.blockFunction.isInRain(location)) {
                this.wolvesInRain.add(wolf);
            } else {
                if (this.wolvesInRain.contains(wolf) && !entityIsMoving(wolf.getVelocity())) {
                    this.wolvesInRain.remove(wolf);
                    wolf.playEffect(EntityEffect.WOLF_SHAKE);
                    world.playSound(location, Sound.ENTITY_WOLF_SHAKE, 0.4F, 1.0F);
                }
            }
        }
    }

    public boolean entityIsMoving(Vector vector) {
        return vector.getX() != 0 || vector.getZ() != 0;
    }
}