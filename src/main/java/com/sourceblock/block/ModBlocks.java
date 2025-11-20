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
            .strength(3.0F, 6.0F)
            .sound(SoundType.METAL)));
}

