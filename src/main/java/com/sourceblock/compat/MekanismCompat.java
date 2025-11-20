package com.sourceblock.compat;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.block.entity.compat.MekanismGasHandler;
import mekanism.common.capabilities.Capabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Mekanism兼容性处理
 * 仅在Mekanism模组存在时加载
 */
public class MekanismCompat {
    
    private static final String MEKANISM_MOD_ID = "mekanism";
    
    /**
     * 检查Mekanism是否已安装
     */
    public static boolean isMekanismLoaded() {
        return ModList.get().isLoaded(MEKANISM_MOD_ID);
    }
    
    /**
     * 初始化Mekanism兼容性
     * 仅在Mekanism已安装时调用
     */
    public static void init(IEventBus modEventBus) {
        if (isMekanismLoaded()) {
            SourceBlockMod.LOGGER.info("检测到Mekanism，启用化学物质处理功能");
            modEventBus.addListener(MekanismCompat::registerCapabilities);
        }
    }
    
    /**
     * 注册Mekanism化学物质能力
     */
    @SuppressWarnings("null")
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        try {
            // 注册化学物质处理能力（包括气体、流体、浆液等）
            event.registerBlockEntity(
                Capabilities.CHEMICAL.block(),
                ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
                (blockEntity, side) -> new MekanismGasHandler(blockEntity)
            );
            
            SourceBlockMod.LOGGER.info("已注册Mekanism化学物质处理能力");
        } catch (Exception e) {
            SourceBlockMod.LOGGER.error("注册Mekanism化学物质能力失败: {}", e.getMessage(), e);
        }
    }
}

