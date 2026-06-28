package mcjty.efab.blockentity;

import mcjty.efab.config.EFabConfig;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.registry.ModBlockEntities;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

/**
 * Auto-crafter. Its 9 input slots are a non-consumed recipe template; the real ingredients are
 * pulled from nearby {@link StorageBlockEntity} blocks whose name matches this crafter's name
 * (an empty name matches any). It only runs while it receives a redstone signal and a Processor
 * is present in its crafting area.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CrafterBlockEntity extends AbstractCraftingBlockEntity implements NamedCrafting, ClientTickingBlockEntity {

    private String craftingName = "";
    private int delay;

    // Client-only flywheel/piston animation (spins while crafting).
    private final SpinAnimation wheel = new SpinAnimation(1.0f, 8.0f, 0.35f, 0.12f, 3.0f);

    public CrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRAFTER.get(), pos, state);
    }

    @Override
    public void clientTick() {
        wheel.tick(isCrafting());
    }

    public float wheelAngle(float partialTick) {
        return wheel.angle(partialTick);
    }

    @Override
    public String getCraftingName() {
        return craftingName;
    }

    @Override
    public void setCraftingName(String name) {
        this.craftingName = name == null ? "" : name;
        setChanged();
    }

    private boolean nameMatches(String storageName) {
        return craftingName.isEmpty() || craftingName.equals(storageName);
    }

    // Crafter is automatic only: ignore the manual craft button.
    @Override
    public void startCraft(boolean repeat) {
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (!crafting) {
            if (--delay <= 0) {
                delay = EFabConfig.CRAFTER_DELAY.get();
                tryAutoStart();
            }
            updateNearbyMonitors();
            return;
        }
        super.serverTick();
    }

    private void tryAutoStart() {
        findRecipe(level).ifPresent(recipe -> beginCraft(recipe, false));
    }

    @Override
    protected boolean canCraft(EFabRecipe recipe) {
        return level.hasNeighborSignal(worldPosition)
                && hasProcessor()
                && super.canCraft(recipe)
                && supplyIngredients(recipe, false);
    }

    @Override
    protected boolean isAutomatedMachine() {
        return true;
    }

    // The Crafter's input slots are only a template, so pull the real items from storage instead.
    @Override
    protected void consumeCraftIngredients(EFabRecipe recipe) {
        supplyIngredients(recipe, true);
    }

    @Override
    protected boolean shouldDropSlot(int slot) {
        return slot >= INPUT_SLOTS && super.shouldDropSlot(slot);
    }

    private List<ItemStackHandler> matchingStorageHandlers() {
        List<ItemStackHandler> handlers = new ArrayList<>();
        for (BlockPos pos : snapshot().storages()) {
            if (level.getBlockEntity(pos) instanceof StorageBlockEntity storage && nameMatches(storage.getCraftingName())) {
                handlers.add(storage.getItemHandler());
            }
        }
        return handlers;
    }

    /**
     * Check (or perform, when {@code execute}) the removal of one matching item per required
     * ingredient from the matching storage blocks. Returns false if anything is missing.
     */
    private boolean supplyIngredients(EFabRecipe recipe, boolean execute) {
        List<Ingredient> needed = recipe.requiredIngredients();
        if (needed.isEmpty()) {
            return true;
        }
        List<ItemStackHandler> handlers = matchingStorageHandlers();
        int[][] reserved = new int[handlers.size()][];
        for (int h = 0; h < handlers.size(); h++) {
            reserved[h] = new int[handlers.get(h).getSlots()];
        }

        for (Ingredient ingredient : needed) {
            boolean found = false;
            for (int h = 0; h < handlers.size() && !found; h++) {
                ItemStackHandler handler = handlers.get(h);
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    ItemStack stack = handler.getStackInSlot(slot);
                    int available = stack.getCount() - reserved[h][slot];
                    if (available > 0 && ingredient.test(stack)) {
                        reserved[h][slot]++;
                        found = true;
                        break;
                    }
                }
            }
            if (!found) {
                return false;
            }
        }

        if (execute) {
            for (int h = 0; h < handlers.size(); h++) {
                ItemStackHandler handler = handlers.get(h);
                for (int slot = 0; slot < handler.getSlots(); slot++) {
                    if (reserved[h][slot] > 0) {
                        handler.extractItem(slot, reserved[h][slot], false);
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("CraftingName", craftingName);
        tag.putInt("Delay", delay);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        craftingName = tag.getString("CraftingName");
        delay = tag.getInt("Delay");
    }
}
