package mcjty.efab.client;

import net.minecraft.core.BlockPos;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientStorageNameCache {

    private static final Map<BlockPos, String> NAMES = new ConcurrentHashMap<>();

    public static void put(BlockPos pos, String name) {
        NAMES.put(pos, name);
    }

    public static String get(BlockPos pos) {
        return NAMES.get(pos);
    }

    private ClientStorageNameCache() {
    }
}
