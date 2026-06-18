package com.sourceblock.client;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.client.renderer.CreativeSourceBlockRenderer;
import com.sourceblock.client.renderer.CreativeItemSourceBlockRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = SourceBlockMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
            ModBlockEntities.CREATIVE_SOURCE_BLOCK_ENTITY.get(),
            CreativeSourceBlockRenderer::new
        );
        
        event.registerBlockEntityRenderer(
            ModBlockEntities.CREATIVE_ITEM_SOURCE_BLOCK_ENTITY.get(),
            CreativeItemSourceBlockRenderer::new
        );
        
        SourceBlockMod.LOGGER.info("Registered block entity renderers");
    }
}

