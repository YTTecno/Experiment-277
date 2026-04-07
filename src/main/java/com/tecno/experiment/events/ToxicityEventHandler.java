package com.tecno.experiment.events;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ToxicityEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run on the server to ensure data saves correctly
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {

            if (!(event.player instanceof ServerPlayer)) return;
            ServerPlayer player = (ServerPlayer) event.player;

            // --- THE METABOLISM TIMER ---
            // player.tickCount is how long the player has existed.
            // 6000 ticks = 5 minutes (20 ticks * 60 seconds * 5)
            // This means every 5 minutes, their body naturally clears 1 pill.
            if (player.tickCount % 6000 == 0) {

                // Read current toxicity
                int currentToxicity = player.getPersistentData().getInt("PillsConsumed");

                // If they have any pills in their system, remove one
                if (currentToxicity > 0) {
                    player.getPersistentData().putInt("PillsConsumed", currentToxicity - 1);

                    // Optional: Give them a visual cue when they are finally "safe" again
                    // Since 5 is the overdose limit, dropping to 4 means they are clear.
                    if (currentToxicity == 5) {
                        player.displayClientMessage(Component.literal("§fYour head begins to clear. Toxicity levels stabilizing."), true);
                    }
                }
            }
        }
    }
}