package com.tecno.experiment;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.tecno.experiment.entity.crasher;
import com.tecno.experiment.entity.stalker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.swing.*;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", value = Dist.CLIENT)
public class ClientEvents {

    // --- SYSTEM STATE ---
    private static final Random RANDOM = new Random();
    private static final ResourceLocation HORROR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "background.png");
    private static int flashTimer = 0;
    private static long sanityTicks = 0;
    private static int insanityLevel = 0;
    private static final int MAX_INSANITY = 5;
    private static void spawnCross(Minecraft mc, BlockPos pos) {
        Level level = mc.level;
        if (level == null) return;

        BlockState wood = Blocks.NETHERITE_BLOCK.defaultBlockState();

        // Vertical Bar (4 blocks high)
        level.setBlockAndUpdate(pos.above(1), wood);
        level.setBlockAndUpdate(pos.above(2), wood);
        level.setBlockAndUpdate(pos.above(3), wood);
        level.setBlockAndUpdate(pos.above(4), wood);
        // Horizontal Bar
        level.setBlockAndUpdate(pos.above(3).north(), wood);
        level.setBlockAndUpdate(pos.above(3).south(), wood);
    }
    private static int loginTimer = -1;
    private static boolean scanTriggered = false;
    private static boolean isTerminated = false;
    private static net.minecraft.world.entity.LivingEntity currentStalker = null;
    // --- HUD OVERLAY ---
    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui || isTerminated) return;

        GuiGraphics graphics = event.getGuiGraphics();
        int displaySanity = MAX_INSANITY - insanityLevel;
        String text = "dementedness: " + displaySanity;

        int color = 0x00FF00;
        if (displaySanity <= 1) color = 0xFF0000;
        else if (displaySanity <= 3) color = 0xFFFF00;

        graphics.drawString(mc.font, text, 10, 10, color, true);
    }

    // --- MAIN TICK LOGIC ---
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            stalker.tick(Minecraft.getInstance(), insanityLevel);
            crasher.tick(Minecraft.getInstance(), insanityLevel);
            // Inside the if (mc.level != null && !mc.isPaused() && !isTerminated) block:

            if (insanityLevel >= 3) { // Only start when Sanity is 2, 1, or 0
                // 1 in 5000 chance per tick
                if (RANDOM.nextInt(5000) == 0) {
                    Level level = mc.level;
                    BlockPos playerPos = mc.player.blockPosition();

                    int offsetX = RANDOM.nextInt(30) - 15;
                    int offsetZ = RANDOM.nextInt(30) - 15;
                    BlockPos targetPos = playerPos.offset(offsetX, 0, offsetZ);

                    BlockPos groundPos = level.getHeightmapPos(Heightmap.Types.WORLD_SURFACE, targetPos).below();

                    if (level.getBlockState(groundPos).isSolidRender(level, groundPos)) {
                        spawnCross(mc, groundPos);

                        mc.level.playLocalSound(groundPos.getX(), groundPos.getY(), groundPos.getZ(),
                                SoundEvents.AMBIENT_CAVE.get(), SoundSource.AMBIENT, 0.5f, 0.5f, false);
                    }
                }
            }

            if (mc.level != null && !mc.isPaused() && !isTerminated) {

                // 1. RENDER DISTANCE LOCK
                if (mc.options.renderDistance().get() > 8) {
                    mc.options.renderDistance().set(8);
                }

                String realName = System.getProperty("user.name").toUpperCase();

                // 2. DYNAMIC WINDOW TITLE
                if (insanityLevel == 0) mc.getWindow().setTitle("Minecraft 1.20.1");
                else if (insanityLevel < MAX_INSANITY) mc.getWindow().setTitle("Patient 277 - Session Active [" + realName + "]");
                else mc.getWindow().setTitle("Patient 277 - TERMINAL STATE");

                // 3. SANITY PROGRESSION
                if (insanityLevel < MAX_INSANITY) {
                    sanityTicks++;
                    if (sanityTicks >= 8000) {
                        insanityLevel++;
                        sanityTicks = 0;
                        mc.player.displayClientMessage(Component.literal("§7[Debug] Sanity Level: " + (MAX_INSANITY - insanityLevel)), true);
                    }
                }

                // 4. THE KILL SWITCH
                if (insanityLevel >= MAX_INSANITY) {
                    triggerFinalCollapse(mc);
                }

                // 5. INITIAL SYSTEM SCAN
                if (!scanTriggered) {
                    if (loginTimer == -1) loginTimer = 6000 + RANDOM.nextInt(6000);
                    if (loginTimer > 0) loginTimer--;
                    else triggerSystemScan(mc);
                }
            }
        }
    }

    public static boolean lowerInsanity() {
        if (insanityLevel > 0 && insanityLevel < MAX_INSANITY && !isTerminated) {
            insanityLevel--;
            sanityTicks = 0; // Reset the timer for the current level
            return true;
        }
        return false;
    }

    private static void triggerSystemScan(Minecraft mc) {
        String realName = System.getProperty("user.name");
        mc.player.displayClientMessage(Component.literal("§e" + realName + " has joined the game"), false);
        mc.player.displayClientMessage(Component.literal("§f<" + realName + "> You need to wake up. This is a dream."), false);
        mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(), SoundEvents.AMBIENT_CAVE.get(), SoundSource.MASTER, 1.0F, 0.1F, false);
        scanTriggered = true;
    }

    public static void triggerFinalCollapse(Minecraft mc) {
        if (isTerminated) return;
        isTerminated = true;

        String realName = System.getProperty("user.name").toUpperCase();
        mc.getWindow().setTitle("TERMINATION_COMPLETE_" + realName);

        new Thread(() -> {
            try {
                JOptionPane.showMessageDialog(null,
                        "Patient non-responsive. Ending session.\n\n" +
                                "ID: SUBJECT_277\n" +
                                "USER: " + realName + "\n" +
                                "STATUS: TERMINATED",
                        "CRITICAL_MEDICAL_ERROR",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            } catch (Exception e) {
                System.exit(0);
            }
        }).start();
    }

    // --- MENU & MUSIC MODIFICATIONS ---
    @SubscribeEvent
    public static void onClientTickMusic(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level != null && mc.getMusicManager() != null) {
                mc.getMusicManager().stopPlaying();
            }
        }
    }

    @SubscribeEvent
    public static void onBackgroundRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof TitleScreen) {
            GuiGraphics graphics = event.getGuiGraphics();
            RenderSystem.setShaderTexture(0, HORROR_BACKGROUND);
            graphics.blit(HORROR_BACKGROUND, 0, 0, 0, 0, event.getScreen().width, event.getScreen().height, event.getScreen().width, event.getScreen().height);
        }
    }

    @SubscribeEvent
    public static void onMenuOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen) {
            Minecraft mc = Minecraft.getInstance();
            mc.getMusicManager().stopPlaying();
            SoundEvent horrorMusic = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.fromNamespaceAndPath("experiment_277", "menu_music"));
            if (horrorMusic != null) mc.getSoundManager().play(SimpleSoundInstance.forMusic(horrorMusic));
        }
    }

    @SubscribeEvent
    public static void onMenuRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (flashTimer <= 0 && RANDOM.nextInt(200) == 0) flashTimer = 3;
            if (flashTimer > 0) {
                event.getGuiGraphics().fill(0, 0, event.getScreen().width, event.getScreen().height, 0x66FF0000);
                flashTimer--;
            }
        }
    }
}