package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.block.entity.CreativeSourceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 创造源方块 - 可以复制任何流体
 * 玩家手持流体桶或流体容器右键，方块会记住该流体并无限输出
 */
public class CreativeSourceBlock extends BaseEntityBlock {
    public static final MapCodec<CreativeSourceBlock> CODEC = simpleCodec(CreativeSourceBlock::new);

    public CreativeSourceBlock(Properties properties) {
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
        
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof CreativeSourceBlockEntity entity) {
            // 检查是否是桶
            if (stack.getItem() instanceof BucketItem bucketItem) {
                Fluid fluid = bucketItem.content;
                if (fluid != Fluids.EMPTY) {
                    entity.setFluid(new FluidStack(fluid, 1000));
                    player.displayClientMessage(
                        net.minecraft.network.chat.Component.translatable(
                            "message.sourceblock.fluid_set",
                            fluid.getFluidType().getDescription()
                        ),
                        true
                    );
                    return ItemInteractionResult.SUCCESS;
                }
            }
            
            // 检查是否是流体容器（如储罐等）
            var fluidHandler = stack.getCapability(Capabilities.FluidHandler.ITEM);
            if (fluidHandler != null) {
                if (fluidHandler.getTanks() > 0) {
                    FluidStack fluidInContainer = fluidHandler.getFluidInTank(0);
                    if (!fluidInContainer.isEmpty()) {
                        entity.setFluid(fluidInContainer.copy());
                        player.displayClientMessage(
                            net.minecraft.network.chat.Component.translatable(
                                "message.sourceblock.fluid_set",
                                fluidInContainer.getHoverName()
                            ),
                            true
                        );
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            
            // 空手Shift右键清空
            if (stack.isEmpty() && player.isShiftKeyDown()) {
                entity.clearFluid();
                player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.sourceblock.fluid_cleared"),
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
        return new CreativeSourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, 
            ModBlockEntities.CREATIVE_SOURCE_BLOCK_ENTITY.get(),
            CreativeSourceBlockEntity::serverTick);
    }

    /**
     * 根据存储的流体类型返回不同的光照等级
     */
    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        return 15;
    }
}

