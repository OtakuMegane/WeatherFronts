package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class FireHandler implements Listener {
    private final Random random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final ConcurrentHashMap<Block, Integer> fireBlocks = new ConcurrentHashMap<Block, Integer>();
    private final LocationTests locationtest;
    private final BlockTests blocktest;
    private Logger logger = Logger.getLogger("Minecraft");

    public FireHandler(WeatherFronts instance) {
        this.plugin = instance;
        this.locationtest = new LocationTests(instance);
        this.blocktest = new BlockTests(instance);
    }

    public void extinguishFire() {
        for (Block block : fireBlocks.keySet()) {
            Location location = block.getLocation();

            if (!this.locationtest.locationIsLoaded(location) || block.getType() != Material.FIRE
                    || block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK) {
                fireBlocks.remove(block);
                continue;
            }

            if (actOnBlock(block)) {
                int age = block.getData();

                if (age < 15) {
                    age += Integer.valueOf(this.random.nextInt(12) / 2); // Adjusted for 5 tick interval
                    if (age > 15) {
                        age = 15;
                    }
                    block.setData((byte) age);
                } else {
                    if (this.random.nextInt(4) == 0) {
                        block.setType(Material.AIR);
                        fireBlocks.remove(block);
                    }
                }
            } else {
                if (fireBlocks.get(block) > 0) {
                    block.setType(Material.AIR);
                    fireBlocks.remove(block);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();
        World world = block.getWorld();
        Location location = block.getLocation();
        YamlConfiguration simulatorConfig = this.plugin.getWorldHandle(world).getSimulatorByLocation(location)
                .getSimulatorConfig();

        if (event.getCause() == IgniteCause.LIGHTNING && this.locationtest.locationInSpawnChunk(location)
                && !simulatorConfig.getBoolean("lightning-fire-in-spawn-chunk")) {
            event.setCancelled(true);
            return;
        }

        if (actOnBlock(block)) {
            addFireBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            event.setCancelled(true);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockSpread(BlockSpreadEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            event.setCancelled(true);
            addFireBlock(block, true);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (event.isCancelled() || !this.plugin.worldEnabled(world)) {
            return;
        }

        Location location = event.getLightning().getLocation();
        Block block = event.getLightning().getLocation().getBlock();
        YamlConfiguration simulatorConfig = this.plugin.getWorldHandle(world).getSimulatorByLocation(location).getSimulatorConfig();

        if (!simulatorConfig.getBoolean("create-fulgurites") ) {
            return;
        }

        if(this.random.nextInt(100) < simulatorConfig.getInt("fulgurite-chance") || simulatorConfig.getInt("fulgurite-chance") == 100)
        {
            generateFulgurite(block);
        }
    }

    private void addAdjacentFire(Block block) {
        if (block.getRelative(BlockFace.UP).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.UP), 0);
        }

        if (block.getRelative(BlockFace.DOWN).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.DOWN), 0);
        }

        if (block.getRelative(BlockFace.NORTH).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.NORTH), 0);
        }

        if (block.getRelative(BlockFace.SOUTH).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.SOUTH), 0);
        }

        if (block.getRelative(BlockFace.EAST).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.EAST), 0);
        }

        if (block.getRelative(BlockFace.WEST).getType() == Material.FIRE) {
            fireBlocks.put(block.getRelative(BlockFace.WEST), 0);
        }
    }

    private void addFireBlock(Block block, Boolean addNearby) {
        if (!fireBlocks.containsKey(block)) {
            fireBlocks.put(block, 0);
        }

        if (addNearby) {
            addAdjacentFire(block);
        }
    }

    public void generateFulgurite(Block block) {

        FrontsWorld frontsWorld = this.plugin.getWorldHandle(block.getWorld());
        YamlConfiguration simulatorConfig = frontsWorld.getSimulatorByLocation(block.getLocation())
                .getSimulatorConfig();

        Material blockType = block.getType();
        Material newType = convertBlock(blockType);

        if (this.locationtest.locationInSpawnChunk(block.getLocation())
                && !simulatorConfig.getBoolean("fulgurite-in-spawn-chunk")) {
            return;
        }

        if (newType == null) {
            return;
        }

        block.setType(newType);
        int limit = random.nextInt(simulatorConfig.getInt("fulgurite-max-size"));
        Block baseBlock = block;

        int i = 0;

        for (i = 0; i < limit; ++i) {
            BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN };
            Block block2 = baseBlock.getRelative(faces[random.nextInt(5)]);

            if (limit > 2 && i == 0) {
                block2 = block.getRelative(BlockFace.DOWN);
            }

            newType = convertBlock(block2.getType());

            if (newType == null) {
                return;
            }

            block2.setType(newType);

            if (random.nextInt(2) == 0) {
                baseBlock = block2;
            }

        }
    }

    private Material convertBlock(Material blockType) {
        if (blockType == Material.SAND) {
            return Material.GLASS;
        } else if (blockType == Material.CLAY) {
            return Material.HARD_CLAY;
        }

        return null;
    }

    private boolean actOnBlock(Block block) {
        return this.locationtest.locationIsInRain(block.getLocation()) || this.blocktest.adjacentBlockExposed(block);
    }
}
