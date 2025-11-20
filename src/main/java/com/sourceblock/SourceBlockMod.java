package com.sourceblock;

import com.mojang.logging.LogUtils;
import com.sourceblock.block.ModBlocks;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.block.entity.SourceBlockEntity;
import com.sourceblock.compat.MekanismCompat;
import com.sourceblock.event.ModEvents;
import com.sourceblock.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

@Mod(SourceBlockMod.MODID)
public class SourceBlockMod {
    public static final String MODID = "sourceblock";
    public static final Logger LOGGER = LogUtils.getLogger();

    // Create a Deferred Register to hold CreativeModeTabs
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = 
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SOURCE_BLOCK_TAB = 
        CREATIVE_MODE_TABS.register("source_block_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sourceblock"))
            .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
            .icon(() -> ModItems.EMPTY_SOURCE_BLOCK.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(ModItems.EMPTY_SOURCE_BLOCK.get());
                output.accept(ModItems.WATER_SOURCE_BLOCK.get());
                output.accept(ModItems.LAVA_SOURCE_BLOCK.get());
                output.accept(ModItems.MILK_SOURCE_BLOCK.get());
            }).build());

    public SourceBlockMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register blocks, items, and block entities
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        // Register event handlers
        NeoForge.EVENT_BUS.register(ModEvents.class);

        // Register creative mode tab contents
        modEventBus.addListener(this::addCreative);
        
        // Register capabilities
        modEventBus.addListener(this::registerCapabilities);
        
        // 初始化Mekanism兼容性（如果Mekanism已安装）
        try {
            MekanismCompat.init(modEventBus);
        } catch (NoClassDefFoundError e) {
            // Mekanism未安装，忽略
            LOGGER.info("Mekanism未安装，跳过气体处理功能");
        }

        LOGGER.info("Source Block Mod initialized");
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // 注册源方块的流体处理能力
        event.registerBlockEntity(
            Capabilities.FluidHandler.BLOCK,
            ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity
        );
        
        // 注册源方块的能量处理能力（用于销毁能量）
        event.registerBlockEntity(
            Capabilities.EnergyStorage.BLOCK,
            ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
            (blockEntity, side) -> blockEntity
        );
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModItems.EMPTY_SOURCE_BLOCK.get());
            event.accept(ModItems.WATER_SOURCE_BLOCK.get());
            event.accept(ModItems.LAVA_SOURCE_BLOCK.get());
            event.accept(ModItems.MILK_SOURCE_BLOCK.get());
        }
    }
}

