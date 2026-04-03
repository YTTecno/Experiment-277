package com.tecno.experiment.entity;

import com.mojang.authlib.GameProfile;
import com.tecno.experiment.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import java.util.Random;
import java.util.UUID;

public class crasher {
    public static RemotePlayer entity = null;
    private static int lifeTicks = 0;
    private static final Random RANDOM = new Random();
    private static final int CRASHER_ID = -7001;

    public static void tick(Minecraft mc, int insanity) {
        if (mc.level == null || mc.player == null) return;

        if (entity == null) {
            if (insanity >= 3 && RANDOM.nextInt(25000) == 0) {
                spawn(mc);
                lifeTicks = 0;
            }
        } else {
            if (!entity.isAlive()) { entity = null; return; }

            lifeTicks++;

            entity.xo = entity.getX();
            entity.yo = entity.getY();
            entity.zo = entity.getZ();

            double dx = mc.player.getX() - entity.getX();
            double dy = mc.player.getEyeY() - entity.getEyeY();
            double dz = mc.player.getZ() - entity.getZ();
            double distanceXZ = Math.sqrt(dx * dx + dz * dz);

            float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0F);
            float pitch = (float) (-Math.toDegrees(Math.atan2(dy, distanceXZ)));

            entity.yRotO = entity.getYRot();
            entity.xRotO = entity.getXRot();
            entity.yHeadRotO = entity.getYHeadRot();

            entity.setYRot(yaw);
            entity.setXRot(pitch);
            entity.setYHeadRot(yaw);
            entity.yBodyRot = yaw;

            double moveDist = Math.sqrt(dx * dx + dz * dz);

            if (moveDist > 0.8) {
                double speed = 0.18;
                Vec3 motion = new Vec3((dx / moveDist) * speed, entity.getDeltaMovement().y - 0.08D, (dz / moveDist) * speed);

                entity.setDeltaMovement(motion);

                // 3. Physically move him
                entity.move(MoverType.SELF, entity.getDeltaMovement());

                float movedX = (float) (entity.getX() - entity.xo);
                float movedZ = (float) (entity.getZ() - entity.zo);
                float distanceMoved = (float) Math.sqrt(movedX * movedX + movedZ * movedZ);

                entity.walkAnimation.update(distanceMoved * 4.0F, 0.4F);

            } else if (lifeTicks > 20) {
                terminateGame(mc);
            }
        }
    }

    private static void spawn(Minecraft mc) {
        float yaw = mc.player.getYRot();
        // Spawn directly behind the player
        double bx = mc.player.getX() + Math.sin(Math.toRadians(yaw)) * 5;
        double bz = mc.player.getZ() - Math.cos(Math.toRadians(yaw)) * 5;

        GameProfile playerProfile = mc.player.getGameProfile();
        GameProfile crasherProfile = new GameProfile(UUID.randomUUID(), playerProfile.getName());
        crasherProfile.getProperties().putAll(playerProfile.getProperties());

        entity = new RemotePlayer(mc.level, crasherProfile);

        entity.setPos(bx, mc.player.getY() + 1.0, bz);

        entity.xo = bx;
        entity.yo = mc.player.getY() + 1.0;
        entity.zo = bz;

        entity.setInvisible(false);
        entity.invulnerableTime = 0;

        entity.setMaxUpStep(1.0F);

        mc.level.putNonPlayerEntity(CRASHER_ID, entity);

        mc.level.playLocalSound(bx, mc.player.getY(), bz,
                ModSounds.CRASHER_CHASE.get(), SoundSource.HOSTILE, 0.9f, 1.0f, false);
    }

    private static void terminateGame(Minecraft mc) {
        System.out.println("CRITICAL_PROCESS_DIED: Connection lost.");
        mc.stop();
    }
}