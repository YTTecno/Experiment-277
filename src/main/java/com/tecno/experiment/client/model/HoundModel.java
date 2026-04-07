package com.tecno.experiment.client.model;

import com.tecno.experiment.entity.HoundEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HoundModel extends GeoModel<HoundEntity> {
    @Override
    public ResourceLocation getModelResource(HoundEntity object) {
        return new ResourceLocation("experiment_277", "geo/hound.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(HoundEntity object) {
        return new ResourceLocation("experiment_277", "textures/entity/hound.png");
    }

    @Override
    public ResourceLocation getAnimationResource(HoundEntity animatable) {
        return new ResourceLocation("experiment_277", "animations/hound.animation.json");
    }
}