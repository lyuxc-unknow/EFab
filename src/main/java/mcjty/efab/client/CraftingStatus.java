package mcjty.efab.client;

import net.minecraft.world.item.ItemStack;

public record CraftingStatus(int progress, int requiredTime, ItemStack result) {
}
