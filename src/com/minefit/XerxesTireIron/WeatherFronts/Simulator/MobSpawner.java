package com.minefit.XerxesTireIron.WeatherFronts.Simulator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.minefit.XerxesTireIron.WeatherFronts.BlockFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class MobSpawner {

    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final BlockFunctions blockFunction;
    private final World world;
    private final Simulator simulator;

    public MobSpawner(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.simulator = simulator;
        this.world = simulator.getWorld();
    }

    public void spawnMobs() {
        // No point in doing our surface spawn routines at night or on Peaceful
        if ((this.world.getTime() > 13187 && this.world.getTime() < 22812)
                || this.world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        List<Player> allPlayers = this.world.getPlayers();

        if (allPlayers.size() == 0) {
            return;
        }

        Set<Chunk> playerChunks = new HashSet<Chunk>();
        int totalHostiles = 0;
        int mobRange = 8;

        if (mobRange > Bukkit.getServer().getViewDistance()) {
            mobRange = Bukkit.getServer().getViewDistance();
        }

        for (Player player : allPlayers) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                continue;
            }

            Chunk playerChunk = player.getLocation().getChunk();
            int maxX = playerChunk.getX() + mobRange;
            int maxZ = playerChunk.getZ() + mobRange;

            for (int x = playerChunk.getX() - mobRange; x <= maxX; ++x) {
                for (int z = playerChunk.getZ() - mobRange; z <= maxZ; ++z) {

                    if (!this.world.isChunkInUse(x, z)) {
                        continue;
                    }

                    Chunk chunk = this.world.getChunkAt(x, z);
                    playerChunks.add(chunk);

                    for (Entity entity : chunk.getEntities()) {
                        if (entity instanceof Monster) {
                            ++totalHostiles;
                        }
                    }
                }
            }
        }

        int worldHostileCap = (int) ((this.world.getMonsterSpawnLimit() * playerChunks.size()) / 256) + 1;

        if (totalHostiles >= worldHostileCap) {
            return;
        }

        for (Chunk chunk : playerChunks) {
            // We add a limiter since we're only checking surface locations
            if (this.random.nextInt(16) != 0) {
                continue;
            }

            int baseX = chunk.getX() << 4;
            int baseZ = chunk.getZ() << 4;
            int x = this.random.nextIntRange(baseX, baseX + 15);
            int z = this.random.nextIntRange(baseZ, baseZ + 15);
            Block block = this.blockFunction.getTopSolidBlock(this.simulator.newFrontLocation(x, 0, z));
            FrontLocation location = this.simulator.newFrontLocation(block);

            if (!location.canSpawnHostile() || !this.blockFunction.blockTypeCanSpawnHostile(block.getType())
                    || block.getLightFromBlocks() > 7) {
                continue;
            }

            Block centerBlock = block.getRelative(BlockFace.UP);

            if (!centerBlock.isEmpty()) {
                return;
            }

            int packAttempts = 3;
            int mobsSpawned = 0;

            for (int i = 0; i < packAttempts && mobsSpawned <= 4; ++i) {
                mobsSpawned += spawnPack(centerBlock);
            }

        }
    }

    private int spawnPack(Block centerBlock) {
        int packMobs = 0;
        int packSize = 4;
        int packTries = 12;
        int centerX = centerBlock.getX();
        int centerY = centerBlock.getY();
        int centerZ = centerBlock.getZ();
        EntityType mob = randomHostile();
        int randomRange = 11;

        for (int i = 0; i < packTries; ++i) {
            if (packMobs >= packSize) {
                return packMobs;
            }

            centerX += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            centerY += this.random.nextInt(1) - this.random.nextInt(1); // Who knows why this was in MC code lol
            centerZ += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            FrontLocation location = this.simulator.newFrontLocation(centerX, centerY, centerZ);

            if (!location.isInWeather()) {
                continue;
            }

            Block block = location.getBlock();

            if (!this.blockFunction.blockTypeCanSpawnHostile(block.getRelative(BlockFace.DOWN).getType())
                    || !block.isEmpty() || !block.getRelative(BlockFace.UP).isEmpty()) {
                continue;
            }

            List<Player> worldPlayers = this.world.getPlayers();
            boolean playerNearby = false;

            for (Player player : worldPlayers) {
                if (player.getLocation().distanceSquared(location) <= 576) {
                    playerNearby = true;
                    break;
                }
            }

            if (playerNearby) {
                continue;
            }

            int mobHeight = 2;
            int mobWidth = 1;

            if (mob == EntityType.SPIDER) {
                mobHeight = 1;
                mobWidth = 3;
            }

            if (mob == EntityType.ENDERMAN) {
                mobHeight = 3;
            }

            if (overworldHostileCanSpawn(block, mobWidth, mobHeight)) {
                this.world.spawnEntity(location, mob);
                ++packMobs;
            }
        }

        return packMobs;
    }

    public boolean overworldHostileCanSpawn(Block block, int width, int height) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        if (block.getLightFromSky() > this.random.nextInt(32)) {
            return false;
        } else {
            byte lightLevel = block.getLightFromBlocks();

            if (lightLevel > 7 || lightLevel <= this.random.nextInt(8)) {
                return false;
            }
        }

        for (int xx = 0; xx < width; ++xx) {
            for (int zz = 0; zz < width; ++zz) {
                for (int yy = 0; yy < height; ++yy) {
                    Block testBlock = this.world.getBlockAt(x + width, y + height, z + width);

                    if (testBlock.getType().isSolid() || testBlock.isLiquid()) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public EntityType randomHostile() {
        // From BiomeBase.java, hostile mob weights (minus slime for now) totals 415
        int roll = this.random.nextInt(415);

        if (roll < 100) {
            return EntityType.SPIDER;
        } else if (roll < 200) {
            return EntityType.ZOMBIE;
        } else if (roll < 300) {
            return EntityType.SKELETON;
        } else if (roll < 400) {
            return EntityType.CREEPER;
        } else if (roll < 410) {
            return EntityType.ENDERMAN;
        } else {
            return EntityType.WITCH;
        }
    }
}
