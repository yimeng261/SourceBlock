package com.github.yimeng261.sourceblock;

import com.mojang.logging.LogUtils;
import com.github.yimeng261.sourceblock.block.ModBlocks;
import com.github.yimeng261.sourceblock.block.entity.ModBlockEntities;
import com.github.yimeng261.sourceblock.compat.MekanismCompat;
import com.github.yimeng261.sourceblock.event.ModEvents;
import com.github.yimeng261.sourceblock.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

@Mod(SourceBlockMod.MODID)
public class SourceBlockMod {
    public static final String MODID = "sourceblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(net.minecraft.core.registries.Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab
    public static final RegistryObject<CreativeModeTab> SOURCE_BLOCK_TAB = 
        CREATIVE_MODE_TABS.register("source_block_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sourceblock"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> ModItems.EMPTY_SOURCE_BLOCK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.EMPTY_SOURCE_BLOCK.get());
                output.accept(ModItems.WATER_SOURCE_BLOCK.get());
                output.accept(ModItems.LAVA_SOURCE_BLOCK.get());
                output.accept(ModItems.MILK_SOURCE_BLOCK.get());
                output.accept(ModItems.CREATIVE_SOURCE_BLOCK.get());
                output.accept(ModItems.CREATIVE_ITEM_SOURCE_BLOCK.get());
                output.accept(ModItems.EMPTY_ITEM_SOURCE_BLOCK.get());
                output.accept(ModItems.COBBLESTONE_SOURCE_BLOCK.get());
                output.accept(ModItems.STONE_SOURCE_BLOCK.get());
                output.accept(ModItems.SMOOTH_STONE_SOURCE_BLOCK.get());
                output.accept(ModItems.OBSIDIAN_SOURCE_BLOCK.get());
            }).build());

    public SourceBlockMod() {
        this(FMLJavaModLoadingContext.get());
    }

    public SourceBlockMod(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ForgeMod.enableMilkFluid();
        
        // Register blocks, items, and block entities
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(ModEvents.class);

        // Initialize Mekanism compatibility
        MekanismCompat.init(modEventBus);

        LOGGER.info("Source Block Mod initialized");
    }

}

