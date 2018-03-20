package com.minefit.xerxestireiron.weatherfronts.Simulator;

import java.util.Map;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockIgniteEvent.IgniteCause;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.ItemStack;

import com.minefit.xerxestireiron.weatherfronts.BlockFunctions;
import com.minefit.xerxestireiron.weatherfronts.FrontLocation;
import com.minefit.xerxestireiron.weatherfronts.WeatherFronts;
import com.minefit.xerxestireiron.weatherfronts.XORShiftRandom;
import com.minefit.xerxestireiron.weatherfronts.NMSBullshit.NMSHandler;

public class FireHandler implements Listener {
    private final XORShiftRandom random = new XORShiftRandom();
    private final WeatherFronts plugin;
    private final ConcurrentHashMap<Block, Integer> fireBlocks = new ConcurrentHashMap<>();
    private final BlockFunctions blockFunction;
    private final Simulator simulator;
    private final Boolean fastExtinguish;
    private final NMSHandler nmsHandler;

    public FireHandler(WeatherFronts instance, Simulator simulator) {
        this.plugin = instance;
        this.blockFunction = new BlockFunctions(instance, simulator);
        this.simulator = simulator;
        this.fastExtinguish = simulator.getSimulatorConfig().getBoolean("fast-extinguish-fire", false);
        this.nmsHandler = new NMSHandler(this.plugin);
    }

    public void extinguishFire() {
        for (Block block : fireBlocks.keySet()) {
            FrontLocation location = this.simulator.newFrontLocation(block);

            if (!location.isLoaded()) {
                continue;
            }

            if (!actOnBlock(block) || block.getType() != Material.FIRE) {
                this.fireBlocks.remove(block);
                continue;
            }

            if (this.fastExtinguish) {
                fastExtinguishFire(block);
            } else {
                int age = block.getData();

                if (random.nextInt(20) == 0 && random.nextFloat() < 0.2F + (float) age * 0.03F) {
                    removeFire(block);
                }
            }
        }
    }

    public void fastExtinguishFire(Block block) {
        int age = block.getData();
        int fireStatus = this.fireBlocks.get(block);

        if (age >= 15 || fireStatus >= 30) {
            if (random.nextInt(4) == 0) {
                removeFire(block);
            }
        } else {
            this.fireBlocks.put(block, fireStatus + 1);
        }
    }

