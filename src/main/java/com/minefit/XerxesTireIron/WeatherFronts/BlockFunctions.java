package com.minefit.XerxesTireIron.WeatherFronts;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import com.minefit.XerxesTireIron.WeatherFronts.Simulator.Simulator;

public class BlockFunctions {
    private final World world;
    private final WeatherFronts plugin;
    private final Simulator simulator;
    private final BiomeData biomeData;

    public BlockFunctions(WeatherFronts instance, Simulator simulator) {
        this.world = simulator.getWorld();
        this.simulator = simulator;
        this.biomeData = new BiomeData();
        this.plugin = instance;
    }

    public FrontLocation getFrontLocation(Block block) {
        return this.simulator.newFrontLocation(block);
    }

    public Block findHighestBlock(Location location, int start) {
        if (start > 256) {
            start = 255;
        }

        for (; start > 0; --start) {
            Block block = this.world.getBlockAt(location.getBlockX(), start, location.getBlockZ());

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
        Block block = findHighestBlock(location, 255);

        for (int start = block.getY(); start > 0; --start) {
            if (isShelter(block)) {
                return block;
            }

            start = block.getY() - 1;
            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopShelterBlock(Block block) {
        return getTopShelterBlock(block.getLocation());
    }

    public Block getTopLiquidBlock(Location location) {
        Block block = findHighestBlock(location, 255);

        for (int start = block.getY(); start > 0; --start) {
            if (block.isLiquid()) {
                return block;
            }

            start = block.getY() - 1;
            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopSolidBlock(Location location) {
        Block block = findHighestBlock(location, 255);

        for (int start = block.getY(); start > 0; --start) {
            if (block.getType().isSolid()) {
                return block;
            }

            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopBlockLightningValid(Location location) {
        Block block = findHighestBlock(location, 255);

        for (int start = block.getY(); start > 0; --start) {
            if ((block.getType().isSolid() || block.isLiquid())
                    && (block.getRelative(BlockFace.UP).isEmpty() || !isShelter(block))) {
                return block;
            }

            block = findHighestBlock(location, start);
        }

        return block;
    }

    public Block getTopEmptyBlock(Location location) {
        return findHighestBlock(location, 255).getRelative(BlockFace.UP);
    }

    public boolean adjacentBlockExposed(Block block) {
        return isInRain(block.getRelative(BlockFace.EAST)) || isInRain(block.getRelative(BlockFace.WEST))
                || isInRain(block.getRelative(BlockFace.NORTH)) || isInRain(block.getRelative(BlockFace.SOUTH));
    }

    public boolean hostileCanSpawnInBlock(Block block) {
        return block.getType().isTransparent() && !block.isLiquid() && block.getLightFromBlocks() <= 7
                && isInWeather(block) && block.getRelative(BlockFace.DOWN).getType().isOccluding();
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

    public boolean isInWeather(Block block) {
        FrontLocation location = getFrontLocation(block);
        return location.isInFront() && block.getY() >= getTopShelterBlock(location).getY();
    }

    public boolean isInWeather(FrontLocation location)
    {
        return isInWeather(location.getBlock());
    }

    public boolean isInRain(Block block) {
        return isInWeather(block) && !this.biomeData.isDry(block) && !this.biomeData.isFrozen(block) && !isCold(block);
    }

    public boolean isInRain(FrontLocation location)
    {
        return isInRain(location.getBlock());
    }

    public boolean isInSnow(Block block) {
        return isInWeather(block) && !isDry(block) && (this.biomeData.isFrozen(block) || isCold(block));
    }

    public boolean isDry(Block block) {
        return this.biomeData.isDry(block);
    }

    public boolean isCold(Block block) {
        return getBlockTemperature(block) < 0.15;
    }

    public double getBlockTemperature(Block block) {
        double temp = block.getTemperature();

        // Taken from the NMS BiomeBase calculation
        if (block.getY() > 64) {
            temp = temp - (block.getY() - 64) * 0.05F / 30.0F;
        }

        return temp;
    }
}