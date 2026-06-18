package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.CreativeItemSourceBlockEntity;
import com.sourceblock.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 创造物品源方块 - 可以复制任何物品
 * 玩家手持物品右键，方块会记住该物品并无限输出
 */
public class CreativeItemSourceBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeItemSourceBlock> CODEC = simpleCodec(CreativeItemSourceBlock::new);

    public CreativeItemSourceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, 
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, 
                                                       @NotNull BlockHitResult hit) {
        
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CreativeItemSourceBlockEntity entity) {
            // 空手Shift右键清空
            if (stack.isEmpty() && player.isShiftKeyDown()) {
                entity.clearItem();
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.sourceblock.item_cleared"),
                    true
                );
                return ItemInteractionResult.SUCCESS;
            }
            
            // 手持物品右键设置
            if (!stack.isEmpty() && !player.isShiftKeyDown()) {
                entity.setItem(stack.copy());
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(
                        "message.sourceblock.item_set",
                        stack.getHoverName()
                    ),
                    true
                );
                return ItemInteractionResult.SUCCESS;
            }
        }
        
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new CreativeItemSourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, 
            ModBlockEntities.CREATIVE_ITEM_SOURCE_BLOCK_ENTITY.get(),
            CreativeItemSourceBlockEntity::serverTick);
    }
}

