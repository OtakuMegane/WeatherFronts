package com.minefit.xerxestireiron.weatherfronts.Simulator;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Ageable;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.weather.LightningStrikeEvent;

import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.NMSBullshit.NMSHandler;

public class FireHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final ConcurrentHashMap<Block, Integer> fireBlocks = new ConcurrentHashMap<>();
    private final BlockFunctions blockFunction;
    private final Simulator simulator;
    private final Boolean fastExtinguish;
    private final NMSHandler nmsHandler;

    public FireHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.simulator = simulator;
        this.fastExtinguish = simulator.getSimulatorConfig().getBoolean("fast-extinguish-fire", false);
        this.nmsHandler = new NMSHandler(this.plugin);
    }

    public void extinguishFire() {
        for (Block block : fireBlocks.keySet()) {
            FrontsLocation location = new FrontsLocation(this.simulator, block);

            if (!location.isLoaded()) {
                continue;
            }

            if (!actOnBlock(block) || block.getType() != Material.FIRE) {
                this.fireBlocks.remove(block);
                continue;
            }

            if (this.fastExtinguish) {
                fastExtinguishFire(block);
            } else {
                Ageable fire = (Ageable) block.getBlockData();

                if (random.nextInt(20) == 0 && random.nextFloat() < 0.2F + (float) fire.getAge() * 0.03F) {
                    removeFire(block);
                }
            }
        }
    }

    public void fastExtinguishFire(Block block) {
        Ageable fire = (Ageable) block.getBlockData();
        int fireStatus = this.fireBlocks.get(block);

        if (fire.getAge() >= 15 || fireStatus >= 30) {
            if (random.nextInt(4) == 0) {
                removeFire(block);
            }
        } else {
            this.fireBlocks.put(block, fireStatus + 1);
        }
    }

    private void removeFire(Block block) {
        block.setType(Material.AIR);
        this.fireBlocks.remove(block);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();
        FrontsLocation location = new FrontsLocation(this.simulator, block.getLocation());

        if (!this.simulator.isInSimulator(location.getBlockX(), location.getBlockZ())) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (event.getCause() == IgniteCause.LIGHTNING && location.inSpawnChunk()
                && !simulatorConfig.getBoolean("lightning-fire-in-spawn-chunk", false)) {
            event.setCancelled(true);
            return;
        }

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockSpread(BlockSpreadEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())
                || event.getBlock().getType() != Material.FIRE) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (event.isCancelled() || !this.plugin.worldEnabled(world)) {
            return;
        }

        Block block = event.getLightning().getLocation().getBlock();
        FrontsLocation location = new FrontsLocation(this.simulator, block.getLocation());

        if (!this.simulator.isInSimulator(location)) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (simulatorConfig.getBoolean("spawn-skeleton-traps", true)
                && random.nextDouble() * 100 < simulatorConfig.getDouble("skeleton-trap-chance")) {
            this.nmsHandler.createHorseTrap(location);
        }

        if (!simulatorConfig.getBoolean("create-fulgurites", false)) {
            return;
        }

        if (location.inSpawnChunk() && !simulatorConfig.getBoolean("fulgurite-in-spawn-chunk", false)) {
            return;
        }

        double fulguriteChance = simulatorConfig.getDouble("fulgurite-chance", 0.25D);

        if (this.random.nextDouble() < (fulguriteChance / 100) || fulguriteChance >= 100) {
            new Fulgurite(this.simulator, block);
        }

    }

    private void addAdjacentFire(Block block) {
        if (block.getRelative(BlockFace.UP).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.UP));
        }

        if (block.getRelative(BlockFace.DOWN).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.DOWN));
        }

        if (block.getRelative(BlockFace.NORTH).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.NORTH));
        }

        if (block.getRelative(BlockFace.SOUTH).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.SOUTH));
        }

        if (block.getRelative(BlockFace.EAST).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.EAST));
        }

        if (block.getRelative(BlockFace.WEST).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.WEST));
        }
    }

    private void addFireBlock(Block block) {
        if (!this.fireBlocks.containsKey(block)) {
            this.fireBlocks.put(block, 0);
        }
    }

    private boolean actOnBlock(Block block) {
        if (this.blockFunction.isInRain(block) || this.blockFunction.adjacentBlockExposed(block)) {
            return true;
        }

        Material downType = block.getRelative(BlockFace.DOWN).getType();

        if (downType.equals(Material.NETHERRACK) || downType.equals(Material.MAGMA_BLOCK)) {
            return false;
        }

        return false;
    }
}
