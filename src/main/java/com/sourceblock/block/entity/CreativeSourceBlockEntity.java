package com.sourceblock.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.sourceblock.block.entity.SourceBlockEntity.tryTransferFluid;


/**
 * 创造源方块实体 - 无限提供指定的流体
 */
public class CreativeSourceBlockEntity extends BlockEntity {
    private FluidStack storedFluid = FluidStack.EMPTY;

    public static final int CAPACITY = Integer.MAX_VALUE;

    public CreativeSourceBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.CREATIVE_SOURCE_BLOCK_ENTITY.get(), pos, blockState);
    }

    public void setFluid(FluidStack fluid) {
        this.storedFluid = fluid.copy();
        this.storedFluid.setAmount(CAPACITY);
        setChanged();
        // 同步到客户端
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public void clearFluid() {
        this.storedFluid = FluidStack.EMPTY;
        setChanged();
        // 同步到客户端
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public FluidStack getStoredFluid() {
        return storedFluid;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CreativeSourceBlockEntity blockEntity) {
        if (level.isClientSide) return;

        // 获取流体类型
        FluidStack fluidStack = blockEntity.storedFluid;
        if (fluidStack.isEmpty()) return;

        // 对每个面进行处理
        for (Direction direction : Direction.values()) {
            BlockPos neighborPos = pos.relative(direction);
            tryTransferFluid(level, neighborPos, direction.getOpposite(), fluidStack);
        }

        blockEntity.setChanged();
    }


    @Override
    protected void saveAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.saveAdditional(tag, registries);
        
        if (!storedFluid.isEmpty()) {
            tag.put("StoredFluid", storedFluid.save(registries));
        }
    }

    @Override
    protected void loadAdditional(@NotNull CompoundTag tag, HolderLookup.@NotNull Provider registries) {
        super.loadAdditional(tag, registries);
        
        if (tag.contains("StoredFluid")) {
            this.storedFluid = FluidStack.parse(registries, tag.getCompound("StoredFluid")).orElse(FluidStack.EMPTY);
        } else {
            this.storedFluid = FluidStack.EMPTY;
        }
    }

    // ========== 客户端同步 ==========

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.@NotNull Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public IFluidHandler createFluidHandler() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return 1;
            }

            @NotNull
            @Override
            public FluidStack getFluidInTank(int tank) {
                if (tank != 0 || storedFluid.isEmpty()) {
                    return FluidStack.EMPTY;
                }
                FluidStack result = storedFluid.copy();
                result.setAmount(CAPACITY);
                return result;
            }

            @Override
            public int getTankCapacity(int tank) {
                return CAPACITY;
            }

            @Override
            public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
                return false; // 不接受输入
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return 0; // 不接受输入
            }

            @NotNull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (resource.isEmpty() || storedFluid.isEmpty()) {
                    return FluidStack.EMPTY;
                }
                
                if (storedFluid.getFluid() == resource.getFluid()) {
                    return new FluidStack(storedFluid.getFluid(), resource.getAmount());
                }
                
                return FluidStack.EMPTY;
            }

            @NotNull
            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                if (maxDrain <= 0 || storedFluid.isEmpty()) {
                    return FluidStack.EMPTY;
                }
                
                return new FluidStack(storedFluid.getFluid(), maxDrain);
            }
        };
    }
}

