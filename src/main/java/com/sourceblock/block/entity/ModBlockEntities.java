package com.sourceblock.block.entity;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, SourceBlockMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SourceBlockEntity>> SOURCE_BLOCK_ENTITY =
        BLOCK_ENTITIES.register("source_block_entity", () ->
            BlockEntityType.Builder.of(SourceBlockEntity::new, ModBlocks.SOURCE_BLOCK.get())
                .build(null));
}

