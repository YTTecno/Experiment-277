package com.tecno.experiment.init;

import com.tecno.experiment.entity.FlashlightBeamEntity;
import com.tecno.experiment.entity.HoundEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, "experiment_277");

    public static final RegistryObject<EntityType<HoundEntity>> HOUND =
            ENTITIES.register("hound", () -> EntityType.Builder.of(HoundEntity::new, MobCategory.MONSTER)
                    .sized(0.6f, 1.95f) // The size of its hitbox (this is standard Zombie/Player size)
                    .clientTrackingRange(32) // How far away the client can see it
                    .build("hound"));

    public static final RegistryObject<EntityType<FlashlightBeamEntity>> FLASHLIGHT_BEAM =
            ENTITIES.register("flashlight_beam",
                    () -> EntityType.Builder.<FlashlightBeamEntity>of(FlashlightBeamEntity::new, MobCategory.MISC)
                            .sized(0.1F, 0.1F) // Tiny, invisible hitbox
                            .build("flashlight_beam"));

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }
}