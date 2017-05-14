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
import com.minefit.XerxesTireIron.WeatherFronts.ChunkFunctions;
import com.minefit.XerxesTireIron.WeatherFronts.FrontLocation;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;
import com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld.FrontsWorld;

public class MobSpawner {

    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final BlockFunctions blockFunction;
    private final ChunkFunctions chunkFunction;
    private final World world;
    private final Simulator simulator;
    private int delay = 0;

    public MobSpawner(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.chunkFunction = new ChunkFunctions(instance);
        this.simulator = simulator;
        this.world = simulator.getWorld();
    }

    public void spawnMobs() {
        if (this.delay < 5) {
            this.delay++;
            return;
        } else {
            this.delay = 0;
        }

        // No point in doing our surface spawn routines at night or on Peaceful
        if ((this.world.getTime() > 13187 && this.world.getTime() < 22812)
                || this.world.getDifficulty() == Difficulty.PEACEFUL) {
            return;

        }

        List<Player> allPlayers = this.world.getPlayers();
        int playerListSize = allPlayers.size();

        if (playerListSize == 0) {
            return;
        }

        Set<Chunk> playerChunks = collectPlayerChunks(allPlayers);
        int totalHostiles = countHostiles(playerChunks);
        int worldHostileCap = (int) ((this.world.getMonsterSpawnLimit() * playerChunks.size()) / 256) + 2;

        if (totalHostiles >= worldHostileCap) {
            return;
        }

        for (Chunk chunk : playerChunks) {
            FrontLocation location = this.chunkFunction.randomLocationInChunk(simulator, chunk, false);

            if (!location.isLoaded()) {
                continue;
            }

            Block block = this.blockFunction.getTopOccludingBlock(location).getRelative(BlockFace.UP);

            if (!this.blockFunction.mobCanSpawnInBlock(block)) {
                continue;
            }

            Block centerBlock = block.getRelative(BlockFace.UP);

            if (!centerBlock.isEmpty()) {
                return;
            }

            int packAttempts = 3;
            int mobsSpawned = 0;

            for (int i = 0; i < packAttempts && mobsSpawned < 4; ++i) {
                mobsSpawned += spawnPack(centerBlock);
            }

        }
    }

    private Set<Chunk> collectPlayerChunks(List<Player> allPlayers) {
        int mobRange = this.plugin.getWorldHandle(world).getMobSpawnRange();
        Set<Chunk> playerChunks = new HashSet<>();

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
                }
            }
        }

        return playerChunks;
    }

    private int countHostiles(Set<Chunk> playerChunks) {
        int totalHostiles = 0;

        for (Chunk chunk : playerChunks) {
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Monster) {
                    ++totalHostiles;
                }
            }
        }

        return totalHostiles;
    }

    private int spawnPack(Block block) {
        int packMobs = 0;
        int packSize = 4;
        int packTries = 12;
        int centerX = block.getX();
        int centerY = block.getY();
        int centerZ = block.getZ();
        EntityType mob = randomHostile();
        int randomRange = 11; // NMS gives ~99% chance of spawning within 10 blocks of center

        for (int i = 0; i < packTries; ++i) {
            if (packMobs >= packSize) {
                return packMobs;
            }

            centerX += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            centerY += this.random.nextInt(1) - this.random.nextInt(1); // Who knows why this was in MC code lol
            centerZ += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            FrontLocation location = this.simulator.newFrontLocation(centerX, centerY, centerZ);

            if (!location.isLoaded() || !blockFunction.isInWeather(block)) {
                continue;
            }

            Block block2 = location.getBlock();

            if (!this.blockFunction.mobCanSpawnInBlock(block2)
                    || !block2.getRelative(BlockFace.DOWN).getType().isOccluding()) {
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
                mobHeight = 2;
                mobWidth = 3;
            }

            if (mob == EntityType.ENDERMAN) {
                mobHeight = 3;
            }

            if (overworldHostileCanSpawn(block2, mobWidth, mobHeight)) {
                this.world.spawnEntity(location, mob);
                this.plugin.logger.info("SPAWNED!  " + location + "  " + mob);
                ++packMobs;
            }
        }

        return packMobs;
    }

    public boolean overworldHostileCanSpawn(Block block, int width, int height) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        int startPoint = 0;
        int endPoint = width;

        if (width > 2) {
            startPoint = -1;
            endPoint = width - 1;
        }

        byte lightLevel = block.getLightFromBlocks();

        if (lightLevel > 7 || lightLevel <= this.random.nextInt(8)) {
            return false;
        }

        for (int xx = startPoint; xx <= endPoint; ++xx) {
            for (int zz = startPoint; zz <= endPoint; ++zz) {
                for (int yy = 0; yy <= height; ++yy) {
                    Block testBlock = this.world.getBlockAt(x + xx, y + yy, z + zz);

                    if (!this.blockFunction.mobCanSpawnInBlock(testBlock)) {
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
