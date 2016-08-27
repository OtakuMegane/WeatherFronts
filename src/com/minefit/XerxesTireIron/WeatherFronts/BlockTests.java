package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;

public class BlockTests {
    private final WeatherFronts plugin;
    private final Functions functions;

    public BlockTests(WeatherFronts instance) {
        this.plugin = instance;
        this.functions = new Functions(instance);
    }

    public Block getTopBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, false, false);
    }

    public Block getTopLightningBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, true, true);
    }

    public Block getTopSolidBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, true, false);
    }

    public Block getTopLiquidBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, false, true);
    }

    public int getTopBlockY(Location location) {
        return this.functions.findHighestBlock(location, 255, false, false).getY();
    }

    public Block getTopEmptyBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, false, false).getRelative(BlockFace.UP);
    }

    public boolean adjacentBlockExposed(Block block) {
        Location eastLoc = block.getRelative(BlockFace.EAST).getLocation();
        Location westLoc = block.getRelative(BlockFace.WEST).getLocation();
        Location northLoc = block.getRelative(BlockFace.NORTH).getLocation();
        Location southLoc = block.getRelative(BlockFace.SOUTH).getLocation();

        if (getTopBlockY(northLoc) >= northLoc.getY() && getTopBlockY(southLoc) >= southLoc.getY()
                && getTopBlockY(eastLoc) >= eastLoc.getY() && getTopBlockY(westLoc) >= westLoc.getY()) {
            return false;
        }

        return true;
    }

    public Boolean blockIsCold(Location location) {
        return this.functions.calculateLocationTemp(location) < 0.15;
    }

    public boolean blockIsDry(Location location) {
        return this.functions.calculateLocationTemp(location) > 1.0;
    }

    public boolean blockTypeCanSpawnHostile(Material material) {
        return material.isOccluding() && material != Material.ENDER_PORTAL_FRAME;
    }

    public boolean blockTypeCanFormSnow(Material material) {
        return (material.isOccluding() && material != Material.ENDER_PORTAL_FRAME && material != Material.MOB_SPAWNER
                && material != Material.PACKED_ICE)
                || (material == Material.TNT || material == Material.REDSTONE_BLOCK || material == Material.LEAVES
                        || material == Material.LEAVES_2 || material == Material.CACTUS);
    }
}
