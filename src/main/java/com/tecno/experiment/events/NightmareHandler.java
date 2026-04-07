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

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {

            // Grab the Server-side player and level
            if (!(event.player instanceof ServerPlayer player)) return;
            ServerLevel level = player.serverLevel();
            MinecraftServer server = player.getServer();

            if (server == null) return;

            CommandSourceStack silentSource = server.createCommandSourceStack()
                    .withSuppressedOutput()
                    .withLevel(level)
                    .withEntity(player)
                    .withPermission(4);

            boolean isNightmareActive = player.getPersistentData().getBoolean("NightmareActive");
            int currentHouseTimer = player.getPersistentData().getInt("HouseTimer");

            // --- SLEEP ABDUCTION ---
            if (player.isSleeping() && player.getSleepTimer() >= 95) {
                if (!isNightmareActive) {

                    player.getPersistentData().putBoolean("NightmareActive", true);

                    if (level.random.nextInt(4) == 0) {
                        player.stopSleepInBed(true, false);

                        // Use the silent source to execute commands
                        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run tp @s 0 33 0");
                        server.getCommands().performPrefixedCommand(silentSource, "effect give @s minecraft:slow_falling 5 0 true");
                        player.displayClientMessage(Component.literal("§fYou didn't wake up in your bed..."), true);

                        server.getCommands().performPrefixedCommand(silentSource, "time set day");
                        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run forceload add 90 90 110 110");

                        player.getPersistentData().putInt("HouseTimer", 0);
                    }
                }
            } else if (!player.isSleeping()) {
                player.getPersistentData().putBoolean("NightmareActive", false);
            }

            // --- HOUSE BUILDING SEQUENCE ---
            if (currentHouseTimer >= 0) {
                currentHouseTimer++;

                if (currentHouseTimer == 5) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 32 100 105 32 105 minecraft:oak_planks");
                }
                if (currentHouseTimer == 15) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 33 100 105 36 105 minecraft:oak_planks outline");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 37 100 105 37 105 minecraft:oak_planks");
                }
                if (currentHouseTimer == 25) {
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 100 33 102 100 34 102 minecraft:air");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run setblock 100 33 102 minecraft:oak_door");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run fill 102 33 102 102 35 102 minecraft:end_gateway");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run setblock 104 33 104 minecraft:redstone_torch");
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garden run forceload remove 90 90 110 110");
                    currentHouseTimer = -1;
                }
                player.getPersistentData().putInt("HouseTimer", currentHouseTimer);
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

                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:your_house run tp @s 0 64 0");

                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:your_house run place template experiment_277:fake_house 0 60 0 none");

                    // 1. Force them into the fake house dimension
                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:your_house run tp @s 3 61 18");

                    server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:your_house run forceload remove -2 -2 2 2");

                    // 2. Play a heavy door slamming sound to signify they are trapped inside
                    level.playSound(null, player.blockPosition(), SoundEvents.IRON_DOOR_CLOSE, SoundSource.MASTER, 1.0F, 0.5F);

                    // 3. Give them brief blindness to hide the dimension chunk-loading
                    server.getCommands().performPrefixedCommand(silentSource, "effect give @s minecraft:blindness 3 0 true");

                    // 4. The psychological text prompt
                    player.displayClientMessage(Component.literal("§7You finally made it home..."), true);

                    // 5. Reset the trigger so the basement stops generating cobblestone
                    player.getPersistentData().putBoolean("NightmareActive", false);
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