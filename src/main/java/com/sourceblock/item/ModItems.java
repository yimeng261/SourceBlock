package com.sourceblock.item;

import com.sourceblock.SourceBlockMod;
import com.sourceblock.block.ModBlocks;
import com.sourceblock.block.SourceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = 
        DeferredRegister.createItems(SourceBlockMod.MODID);

    public static final DeferredItem<BlockItem> EMPTY_SOURCE_BLOCK = ITEMS.register("empty_source_block",
        () -> new SourceBlockItem(ModBlocks.SOURCE_BLOCK.get(), 
            new Item.Properties(), SourceBlock.FluidType.EMPTY));

    public static final DeferredItem<BlockItem> WATER_SOURCE_BLOCK = ITEMS.register("water_source_block",
        () -> new SourceBlockItem(ModBlocks.SOURCE_BLOCK.get(), 
            new Item.Properties(), SourceBlock.FluidType.WATER));

    public static final DeferredItem<BlockItem> LAVA_SOURCE_BLOCK = ITEMS.register("lava_source_block",
        () -> new SourceBlockItem(ModBlocks.SOURCE_BLOCK.get(), 
            new Item.Properties(), SourceBlock.FluidType.LAVA));

    public static final DeferredItem<BlockItem> MILK_SOURCE_BLOCK = ITEMS.register("milk_source_block",
        () -> new SourceBlockItem(ModBlocks.SOURCE_BLOCK.get(), 
            new Item.Properties(), SourceBlock.FluidType.MILK));
}

