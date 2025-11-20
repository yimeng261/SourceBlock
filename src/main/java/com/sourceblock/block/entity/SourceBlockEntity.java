package com.sourceblock.block.entity;

import com.sourceblock.block.SourceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 源方块实体
 * 功能说明：
 * - 空源方块（EMPTY）：销毁所有输入的流体、气体和能量（虚空功能）
 * - 水源方块（WATER）：无限提供水，容量为Integer.MAX_VALUE
 * - 岩浆源方块（LAVA）：无限提供岩浆，容量为Integer.MAX_VALUE
 * - 牛奶源方块（MILK）：无限提供牛奶（如果安装了机械动力等模组）
 * 输出机制：
 * - 每20tick尝试向周围6个面输出流体
 * - 成功输出后该面切换为每tick输出
 * - 失败后恢复为每20tick输出
 * - 每次输出Integer.MAX_VALUE的流体量（瞬间填满）
 */
public class SourceBlockEntity extends BlockEntity implements IFluidHandler, IEnergyStorage {
    // 每个面的计时器
    private final Map<Direction, Integer> tickCounters = new HashMap<>();
    private final Map<Direction, Boolean> fastMode = new HashMap<>();
    
    private static final int SLOW_INTERVAL = 20;
    private static final int FAST_INTERVAL = 1;
    private static final int TRANSFER_AMOUNT = Integer.MAX_VALUE;
    public static final int CAPACITY = Integer.MAX_VALUE;
    
    // 缓存牛奶流体类型（如果存在）
    private static Fluid cachedMilkFluid = null;
    private static boolean milkFluidChecked = false;

    public SourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.SOURCE_BLOCK_ENTITY.get(), pos, blockState);
        
        // 初始化所有面的计时器和模式
        for (Direction direction : Direction.values()) {
            tickCounters.put(direction, 0);
            fastMode.put(direction, false);
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
        for (Direction direction : Direction.values()) {
            int currentTick = blockEntity.tickCounters.get(direction);
            boolean isFastMode = blockEntity.fastMode.get(direction);
            
            // 增加计时器
            currentTick++;
            
            // 根据模式检查是否应该尝试输出
            int interval = isFastMode ? FAST_INTERVAL : SLOW_INTERVAL;
            if (currentTick >= interval) {
                currentTick = 0;
                
                // 尝试向该面输出流体
                BlockPos neighborPos = pos.relative(direction);
                boolean success = tryTransferFluid(level, neighborPos, direction.getOpposite(), fluidStack);
                
                // 根据输出结果更新模式
                if (success) {
                    // 成功输出，切换到快速模式
                    blockEntity.fastMode.put(direction, true);
                } else {
                    // 输出失败，切换到慢速模式
                    blockEntity.fastMode.put(direction, false);
                }
            }
            
            blockEntity.tickCounters.put(direction, currentTick);
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
     * 支持的流体ID（按优先级）：
     * 1. create:milk (机械动力)
     * 2. createbigcannons:milk (机械动力大炮)
     * 3. create_confectionery:milk (机械动力糖果)
     * 4. 其他包含"milk"的流体
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
            "create:milk",                    // 机械动力
            "createbigcannons:milk",          // 机械动力大炮
            "create_confectionery:milk",      // 机械动力糖果
            "ad_astra:milk",                  // Ad Astra
            "thermal:milk",                   // 热力系列
            "productivebees:milk"             // 生产蜜蜂
        };
        
        for (String fluidId : milkFluidIds) {
            Fluid fluid = BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluidId));
            if (fluid != Fluids.EMPTY) {
                cachedMilkFluid = fluid;
                milkFluidChecked = true;
                return new FluidStack(fluid, TRANSFER_AMOUNT);
            }
        }
        
        // 如果上面的都没找到，尝试查找任何包含"milk"的流体
        for (var entry : BuiltInRegistries.FLUID.entrySet()) {
            String id = entry.getKey().location().toString();
            if (id.contains("milk") && !id.equals("minecraft:milk")) {
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

    private static boolean tryTransferFluid(Level level, BlockPos pos, Direction direction, FluidStack fluidStack) {
        // 获取目标位置的流体处理能力
        IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, direction);
        
        if (handler != null) {
            // 尝试填充流体
            int filled = handler.fill(fluidStack, IFluidHandler.FluidAction.EXECUTE);
            return filled > 0;
        }
        
        return false;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        
        // 保存每个面的状态
        CompoundTag facesTag = new CompoundTag();
        for (Direction direction : Direction.values()) {
            CompoundTag faceTag = new CompoundTag();
            faceTag.putInt("tick", tickCounters.get(direction));
            faceTag.putBoolean("fast", fastMode.get(direction));
            facesTag.put(direction.getName(), faceTag);
        }
        tag.put("faces", facesTag);
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        
        // 加载每个面的状态
        if (tag.contains("faces")) {
            CompoundTag facesTag = tag.getCompound("faces");
            for (Direction direction : Direction.values()) {
                if (facesTag.contains(direction.getName())) {
                    CompoundTag faceTag = facesTag.getCompound(direction.getName());
                    tickCounters.put(direction, faceTag.getInt("tick"));
                    fastMode.put(direction, faceTag.getBoolean("fast"));
                }
            }
        }
    }

    // ========== IFluidHandler 实现 ==========

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
            case MILK -> getMilkFluidStack(); // 尝试获取模组注册的牛奶流体
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
        
        // 其他类型的源方块不接受流体输入
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

    // ========== IEnergyStorage 实现（能量销毁/生成）==========

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
        // 源方块不提供能量输出（可以根据需要修改）
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

