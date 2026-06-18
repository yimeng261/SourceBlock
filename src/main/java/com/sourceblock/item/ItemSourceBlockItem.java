package com.sourceblock.item;

import com.sourceblock.block.ItemSourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemSourceBlockItem extends BlockItem {
    private final ItemSourceBlock.ItemType itemType;

    public ItemSourceBlockItem(Block block, Properties properties, ItemSourceBlock.ItemType itemType) {
        super(block, properties);
        this.itemType = itemType;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, @NotNull Level level, @Nullable Player player,
                                                 @NotNull ItemStack stack, @NotNull BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        
        // 设置方块的物品类型
        if (!level.isClientSide) {
            BlockState newState = state.setValue(ItemSourceBlock.ITEM_TYPE, this.itemType);
            level.setBlockAndUpdate(pos, newState);
        }
        
        return result;
    }

    @Nullable
    @Override
    protected BlockState getPlacementState(@NotNull BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        if (state != null) {
            state = state.setValue(ItemSourceBlock.ITEM_TYPE, this.itemType);
        }
        return state;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        // 根据物品类型返回不同的名称
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        // 为每种物品类型返回不同的翻译键
        return switch (this.itemType) {
            case EMPTY -> "item.sourceblock.empty_item_source_block";
            case COBBLESTONE -> "item.sourceblock.cobblestone_source_block";
            case STONE -> "item.sourceblock.stone_source_block";
            case SMOOTH_STONE -> "item.sourceblock.smooth_stone_source_block";
            case OBSIDIAN -> "item.sourceblock.obsidian_source_block";
        };
    }

    public ItemSourceBlock.ItemType getItemType() {
        return this.itemType;
    }
}

