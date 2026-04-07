package com.tecno.experiment.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;

public class HoundEntity extends Monster implements GeoEntity {

    private boolean isBeingStaredAt = false;

    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache cache = software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);

    public HoundEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    // --- 1. THE STATS ---
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.35D) // Extremely fast when not observed
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.FOLLOW_RANGE, 32.0D);
    }

    // --- 2. THE GOALS (What it wants to do) ---
    @Override
    protected void registerGoals() {
        // High priority: Float in water, attack player
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.2D, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(5, new RandomLookAroundGoal(this));

        // Target the player automatically
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    // --- 3. THE "STARE DOWN" MECHANIC ---
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) return;

        if (this.getTarget() instanceof Player player) {
            this.isBeingStaredAt = isPlayerLookingAtMe(player);

            if (this.isBeingStaredAt) {
                // Freeze the Hound completely!
                this.getNavigation().stop();
                this.setDeltaMovement(0, this.getDeltaMovement().y, 0); // Keep gravity, kill momentum

                // Optional: Play a low growl here occasionally to build tension
            }
        }
    }

    // --- 4. THE CAMERA MATH ---
    private boolean isPlayerLookingAtMe(Player player) {
        Vec3 playerLookVector = player.getViewVector(1.0F).normalize();
        Vec3 vectorToHound = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());

        double distance = vectorToHound.length();
        vectorToHound = vectorToHound.normalize();

        // Calculate the angle between where the player is looking and where the Hound is
        double angle = playerLookVector.dot(vectorToHound);

        // If the angle is high enough (0.95-ish means the crosshair is on/very near the entity)
        // AND the player has line of sight (no walls in between), it returns true.
        return angle > 1.0D - 0.05D / distance && player.hasLineOfSight(this);
    }
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        // If the damage is from /kill, the void, or custom admin commands, let it die
        if (source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }

        // Optional: Play a metallic "clank" or a dull thud sound to let the player know
        // their weapons are completely useless against it.
        this.playSound(SoundEvents.VEX_HURT, 1.0F, 0.5F);

        // Cancel the damage
        return false;
    }
    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // 2. The Controller (Tells the game WHEN to play your animations)
    @Override
    public void registerControllers(software.bernie.geckolib.core.animation.AnimatableManager.ControllerRegistrar controllers) {

        // CORE 1: MOVEMENT (Legs and Body)
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<>(this, "movement", 5, state -> {
            if (state.isMoving()) {
                // If the Hound is moving fast (sprinting at the player in the dark)
                if (this.isAggressive() || this.isSprinting()) {
                    return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop("animation.hound.run"));
                }
                // Otherwise, just a creepy pacing walk
                return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop("animation.hound.walk"));
            }
            // Standing still
            return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenLoop("animation.hound.idle"));
        }));

        // CORE 2: ATTACK (Arms and Jaw)
        // We set the transition ticks to 0 so the attack snaps out instantly!
        controllers.add(new software.bernie.geckolib.core.animation.AnimationController<>(this, "attack", 0, state -> {
            // 'swinging' is a vanilla Minecraft variable that turns true when a monster tries to hit you
            if (this.swinging) {
                return state.setAndContinue(software.bernie.geckolib.core.animation.RawAnimation.begin().thenPlay("animation.hound.attack"));
            }
            // If not attacking, let Core 1 handle the arms (so they swing while running)
            return software.bernie.geckolib.core.object.PlayState.STOP;
        }));
    }
}