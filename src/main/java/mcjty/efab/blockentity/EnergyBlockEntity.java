package mcjty.efab.blockentity;

import mcjty.efab.block.FeControlBlock;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EnergyBlockEntity extends BlockEntity implements ServerTickingBlockEntity {

    private final EFabEnergyStorage energy;
    private final int flow;
    private int sparkTicks;

    public EnergyBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ENERGY_STORAGE.get(), pos, state);
        int capacity = capacityFor(state.getBlock());
        this.flow = flowFor(state.getBlock());
        this.energy = new EFabEnergyStorage(capacity, flow, this::setChanged);
    }

    private static int capacityFor(Block block) {
        if (block == ModBlocks.FE_CONTROL.get()) {
            return EFabConfig.FE_CONTROL_CAPACITY.get();
        }
        if (block == ModBlocks.ADVANCED_FE_STORAGE.get()) {
            return EFabConfig.ADVANCED_FE_STORAGE_CAPACITY.get();
        }
        return EFabConfig.FE_STORAGE_CAPACITY.get();
    }

    private static int flowFor(Block block) {
        if (block == ModBlocks.FE_CONTROL.get()) {
            return EFabConfig.FE_CONTROL_FLOW.get();
        }
        if (block == ModBlocks.ADVANCED_FE_STORAGE.get()) {
            return EFabConfig.ADVANCED_FE_STORAGE_FLOW.get();
        }
        return EFabConfig.FE_STORAGE_FLOW.get();
    }

    public IEnergyStorage getEnergy(Direction direction) {
        return energy;
    }

    /** Per-tick FE contribution to crafting (the transfer limit of this block). */
    public int getFlow() {
        return flow;
    }

    public boolean hasEnergyStored() {
        return energy.getEnergyStored() > 0;
    }

    /** Extract ignoring the per-tick flow limit (used by the Power Optimizer). */
    public int extractIgnoringLimit(int amount, boolean simulate) {
        return energy.extractIgnoringLimit(amount, simulate);
    }

    public void showSparks(int ticks) {
        if (level == null || level.isClientSide || getBlockState().getBlock() != ModBlocks.FE_CONTROL.get() || !hasEnergyStored()) {
            return;
        }
        sparkTicks = Math.max(sparkTicks, ticks);
        setSparksState(true);
    }

    @Override
    public void serverTick() {
        if (!hasEnergyStored()) {
            sparkTicks = 0;
            setSparksState(false);
            return;
        }
        if (sparkTicks > 0) {
            sparkTicks--;
            if (sparkTicks == 0) {
                setSparksState(false);
            }
            return;
        }
        if (getBlockState().hasProperty(FeControlBlock.SPARKS) && getBlockState().getValue(FeControlBlock.SPARKS)) {
            setSparksState(false);
        }
    }

    private void setSparksState(boolean sparks) {
        BlockState state = getBlockState();
        if (level != null && state.hasProperty(FeControlBlock.SPARKS) && state.getValue(FeControlBlock.SPARKS) != sparks) {
            level.setBlock(worldPosition, state.setValue(FeControlBlock.SPARKS, sparks), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Energy", energy.getEnergyStored());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy.setEnergy(tag.getInt("Energy"));
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
}
