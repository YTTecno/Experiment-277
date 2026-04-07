package com.tecno.experiment.client.renderer;

import com.tecno.experiment.client.model.HoundModel;
import com.tecno.experiment.entity.HoundEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class HoundRenderer extends GeoEntityRenderer<HoundEntity> {
    public HoundRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new HoundModel());
        this.shadowRadius = 1.0f;
    }

    @Override
    public void render(HoundEntity entity, float entityYaw, float partialTick, com.mojang.blaze3d.vertex.PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose(); // Save the normal world scale

        poseStack.scale(2.5F, 2.5F, 2.5F);

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        poseStack.popPose();
    }
}