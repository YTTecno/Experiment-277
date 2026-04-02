package com.tecno.experiment;

import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.lwjgl.glfw.GLFW;

import javax.swing.JOptionPane;
import java.util.Random;

@Mod.EventBusSubscriber(modid = "experiment_277", value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onClientTickMusic(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            // ONLY stop music if the player is actually in a world/level
            // mc.level is null when you are on the Main Menu
            if (mc.level != null && mc.getMusicManager() != null) {
                mc.getMusicManager().stopPlaying();
            }
        }
    }

    private static final Random RANDOM = new Random();
    private static final ResourceLocation HORROR_BACKGROUND = ResourceLocation.fromNamespaceAndPath("minecraft", "background.png");

    // --- SYSTEM STATE ---
    private static int flashTimer = 0;
    private static long sanityTicks = 0;
    private static int insanityLevel = 0;
    private static final int MAX_INSANITY = 5;

    private static int loginTimer = -1;
    private static boolean scanTriggered = false;
    private static boolean isTerminated = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            // Lock Atmosphere
            if (mc.options.renderDistance().get() > 8) {
                mc.options.renderDistance().set(8);
            }

            if (mc.level != null && !mc.isPaused() && !isTerminated) {
                String realName = System.getProperty("user.name").toUpperCase();

                // --- DYNAMIC WINDOW TITLE ---
                if (insanityLevel == 0) {
                    mc.getWindow().setTitle("Minecraft 1.20.1");
                } else if (insanityLevel > 0 && insanityLevel < MAX_INSANITY) {
                    mc.getWindow().setTitle("Patient 277 - Session Active [" + realName + "]");
                } else if (insanityLevel == MAX_INSANITY) {
                    mc.getWindow().setTitle("Patient 277 - TERMINAL STATE // DO NOT DISCONNECT");
                }

                // --- DEBUG KEY: HOLD 'J' TO FAST-FORWARD ---
                if (InputConstants.isKeyDown(mc.getWindow().getWindow(), GLFW.GLFW_KEY_J)) {
                    if (insanityLevel < MAX_INSANITY) sanityTicks = (long) (insanityLevel + 1) * 12000 - 10;
                    if (!scanTriggered) loginTimer = 50;
                    mc.player.displayClientMessage(Component.literal("§c[Debug] Syncing session data..."), true);
                }

                // --- SANITY COUNTER ---
                if (insanityLevel < MAX_INSANITY) {
                    sanityTicks++;

                    if (sanityTicks % 12000 == 0) {
                        insanityLevel++;
                        mc.player.displayClientMessage(Component.literal("§7[Debug] Sanity Level: " + insanityLevel + "/" + MAX_INSANITY), true);

                        if (insanityLevel == MAX_INSANITY) {
                            triggerFinalCollapse(mc);
                        }
                    }
                }

                // --- INITIAL SYSTEM SCAN (10-15 MINS) ---
                if (!scanTriggered) {
                    if (loginTimer == -1) {
                        loginTimer = 12000 + RANDOM.nextInt(6000);
                    }

                    if (loginTimer > 0) {
                        loginTimer--;
                    } else if (loginTimer == 0) {
                        triggerSystemScan(mc);
                    }
                }
            }
        }
    }

    private static void triggerSystemScan(Minecraft mc) {
        String realName = System.getProperty("user.name");
        mc.player.displayClientMessage(Component.literal("§e" + realName + " has joined the game"), false);
        mc.player.displayClientMessage(Component.literal("§f<" + realName + "> You need to wake up " + realName + ". This is a dream. §f" ), false);

        mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.MASTER, 1.0F, 0.1F, false);
        scanTriggered = true;
    }

    private static void triggerFinalCollapse(Minecraft mc) {
        isTerminated = true;
        String realName = System.getProperty("user.name").toUpperCase();

        // Final "Ear-Ringer" sound
        mc.level.playLocalSound(mc.player.getX(), mc.player.getY(), mc.player.getZ(),
                SoundEvents.ANVIL_LAND, SoundSource.MASTER, 2.0F, 0.1F, false);

        mc.getWindow().setTitle("TERMINATION_COMPLETE_" + realName);

        // System Popup Thread
        new Thread(() -> {
            try {
                JOptionPane.showMessageDialog(null,
                        "Patient non-responsive. Ending session.\n\n" +
                                "ID: SUBJECT_277\n" +
                                "USER: " + realName + "\n" +
                                "STATUS: TERMINATED",
                        "CRITICAL_MEDICAL_ERROR",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        System.exit(0);
    }

    // --- MAIN MENU RENDERING ---

    @SubscribeEvent
    public static void onBackgroundRender(ScreenEvent.Render.Pre event) {
        if (event.getScreen() instanceof TitleScreen) {
            GuiGraphics graphics = event.getGuiGraphics();
            int width = event.getScreen().width;
            int height = event.getScreen().height;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, HORROR_BACKGROUND);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

            graphics.blit(HORROR_BACKGROUND, 0, 0, 0, 0, width, height, width, height);
        }
    }

    @SubscribeEvent
    public static void onMenuOpen(ScreenEvent.Opening event) {
        if (event.getScreen() instanceof TitleScreen) {
            Minecraft mc = Minecraft.getInstance();
            mc.getMusicManager().stopPlaying();

            SoundEvent horrorMusic = ForgeRegistries.SOUND_EVENTS.getValue(
                    ResourceLocation.fromNamespaceAndPath("experiment_277", "menu_music")
            );
            if (horrorMusic != null) {
                mc.getSoundManager().play(SimpleSoundInstance.forMusic(horrorMusic));
            }
        }
    }

    public static boolean lowerInsanity() {
        // If they are between Level 1 and 4, the pill works
        if (insanityLevel > 0 && insanityLevel < MAX_INSANITY) {
            insanityLevel--;
            // Reset the timer for the current level so they buy more time
            sanityTicks = (long) insanityLevel * 12000;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onMenuRender(ScreenEvent.Render.Post event) {
        if (event.getScreen() instanceof TitleScreen) {
            if (flashTimer <= 0 && RANDOM.nextInt(200) == 0) {
                flashTimer = 3;
            }

            if (flashTimer > 0) {
                GuiGraphics graphics = event.getGuiGraphics();
                int width = event.getScreen().width;
                int height = event.getScreen().height;

                graphics.fill(0, 0, width, height, 0x66FF0000);
                flashTimer--;
            }
        }
    }
}