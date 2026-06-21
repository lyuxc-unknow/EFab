package mcjty.efab.compat.jei;

import mcjty.efab.menu.EFabCraftingMenu;
import mcjty.efab.network.CrafterTemplatePayload;
import mcjty.efab.network.GridTransferPayload;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.registry.ModMenus;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
public class EFabRecipeTransferHandler implements IRecipeTransferHandler<EFabCraftingMenu, EFabRecipe> {

    private final IRecipeTransferHandlerHelper helper;

    public EFabRecipeTransferHandler(IRecipeTransferHandlerHelper helper) {
        this.helper = helper;
    }

    @Override
    public Class<? extends EFabCraftingMenu> getContainerClass() {
        return EFabCraftingMenu.class;
    }

    @Override
    public Optional<MenuType<EFabCraftingMenu>> getMenuType() {
        return Optional.of(ModMenus.EFAB_CRAFTING.get());
    }

    @Override
    public RecipeType<EFabRecipe> getRecipeType() {
        return EFabJeiPlugin.EFAB_GRID;
    }

    @Override
    public IRecipeTransferError transferRecipe(EFabCraftingMenu container, EFabRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                               Player player, boolean maxTransfer, boolean doTransfer) {
        if (!container.isCrafterMenu()) {
            if (container.getBlockEntity() == null) {
                return helper.createInternalError();
            }
            List<ItemStack> stacks = gridStacks(recipe, container, player, maxTransfer);
            if (!container.canFillGridFromPlayer(stacks)) {
                List<IRecipeSlotView> missingSlots = missingInputSlots(recipe, recipeSlotsView, container, player);
                if (!missingSlots.isEmpty()) {
                    return helper.createUserErrorForMissingSlots(
                            Component.translatable("jei.efab.transfer.missing_ingredients"),
                            missingSlots);
                }
                return helper.createUserErrorWithTooltip(Component.translatable("jei.efab.transfer.no_inventory_space"));
            }
            if (doTransfer) {
                PacketDistributor.sendToServer(new GridTransferPayload(container.getBlockEntity().getBlockPos(), stacks));
            }
            return null;
        }
        if (container.getBlockEntity() == null) {
            return helper.createInternalError();
        }
        if (doTransfer) {
            List<ItemStack> stacks = templateStacks(recipe);
            container.setTemplateSlots(stacks);
            PacketDistributor.sendToServer(new CrafterTemplatePayload(container.getBlockEntity().getBlockPos(), stacks));
        }
        return null;
    }

    private static List<ItemStack> gridStacks(EFabRecipe recipe, EFabCraftingMenu container, Player player, boolean maxTransfer) {
        List<ItemStack> available = availableStacks(container, player);
        List<Ingredient> ingredients = recipeIngredients(recipe);
        List<ItemStack> targets = emptyStacks();

        boolean transferred = false;
        do {
            List<ItemStack> nextAvailable = copyStacks(available);
            List<ItemStack> nextTargets = copyStacks(targets);
            if (!addOneRecipeSet(ingredients, nextAvailable, nextTargets)) {
                break;
            }
            available = nextAvailable;
            targets = nextTargets;
            transferred = true;
        } while (maxTransfer);

        if (!transferred) {
            return recipeStacks(recipe, EFabRecipeTransferHandler::representativeStack);
        }
        return targets;
    }

    private static List<IRecipeSlotView> missingInputSlots(EFabRecipe recipe, IRecipeSlotsView recipeSlotsView,
                                                           EFabCraftingMenu container, Player player) {
        List<Integer> missingIngredientSlots = missingIngredientSlots(recipe, container, player);
        if (missingIngredientSlots.isEmpty()) {
            return List.of();
        }

        List<Ingredient> ingredients = recipeIngredients(recipe);
        List<IRecipeSlotView> inputSlots = recipeSlotsView.getSlotViews(RecipeIngredientRole.INPUT);
        List<IRecipeSlotView> missingInputSlots = new ArrayList<>();
        int inputSlotIndex = 0;
        for (int i = 0; i < ingredients.size(); i++) {
            if (ingredients.get(i) == Ingredient.EMPTY) {
                continue;
            }
            if (missingIngredientSlots.contains(i) && inputSlotIndex < inputSlots.size()) {
                missingInputSlots.add(inputSlots.get(inputSlotIndex));
            }
            inputSlotIndex++;
        }
        return missingInputSlots;
    }

