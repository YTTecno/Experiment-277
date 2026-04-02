package com.tecno.experiment;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277")
public class MovementHandler {

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Player player = event.player;

            if (player.isSprinting()) {
                player.setSprinting(false);
            }

            // This ensures they move at the classic 1.0 "Heavy" speed
            // 0.1D is the default walking speed.
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null && speedAttr.getBaseValue() > 0.1D) {
                speedAttr.setBaseValue(0.1D);
            }
        }
    }
}