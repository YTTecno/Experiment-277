package com.tecno.experiment.events;

import com.tecno.experiment.ClientEvents;
import com.tecno.experiment.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MotionBlurEvent {

    private static int blurTimer = -1;
    private static final Random RANDOM = new Random();
    private static boolean isBlurActive = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            // Safety check
            if (mc.player == null || mc.level == null) return;

            // 1. The Trigger
            // Random chance when insanity is high enough
            if (blurTimer == -1 && ClientEvents.insanityLevel >= 0 && RANDOM.nextInt(13000) == 0) {
                blurTimer = 120 + RANDOM.nextInt(240);
            }

            // 2. The Hallucination
            if (blurTimer > 0) {
                blurTimer--;

                // Turn the shader ON (Only run this once so it doesn't lag the game)
                if (!isBlurActive) {
                    // "phosphor.json" is the vanilla shader for screen ghosting/trails
                    mc.gameRenderer.loadEffect(new ResourceLocation("minecraft", "shaders/post/phosphor.json"));
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                            ModSounds.RINGING.get(), SoundSource.AMBIENT, 0.6F, 1.0F, false);
                    mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                            ModSounds.THE_VOICES.get(), SoundSource.AMBIENT, 0.6F, 1.0F, false);
                    isBlurActive = true;
                }

                // 3. The Recovery
                if (blurTimer == 0) {
                    // Turn the shader OFF
                    mc.gameRenderer.shutdownEffect();
                    isBlurActive = false;

                    // Reset timer so it can happen again
                    blurTimer = -1;
                }
            }
        }
    }
}