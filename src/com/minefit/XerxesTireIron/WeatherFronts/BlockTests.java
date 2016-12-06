package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Functions;
import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class BlockTests {
    private final WeatherFronts plugin;
    private final Functions functions;
    private final Simulator simulator;

    public BlockTests(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.functions = new Functions(instance);
        this.simulator = simulator;
    }

    public Block getTopBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, false, false);
    }

    public Block getTopLightningBlock(Location location) {
        return this.functions.findHighestBlock(location, 255, true, true).getRelative(BlockFace.UP);
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
        FrontLocation eastLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.EAST));
        FrontLocation westLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.WEST));
        FrontLocation northLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.NORTH));
        FrontLocation southLoc = this.simulator.newFrontLocation(block.getRelative(BlockFace.SOUTH));
        return eastLoc.isInRain() || westLoc.isInRain() || northLoc.isInRain() || southLoc.isInRain();
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