    private void removeFire(Block block) {
        block.setType(Material.AIR);
        this.fireBlocks.remove(block);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockIgnite(BlockIgniteEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (!this.simulator.isInSimulator(location.getBlockX(), location.getBlockZ())) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (event.getCause() == IgniteCause.LIGHTNING && location.inSpawnChunk()
                && !simulatorConfig.getBoolean("lightning-fire-in-spawn-chunk", false)) {
            event.setCancelled(true);
            return;
        }

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockBurn(BlockBurnEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onBlockSpread(BlockSpreadEvent event) {
        if (event.isCancelled() || !this.plugin.worldEnabled(event.getBlock().getWorld())
                || event.getBlock().getType() != Material.FIRE) {
            return;
        }

        Block block = event.getBlock();

        if (actOnBlock(block)) {
            addFireBlock(block);
            addAdjacentFire(block);
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        if (event.isCancelled() || event.getState() != PlayerFishEvent.State.FISHING
                || !this.plugin.worldEnabled(player.getWorld())) {
            return;
        }

        Fish hook = event.getHook();
        FrontLocation hookLocation = this.simulator.newFrontLocation(hook.getLocation());

        if (!hookLocation.isInWeather()) {
            return;
        }

        int hookTime = this.random.nextIntRange(100, 600);

        if (!hookLocation.isExposedToSky()) {
            hookTime *= 1.5; // If the bobber location is sheltered we increase time by 50%
        }

        double reductionPercentage = this.simulator.getSimulatorConfig().getDouble("fishing-time-reduction", 20);
        double reductionTime = (reductionPercentage / 100) * hookTime;
        hookTime = (int) Math.round(hookTime - reductionTime);
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (rod.containsEnchantment(Enchantment.LURE)) {
            hookTime -= rod.getEnchantmentLevel(Enchantment.LURE) * 20 * 5;
        }

        if (hookTime <= 0) {
            hookTime = 1;
        }

        this.nmsHandler.fishingTime(hook, hookTime);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    private void onLightningStrike(LightningStrikeEvent event) {
        World world = event.getWorld();
        if (event.isCancelled() || !this.plugin.worldEnabled(world)) {
            return;
        }

        Block block = event.getLightning().getLocation().getBlock();
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (!this.simulator.isInSimulator(location)) {
            return;
        }

        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();

        if (simulatorConfig.getBoolean("spawn-skeleton-traps", true)
                && random.nextDouble() * 100 < simulatorConfig.getDouble("skeleton-trap-chance")) {
            this.nmsHandler.createHorseTrap(location);
        }

        if (!simulatorConfig.getBoolean("create-fulgurites", false)) {
            return;
        }

        if (this.random.nextInt(100) < simulatorConfig.getInt("fulgurite-chance", 3)
                || simulatorConfig.getInt("fulgurite-chance") == 100) {
            generateFulgurite(block);
        }

    }

    /*private int calculateHookTime()
    {
        int hookTime = this.random.nextIntRange(100, 600);
        this.plugin.logger.info("time " + hookTime);
        double reductionPercentage = this.simulator.getSimulatorConfig().getDouble("fishing-time-reduction", 20);
        double reductionTime = (reductionPercentage / 100) * hookTime;
        hookTime = (int) Math.round(hookTime - reductionTime);
        this.plugin.logger.info("time2 " + hookTime);
        ItemStack rod = player.getInventory().getItemInMainHand();

        if (rod.containsEnchantment(Enchantment.LURE)) {
            hookTime -= rod.getEnchantmentLevel(Enchantment.LURE) * 20 * 5;
        }

        this.plugin.logger.info("time3 " + hookTime);
    }*/

    private void addAdjacentFire(Block block) {
        if (block.getRelative(BlockFace.UP).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.UP));
        }

        if (block.getRelative(BlockFace.DOWN).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.DOWN));
        }

        if (block.getRelative(BlockFace.NORTH).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.NORTH));
        }

        if (block.getRelative(BlockFace.SOUTH).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.SOUTH));
        }

        if (block.getRelative(BlockFace.EAST).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.EAST));
        }

        if (block.getRelative(BlockFace.WEST).getType() == Material.FIRE) {
            addFireBlock(block.getRelative(BlockFace.WEST));
        }
    }

    private void addFireBlock(Block block) {
        if (!this.fireBlocks.containsKey(block)) {
            this.fireBlocks.put(block, 0);
        }
    }

    public void generateFulgurite(Block block) {
        YamlConfiguration simulatorConfig = this.simulator.getSimulatorConfig();
        FrontLocation location = this.simulator.newFrontLocation(block.getLocation());

        if (location.inSpawnChunk() && !simulatorConfig.getBoolean("fulgurite-in-spawn-chunk", false)) {
            return;
        }

        Block blockDown = block.getRelative(BlockFace.DOWN);
        Material blockType = convertBlock(blockDown.getType());

        if (blockType == null) {
            return;
        }

        blockDown.setType(blockType);
        int limit = this.random.nextInt(simulatorConfig.getInt("fulgurite-max-size", 5));
        Block baseBlock = blockDown;
        int i = 0;
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN };

        for (i = 0; i < limit; ++i) {
            Block block2 = baseBlock.getRelative(faces[this.random.nextInt(5)]);

            if (i == 0 && limit > 2) {
                block2 = baseBlock.getRelative(BlockFace.DOWN);
            }

            Material newType = convertBlock(block2.getType());

            if (newType != null) {
                block2.setType(newType);
            }

            if (this.random.nextInt(2) == 0) {
                baseBlock = block2;
            }

        }
    }

    private Material convertBlock(Material blockType) {
        if (blockType == Material.SAND) {
            return Material.GLASS;
        } else if (blockType == Material.CLAY) {
            return Material.HARD_CLAY;
        }

        return null;
    }

    private boolean actOnBlock(Block block) {
        if (this.blockFunction.isInRain(block) || this.blockFunction.adjacentBlockExposed(block)) {
            return true;
        }

        Material downType = block.getRelative(BlockFace.DOWN).getType();

        if (downType == Material.NETHERRACK) { // TODO: Add 1.12 magma blocks
            return false;
        }

        return false;
    }
}