    private static List<Integer> missingIngredientSlots(EFabRecipe recipe, EFabCraftingMenu container, Player player) {
        List<ItemStack> available = availableStacks(container, player);
        List<Ingredient> ingredients = recipeIngredients(recipe);
        List<Integer> missingSlots = new ArrayList<>();
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient == Ingredient.EMPTY) {
                continue;
            }
            if (consumeTransferStack(ingredient, ItemStack.EMPTY, available).isEmpty()) {
                missingSlots.add(i);
            }
        }
        return missingSlots;
    }

    private static List<ItemStack> templateStacks(EFabRecipe recipe) {
        return recipeStacks(recipe, EFabRecipeTransferHandler::representativeStack);
    }

    private static List<ItemStack> recipeStacks(EFabRecipe recipe, IngredientStackPicker picker) {
        List<ItemStack> stacks = emptyStacks();

        if (recipe.isShaped()) {
            for (int y = 0; y < 3; y++) {
                String row = y < recipe.pattern().size() ? recipe.pattern().get(y) : "";
                for (int x = 0; x < 3; x++) {
                    if (x >= row.length() || row.charAt(x) == ' ') {
                        continue;
                    }
                    Ingredient ingredient = recipe.key().get(String.valueOf(row.charAt(x)));
                    stacks.set(y * 3 + x, picker.pick(ingredient));
                }
            }
            return stacks;
        }

        List<Ingredient> ingredients = recipe.efabIngredients();
        for (int i = 0; i < Math.min(ingredients.size(), CrafterTemplatePayload.TEMPLATE_SLOTS); i++) {
            stacks.set(i, picker.pick(ingredients.get(i)));
        }
        return stacks;
    }

    private static List<Ingredient> recipeIngredients(EFabRecipe recipe) {
        List<Ingredient> ingredients = new ArrayList<>(CrafterTemplatePayload.TEMPLATE_SLOTS);
        for (int i = 0; i < CrafterTemplatePayload.TEMPLATE_SLOTS; i++) {
            ingredients.add(Ingredient.EMPTY);
        }

        if (recipe.isShaped()) {
            for (int y = 0; y < 3; y++) {
                String row = y < recipe.pattern().size() ? recipe.pattern().get(y) : "";
                for (int x = 0; x < 3; x++) {
                    if (x < row.length() && row.charAt(x) != ' ') {
                        ingredients.set(y * 3 + x, recipe.key().getOrDefault(String.valueOf(row.charAt(x)), Ingredient.EMPTY));
                    }
                }
            }
            return ingredients;
        }

        List<Ingredient> shapeless = recipe.efabIngredients();
        for (int i = 0; i < Math.min(shapeless.size(), CrafterTemplatePayload.TEMPLATE_SLOTS); i++) {
            ingredients.set(i, shapeless.get(i));
        }
        return ingredients;
    }

    private static boolean addOneRecipeSet(List<Ingredient> ingredients, List<ItemStack> available, List<ItemStack> targets) {
        boolean usedIngredient = false;
        for (int i = 0; i < ingredients.size(); i++) {
            Ingredient ingredient = ingredients.get(i);
            if (ingredient == Ingredient.EMPTY) {
                continue;
            }
            usedIngredient = true;

            ItemStack target = targets.get(i);
            if (!target.isEmpty() && target.getCount() >= target.getMaxStackSize()) {
                return false;
            }

            ItemStack stack = consumeTransferStack(ingredient, target, available);
            if (stack.isEmpty()) {
                return false;
            }

            if (target.isEmpty()) {
                targets.set(i, stack);
            } else if (ItemStack.isSameItemSameComponents(target, stack)) {
                target.grow(1);
            } else {
                return false;
            }
        }
        return usedIngredient;
    }

    private static List<ItemStack> availableStacks(EFabCraftingMenu container, Player player) {
        List<ItemStack> available = new ArrayList<>(EFabCraftingMenu.INPUT_COUNT + Inventory.INVENTORY_SIZE);
        ItemStackHandler handler = container.getBlockEntity().getItemHandler();
        for (int i = EFabCraftingMenu.INPUT_START; i < EFabCraftingMenu.INPUT_START + EFabCraftingMenu.INPUT_COUNT; i++) {
            available.add(handler.getStackInSlot(i).copy());
        }
        for (int i = 0; i < Inventory.INVENTORY_SIZE; i++) {
            available.add(player.getInventory().getItem(i).copy());
        }
        return available;
    }

    private static ItemStack consumeTransferStack(Ingredient ingredient, ItemStack target, List<ItemStack> available) {
        if (ingredient == null || ingredient == Ingredient.EMPTY) {
            return ItemStack.EMPTY;
        }
        for (ItemStack stack : available) {
            if (!stack.isEmpty()
                    && ingredient.test(stack)
                    && (target.isEmpty() || ItemStack.isSameItemSameComponents(target, stack))) {
                ItemStack result = stack.copy();
                result.setCount(1);
                stack.shrink(1);
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    private static List<ItemStack> emptyStacks() {
        List<ItemStack> stacks = new ArrayList<>(CrafterTemplatePayload.TEMPLATE_SLOTS);
        for (int i = 0; i < CrafterTemplatePayload.TEMPLATE_SLOTS; i++) {
            stacks.add(ItemStack.EMPTY);
        }
        return stacks;
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        List<ItemStack> copies = new ArrayList<>(stacks.size());
        for (ItemStack stack : stacks) {
            copies.add(stack.copy());
        }
        return copies;
    }

    private static ItemStack representativeStack(Ingredient ingredient) {
        if (ingredient == null || ingredient == Ingredient.EMPTY) {
            return ItemStack.EMPTY;
        }
        ItemStack[] stacks = ingredient.getItems();
        if (stacks.length == 0) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = stacks[0].copy();
        stack.setCount(1);
        return stack;
    }

    private interface IngredientStackPicker {
        ItemStack pick(Ingredient ingredient);
    }
}
