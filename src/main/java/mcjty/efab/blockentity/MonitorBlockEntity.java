package mcjty.efab.blockentity;

import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MonitorBlockEntity extends BlockEntity {

    public static final int MAX_LINES = 9;

    private boolean autoCraftingMonitor;
    private final List<String> lines = new ArrayList<>(MAX_LINES);

    public MonitorBlockEntity(BlockPos pos, BlockState state, boolean autoCraftingMonitor) {
        super(ModBlockEntities.MONITOR.get(), pos, state);
        this.autoCraftingMonitor = autoCraftingMonitor;
        setDefaultLines();
    }

    public boolean isAutoCraftingMonitor() {
        return autoCraftingMonitor;
    }

    public List<String> getLines() {
        return lines;
    }

    public void setCraftStatus(String status, String result) {
        List<String> newLines = List.of(tr("status.efab.status_label"), status, tr("status.efab.result_label"), result);
        setLines(newLines);
    }

    private static String tr(String key) {
        return Component.translatable(key).getString();
    }

    public void setLines(List<String> newLines) {
        boolean changed = false;
        for (int i = 0; i < MAX_LINES; i++) {
            String line = i < newLines.size() ? newLines.get(i) : "";
            if (!line.equals(lines.get(i))) {
                lines.set(i, line);
                changed = true;
            }
        }
        if (changed) {
            setChanged();
        }
    }

    private void setDefaultLines() {
        lines.clear();
        if (autoCraftingMonitor) {
            lines.add(tr("status.efab.crafters"));
        } else {
            lines.add(tr("status.efab.status_label"));
            lines.add("  " + tr("status.efab.idle"));
            lines.add(tr("status.efab.result_label"));
            lines.add("  " + tr("status.efab.none"));
        }
        while (lines.size() < MAX_LINES) {
            lines.add("");
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("Auto", autoCraftingMonitor);
        ListTag list = new ListTag();
        for (String line : lines) {
            list.add(StringTag.valueOf(line));
        }
        tag.put("Lines", list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        autoCraftingMonitor = tag.getBoolean("Auto");
        setDefaultLines();
        ListTag list = tag.getList("Lines", StringTag.TAG_STRING);
        for (int i = 0; i < Math.min(MAX_LINES, list.size()); i++) {
            lines.set(i, list.getString(i));
        }
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
