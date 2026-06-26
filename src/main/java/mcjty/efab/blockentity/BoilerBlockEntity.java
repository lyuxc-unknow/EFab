package mcjty.efab.blockentity;

import mcjty.efab.config.EFabConfig;
import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class BoilerBlockEntity extends BlockEntity implements ServerTickingBlockEntity {

    private double temperature = EFabConfig.AMBIENT_BOILER_TEMPERATURE.get();

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER.get(), pos, state);
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }

        double previous = temperature;
        if (hasHeatBelow()) {
            temperature = Math.min(EFabConfig.MAX_BOILER_TEMPERATURE.get(),
                    temperature + EFabConfig.BOILER_RISE_TEMPERATURE.get());
        } else {
            temperature = Math.max(EFabConfig.AMBIENT_BOILER_TEMPERATURE.get(),
                    temperature - EFabConfig.BOILER_COOL_TEMPERATURE.get());
        }

        if (temperature != previous) {
            setChanged();
        }
    }

    public double getTemperature() {
        return temperature;
    }

    public boolean canMakeSteam() {
        return temperature >= 100.0;
    }

    private boolean hasHeatBelow() {
        return level != null && isHeatSource(level.getBlockState(worldPosition.below()));
    }

    public static boolean isHeatSource(BlockState state) {
        return state.is(Blocks.MAGMA_BLOCK)
                || state.is(Blocks.LAVA)
                || state.is(Blocks.FIRE)
                || state.is(Blocks.SOUL_FIRE)
                || state.is(Blocks.CAMPFIRE)
                || state.is(Blocks.SOUL_CAMPFIRE)
                || state.getFluidState().is(Fluids.LAVA);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putDouble("Temperature", temperature);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        temperature = tag.contains("Temperature")
                ? tag.getDouble("Temperature")
                : EFabConfig.AMBIENT_BOILER_TEMPERATURE.get();
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
