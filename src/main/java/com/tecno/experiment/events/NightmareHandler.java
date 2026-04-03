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
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class NightmareHandler {

    private static boolean nightmareTriggered = false;
    private static int houseTimer = -1;

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {

            // Grab the Server-side player and level
            if (!(event.player instanceof ServerPlayer)) return;
            ServerPlayer player = (ServerPlayer) event.player;
            ServerLevel level = player.serverLevel();
            MinecraftServer server = player.getServer();

            if (server == null) return;

            CommandSourceStack silentSource = server.createCommandSourceStack()
                    .withSuppressedOutput()
                    .withLevel(level)
                    .withEntity(player);

            // --- SLEEP ABDUCTION ---
            if (player.isSleeping() && player.getSleepTimer() >= 95) {
                if (!nightmareTriggered) {
                    nightmareTriggered = true;

                    if (level.random.nextInt(4) == 0) {
                        player.stopSleepInBed(true, false);

                        // Use the silent source to execute commands
                        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run tp @s 0 33 0");
                        server.getCommands().performPrefixedCommand(silentSource, "effect give @s minecraft:slow_falling 5 0 true");
                        player.displayClientMessage(Component.literal("§fYou didn't wake up in your bed..."), true);

                        server.getCommands().performPrefixedCommand(silentSource, "time set day");
                        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run forceload add 90 90 110 110");

                        houseTimer = 0;
                    }
                }
            } else if (!player.isSleeping()) {
                nightmareTriggered = false;
            }

            // --- HOUSE BUILDING SEQUENCE ---
            if (houseTimer != -1) {
                houseTimer++;

                if (houseTimer == 5) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 32 100 105 32 105 minecraft:oak_planks");
                }
                if (houseTimer == 15) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 33 100 105 36 105 minecraft:oak_planks outline");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 37 100 105 37 105 minecraft:oak_planks");
                }
                if (houseTimer == 25) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 33 102 100 34 102 minecraft:air");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run setblock 100 33 102 minecraft:oak_door");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 102 33 102 102 35 102 minecraft:end_gateway");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run setblock 104 33 104 minecraft:redstone_torch");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run forceload remove 90 90 110 110");
                    houseTimer = -1;
                }
            }

            // --- PORTAL CHECK ---
            if (level.dimension().location().toString().equals("experiment_277:garden")) {
                BlockState state = level.getBlockState(player.blockPosition());
                if (state.is(Blocks.END_GATEWAY)) {
                    // Server-side sound playing
                    level.playSound(null, player.blockPosition(), SoundEvents.VEX_DEATH, SoundSource.AMBIENT, 1.0F, 0.1F);
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:basement run tp @s 0 64 0");
                    player.displayClientMessage(Component.literal("§k||§r §fMEMORY CRITICAL §k||"), true);
                }
            }

            // --- MAZE & EXIT TRIGGER ---
            if (level.dimension().location().toString().equals("experiment_277:basement")) {
                // 1. Generate the maze
                if (player.tickCount % 20 == 0) {
                    generateMazeAroundPlayer(player, level);
                }

                // 2. The Escape: Run 100 blocks away from the center (0,0)
                if (Math.abs(player.getX()) > 100 || Math.abs(player.getZ()) > 100) {

                    // Grab the Overworld
                    ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
                    if (overworld != null) {
                        // Find their bed (or the world spawn if their bed was destroyed)
                        BlockPos bedPos = player.getRespawnPosition();
                        if (bedPos == null) bedPos = overworld.getSharedSpawnPos();

                        // Safely teleport them out of the dimension and back into bed
                        player.teleportTo(overworld, bedPos.getX(), bedPos.getY(), bedPos.getZ(), player.getYRot(), player.getXRot());

                        // Shatter the illusion
                        overworld.playSound(null, bedPos, SoundEvents.GLASS_BREAK, SoundSource.AMBIENT, 1.0F, 0.5F);
                        player.displayClientMessage(Component.literal("§c[SYSTEM] MEMORY OVERLOAD. RESETTING..."), true);

                        // Reset the trigger so they can have the nightmare again another night
                        nightmareTriggered = false;
                    }
                }
            }
        }
    }

    private static void generateMazeAroundPlayer(ServerPlayer player, ServerLevel level) {
        BlockPos pos = player.blockPosition();
        int radius = 12;

        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                BlockPos checkPos = pos.offset(x, 0, z);

                if (checkPos.getX() % 8 == 0 || checkPos.getZ() % 8 == 0) {
                    for (int yOffset = 1; yOffset <= 7; yOffset++) {
                        BlockPos wallPos = new BlockPos(checkPos.getX(), 32 + yOffset, checkPos.getZ());

                        if (level.getBlockState(wallPos).isAir()) {
                            level.setBlock(wallPos, Blocks.COBBLESTONE.defaultBlockState(), 3);
                        }
                    }

                    BlockPos roofPos = new BlockPos(checkPos.getX(), 40, checkPos.getZ());
                    level.setBlock(roofPos, Blocks.STONE.defaultBlockState(), 3);
                }
            }
        }
    }
}