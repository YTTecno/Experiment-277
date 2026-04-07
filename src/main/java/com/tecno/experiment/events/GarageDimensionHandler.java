package com.tecno.experiment.events;

import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class GarageDimensionHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            if (!(event.player instanceof ServerPlayer player)) return;
            ServerLevel level = player.serverLevel();
            MinecraftServer server = player.getServer();
            if (server == null) return;

            // ISOLATION CHECK: Are we in the garage?
            if (level.dimension().location().toString().equals("experiment_277:garage")) {

                // 1. INVENTORY CONFISCATION
                if (!player.getPersistentData().getBoolean("InventorySaved")) {
                    net.minecraft.nbt.ListTag inventoryNbt = new net.minecraft.nbt.ListTag();
                    player.getInventory().save(inventoryNbt);
                    player.getPersistentData().put("SavedInventory", inventoryNbt);
                    player.getInventory().clearContent();
                    player.getPersistentData().putBoolean("InventorySaved", true);
                }

                // 2. FLASHLIGHT HANDOVER
                if (!player.getPersistentData().getBoolean("HasGarageFlashlight")) {
                    equipFlashlight(player);
                }

                // 3. BUILD THE STRUCTURE
                if (!player.getPersistentData().getBoolean("GarageBuilt")) {
                    buildGarageStructure(player, level, server);
                }

                // --- NEW: 4. THE DIRECTOR SPAWNS THE HOUND ---
                if (!player.getPersistentData().getBoolean("HoundSpawned")) {

                    // NOTE: Make sure 'ModEntities.HOUND' matches your actual entity registry name!
                    player.displayClientMessage(Component.literal("§e[DEBUG] Triggering Hound Spawn..."), false);
                    // (It might be com.tecno.experiment.init.ModEntities.HOUND)
                    com.tecno.experiment.entity.HoundEntity hound = com.tecno.experiment.init.ModEntities.HOUND.get().create(level);

                    if (hound != null) {
                        // 2. Spawn it right in your face so we know it didn't get stuck in a wall
                        double spawnX = player.getX() + 3.0;
                        double spawnY = player.getY() + 7.0; // Drop it slightly from the air
                        double spawnZ = player.getZ();

                        hound.setPos(spawnX, spawnY, spawnZ);
                        boolean success = level.addFreshEntity(hound);


                        // Lock the spawner so it only summons ONE monster
                        player.getPersistentData().putBoolean("HoundSpawned", true);
                    }
                }

                // EXIT TRIGGER: If the player reaches the "Exit" of your structure
                BlockPos exitPos = new BlockPos(38, 67, 44);
                if (player.blockPosition().closerThan(exitPos, 2.0)) {
                    handleEscape(player, server);
                }
            }
        }
    }

    private static void equipFlashlight(ServerPlayer player) {
        ItemStack flashlight = new ItemStack(com.tecno.experiment.init.ModItems.FLASHLIGHT.get());
        flashlight.getOrCreateTag().putBoolean("IsOn", true);

        if (player.getMainHandItem().isEmpty()) {
            player.setItemInHand(InteractionHand.MAIN_HAND, flashlight);
        } else if (!player.getInventory().add(flashlight)) {
            player.drop(flashlight, false);
        }

        player.getPersistentData().putBoolean("HasGarageFlashlight", true);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.MASTER, 1.0f, 0.5f);
    }

    private static void buildGarageStructure(ServerPlayer player, ServerLevel level, MinecraftServer server) {
        CommandSourceStack silentSource = server.createCommandSourceStack()
                .withSuppressedOutput()
                .withLevel(level)
                .withPermission(4);

        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garage run tp @s 0 64 0");
        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garage run place template experiment_277:garage 0 60 0 none");
        server.getCommands().performPrefixedCommand(silentSource, "execute in experiment_277:garage run tp @s 21 61 46 180 0");

        player.getPersistentData().putBoolean("GarageBuilt", true);
        player.displayClientMessage(Component.literal("§8This place feels familiar..."), true);
    }

    private static void handleEscape(ServerPlayer player, MinecraftServer server) {
        player.getInventory().clearContent();

        if (player.getPersistentData().getBoolean("InventorySaved")) {
            net.minecraft.nbt.Tag savedData = player.getPersistentData().get("SavedInventory");
            if (savedData instanceof net.minecraft.nbt.ListTag savedInventory) {
                player.getInventory().load(savedInventory);
            }
            player.getPersistentData().remove("SavedInventory");
            player.getPersistentData().putBoolean("InventorySaved", false);
        }

        player.removeAllEffects();

        // RESET THE NIGHTMARE CYCLE TAGS
        player.getPersistentData().putBoolean("NightmareActive", false);
        player.getPersistentData().putBoolean("HasGarageFlashlight", false);

        // --- NEW: UNLOCK THE HOUND SPAWNER FOR NEXT TIME ---
        player.getPersistentData().putBoolean("HoundSpawned", false);

        ServerLevel overworld = server.getLevel(net.minecraft.world.level.Level.OVERWORLD);
        if (overworld != null) {
            net.minecraft.core.BlockPos respawnPos = player.getRespawnPosition();
            if (respawnPos == null) {
                respawnPos = overworld.getSharedSpawnPos();
            }
            player.teleportTo(overworld, respawnPos.getX(), respawnPos.getY(), respawnPos.getZ(), player.getYRot(), player.getXRot());
        }

        player.displayClientMessage(net.minecraft.network.chat.Component.literal("§7You woke up... was it even real?"), true);
    }
}