package com.sourceblock.item;

import com.sourceblock.block.SourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SourceBlockItem extends BlockItem {
    private final SourceBlock.FluidType fluidType;

    public SourceBlockItem(Block block, Properties properties, SourceBlock.FluidType fluidType) {
        super(block, properties);
        this.fluidType = fluidType;
    }

    @Override
    protected boolean updateCustomBlockEntityTag(@NotNull BlockPos pos, @NotNull Level level, @Nullable Player player,
                                                 @NotNull ItemStack stack, @NotNull BlockState state) {
        boolean result = super.updateCustomBlockEntityTag(pos, level, player, stack, state);
        
        // 设置方块的流体类型
        if (!level.isClientSide) {
            BlockState newState = state.setValue(SourceBlock.FLUID_TYPE, this.fluidType);
            level.setBlockAndUpdate(pos, newState);
        }
        
        return result;
    }

    @Override
    public @NotNull Component getName(@NotNull ItemStack stack) {
        // 根据流体类型返回不同的名称
        return Component.translatable(this.getDescriptionId(stack));
    }

    @Override
    public @NotNull String getDescriptionId(@NotNull ItemStack stack) {
        // 为每种流体类型返回不同的翻译键
        return switch (this.fluidType) {
            case EMPTY -> "item.sourceblock.empty_source_block";
            case WATER -> "item.sourceblock.water_source_block";
            case LAVA -> "item.sourceblock.lava_source_block";
            case MILK -> "item.sourceblock.milk_source_block";
        };
    }

    public SourceBlock.FluidType getFluidType() {
        return this.fluidType;
    }
}

