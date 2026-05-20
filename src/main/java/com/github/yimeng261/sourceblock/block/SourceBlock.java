package com.github.yimeng261.sourceblock.block;

import com.github.yimeng261.sourceblock.block.entity.ModBlockEntities;
import com.github.yimeng261.sourceblock.block.entity.SourceBlockEntity;
import com.github.yimeng261.sourceblock.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
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
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class SourceBlock extends BaseEntityBlock {
    public static final EnumProperty<FluidType> FLUID_TYPE = EnumProperty.create("fluid_type", FluidType.class);

    public SourceBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FLUID_TYPE, FluidType.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FLUID_TYPE);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        FluidType currentType = state.getValue(FLUID_TYPE);

        FluidType containerFluidType = getSupportedContainerFluidType(stack);
        if (currentType == FluidType.EMPTY && containerFluidType != null) {
            if (!level.isClientSide) {
                ItemStack result = drainOneContainer(stack, containerFluidType);
                if (!result.isEmpty()) {
                    level.setBlockAndUpdate(pos, state.setValue(FLUID_TYPE, containerFluidType));
                    if (!player.isCreative()) {
                        replaceHeldContainer(player, hand, stack, result);
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        if (currentType != FluidType.EMPTY && hasFluidContainerCapability(stack) && level.getBlockEntity(pos) instanceof SourceBlockEntity entity) {
            boolean handled = level.isClientSide || entity.getCapability(ForgeCapabilities.FLUID_HANDLER, hit.getDirection())
                .map(handler -> FluidUtil.interactWithFluidHandler(player, hand, handler))
                .orElse(false);
            if (handled) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }

    @Nullable
    private static FluidType getSupportedContainerFluidType(ItemStack stack) {
        if (stack.isEmpty()) {
            return null;
        }

        return FluidUtil.getFluidContained(stack)
            .map(fluidStack -> {
                if (fluidStack.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
                    return FluidType.WATER;
                }
                if (fluidStack.getFluid() == net.minecraft.world.level.material.Fluids.LAVA) {
                    return FluidType.LAVA;
                }
                return null;
            })
            .orElse(null);
    }

    private static boolean hasFluidContainerCapability(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return stack.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM).isPresent();
    }

    private static ItemStack drainOneContainer(ItemStack stack, FluidType fluidType) {
        ItemStack singleContainer = ItemHandlerHelper.copyStackWithSize(stack, 1);
        return singleContainer.getCapability(ForgeCapabilities.FLUID_HANDLER_ITEM)
            .map(handler -> {
                FluidStack requested = switch (fluidType) {
                    case WATER -> new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000);
                    case LAVA -> new FluidStack(net.minecraft.world.level.material.Fluids.LAVA, 1000);
                    default -> FluidStack.EMPTY;
                };

                if (requested.isEmpty() || !handler.drain(requested, IFluidHandler.FluidAction.SIMULATE).isFluidStackIdentical(requested)) {
                    return ItemStack.EMPTY;
                }

                handler.drain(requested, IFluidHandler.FluidAction.EXECUTE);
                return handler.getContainer();
            })
            .orElse(ItemStack.EMPTY);
    }

    private static void replaceHeldContainer(Player player, InteractionHand hand, ItemStack original, ItemStack result) {
        if (original.getCount() == 1) {
            player.setItemInHand(hand, result);
            return;
        }

        original.shrink(1);
        if (!player.addItem(result)) {
            player.drop(result, false);
        }
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
            ModBlockEntities.SOURCE_BLOCK_ENTITY.get(),
            SourceBlockEntity::serverTick);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(BlockState state, LootParams.@NotNull Builder builder) {
        return Collections.singletonList(new ItemStack(getItem(state)));
    }

    private Item getItem(BlockState state) {
        FluidType fluidType = state.getValue(FLUID_TYPE);
        return switch (fluidType) {
            case WATER -> ModItems.WATER_SOURCE_BLOCK.get();
            case LAVA -> ModItems.LAVA_SOURCE_BLOCK.get();
            case MILK -> ModItems.MILK_SOURCE_BLOCK.get();
            default -> ModItems.EMPTY_SOURCE_BLOCK.get();
        };
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, net.minecraft.world.phys.HitResult target, 
                                                @NotNull net.minecraft.world.level.BlockGetter level, 
                                                @NotNull BlockPos pos, @NotNull Player player) {
        return new ItemStack(getItem(state));
    }

    /**
     * 根据源方块类型返回不同的光照等级
     * 岩浆源方块：15（最高亮度）
     * 水源方块：8（中等亮度）
     * 牛奶源方块：5（较低亮度）
     * 空源方块：0（无亮度）
     */
    @Override
    public int getLightEmission(BlockState state, net.minecraft.world.level.BlockGetter level, BlockPos pos) {
        FluidType fluidType = state.getValue(FLUID_TYPE);
        return switch (fluidType) {
            case LAVA -> 15;  // 岩浆源方块最亮，等同于萤石
            case WATER -> 8;  // 水源方块中等亮度
            case MILK -> 5;   // 牛奶源方块较低亮度
            case EMPTY -> 0;  // 空源方块无亮度
        };
    }

    public enum FluidType implements StringRepresentable {
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
