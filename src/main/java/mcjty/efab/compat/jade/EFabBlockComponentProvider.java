package mcjty.efab.compat.jade;

import mcjty.efab.EFab;
import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

import java.util.Locale;

/**
 * Client-side block component provider. Reads pre-computed data from the
 * server-synced NBT tag instead of computing it locally.
 */
final class EFabBlockComponentProvider implements IBlockComponentProvider {

    static final EFabBlockComponentProvider INSTANCE = new EFabBlockComponentProvider();

    private EFabBlockComponentProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return EFab.rl("jade_info");
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag data = accessor.getServerData();
        HolderLookup.Provider registries = accessor.getLevel().registryAccess();

        if (data.getBoolean("efab:isCrafter")) {
            addCrafterInfo(tooltip, data, registries);
        } else if (data.contains("efab:craftState")) {
            addCraftingInfo(tooltip, data, registries);
        }

        if (data.contains("efab:craftingName")) {
            tooltip.add(Component.translatable("info.efab.name")
                    .append(data.getString("efab:craftingName")));
        }

        if (data.getBoolean("efab:isBoiler")) {
            double temp = data.getDouble("efab:temperature");
            tooltip.add(Component.translatable("info.efab.temperature")
                    .append(Component.translatable("info.efab.temperature.value",
                            String.format(Locale.ROOT, "%.1f", temp))));
        }
    }

    private static void addCrafterInfo(ITooltip tooltip, CompoundTag data, HolderLookup.Provider registries) {
        boolean isCrafting = data.getBoolean("efab:isCrafting");
        boolean hasSignal = data.getBoolean("efab:hasSignal");
        if (!isCrafting && !hasSignal) {
            addStatus(tooltip, Component.translatable("info.efab.status.off"), false);
        } else {
            addCraftingInfo(tooltip, data, registries);
        }
    }

    private static void addCraftingInfo(ITooltip tooltip, CompoundTag data, HolderLookup.Provider registries) {
        boolean isCrafting = data.getBoolean("efab:isCrafting");
        int progress = data.getInt("efab:progress");
        int requiredTime = data.getInt("efab:requiredTime");

        if (isCrafting && requiredTime > 0) {
            int percent = Math.min(100, progress * 100 / requiredTime);
            addStatus(tooltip, Component.translatable("info.efab.status.progress", percent), false);
        } else {
            String stateName = data.getString("efab:craftState");
            AbstractCraftingBlockEntity.CraftButtonState state;
            try {
                state = AbstractCraftingBlockEntity.CraftButtonState.valueOf(stateName);
            } catch (IllegalArgumentException e) {
                state = AbstractCraftingBlockEntity.CraftButtonState.NO_INPUT;
            }
            boolean error = state != AbstractCraftingBlockEntity.CraftButtonState.NO_INPUT
                    && state != AbstractCraftingBlockEntity.CraftButtonState.READY;
            addStatus(tooltip, error ? statusFor(state) : Component.translatable("info.efab.status.idle"), error);
        }

        if (data.contains("efab:output")) {
            ItemStack output = ItemStack.parse(registries, data.getCompound("efab:output")).orElse(ItemStack.EMPTY);
            if (!output.isEmpty()) {
                tooltip.add(Component.translatable("info.efab.output"));
                tooltip.append(IElementHelper.get().item(output));
            }
        }
    }

    private static void addStatus(ITooltip tooltip, Component status, boolean error) {
        tooltip.add(Component.translatable("info.efab.status"));
        if (error) {
            tooltip.append(Component.literal(" ").append(status).withColor(0xFF5555));
        } else {
            tooltip.append(Component.literal(" ").append(status));
        }
    }

    private static Component statusFor(AbstractCraftingBlockEntity.CraftButtonState state) {
        return Component.translatable("info.efab.status." + state.name().toLowerCase(Locale.ROOT));
    }
}
