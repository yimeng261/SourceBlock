package com.github.yimeng261.sourceblock.block.entity;

import com.github.yimeng261.sourceblock.block.SourceBlock;
import com.github.yimeng261.sourceblock.block.entity.compat.MekanismGasHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * 源方块实体
 * 功能说明：
 * - 空源方块（EMPTY）：销毁所有输入的流体、气体、能量和物品（虚空功能）
 * - 水源方块（WATER）：无限提供水，容量为Integer.MAX_VALUE
 * - 岩浆源方块（LAVA）：无限提供岩浆，容量为Integer.MAX_VALUE
 * - 牛奶源方块（MILK）：无限提供牛奶（如果安装了机械动力等模组）
 * 输出机制：
 * - 每20tick尝试向周围6个面输出流体
 * - 成功输出后该面切换为每tick输出
 * - 失败后恢复为每20tick输出
 * - 每次输出Integer.MAX_VALUE的流体量（瞬间填满）
 */
public class SourceBlockEntity extends BlockEntity {
    // 每个面的计时器
    private int tickCounter=0;
    private final Map<Direction, Boolean> fastMode = new HashMap<>();
    
    private static final int SLOW_INTERVAL = 20;
    private static final int FAST_INTERVAL = 1;
    private static final int TRANSFER_AMOUNT = Integer.MAX_VALUE;
    public static final int CAPACITY = Integer.MAX_VALUE;
    
    // 缓存牛奶流体类型（如果存在）
    private static Fluid cachedMilkFluid = null;
    private static boolean milkFluidChecked = false;

    // Capabilities
    private final LazyOptional<IFluidHandler> fluidHandler = LazyOptional.of(FluidHandlerImpl::new);
    private final LazyOptional<IEnergyStorage> energyStorage = LazyOptional.of(EnergyStorageImpl::new);
    private final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(ItemHandlerImpl::new);
    private LazyOptional<?> mekanismGasHandler = null;

