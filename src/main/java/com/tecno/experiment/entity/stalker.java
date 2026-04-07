package com.tecno.experiment.entity;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import java.util.Random;

public class stalker {
    public static RemotePlayer entity = null;
    private static final Random RANDOM = new Random();
    private static final int WATCHER_ID = -7000; // Unique ID to stop flickering
    private static int lifeTicks = 0;

    public static void tick(Minecraft mc, int insanity) {
        if (mc.level == null || mc.player == null) return;

        if (entity == null) {
            // Spawn logic: Try to spawn every tick if insanity is met
            if (insanity >= 1 && RANDOM.nextInt(8000) == 0) {
                spawn(mc);
            }
        } else {
            // If the entity somehow dies or is removed elsewhere
            if (!entity.isAlive()) {
                entity = null;
                return;
            }

            lifeTicks++;

            // 1. FORCED STARE LOGIC
            double dx = mc.player.getX() - entity.getX();
            double dy = mc.player.getEyeY() - entity.getEyeY();
            double dz = mc.player.getZ() - entity.getZ();
            double distXZ = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distXZ)));

            entity.setXRot(pitch);
            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);
            entity.yHeadRotO = yaw; // Prevents head-snapping jitter
            entity.yBodyRot = yaw;  // Keeps the body facing the player

            // 2. DETECTION LOGIC (Are you looking at him?)
            Vec3 playerLook = mc.player.getViewVector(1.0F);
            Vec3 playerEyes = mc.player.getEyePosition(1.0F);
            Vec3 entityCenter = entity.position().add(0, entity.getBbHeight() / 2.0, 0);

            // Calculate the direction from eyes to chest
            Vec3 toEntity = entityCenter.subtract(playerEyes).normalize();
            double dot = playerLook.dot(toEntity);

            // Vanishes if he's in the center ~30% of your screen
            if (lifeTicks > 10 && (dot > 0.85 || mc.player.distanceTo(entity) < 5.0)) {

                mc.level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                        SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 1.0f, 0.5f, false);

                entity.setInvisible(true);
                mc.level.removeEntity(WATCHER_ID, Entity.RemovalReason.DISCARDED);
                entity = null;
            }
        }
    }

    private static void spawn(Minecraft mc) {
        float yaw = mc.player.getYRot();
        float sideOffset = RANDOM.nextBoolean() ? 35.0f : -35.0f;
        double angle = Math.toRadians(yaw + sideOffset);

        double bx = mc.player.getX() - (Math.sin(angle) * 12);
        double bz = mc.player.getZ() + (Math.cos(angle) * 12);

        // Fetch the player's real profile
        GameProfile playerProfile = mc.player.getGameProfile();

        // Create a fake identity with a random UUID to bypass culling, but copy the player's name
        GameProfile stalkerProfile = new GameProfile(UUID.randomUUID(), playerProfile.getName());

        // Clone the texture properties (This copies the skin)
        stalkerProfile.getProperties().putAll(playerProfile.getProperties());

        // Spawn the stalker using the cloned profile
        entity = new RemotePlayer(mc.level, stalkerProfile);
        entity.setPos(bx, mc.player.getY(), bz);

        // Force rendering flags
        entity.setInvisible(false);
        entity.noPhysics = true;

        mc.level.putNonPlayerEntity(WATCHER_ID, entity);
        lifeTicks = 0;
    }
}