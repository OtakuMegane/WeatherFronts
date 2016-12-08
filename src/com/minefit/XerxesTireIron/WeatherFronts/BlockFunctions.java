package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class BlockFunctions {
    private final World world;
    private final WeatherFronts plugin;
    private final Simulator simulator;

    public BlockFunctions(WeatherFronts instance, Simulator simulator) {
        this.world = simulator.getWorld();
        this.simulator = simulator;
        this.plugin = instance;
    }

    public Block findHighestBlock(Location location, int start) {
        int i = 255;

        if (start < 256 && start > 0) {
            i = start;
        }

        for (; i > 0; --i) {
            Block block = this.world.getBlockAt(location.getBlockX(), i, location.getBlockZ());

            if (!block.isEmpty()) {
                return block;
            }
        }

        return world.getBlockAt(location.getBlockX(), 0, location.getBlockZ());
    }

    public Block getTopBlock(Location location) {
        return findHighestBlock(location, 255);
    }

    public Block getTopShelterBlock(Location location) {
        int start = 255;
        Block block = findHighestBlock(location, start);

        while (!isShelter(block)) {
            start = block.getY();
            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopLiquidBlock(Location location) {
        int start = 255;
        Block block = findHighestBlock(location, start);

        while (!block.isLiquid()) {
            start = block.getY();
            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopSolidBlock(Location location) {
        int start = 255;
        Block block = findHighestBlock(location, start);

        while (!block.getType().isSolid()) {
            start = block.getY();
            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopBlockLightningValid(Location location) {
        int start = 255;
        Block block = findHighestBlock(location, start);

        while (!block.getType().isSolid() && !block.isLiquid()) {
            start = block.getY();
            block = findHighestBlock(location, start);
        }

        return block.getRelative(BlockFace.UP);
    }

    public Block getTopEmptyBlock(Location location) {
        return findHighestBlock(location, 255).getRelative(BlockFace.UP);
    }

    public boolean adjacentBlockExposed(Block block) {
        FrontLocation eastLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.EAST));
        FrontLocation westLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.WEST));
        FrontLocation northLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.NORTH));
        FrontLocation southLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.SOUTH));
        return eastLoc.isInRain() || westLoc.isInRain() || northLoc.isInRain() || southLoc.isInRain();
    }

    public boolean blockTypeCanSpawnHostile(Material material) {
        return material.isOccluding() && material != Material.ENDER_PORTAL_FRAME;
    }

    public boolean isShelter(Block block) {
        Material material = block.getType();
        return material != Material.CARPET && material != Material.DIODE_BLOCK_OFF
                && material != Material.DIODE_BLOCK_ON && material != Material.FLOWER_POT && material != Material.LADDER
                && material != Material.SKULL && material != Material.SNOW && material != Material.TORCH
                && material != Material.VINE && material != Material.WEB;
    }

    public boolean canFormSnow(Material material) {
        return (material.isOccluding() && material != Material.ENDER_PORTAL_FRAME && material != Material.MOB_SPAWNER
                && material != Material.PACKED_ICE)
                || (material == Material.TNT || material == Material.REDSTONE_BLOCK || material == Material.LEAVES
                        || material == Material.LEAVES_2 || material == Material.CACTUS);
    }

}
