package com.tecno.experiment.init;

import com.tecno.experiment.item.FlashlightItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    // 1. Create the master list for Items attached to your mod ID
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, "experiment_277");

    // 2. Register the Flashlight to the list
    public static final RegistryObject<Item> FLASHLIGHT = ITEMS.register("flashlight",
            () -> new FlashlightItem(new Item.Properties()));

    // 3. This method will be called to tell the main game bus about your list
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}