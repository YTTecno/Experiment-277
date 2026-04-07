package com.tecno.experiment.events;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class HouseDimensionHandler {

    // --- 1. PASSIVE HOUSE EVENTS (Ambient Scares) ---
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            if (!(event.player instanceof ServerPlayer player)) return;
            ServerLevel level = player.serverLevel();

            // ISOLATION CHECK
            if (level.dimension().location().toString().equals("experiment_277:your_house")) {
            }
        }
    }

    // --- 2. THE BOOKSHELF PUZZLE (The Escape) ---
    @SubscribeEvent
    public static void onBookshelfClick(PlayerInteractEvent.RightClickBlock event) {
        if (event.getSide().isServer() && event.getEntity() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            if (level.dimension().location().toString().equals("experiment_277:your_house")) {

                BlockPos clickedPos = event.getPos();
                BlockState state = level.getBlockState(clickedPos);

                if (state.is(Blocks.BOOKSHELF) || state.is(Blocks.CHISELED_BOOKSHELF)) {

                    // CHANGE THESE COORDINATES to wherever the secret book is in your structure!
                    BlockPos secretBookshelfPos = new BlockPos(21, 67, 16);

                    // SUCCESS: THEY FOUND IT
                    if (clickedPos.equals(secretBookshelfPos)) {
                        event.setCanceled(true);

                        CommandSourceStack silentSource = server.createCommandSourceStack()
                                .withSuppressedOutput()
                                .withLevel(level)
                                .withEntity(player);

                        // Send them to the Void / Garage
                        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garage run tp @s 21 61 46 180 0");

                        // Transition effects
                        level.playSound(null, player.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.MASTER, 1.0F, 0.5F);
                        server.getCommands().performPrefixedCommand(silentSource, "effect give @s minecraft:nausea 10 0 true");
                        server.getCommands().performPrefixedCommand(silentSource, "effect give @s minecraft:blindness 2 0 true");

                        player.displayClientMessage(Component.literal("§8You pull a heavy book... The walls fall away."), true);
                    }
                    // FAILURE: WRONG BOOKSHELF
                    else {
                        if (level.random.nextInt(5) == 0) { // 20% chance to play a creepy sound
                            event.setCanceled(true);
                            level.playSound(null, clickedPos, SoundEvents.AMBIENT_CAVE.get(), SoundSource.MASTER, 1.0F, 0.5F);
                        }
                    }
                }
            }
        }
    }

    // --- 3. THE INDESTRUCTIBLE CAGE (No Breaking Out) ---
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getPlayer() instanceof ServerPlayer player) {
            ServerLevel level = player.serverLevel();

            if (level.dimension().location().toString().equals("experiment_277:your_house")) {

                // Instantly stop the block from breaking
                event.setCanceled(true);

                // Play a heavy, muffled thud to make the house feel solid and unnatural
                level.playSound(null, event.getPos(), SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.BLOCKS, 0.3F, 0.5F);
            }
        }
    }
}