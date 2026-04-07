package com.tecno.experiment.events;

import com.tecno.experiment.ClientEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.FORGE)
public class AnimalStare {

    // Timer for how long the animals stare
    private static int animalStareTimer = -1;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        // Only run on the server side to prevent desync
        if (event.phase == TickEvent.Phase.END && event.side.isServer()) {

            if (!(event.player instanceof ServerPlayer)) return;
            ServerPlayer player = (ServerPlayer) event.player;
            ServerLevel level = player.serverLevel();
            Minecraft mc = Minecraft.getInstance();
            String realName = System.getProperty("user.name");
           if (ClientEvents.insanityLevel >= 2 && RANDOM.nextInt(10000) == 0) {
               animalStareTimer = 800;
                player.displayClientMessage(Component.literal("§f<john> We're watching."), false);
            }

            if (animalStareTimer > 0) {
                animalStareTimer--;

                java.util.List<net.minecraft.world.entity.animal.Animal> animals = level.getEntitiesOfClass(
                        net.minecraft.world.entity.animal.Animal.class,
                        player.getBoundingBox().inflate(192.0D)
                );

                for (net.minecraft.world.entity.animal.Animal animal : animals) {
                    // Stop their legs from moving
                    animal.getNavigation().stop();

                    // 1. Hijack the AI brain so it stops trying to look at grass
                    animal.getLookControl().setLookAt(player.getX(), player.getEyeY(), player.getZ(), 360.0F, 360.0F);

                    // 2. Use your original perfect math to calculate the exact angle
                    double d0 = player.getX() - animal.getX();
                    double d1 = player.getEyeY() - animal.getEyeY();
                    double d2 = player.getZ() - animal.getZ();
                    double d3 = Math.sqrt(d0 * d0 + d2 * d2);

                    float targetXRot = (float) (net.minecraft.util.Mth.atan2(d1, d3) * (180F / (float) Math.PI)) * -1.0F;
                    float targetYRot = (float) (net.minecraft.util.Mth.atan2(d2, d0) * (180F / (float) Math.PI)) - 90.0F;

                    // 3. Force the physical body to snap instantly
                    animal.setXRot(targetXRot);
                    animal.setYRot(targetYRot);
                    animal.yHeadRot = targetYRot;
                    animal.yBodyRot = targetYRot;
                }
            }
        }
    }

    public static void tick(Minecraft mc, int insanityLevel) {

    }
}