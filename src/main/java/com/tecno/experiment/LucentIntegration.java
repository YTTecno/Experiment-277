package com.tecno.experiment;

import com.legacy.lucent.api.EntityBrightness;
import com.legacy.lucent.api.plugin.ILucentPlugin;
import com.legacy.lucent.api.plugin.LucentPlugin;
import com.tecno.experiment.entity.FlashlightBeamEntity;
import net.minecraft.world.entity.Entity;

// @LucentPlugin
public class LucentIntegration implements ILucentPlugin {

    @Override
    public String ownerModID() {
        return "experiment_277";
    }

    @Override
    public void getEntityLightLevel(EntityBrightness entityBrightness) {
        Entity entity = entityBrightness.getEntity();

        // If the entity is our custom beam, make it glow at level 15!
        if (entity instanceof FlashlightBeamEntity) {
            entityBrightness.setLightLevel(15);
        }
    }
}