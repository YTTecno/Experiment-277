package com.tecno.experiment.client;

import com.tecno.experiment.init.ModEntities;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// This listens to the MOD bus during startup!
@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // "When you see the Flashlight Beam, render absolutely nothing."
        event.registerEntityRenderer(ModEntities.FLASHLIGHT_BEAM.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.HOUND.get(), com.tecno.experiment.client.renderer.HoundRenderer::new);
    }
}