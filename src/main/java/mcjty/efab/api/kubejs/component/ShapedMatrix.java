package mcjty.efab.api.kubejs.component;

import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public record ShapedMatrix(List<List<Ingredient>> rows) {
}
