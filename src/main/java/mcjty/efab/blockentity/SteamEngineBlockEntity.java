package mcjty.efab.blockentity;

import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Steam engine. It has no inventory or gameplay logic of its own; nearby fabricators
 * ({@link AbstractCraftingBlockEntity}) call {@link #keepWorking()} every tick while they craft a
 * steam recipe. The synced {@code working} flag drives the flywheel renderer: the wheel spins fast
 * while working and idles slowly otherwise.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SteamEngineBlockEntity extends BlockEntity implements ServerTickingBlockEntity, ClientTickingBlockEntity {

    // How long (ticks) the wheel keeps spinning fast after the last keepWorking() call.
    private static final int WORKING_TICKS = 40;

    private int working;            // server-side countdown
    private boolean syncedWorking;  // last state pushed to clients

    // Client-only flywheel animation.
    private final SpinAnimation wheel = new SpinAnimation(1.0f, 8.0f, 0.35f, 0.12f, 3.0f);

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_ENGINE.get(), pos, state);
    }

    /** Called by a crafting block entity each tick while it is busy with a steam recipe. */
    public void keepWorking() {
        working = WORKING_TICKS;
        if (!syncedWorking) {
            sync();
        }
    }

    public boolean isWorking() {
        return working > 0;
    }

    public float wheelAngle(float partialTick) {
        return wheel.angle(partialTick);
    }

    @Override
    public void serverTick() {
        if (working > 0) {
            working--;
            if (working == 0) {
                sync();
            }
        }
    }

    @Override
    public void clientTick() {
        wheel.tick(isWorking());
    }

    private void sync() {
        syncedWorking = working > 0;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Working", working > 0);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        boolean wasWorking = tag.getBoolean("Working");
        working = wasWorking ? WORKING_TICKS : 0;
        syncedWorking = wasWorking;
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
