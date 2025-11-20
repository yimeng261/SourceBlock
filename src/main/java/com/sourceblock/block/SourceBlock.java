package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.SourceBlockEntity;
import com.sourceblock.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SourceBlock extends BaseEntityBlock {
    public static final MapCodec<SourceBlock> CODEC = simpleCodec(SourceBlock::new);
    public static final EnumProperty<FluidType> FLUID_TYPE = EnumProperty.create("fluid_type", FluidType.class);

    public SourceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FLUID_TYPE, FluidType.EMPTY));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLUID_TYPE);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hitResult) {
        FluidType currentType = state.getValue(FLUID_TYPE);

        // 用水桶右键空槽
        if (currentType == FluidType.EMPTY && stack.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FLUID_TYPE, FluidType.WATER));
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 用岩浆桶右键空槽
        if (currentType == FluidType.EMPTY && stack.is(Items.LAVA_BUCKET)) {
            if (!level.isClientSide) {
                level.setBlockAndUpdate(pos, state.setValue(FLUID_TYPE, FluidType.LAVA));
                if (!player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.BUCKET));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // 用空桶右键已填充的槽，可以获得流体
        if (stack.is(Items.BUCKET)) {
            if (currentType == FluidType.WATER) {
                if (!level.isClientSide && !player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.WATER_BUCKET));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else if (currentType == FluidType.LAVA) {
                if (!level.isClientSide && !player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.LAVA_BUCKET));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else if (currentType == FluidType.MILK) {
                if (!level.isClientSide && !player.isCreative()) {
                    stack.shrink(1);
                    player.addItem(new ItemStack(Items.MILK_BUCKET));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new SourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, 
            com.sourceblock.block.entity.ModBlockEntities.SOURCE_BLOCK_ENTITY.get(), 
            SourceBlockEntity::serverTick);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        FluidType fluidType = state.getValue(FLUID_TYPE);
        Item dropItem = switch (fluidType) {
            case WATER -> ModItems.WATER_SOURCE_BLOCK.get();
            case LAVA -> ModItems.LAVA_SOURCE_BLOCK.get();
            case MILK -> ModItems.MILK_SOURCE_BLOCK.get();
            default -> ModItems.EMPTY_SOURCE_BLOCK.get();
        };
        return Collections.singletonList(new ItemStack(dropItem));
    }

    public enum FluidType implements net.minecraft.util.StringRepresentable {
        EMPTY("empty"),
        WATER("water"),
        LAVA("lava"),
        MILK("milk");

        private final String name;

        FluidType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}

