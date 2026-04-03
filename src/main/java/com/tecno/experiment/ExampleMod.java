package com.tecno.experiment;

import com.mojang.logging.LogUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.network.chat.Component;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import static com.tecno.experiment.init.ModSounds.SOUND_EVENTS;

@Mod(ExampleMod.MODID)
public class ExampleMod {

    public static final String MODID = "experiment_277";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> INTEGRITY_PILL = ITEMS.register("integrity_pill",
            () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)) {

                private int totalPillsConsumed = 0;

                @Override
                public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.EAT; }

                @Override
                public int getUseDuration(ItemStack stack) { return 32; }

                @Override
                public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
                    if (player.getCooldowns().isOnCooldown(this)) {
                        return InteractionResultHolder.fail(player.getItemInHand(hand));
                    }
                    player.startUsingItem(hand);
                    return InteractionResultHolder.consume(player.getItemInHand(hand));
                }

                @Override
                public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
                    if (entity instanceof Player player) {
                        player.getCooldowns().addCooldown(this, 100);

                        if (level.isClientSide) {
                            if (ClientEvents.lowerInsanity()) {
                                player.displayClientMessage(Component.literal("§fПрепарат-Б §7administered. Sanity recalibrating..."), true);
                            } else {
                                player.displayClientMessage(Component.literal("§fIntegrity at nominal levels. No action required."), true);
                            }
                        }

                        totalPillsConsumed++;
                        if (totalPillsConsumed > 5) {
                            player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 400, 0));
                            player.hurt(player.damageSources().magic(), 2.0F); // 1 heart damage
                            player.displayClientMessage(Component.literal("§c[WARNING] Toxicity detected. Cease administration."), true);
                        }
                    }
                    // (Infinite use)
                    return stack;
                }
            });

    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        com.tecno.experiment.init.ModSounds.register(modEventBus);
        modEventBus.addListener(this::addCreative);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.INGREDIENTS) {
            event.accept(INTEGRITY_PILL);
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.getPersistentData().getBoolean("JoinedExperiment")) {
            player.getInventory().add(new ItemStack(INTEGRITY_PILL.get(), 1));
            player.getPersistentData().putBoolean("JoinedExperiment", true);
        }
    }
}