package mcjty.efab.compat.jade;

import mcjty.efab.EFab;
import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import mcjty.efab.blockentity.BoilerBlockEntity;
import mcjty.efab.blockentity.CrafterBlockEntity;
import mcjty.efab.blockentity.NamedCrafting;
import mcjty.efab.blockentity.StorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

/**
 * Server-side data provider. Computes crafting state on the server and
 * writes it into the NBT tag that Jade syncs to the client.
 */
final class EFabServerDataProvider implements IServerDataProvider<BlockAccessor> {

    static final EFabServerDataProvider INSTANCE = new EFabServerDataProvider();

    private EFabServerDataProvider() {
    }

    @Override
    public ResourceLocation getUid() {
        return EFab.rl("server_data");
    }

    @Override
    public void appendServerData(CompoundTag data, BlockAccessor accessor) {
        BlockEntity blockEntity = accessor.getBlockEntity();
        Level level = accessor.getLevel();
        BlockPos pos = accessor.getPosition();

        HolderLookup.Provider registries = level != null ? level.registryAccess() : null;

        if (blockEntity instanceof CrafterBlockEntity crafter) {
            data.putBoolean("efab:isCrafter", true);
            boolean hasSignal = level != null && level.hasNeighborSignal(pos);
            data.putBoolean("efab:hasSignal", hasSignal);
            addCraftingData(data, crafter, registries);
            addCraftingNameData(data, crafter);
        } else if (blockEntity instanceof AbstractCraftingBlockEntity crafting) {
            addCraftingData(data, crafting, registries);
        } else if (blockEntity instanceof StorageBlockEntity storage) {
            addCraftingNameData(data, storage);
        } else if (blockEntity instanceof BoilerBlockEntity boiler) {
            data.putBoolean("efab:isBoiler", true);
            data.putDouble("efab:temperature", boiler.getTemperature());
        }
    }

    private static void addCraftingData(CompoundTag data, AbstractCraftingBlockEntity crafting, HolderLookup.Provider registries) {
        data.putBoolean("efab:isCrafting", crafting.isCrafting());
        data.putInt("efab:progress", crafting.getProgress());
        data.putInt("efab:requiredTime", crafting.getRequiredTime());

        AbstractCraftingBlockEntity.CraftButtonState state = crafting.getCraftButtonState();
        data.putString("efab:craftState", state.name());

        ItemStack output = crafting.getCurrentResult();
        if (!output.isEmpty() && registries != null) {
            data.put("efab:output", output.save(registries));
        }
    }

    private static void addCraftingNameData(CompoundTag data, NamedCrafting namedCrafting) {
        String name = namedCrafting.getCraftingName();
        if (name != null && !name.trim().isEmpty()) {
            data.putString("efab:craftingName", name);
        }
    }
}
