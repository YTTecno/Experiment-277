package com.tecno.experiment.item;

import com.tecno.experiment.entity.FlashlightBeamEntity;
import com.tecno.experiment.init.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import com.tecno.experiment.init.ModBlocks;

public class FlashlightItem extends Item {

    public FlashlightItem(Properties properties) {
        super(properties.stacksTo(1)); // Flashlights shouldn't stack!
    }

    // --- TOGGLE ON/OFF WITH RIGHT CLICK ---
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide()) {
            CompoundTag nbt = stack.getOrCreateTag();
            boolean isOn = nbt.getBoolean("IsOn");

            // Flip the switch
            nbt.putBoolean("IsOn", !isOn);

            // Play a satisfying click sound
            float pitch = isOn ? 0.8F : 1.2F; // Lower pitch for off, higher for on
            level.playSound(null, player.blockPosition(), SoundEvents.LEVER_CLICK, SoundSource.PLAYERS, 0.5F, pitch);
        }

        return InteractionResultHolder.success(stack);
    }

    // --- THE BEAM LOGIC ---
    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide() && entity instanceof Player player) {

            CompoundTag nbt = stack.getOrCreateTag();
            boolean isOn = nbt.getBoolean("IsOn");
            boolean isHolding = isSelected || player.getOffhandItem() == stack;

            if (isOn && isHolding) {
                BlockHitResult hitResult = shootFlashlightBeam(level, player, 20.0D);

                if (hitResult.getType() == HitResult.Type.BLOCK) {
                    BlockPos currentTarget = hitResult.getBlockPos().relative(hitResult.getDirection());
                    BlockPos lastTarget = new BlockPos(nbt.getInt("LastX"), nbt.getInt("LastY"), nbt.getInt("LastZ"));

                    // 1. SILENT ENTITY MOVEMENT (For future Dynamic Lights)
                    if (!nbt.contains("BeamUUID")) {
                        FlashlightBeamEntity beam = new FlashlightBeamEntity(ModEntities.FLASHLIGHT_BEAM.get(), level);
                        beam.setPos(currentTarget.getX() + 0.5, currentTarget.getY() + 0.5, currentTarget.getZ() + 0.5);
                        level.addFreshEntity(beam);
                        nbt.putUUID("BeamUUID", beam.getUUID());
                    } else {
                        Entity beam = ((ServerLevel) level).getEntity(nbt.getUUID("BeamUUID"));
                        if (beam != null) beam.setPos(currentTarget.getX() + 0.5, currentTarget.getY() + 0.5, currentTarget.getZ() + 0.5);
                    }

                    // 2. THE GRID-SNAP BLOCK FALLBACK (So you actually have light right now!)
                    if (currentTarget.distSqr(lastTarget) > 1.0D) {
                        if (level.getBlockState(lastTarget).is(ModBlocks.FAKE_LIGHT.get())) {
                            level.removeBlock(lastTarget, false);
                        }
                        if (level.getBlockState(currentTarget).isAir()) {
                            level.setBlock(currentTarget, ModBlocks.FAKE_LIGHT.get().defaultBlockState(), 3);
                            nbt.putInt("LastX", currentTarget.getX());
                            nbt.putInt("LastY", currentTarget.getY());
                            nbt.putInt("LastZ", currentTarget.getZ());
                        }
                    } else {
                        if (level.getGameTime() % 10 == 0 && level.getBlockState(lastTarget).isAir()) {
                            level.setBlock(lastTarget, ModBlocks.FAKE_LIGHT.get().defaultBlockState(), 3);
                        }
                    }
                }
            } else {
                // TURNED OFF: Clean up everything
                if (nbt.contains("BeamUUID")) {
                    Entity beam = ((ServerLevel) level).getEntity(nbt.getUUID("BeamUUID"));
                    if (beam != null) beam.discard();
                    nbt.remove("BeamUUID");
                }
                if (nbt.contains("LastX")) {
                    BlockPos lastTarget = new BlockPos(nbt.getInt("LastX"), nbt.getInt("LastY"), nbt.getInt("LastZ"));
                    if (level.getBlockState(lastTarget).is(ModBlocks.FAKE_LIGHT.get())) {
                        level.removeBlock(lastTarget, false);
                    }
                    nbt.remove("LastX"); nbt.remove("LastY"); nbt.remove("LastZ");
                }
            }
        }
    }
    // --- CUSTOM 20-BLOCK FLASHLIGHT BEAM ---
    private BlockHitResult shootFlashlightBeam(Level level, Player player, double maxDistance) {
        net.minecraft.world.phys.Vec3 eyePosition = player.getEyePosition(1.0F);
        net.minecraft.world.phys.Vec3 lookVector = player.getViewVector(1.0F);
        net.minecraft.world.phys.Vec3 endPosition = eyePosition.add(lookVector.x * maxDistance, lookVector.y * maxDistance, lookVector.z * maxDistance);

        return level.clip(new ClipContext(eyePosition, endPosition, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }
}