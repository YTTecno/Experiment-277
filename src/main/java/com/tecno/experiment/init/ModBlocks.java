package com.tecno.experiment.init;

import com.tecno.experiment.block.FakeLightBlock;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    // 1. Create the master list for Blocks attached to your mod ID
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "experiment_277");

    // 2. Register the Fake Light Block to the list
    public static final RegistryObject<Block> FAKE_LIGHT = BLOCKS.register("fake_light",
            () -> new FakeLightBlock());

    // 3. This method will be called to tell the main game bus about your list
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}