    public SourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOURCE_BLOCK_ENTITY.get(), pos, blockState);
        
        // 初始化所有面的计时器和模式
        for (Direction direction : Direction.values()) {
            fastMode.put(direction, true);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SourceBlockEntity blockEntity) {
        if (level.isClientSide) return;

        SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
        if (fluidType == SourceBlock.FluidType.EMPTY) return;

        // 获取流体类型
        FluidStack fluidStack = getFluidStackFromType(fluidType);
        if (fluidStack.isEmpty()) return;

        // 对每个面进行处理
        blockEntity.tickCounter++;
        for (Direction direction : Direction.values()) {
            boolean isFastMode = blockEntity.fastMode.get(direction);
            int interval = isFastMode ? FAST_INTERVAL : SLOW_INTERVAL;

            if (blockEntity.tickCounter % interval == 0) {
                BlockPos neighborPos = pos.relative(direction);
                boolean success = tryTransferFluid(level, neighborPos, direction.getOpposite(), fluidStack);
                blockEntity.fastMode.put(direction, success);
            }
        }

        blockEntity.setChanged();
    }

    private static FluidStack getFluidStackFromType(SourceBlock.FluidType type) {
        return switch (type) {
            case WATER -> new FluidStack(Fluids.WATER, TRANSFER_AMOUNT);
            case LAVA -> new FluidStack(Fluids.LAVA, TRANSFER_AMOUNT);
            case MILK -> getMilkFluidStack();
            default -> FluidStack.EMPTY;
        };
    }
    
    /**
     * 尝试获取牛奶流体。
     */
    private static FluidStack getMilkFluidStack() {
        // 如果已经检查过且没找到，直接返回空
        if (milkFluidChecked && cachedMilkFluid == null) {
            return FluidStack.EMPTY;
        }
        
        // 如果已经缓存了牛奶流体，直接使用
        if (cachedMilkFluid != null && cachedMilkFluid != Fluids.EMPTY) {
            return new FluidStack(cachedMilkFluid, TRANSFER_AMOUNT);
        }
        
        // 尝试按优先级查找牛奶流体
        String[] milkFluidIds = {
            "minecraft:milk",                 // Forge 原生牛奶流体
            "create:milk",                    // 机械动力
            "ad_astra:milk",                  // Ad Astra
            "thermal:milk",                   // 热力系列
            "productivebees:milk"             // 生产蜜蜂
        };
        
        for (String fluidId : milkFluidIds) {
            ResourceLocation location = ResourceLocation.tryParse(fluidId);
            if (location != null) {
                Fluid fluid = BuiltInRegistries.FLUID.get(location);
                if (fluid != Fluids.EMPTY) {
                    cachedMilkFluid = fluid;
                    milkFluidChecked = true;
                    return new FluidStack(fluid, TRANSFER_AMOUNT);
                }
            }
        }

        try {
            Fluid forgeMilk = ForgeMod.MILK.get();
            if (forgeMilk != null && forgeMilk != Fluids.EMPTY) {
                cachedMilkFluid = forgeMilk;
                milkFluidChecked = true;
                return new FluidStack(forgeMilk, TRANSFER_AMOUNT);
            }
        } catch (Exception ignored) {
            // Forge milk is optional and only exists if a mod enables it.
        }
        

        for (var entry : BuiltInRegistries.FLUID.entrySet()) {
            String id = entry.getKey().location().toString();
            String name = id.split(":")[1];
            if (name.equals("milk")) {
                Fluid fluid = entry.getValue();
                if (fluid != null && fluid != Fluids.EMPTY) {
                    cachedMilkFluid = fluid;
                    milkFluidChecked = true;
                    return new FluidStack(fluid, TRANSFER_AMOUNT);
                }
            }
        }
        
        // 没有找到任何牛奶流体，标记为已检查
        milkFluidChecked = true;
        cachedMilkFluid = Fluids.EMPTY;
        return FluidStack.EMPTY;
    }

    static boolean tryTransferFluid(Level level, BlockPos pos, Direction direction, FluidStack fluidStack) {
        // 获取目标位置的流体处理能力
        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            LazyOptional<IFluidHandler> capability = be.getCapability(ForgeCapabilities.FLUID_HANDLER, direction);
            return capability.map(handler -> {
                int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
                return filled > 0;
            }).orElse(false);
        }
        
        return false;
    }


    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.FLUID_HANDLER) {
            return fluidHandler.cast();
        }
        if (cap == ForgeCapabilities.ENERGY) {
            return energyStorage.cast();
        }
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemHandler.cast();
        }
        
        // Mekanism气体能力支持
        if (ModList.get().isLoaded("mekanism")) {
            try {
                // 尝试获取Mekanism的气体能力
                String capName = cap.getName();
                if (capName != null && (capName.contains("gas_handler") || capName.contains("chemical"))) {
                    if (mekanismGasHandler == null) {
                        mekanismGasHandler = LazyOptional.of(() -> new MekanismGasHandler(this));
                    }
                    return mekanismGasHandler.cast();
                }
            } catch (Exception e) {
                // 忽略异常，继续使用默认能力
            }
        }
        
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        fluidHandler.invalidate();
        energyStorage.invalidate();
        itemHandler.invalidate();
        if (mekanismGasHandler != null) {
            mekanismGasHandler.invalidate();
        }
    }

    // ========== IFluidHandler 实现 ==========

    private class FluidHandlerImpl implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            if (tank != 0 || level == null) {
                return FluidStack.EMPTY;
            }
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 返回"无限"容量的流体
            return switch (fluidType) {
                case WATER -> new FluidStack(Fluids.WATER, CAPACITY);
                case LAVA -> new FluidStack(Fluids.LAVA, CAPACITY);
                case MILK -> getMilkFluidStack();
                default -> FluidStack.EMPTY;
            };
        }

        @Override
        public int getTankCapacity(int tank) {
            return CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            if (level == null) return false;
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块接受任何流体（用于销毁）
            return fluidType == SourceBlock.FluidType.EMPTY;
        }

        @Override
        public int fill(FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || level == null) {
                return 0;
            }
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块销毁所有输入的流体
            if (fluidType == SourceBlock.FluidType.EMPTY) {
                // 返回全部接受（实际上是销毁）
                return resource.getAmount();
            }
            
            // 其他类型的源方块不接受流体输入
            return 0;
        }

        @NotNull
        @Override
        public FluidStack drain(FluidStack resource, @NotNull FluidAction action) {
            if (resource.isEmpty() || level == null) {
                return FluidStack.EMPTY;
            }
            
            FluidStack stored = getFluidInTank(0);
            
            // 检查请求的流体是否匹配
            if (!stored.isEmpty() && stored.getFluid() == resource.getFluid()) {
                // 返回请求的量（无限供应）
                return new FluidStack(resource.getFluid(), resource.getAmount());
            }
            
            return FluidStack.EMPTY;
        }

        @NotNull
        @Override
        public FluidStack drain(int maxDrain, @NotNull FluidAction action) {
            if (maxDrain <= 0 || level == null) {
                return FluidStack.EMPTY;
            }
            
            FluidStack stored = getFluidInTank(0);
            if (stored.isEmpty()) {
                return FluidStack.EMPTY;
            }
            
            // 返回请求的量（无限供应）
            return new FluidStack(stored.getFluid(), maxDrain);
        }
    }

    // ========== IEnergyStorage 实现（能量销毁）==========

    private class EnergyStorageImpl implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (level == null) return 0;
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块销毁所有输入的能量
            if (fluidType == SourceBlock.FluidType.EMPTY) {
                // 返回全部接受（实际上是销毁）
                return maxReceive;
            }
            
            // 其他类型不接受能量输入
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            // 源方块不提供能量输出
            return 0;
        }

        @Override
        public int getEnergyStored() {
            // 源方块不存储能量
            return 0;
        }

        @Override
        public int getMaxEnergyStored() {
            if (level == null) return 0;
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块显示无限容量用于销毁能量
            if (fluidType == SourceBlock.FluidType.EMPTY) {
                return Integer.MAX_VALUE;
            }
            
            return 0;
        }

        @Override
        public boolean canExtract() {
            // 不提供能量输出
            return false;
        }

        @Override
        public boolean canReceive() {
            if (level == null) return false;
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块可以接受能量（用于销毁）
            return fluidType == SourceBlock.FluidType.EMPTY;
        }
    }

    // ========== IItemHandler 实现（物品销毁）==========

    private class ItemHandlerImpl implements IItemHandler {
        @Override
        public int getSlots() {
            return 1;
        }

        @NotNull
        @Override
        public ItemStack getStackInSlot(int slot) {
            // 不存储物品，总是返回空
            return ItemStack.EMPTY;
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || level == null) {
                return stack;
            }
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块销毁所有输入的物品
            if (fluidType == SourceBlock.FluidType.EMPTY) {
                return ItemStack.EMPTY;
            }
            
            return stack;
        }

        @NotNull
        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (level == null) return false;
            
            BlockState state = getBlockState();
            SourceBlock.FluidType fluidType = state.getValue(SourceBlock.FLUID_TYPE);
            
            // 空源方块接受任何物品（用于销毁）
            return fluidType == SourceBlock.FluidType.EMPTY;
        }
    }
}
