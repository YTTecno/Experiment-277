package com.tecno.experiment.events;

import com.tecno.experiment.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class OpenALFail {

    private static int silenceTimer = -1;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            // Don't run this if in the main menu
            if (mc.player == null || mc.level == null) return;

            // 1. The Trigger
            if (silenceTimer == -1 && ClientEvents.insanityLevel >= 0 && RANDOM.nextInt(15000) == 0) {
                silenceTimer = 3600;

                mc.player.displayClientMessage(Component.literal("§c[OpenAL] AL lib: (EE) alc_cleanup: 1 device not closed"), false);
                mc.player.displayClientMessage(Component.literal("§cjava.lang.IllegalStateException: Error initializing SoundSystem. Turning off sounds & music."), false);
                mc.player.displayClientMessage(Component.literal("§c[OpenAL] Attempting to restart sound driver... Failed."), false);
            }

            // 2. The Silence Loop
            if (silenceTimer > 0) {
                silenceTimer--;

                // Force-stop any lingering background music or looping sounds
                mc.getSoundManager().stop();

                // 3. The "Snap Back" sound when the timer ends
                if (silenceTimer == 0) {
                    // Play the cave sound directly to the client
                    mc.player.playSound(SoundEvents.AMBIENT_CAVE.get(), 1.0f, 0.5f);
                    silenceTimer = -1; // Reset
                }
            }
        }
    }

    // --- THE ABSOLUTE MUTE FILTER ---
    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        if (silenceTimer > 0) {
            // If the timer is active, the sound is set to "null".
            // This deletes footsteps, block breaks, and mob noises instantly.
            event.setSound(null);
        }
    }
}