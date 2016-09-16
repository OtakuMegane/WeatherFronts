package com.minefit.XerxesTireIron.WeatherFronts.FrontsWorld;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Difficulty;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

import com.minefit.XerxesTireIron.WeatherFronts.BlockTests;
import com.minefit.XerxesTireIron.WeatherFronts.LocationTests;
import com.minefit.XerxesTireIron.WeatherFronts.WeatherFronts;
import com.minefit.XerxesTireIron.WeatherFronts.XORShiftRandom;

public class MobSpawner {

    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final LocationTests locationtest;
    private final BlockTests blocktest;
    private final FrontsWorld frontsWorld;
    private final World world;
    private Logger logger = Logger.getLogger("Minecraft");

    public MobSpawner(WeatherFronts instance, FrontsWorld frontsWorld) {
        this.plugin = instance;
        this.locationtest = new LocationTests(instance);
        this.blocktest = new BlockTests(instance);
        this.frontsWorld = frontsWorld;
        this.world = frontsWorld.getWorld();
    }

    public void spawnMobs() {
        // No point in doing our surface spawn routines at night or on Peaceful
        if ((world.getTime() > 13187 && world.getTime() < 22812) || world.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }

        List<Player> allPlayers = world.getPlayers();

        if (allPlayers.size() == 0) {
            return;
        }

        Set<Chunk> playerChunks = new HashSet<Chunk>();
        int mobRange = 8;

        if (mobRange > Bukkit.getServer().getViewDistance()) {
            mobRange = Bukkit.getServer().getViewDistance();
        }

        for (Player player : allPlayers) {
            Chunk chunk = player.getLocation().getChunk();
            int maxX = chunk.getX() + mobRange;
            int maxZ = chunk.getZ() + mobRange;

            for (int x = chunk.getX() - mobRange; x <= maxX; ++x) {
                for (int z = chunk.getZ() - mobRange; z <= maxZ; ++z) {
                    playerChunks.add(world.getChunkAt(x, z));
                }
            }

        }

        int totalPlayerChunks = playerChunks.size();
        int worldHostileCap = (int) (world.getMonsterSpawnLimit() * totalPlayerChunks) / 289;
        int totalHostiles = 0;
        List<LivingEntity> allLivingEntities = world.getLivingEntities();

        for (LivingEntity entity : allLivingEntities) {
            if (totalHostiles >= worldHostileCap) {
                return;
            }

            if (entity instanceof Monster) {
                ++totalHostiles;
            }
        }

        for (Chunk chunk : this.world.getLoadedChunks()) {
            if (this.random.nextInt(64) == 0) // We add a limiter since we're only checking surface locations
            {
                int baseX = chunk.getX() << 4;
                int baseZ = chunk.getZ() << 4;
                int x = random.nextIntRange(baseX, baseX + 15);
                int z = random.nextIntRange(baseZ, baseZ + 15);
                Block block = this.blocktest.getTopSolidBlock(new Location(world, x, 0, z));

                if (!this.locationtest.locationIsInWeather(block.getLocation())
                        || !this.blocktest.blockTypeCanSpawnHostile(block.getType())
                        || block.getLightFromBlocks() > 7) {
                    return;
                }

                // An extra to try and avoid piles of mobs
                Collection<Entity> entities = this.world.getNearbyEntities(block.getLocation(), 3, 2, 3);

                for (Entity entity : entities) {
                    if (entity instanceof Monster) {
                        return;
                    }
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
    }

    private int spawnPack(Block centerBlock) {
        int packMobs = 0;
        int packSize = 3;
        int centerX = centerBlock.getX();
        int centerY = centerBlock.getY();
        int centerZ = centerBlock.getZ();
        EntityType mob = randomHostile();
        int randomRange = 6;

        for (int i = 0; i < packSize; ++i) {
            centerX += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            centerY += this.random.nextInt(1) - this.random.nextInt(1); // Who knows why this was in MC code lol
            centerZ += this.random.nextInt(randomRange) - this.random.nextInt(randomRange);
            Location testLoc = new Location(this.world, centerX, centerY, centerZ);

            if (!this.locationtest.locationIsLoaded(testLoc) || !this.locationtest.locationIsInWeather(testLoc)) {
                continue;
            }

            Block block2 = testLoc.getBlock();

            if (!this.blocktest.blockTypeCanSpawnHostile(block2.getRelative(BlockFace.DOWN).getType())
                    || !block2.isEmpty() || !block2.getRelative(BlockFace.UP).isEmpty()) {
                continue;
            }

            List<Player> worldPlayers = this.world.getPlayers();
            boolean playerNearby = false;

            for (Player player : worldPlayers) {
                if (player.getLocation().distanceSquared(testLoc) <= 576) {
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

            if (overworldHostileCanSpawn(block2, mobWidth, mobHeight)) {
                this.world.spawnEntity(testLoc, mob);
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
                    Block testBlock = world.getBlockAt(x + width, y + height, z + width);

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
        int roll = random.nextInt(415);

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
