package com.minefit.xerxestireiron.weatherfronts.Simulator;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;

import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;

public class Fulgurite {
    private final Simulator simulator;
    private final XORShiftRandom random = new XORShiftRandom();

    public Fulgurite(Simulator simulator, Block block) {
        this.simulator = simulator;
        generateFulgurite(block);
    }

    public void generateFulgurite(Block block) {
        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();
        convertBlock(block);

        int limit = this.random.nextInt(simulatorConfig.getInt("fulgurite-max-size", 5));
        Block baseBlock = block;
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN };

        for (int i = 0; i < limit; ++i) {
            Block block2 = baseBlock.getRelative(faces[this.random.nextInt(5)]);

            if (i == 0 && this.random.nextInt(4) != 0) {
                block2 = baseBlock;
            }

            if (i == 1 && limit > 2) {
                block2 = baseBlock.getRelative(BlockFace.DOWN);
            }

            convertBlock(block2);

            if (this.random.nextInt(5) != 0) {
                baseBlock = block2;
            }
        }
    }

    private boolean convertBlock(Block block) {
        Material blockType = block.getType();
        boolean success = false;

        if (blockType == Material.SAND) {
            block.setType(Material.GLASS);
            success = true;
        } else if (blockType == Material.CLAY) {
            block.setType(Material.TERRACOTTA);
            success = true;
        }

        return success;
    }
}
