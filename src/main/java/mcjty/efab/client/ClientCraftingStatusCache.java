package mcjty.efab.client;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientCraftingStatusCache {

    private static final Map<BlockPos, CraftingStatus> STATUS = new ConcurrentHashMap<>();

    public static void put(BlockPos pos, CraftingStatus status) {
        STATUS.put(pos, status);
    }

    public static CraftingStatus get(BlockPos pos) {
        return pos == null ? null : STATUS.get(pos);
    }

    private ClientCraftingStatusCache() {
    }
}
