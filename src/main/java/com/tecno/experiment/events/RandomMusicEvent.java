package com.tecno.experiment.events;

import com.tecno.experiment.ClientEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RandomMusicEvent {

    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Run safely on the server
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {
            if (!(event.player instanceof ServerPlayer player)) return;

            // THE INVISIBLE DICE ROLL
            // Minecraft runs 20 ticks per second.
            // A 1-in-24000 chance means this will happen roughly once every 20 real-world minutes.
            // Change this number to make it more or less frequent!
            if (ClientEvents.insanityLevel >=0 && RANDOM.nextInt(15000) == 0) {

                // Play the sound specifically to this player's client.
                // We use SoundSource.MUSIC so it respects the player's music volume slider in their settings!

                // NOTE: You will need to replace 'ModSounds.EXCUSE.get()' with wherever you register the sound.
                player.playNotifySound(com.tecno.experiment.init.ModSounds.EXCUSE.get(), SoundSource.MASTER, 1.0F, 1.0F);
            }
        }
    }
}