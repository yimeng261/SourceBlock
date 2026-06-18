package com.sourceblock.block;

import com.mojang.serialization.MapCodec;
import com.sourceblock.block.entity.ItemSourceBlockEntity;
import com.sourceblock.block.entity.ModBlockEntities;
import com.sourceblock.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
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

public class ItemSourceBlock extends BaseEntityBlock {
    public static final MapCodec<ItemSourceBlock> CODEC = simpleCodec(ItemSourceBlock::new);
    public static final EnumProperty<ItemType> ITEM_TYPE = EnumProperty.create("item_type", ItemType.class);

    public ItemSourceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ITEM_TYPE, ItemType.EMPTY));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ITEM_TYPE);
    }

    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, 
                                                       @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, 
                                                       @NotNull BlockHitResult hit) {
        ItemType currentType = state.getValue(ITEM_TYPE);

        // 空手右键已填充的槽，可以获得物品
        if (stack.isEmpty()) {
            if (currentType == ItemType.COBBLESTONE) {
                if (!level.isClientSide && !player.isCreative()) {
                    player.addItem(new ItemStack(Items.COBBLESTONE));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else if (currentType == ItemType.STONE) {
                if (!level.isClientSide && !player.isCreative()) {
                    player.addItem(new ItemStack(Items.STONE));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else if (currentType == ItemType.SMOOTH_STONE) {
                if (!level.isClientSide && !player.isCreative()) {
                    player.addItem(new ItemStack(Items.SMOOTH_STONE));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            } else if (currentType == ItemType.OBSIDIAN) {
                if (!level.isClientSide && !player.isCreative()) {
                    player.addItem(new ItemStack(Items.OBSIDIAN));
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected @NotNull RenderShape getRenderShape(@NotNull BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new ItemSourceBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, 
            ModBlockEntities.ITEM_SOURCE_BLOCK_ENTITY.get(),
            ItemSourceBlockEntity::serverTick);
    }

    @Override
    protected @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return Collections.singletonList(new ItemStack(getItem(state)));
    }

    private Item getItem(BlockState state) {
        ItemType itemType = state.getValue(ITEM_TYPE);
        return switch (itemType) {
            case COBBLESTONE -> ModItems.COBBLESTONE_SOURCE_BLOCK.get();
            case STONE -> ModItems.STONE_SOURCE_BLOCK.get();
            case SMOOTH_STONE -> ModItems.SMOOTH_STONE_SOURCE_BLOCK.get();
            case OBSIDIAN -> ModItems.OBSIDIAN_SOURCE_BLOCK.get();
            default -> ModItems.EMPTY_ITEM_SOURCE_BLOCK.get();
        };
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(BlockState state, net.minecraft.world.phys.HitResult target, 
                                                net.minecraft.world.level.LevelReader level, 
                                                @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(getItem(state));
    }

    public enum ItemType implements StringRepresentable {
        EMPTY("empty"),
        COBBLESTONE("cobblestone"),
        STONE("stone"),
        SMOOTH_STONE("smooth_stone"),
        OBSIDIAN("obsidian");

        private final String name;

        ItemType(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}

