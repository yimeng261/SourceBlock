package com.sourceblock.block;

import com.sourceblock.SourceBlockMod;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = 
        DeferredRegister.createBlocks(SourceBlockMod.MODID);

    public static final DeferredBlock<Block> SOURCE_BLOCK = BLOCKS.register("source_block",
        () -> new SourceBlock(BlockBehaviour.Properties.of()
            .strength(0.5F, 1200.0F)
            .sound(SoundType.METAL)
            .lightLevel((state) -> {
                // 根据流体类型返回不同的亮度
                SourceBlock.FluidType type = state.getValue(SourceBlock.FLUID_TYPE);
                return switch (type) {
                    case LAVA -> 15;
                    case WATER -> 8;
                    case MILK -> 5;
                    case EMPTY -> 0;
                };
            })));

    public static final DeferredBlock<Block> CREATIVE_SOURCE_BLOCK = BLOCKS.register("creative_source_block",
        () -> new CreativeSourceBlock(BlockBehaviour.Properties.of()
            .strength(0.5F, 3600000.0F)
            .noLootTable()
            .sound(SoundType.METAL)
            .lightLevel((state) -> 15)));

    public static final DeferredBlock<Block> CREATIVE_ITEM_SOURCE_BLOCK = BLOCKS.register("creative_item_source_block",
        () -> new CreativeItemSourceBlock(BlockBehaviour.Properties.of()
            .strength(0.5F, 3600000.0F)
            .noLootTable()
            .sound(SoundType.METAL)));

    public static final DeferredBlock<Block> ITEM_SOURCE_BLOCK = BLOCKS.register("item_source_block",
        () -> new ItemSourceBlock(BlockBehaviour.Properties.of()
            .strength(0.5F, 1200.0F)
            .sound(SoundType.METAL)));
}

