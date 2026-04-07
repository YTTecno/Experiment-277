package com.tecno.experiment.events;

import com.tecno.experiment.entity.HoundEntity;
import com.tecno.experiment.init.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "experiment_277", bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventBusEvents {

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        // "Take the stats we wrote in HoundEntity, and officially apply them to the HOUND registry item."
        event.put(ModEntities.HOUND.get(), HoundEntity.createAttributes().build());
    }
}