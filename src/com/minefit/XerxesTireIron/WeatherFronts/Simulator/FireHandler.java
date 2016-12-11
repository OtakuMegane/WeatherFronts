package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

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

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class FireHandler implements Listener {
    private final Random random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final ConcurrentHashMap<Block, Integer> fireBlocks = new ConcurrentHashMap<Block, Integer>();
    private final BlockFunctions blockFunction;
    private final Simulator simulator;

    public FireHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.simulator = simulator;
    }

    public void extinguishFire() {
        for (Block block : fireBlocks.keySet()) {
            FrontLocation location = this.simulator.newFrontLocation(block);

            if (!location.isLoaded() || block.getType() != Material.FIRE
                    || block.getRelative(BlockFace.DOWN).getType() == Material.NETHERRACK) {
                this.fireBlocks.remove(block);
                continue;
            }
            if (actOnBlock(block)) {
                int age = block.getData();

                if (age < 15) {
                    age += Integer.valueOf(this.random.nextInt(3) / 2);
                    if (age > 15) {
                        age = 15;
                    }
                    block.setData((byte) age);
                } else {
                    if (this.random.nextInt(4) == 0) {
                        block.setType(Material.AIR);
                        this.fireBlocks.remove(block);
                    }
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
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (!this.simulator.isInSimulator(location.getBlockX(), location.getBlockZ())) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (event.getCause() == IgniteCause.LIGHTNING && location.inSpawnChunk()
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

        if (block.getType() == Material.FIRE && actOnBlock(block)) {
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

        Block block = event.getLightning().getLocation().getBlock();
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (!this.simulator.isInSimulator(location)) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (!simulatorConfig.getBoolean("create-fulgurites")) {
            return;
        }

        if (this.random.nextInt(1) < simulatorConfig.getInt("fulgurite-chance")
                || simulatorConfig.getInt("fulgurite-chance") == 100) {
            generateFulgurite(block);
        }
    }

    private void addAdjacentFire(Block block) {
        if (block.getRelative(BlockFace.UP).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.UP), 0);
        }

        if (block.getRelative(BlockFace.DOWN).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.DOWN), 0);
        }

        if (block.getRelative(BlockFace.NORTH).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.NORTH), 0);
        }

        if (block.getRelative(BlockFace.SOUTH).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.SOUTH), 0);
        }

        if (block.getRelative(BlockFace.EAST).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.EAST), 0);
        }

        if (block.getRelative(BlockFace.WEST).getType() == Material.FIRE) {
            this.fireBlocks.put(block.getRelative(BlockFace.WEST), 0);
        }
    }

    private void addFireBlock(Block block, Boolean addNearby) {
        if (!this.fireBlocks.containsKey(block)) {
            this.fireBlocks.put(block, 0);
        }

        if (addNearby) {
            addAdjacentFire(block);
        }
    }

    public void generateFulgurite(Block block) {
        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (location.inSpawnChunk() && !simulatorConfig.getBoolean("fulgurite-in-spawn-chunk")) {
            return;
        }

        Block blockDown = block.getRelative(BlockFace.DOWN);
        Material blockType = convertBlock(blockDown.getType());

        if (blockType == null) {
            return;
        }

        blockDown.setType(blockType);
        int limit = this.random.nextInt(simulatorConfig.getInt("fulgurite-max-size"));
        Block baseBlock = blockDown;
        int i = 0;
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN };

        for (i = 0; i < limit; ++i) {
            Block block2 = baseBlock.getRelative(faces[this.random.nextInt(5)]);

            if (i == 0 && limit > 2) {
                block2 = baseBlock.getRelative(BlockFace.DOWN);
            }

            Material newType = convertBlock(block2.getType());

            if (newType != null) {
                block2.setType(newType);
            }

            if (this.random.nextInt(2) == 0) {
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
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());
        return location.isInRain() || this.blockFunction.adjacentBlockExposed(block);
    }
}
