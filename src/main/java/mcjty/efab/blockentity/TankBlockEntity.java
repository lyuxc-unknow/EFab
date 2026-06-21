package mcjty.efab.blockentity;

import mcjty.efab.config.EFabConfig;
import mcjty.efab.registry.ModBlockEntities;
import mcjty.efab.registry.ModBlocks;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class TankBlockEntity extends BlockEntity {

    private final int blockCapacity;
    private final EFabFluidTank tank;
    private FluidStack displayFluid = FluidStack.EMPTY;
    private int displayCapacity;
    private boolean rebuilding;

    public TankBlockEntity(BlockPos pos, BlockState state, int capacity) {
        super(ModBlockEntities.TANK.get(), pos, state);
        this.blockCapacity = capacityFor(state, capacity);
        this.tank = new EFabFluidTank(blockCapacity);
        this.displayCapacity = blockCapacity;
    }

    public IFluidHandler getFluidHandler(Direction direction) {
        return getTank();
    }

    public FluidTank getTank() {
        if (level == null) {
            return tank;
        }
        TankBlockEntity bottomTank = getBottomTank();
        bottomTank.rebuildMultiblock();
        return bottomTank.tank;
    }

    public FluidStack getDisplayFluid() {
        return displayFluid.isEmpty() ? tank.getFluid() : displayFluid;
    }

    public int getDisplayCapacity() {
        return displayCapacity > 0 ? displayCapacity : tank.getCapacity();
    }

    public boolean isBottomTank() {
        return level == null || !level.getBlockState(worldPosition.below()).is(getBlockState().getBlock());
    }

    public void rebuildMultiblock() {
        if (level == null || rebuilding) {
            return;
        }

        TankBlockEntity bottomTank = getBottomTank();
        if (bottomTank != this) {
            bottomTank.rebuildMultiblock();
            return;
        }

        rebuilding = true;
        try {
            List<TankBlockEntity> column = collectTankColumn();
            int capacity = combinedCapacity(column.size());
            if (tank.getCapacity() != capacity) {
                tank.setTankCapacity(capacity);
                setChanged();
            }
            if (tank.getFluidAmount() > capacity) {
                tank.drain(tank.getFluidAmount() - capacity, IFluidHandler.FluidAction.EXECUTE);
            }

            for (TankBlockEntity tankBlockEntity : column) {
                if (tankBlockEntity != this) {
                    mergeLocalFluid(tankBlockEntity);
                }
            }
            syncBottomDisplay();
        } finally {
            rebuilding = false;
        }
    }

    private TankBlockEntity getBottomTank() {
        TankBlockEntity bottomTank = this;
        BlockPos bottomPos = worldPosition;
        while (isSameTankBlock(bottomPos.below())) {
            BlockEntity blockEntity = level.getBlockEntity(bottomPos.below());
            if (!(blockEntity instanceof TankBlockEntity tankBlockEntity)) {
                break;
            }
            bottomTank = tankBlockEntity;
            bottomPos = bottomPos.below();
        }
        return bottomTank;
    }

    private List<TankBlockEntity> collectTankColumn() {
        List<TankBlockEntity> column = new ArrayList<>();
        BlockPos pos = worldPosition;
        while (isSameTankBlock(pos)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof TankBlockEntity tankBlockEntity)) {
                break;
            }
            column.add(tankBlockEntity);
            pos = pos.above();
        }
        return column;
    }

    private boolean isSameTankBlock(BlockPos pos) {
        return level != null && level.getBlockState(pos).is(getBlockState().getBlock());
    }

    private int combinedCapacity(int blocks) {
        long capacity = (long) blockCapacity * Math.max(1, blocks);
        return capacity > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) capacity;
    }

    private void mergeLocalFluid(TankBlockEntity other) {
        FluidStack fluid = other.tank.getFluid();
        if (fluid.isEmpty()) {
            return;
        }

        int accepted = tank.fill(fluid.copy(), IFluidHandler.FluidAction.EXECUTE);
        if (accepted > 0) {
            other.tank.drain(accepted, IFluidHandler.FluidAction.EXECUTE);
            other.setChanged();
        }
    }

    private void onTankContentsChanged() {
        setChanged();
        syncBottomDisplay();
    }

    private void syncBottomDisplay() {
        if (level == null || level.isClientSide) {
            return;
        }
        TankBlockEntity bottomTank = getBottomTank();
        FluidStack fluid = bottomTank.tank.getFluid();
        int capacity = bottomTank.tank.getCapacity();
        bottomTank.setDisplaySnapshot(fluid, capacity);
    }

    private void setDisplaySnapshot(FluidStack fluid, int capacity) {
        displayFluid = fluid.copy();
        displayCapacity = capacity;
        setChanged();
    }

    private static int capacityFor(BlockState state, int fallback) {
        if (state.is(ModBlocks.ADVANCED_TANK.get())) {
            return EFabConfig.ADVANCED_TANK_CAPACITY.get();
        }
        if (state.is(ModBlocks.TANK.get())) {
            return EFabConfig.TANK_CAPACITY.get();
        }
        return fallback;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Capacity", tank.getCapacity());
        CompoundTag tankTag = new CompoundTag();
        tank.writeToNBT(registries, tankTag);
        tag.put("Tank", tankTag);
        tag.putInt("DisplayCapacity", getDisplayCapacity());
        tag.put("DisplayFluid", getDisplayFluid().saveOptional(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Capacity")) {
            tank.setTankCapacity(tag.getInt("Capacity"));
        }
        tank.readFromNBT(registries, tag.getCompound("Tank"));
        displayCapacity = tag.getInt("DisplayCapacity");
        displayFluid = FluidStack.parseOptional(registries, tag.getCompound("DisplayFluid"));
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private class EFabFluidTank extends FluidTank {

        private EFabFluidTank(int capacity) {
            super(capacity);
        }

        private void setTankCapacity(int capacity) {
            this.capacity = capacity;
        }

        @Override
        protected void onContentsChanged() {
            onTankContentsChanged();
        }
    }
}
