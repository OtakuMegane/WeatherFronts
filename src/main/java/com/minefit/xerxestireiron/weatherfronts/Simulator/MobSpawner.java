package com.minefit.xerxestireiron.weatherfronts.Simulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import com.minefit.xerxestireiron.weatherfronts.BiomeData;
import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.ChunkFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.FrontsWorld.FrontsWorld;

public class MobSpawner {

    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final BlockFunctions blockFunction;
    private final ChunkFunctions chunkFunction;
    private final World world;
    private final FrontsWorld frontsWorld;
    private final Simulator simulator;
    private final BiomeData biomeData;

    public MobSpawner(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.chunkFunction = new ChunkFunctions(instance);
        this.simulator = simulator;
        this.world = simulator.getWorld();
        this.frontsWorld = instance.getWorldHandle(simulator.getWorld());
        this.biomeData = new BiomeData();
    }

    public void spawnMobs() {
        int playerCount = this.world.getPlayers().size();
        // Mob spawns are based on players so if nobody is on, don't gotta do shit
        if (!this.simulator.getSimulatorConfig().getBoolean("spawn-thunderstorm-mobs", true) || playerCount == 0) {
            return;
        }

        Set<Player> validPlayers = getValidPlayers();
        Set<Chunk> playerChunks = collectPlayerChunks(validPlayers);
        int totalHostiles = countChunkSetHostiles(playerChunks);
        int worldHostileCap = (int) ((this.world.getMonsterSpawnLimit()
                * (validPlayers.size() * (Math.pow((this.frontsWorld.getMobSpawnRange() * 2) + 1, 2))) / 256));
        // No point in doing our surface spawn routines at night, on Peaceful or if mob spawning is disabled
        if ((this.world.getTime() > 13187 && this.world.getTime() < 22812)
                || this.world.getDifficulty() == Difficulty.PEACEFUL
                || this.world.getGameRuleValue("doMobSpawning").equals("false")) {
            return;

        }

        int mobLimit = worldHostileCap - totalHostiles;

        if (mobLimit <= 0) {
            return;
        }

        List<Chunk> chunkList = new ArrayList<>(playerChunks);
        Collections.shuffle(chunkList);

        for (Chunk chunk : chunkList) {
            // Throttle spawn rate since we only check surface locations
            if (this.random.nextInt(128) != 0) {
                continue;
            }

            FrontsLocation location = this.chunkFunction.randomLocationInChunk(simulator, chunk, true);

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
                int packSize = spawnPack(centerBlock);
                mobsSpawned += packSize;
                mobLimit -= packSize;

                if (mobLimit <= 0) {
                    return;
                }
            }

        }
    }

    private Set<Player> getValidPlayers() {
        Set<Player> players = new HashSet<>();

        for (Player player : this.world.getPlayers()) {
            if (player.getGameMode() != GameMode.SPECTATOR) {
                players.add(player);
            }
        }

        return players;
    }

    private Set<Chunk> collectPlayerChunks(Set<Player> validPlayers) {
        Set<Chunk> playerChunks = new HashSet<>();

        for (Player player : this.world.getPlayers()) {
            int playerChunkX = player.getLocation().getChunk().getX();
            int playerChunkZ = player.getLocation().getChunk().getZ();
            int radius = this.frontsWorld.getMobSpawnRange();

            for (int i = -radius + 1; i < radius; ++i) {
                for (int j = -radius + 1; j < radius; ++j) {
                    int chunkX = playerChunkX + i;
                    int chunkZ = playerChunkZ + j;

                    if (this.world.isChunkInUse(chunkX, chunkZ)) {
                        playerChunks.add(this.world.getChunkAt(chunkX, chunkZ));
                    }
                }
            }
        }

        return playerChunks;
    }

    private int countChunkSetHostiles(Set<Chunk> chunks) {
        int hostiles = 0;
        for (Chunk chunk : chunks) {
            for (Entity entity : chunk.getEntities()) {
                if (entity instanceof Monster) {
                    hostiles++;
                }
            }
        }

        return hostiles;
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
            FrontsLocation location = new FrontsLocation(this.simulator, centerX, centerY, centerZ);

            if (location.getStorm() == null || !location.isLoaded() || !location.getStorm().hasLightning()
                    || !blockFunction.isInWeather(block)) {
                continue;
            }

            Block block2 = location.getBlock();
            Block block2down = block2.getRelative(BlockFace.DOWN);

            if (!this.blockFunction.mobCanSpawnInBlock(block2) || !block2down.getType().isOccluding()) {
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
                location.setX(location.getX() + 0.5);
                location.setZ(location.getZ() + 0.5);
                this.world.spawnEntity(location, mob);
                ++packMobs;
            }
        }

        return packMobs;
    }

    public boolean overworldHostileCanSpawn(Block block, int width, int height) {
        if (biomeData.isSafe(block.getBiome())) {
            return false;
        }

        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        int startPoint = 0;
        int endPoint = width;

        if (width > 2) {
            startPoint = -1;
            endPoint = width - 1;
        }

        if (block.getLightFromSky() > this.random.nextInt(32) || block.getLightFromBlocks() > 7) {
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
