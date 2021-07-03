package com.minefit.xerxestireiron.weatherfronts.Storm;

import org.bukkit.Bukkit;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LightningStrike;

import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.ChunkFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontsLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.NMSBullshit.NMSHandler;
import com.minefit.xerxestireiron.weatherfronts.Simulator.Fulgurite;

public class LightningGen {
    private final WeatherFronts plugin;
    private final Storm storm;
    private final YamlConfiguration stormConfig;
    private final YamlConfiguration simulatorConfig;
    private final YamlConfiguration systemConfig;
    private final BlockFunctions blockFunction;
    private final ChunkFunctions chunkFunctions;
    private double accumulator = 0.0;
    private double baseLPM;
    private double weightedLPM;
    private double lightningPerCheck;
    private boolean weighted = false;
    private final XORShiftRandom random;
    private final NMSHandler nmsHandler;

    public LightningGen(WeatherFronts instance, YamlConfiguration config, Storm storm) {
        this.plugin = instance;
        this.storm = storm;
        this.stormConfig = storm.getData();
        this.simulatorConfig = config;
        this.systemConfig = storm.getSimulator().getWeatherSystem().getConfig();
        this.blockFunction = new BlockFunctions(instance, storm.getSimulator());
        this.chunkFunctions = new ChunkFunctions(instance);
        this.random = new XORShiftRandom();
        this.baseLPM = this.stormConfig.getInt("lightning-per-minute");
        this.weightedLPM = this.baseLPM;
        this.nmsHandler = new NMSHandler(this.plugin);

        if (this.systemConfig.getBoolean("use-weighted-lightning", true)) {
            weight(this.systemConfig.getInt("weight-radius-threshold", 192));
        }

        this.lightningPerCheck = this.weightedLPM / (60 * 20);

    }

    private void weight(int threshold) {
        if (this.stormConfig.getInt("radius-x") > threshold) {
            this.weightedLPM *= this.stormConfig.getInt("radius-x") / threshold;
            this.weighted = true;
        }

        if (this.stormConfig.getInt("radius-z") > threshold) {
            this.weightedLPM *= this.stormConfig.getInt("radius-z") / threshold;
            this.weighted = true;
        }
    }

    public void lightningGen(World world) {
        this.accumulator += this.lightningPerCheck;

        // Add some randomness to small fronts so strikes aren't quite so predictable
        // Larger weighted fronts have an inherently more random experience
        if (!this.weighted) {
            int randomDelay = (int) (this.baseLPM * 8);

            if (this.random.nextBoolean()) {
                this.accumulator += this.lightningPerCheck + (this.random.nextDouble() / randomDelay);
            } else {
                this.accumulator -= this.lightningPerCheck + (this.random.nextDouble() / randomDelay);
            }
        }

        if (this.accumulator < 1.0) {
            return;
        }

        if (this.accumulator < 0) {
            this.accumulator = 0;
        }

        while (this.accumulator >= 1.0) {
            randomStrike(world);
            this.accumulator -= 1.0;
        }
    }

    private void randomStrike(World world) {
        FrontsLocation location = this.storm.randomLocationInStorm();

        if (!location.isLoaded()) {
            return;
        }

        boolean lightningDry = this.simulatorConfig.getBoolean("lightning-in-dry-biomes", false);
        boolean lightningCold = this.simulatorConfig.getBoolean("lightning-in-cold-biomes", false);
        LightningStrike newLightning = null;
        Location strikeLocation = location.getBukkitLocation();

        if (this.random.nextInt(100) < this.storm.intracloudPecentage()) {
            strikeLocation.setY(this.random.nextInt(45) + 256); // TODO: Figure out max height
            newLightning = world.strikeLightning(location);
        } else {
            Block block = this.blockFunction.getTopBlockLightningValid(location);

            strikeLocation = strikeLocation(block.getLocation());
            Block strikeBlock = world.getBlockAt(strikeLocation);

            if (!this.blockFunction.lightningCanStrike(strikeBlock)) {
                return;
            }

            if ((!this.blockFunction.isDry(strikeBlock) && !this.blockFunction.isCold(strikeBlock))
                    || (this.blockFunction.isDry(strikeBlock) && lightningDry)
                    || (this.blockFunction.isCold(strikeBlock) && lightningCold)) {
                newLightning = world.strikeLightning(strikeLocation);
            }
        }

        if (world.getGameRuleValue(GameRule.DO_MOB_SPAWNING)
                && this.simulatorConfig.getBoolean("spawn-skeleton-traps", true)) {
            if (!location.inSpawnChunk() || this.simulatorConfig.getBoolean("skeleton-traps-in-spawn-chunk", false)) {

                Difficulty difficulty = world.getDifficulty();
                double chanceLimit = 0.0;

                if (difficulty == Difficulty.EASY) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.0075) + 0.0075;
                } else if (difficulty == Difficulty.NORMAL) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.025) + 0.015;
                } else if (difficulty == Difficulty.HARD) {
                    chanceLimit = this.random.nextDoubleRange(0.0, 0.039375) + 0.028125;
                }

                double spawnChance = this.random.nextDouble();
                boolean doSpawn = spawnChance > 0.0 && spawnChance <= chanceLimit;

                if (doSpawn) {
                    this.nmsHandler.createHorseTrap(location);
                }
            }
        }

        if (this.simulatorConfig.getBoolean("create-fulgurites", false)) {
            if (!location.inSpawnChunk() || this.simulatorConfig.getBoolean("fulgurite-in-spawn-chunk", false)) {

                double fulguriteChance = this.simulatorConfig.getDouble("fulgurite-chance", 0.10D) / 100;

                if (this.random.nextDouble() <= fulguriteChance) {
                    new Fulgurite(this.storm.getSimulator(), world.getBlockAt(strikeLocation)); // TODO: Call generate manually
                }
            }
        }
    }

    private Location strikeLocation(Location location) {
        int blockX = location.getBlockX();
        int blockY = location.getBlockY();
        int blockZ = location.getBlockZ();
        World world = location.getWorld();

        this.chunkFunctions.chunksInRadiusByLevel(location, 8, false);

        for (int i = -16; i < 16; ++i) {
            for (int j = 0; j > -4; --j) {
                for (int k = -16; k < 16; ++k) {

                    Block checkBlock = world.getBlockAt(blockX + i, blockY + j, blockZ + k);

                    if (checkBlock.getRelative(BlockFace.DOWN).getType() == Material.LIGHTNING_ROD) {
                        return checkBlock.getLocation();
                    }
                }
            }
        }

        return location;
    }


}
