package mcjty.efab.menu;

import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import mcjty.efab.blockentity.CrafterBlockEntity;
import mcjty.efab.registry.ModItems;
import mcjty.efab.registry.ModMenus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EFabCraftingMenu extends AbstractContainerMenu {

    public static final int INPUT_START = 0;
    public static final int INPUT_COUNT = 9;
    public static final int OUTPUT_START = 9;
    public static final int OUTPUT_COUNT = 3;
    public static final int UPGRADE_START = 12;
    public static final int UPGRADE_COUNT = 9;
    public static final int GHOST_SLOT = 21;
    public static final int MACHINE_SLOT_COUNT = 22;
    public static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    public static final int PLAYER_INV_COUNT = 36;

    private final AbstractCraftingBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Block menuBlock;
    private int progress;
    private int requiredTime;
    private int craftButtonState;

    public EFabCraftingMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, lookupBlockEntity(playerInventory, extraData));
    }

    public EFabCraftingMenu(int containerId, Inventory playerInventory, AbstractCraftingBlockEntity blockEntity) {
        super(ModMenus.EFAB_CRAFTING.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.menuBlock = blockEntity.getBlockState().getBlock();

        ItemStackHandler handler = blockEntity.getItemHandler();
        addMachineSlots(handler, isCrafterMenu());
        addPlayerSlots(playerInventory);

        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getProgress();
            }

            @Override
            public void set(int value) {
                progress = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity.getRequiredTime();
            }

            @Override
            public void set(int value) {
                requiredTime = value;
            }
        });
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return blockEntity == null
                        ? craftButtonState
                        : blockEntity.getCraftButtonState().ordinal();
            }

            @Override
            public void set(int value) {
                craftButtonState = value;
            }
        });
    }

    private static AbstractCraftingBlockEntity lookupBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        if (extraData == null) {
            return null;
        }
        BlockPos pos = extraData.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof AbstractCraftingBlockEntity craftingBlockEntity) {
            return craftingBlockEntity;
        }
        return null;
    }

    private void addMachineSlots(ItemStackHandler handler, boolean crafter) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int slot = row * 3 + col;
                addSlot(crafter
                        ? new TemplateSlot(handler, slot, 23 + col * 18, 12 + row * 18)
                        : new SlotItemHandler(handler, slot, 23 + col * 18, 12 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            addSlot(new OutputSlot(handler, OUTPUT_START + row, 132, 12 + row * 18));
        }
        for (int col = 0; col < UPGRADE_COUNT; col++) {
            addSlot(new SlotItemHandler(handler, UPGRADE_START + col, 6 + col * 18, 72));
        }
        addSlot(new GhostSlot(handler, GHOST_SLOT, 95, 45));
    }

    private void addPlayerSlots(Inventory playerInventory) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 6 + col * 18, 97 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 6 + col * 18, 155));
        }
    }

    public AbstractCraftingBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public boolean isCrafterMenu() {
        return blockEntity instanceof CrafterBlockEntity;
    }

    public void setTemplateSlots(List<ItemStack> stacks) {
        if (!isCrafterMenu() || blockEntity == null) {
            return;
        }
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY;
            setTemplateSlot(i, stack);
        }
        broadcastChanges();
    }

    private void setTemplateSlot(int slot, ItemStack stack) {
        if (!isCrafterMenu() || blockEntity == null || slot < INPUT_START || slot >= INPUT_START + INPUT_COUNT) {
            return;
        }
        blockEntity.getItemHandler().setStackInSlot(slot, asTemplateStack(stack));
    }

    public boolean canFillGridFromPlayer(List<ItemStack> targets) {
        return !isCrafterMenu() && blockEntity != null && canFillGridTargets(normalizeGridTargets(targets));
    }

    public boolean fillGridFromPlayer(List<ItemStack> targets) {
        if (!canFillGridFromPlayer(targets)) {
            return false;
        }

        List<ItemStack> normalized = normalizeGridTargets(targets);
        ItemStackHandler handler = blockEntity.getItemHandler();
        for (ItemStack target : normalized) {
            if (!target.isEmpty()) {
                consumeFromGridOrPlayer(target, handler);
            }
        }

        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            ItemStack remaining = handler.getStackInSlot(slot).copy();
            if (!remaining.isEmpty()) {
                handler.setStackInSlot(slot, ItemStack.EMPTY);
                insertIntoPlayerSlots(remaining);
            }
        }

        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            handler.setStackInSlot(slot, normalized.get(slot));
        }
        broadcastChanges();
        return true;
    }

    private boolean canFillGridTargets(List<ItemStack> targets) {
        List<ItemStack> gridCopies = copyInputSlots();
        List<ItemStack> playerCopies = copyPlayerSlots();

        for (ItemStack target : targets) {
            if (!target.isEmpty() && !consumeFromCopies(target, gridCopies, playerCopies)) {
                return false;
            }
        }

        for (ItemStack stack : gridCopies) {
            if (!stack.isEmpty() && !insertIntoCopies(stack, playerCopies)) {
                return false;
            }
        }
        return true;
    }

    private List<ItemStack> copyInputSlots() {
        List<ItemStack> stacks = new ArrayList<>(INPUT_COUNT);
        ItemStackHandler handler = blockEntity.getItemHandler();
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            stacks.add(handler.getStackInSlot(slot).copy());
        }
        return stacks;
    }

    private List<ItemStack> copyPlayerSlots() {
        List<ItemStack> stacks = new ArrayList<>(PLAYER_INV_COUNT);
        for (int slot = 0; slot < PLAYER_INV_COUNT; slot++) {
            stacks.add(slots.get(PLAYER_INV_START + slot).getItem().copy());
        }
        return stacks;
    }

    private static boolean consumeFromCopies(ItemStack target, List<ItemStack> gridCopies, List<ItemStack> playerCopies) {
        for (int i = 0; i < target.getCount(); i++) {
            if (!consumeFromCopyList(target, gridCopies) && !consumeFromCopyList(target, playerCopies)) {
                return false;
            }
        }
        return true;
    }

    private static boolean consumeFromCopyList(ItemStack target, List<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            if (matchesTarget(target, stack)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    private void consumeFromGridOrPlayer(ItemStack target, ItemStackHandler handler) {
        for (int i = 0; i < target.getCount(); i++) {
            consumeOneFromGridOrPlayer(target, handler);
        }
    }

    private void consumeOneFromGridOrPlayer(ItemStack target, ItemStackHandler handler) {
        for (int slot = INPUT_START; slot < INPUT_START + INPUT_COUNT; slot++) {
            if (matchesTarget(target, handler.getStackInSlot(slot))) {
                handler.extractItem(slot, 1, false);
                return;
            }
        }
        for (int slot = 0; slot < PLAYER_INV_COUNT; slot++) {
            Slot playerSlot = slots.get(PLAYER_INV_START + slot);
            ItemStack stack = playerSlot.getItem();
            if (matchesTarget(target, stack)) {
                stack.shrink(1);
                if (stack.isEmpty()) {
                    playerSlot.setByPlayer(ItemStack.EMPTY);
                } else {
                    playerSlot.setChanged();
                }
                return;
            }
        }
    }

    private static boolean insertIntoCopies(ItemStack stack, List<ItemStack> playerCopies) {
        ItemStack remaining = stack.copy();
        for (ItemStack target : playerCopies) {
            if (canStack(target, remaining)) {
                int moved = Math.min(remaining.getCount(), target.getMaxStackSize() - target.getCount());
                target.grow(moved);
                remaining.shrink(moved);
                if (remaining.isEmpty()) {
                    return true;
                }
            }
        }
        for (int i = 0; i < playerCopies.size(); i++) {
            if (playerCopies.get(i).isEmpty()) {
                playerCopies.set(i, remaining.copy());
                return true;
            }
        }
        return false;
    }

    private void insertIntoPlayerSlots(ItemStack stack) {
        for (int slot = 0; slot < PLAYER_INV_COUNT && !stack.isEmpty(); slot++) {
            Slot playerSlot = slots.get(PLAYER_INV_START + slot);
            ItemStack existing = playerSlot.getItem();
            if (canStack(existing, stack)) {
                int moved = Math.min(stack.getCount(), existing.getMaxStackSize() - existing.getCount());
                existing.grow(moved);
                stack.shrink(moved);
                playerSlot.setChanged();
            }
        }
        for (int slot = 0; slot < PLAYER_INV_COUNT && !stack.isEmpty(); slot++) {
            Slot playerSlot = slots.get(PLAYER_INV_START + slot);
            if (playerSlot.getItem().isEmpty()) {
                playerSlot.setByPlayer(stack.copy());
                stack.setCount(0);
            }
        }
    }

    private static List<ItemStack> normalizeTargets(List<ItemStack> targets) {
        List<ItemStack> normalized = new ArrayList<>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = i < targets.size() ? targets.get(i) : ItemStack.EMPTY;
            normalized.add(asTemplateStack(stack));
        }
        return normalized;
    }

    private static List<ItemStack> normalizeGridTargets(List<ItemStack> targets) {
        List<ItemStack> normalized = new ArrayList<>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            ItemStack stack = i < targets.size() ? targets.get(i) : ItemStack.EMPTY;
            if (stack.isEmpty()) {
                normalized.add(ItemStack.EMPTY);
            } else {
                ItemStack target = stack.copy();
                target.setCount(Math.min(target.getCount(), target.getMaxStackSize()));
                normalized.add(target);
            }
        }
        return normalized;
    }

    private static boolean matchesTarget(ItemStack target, ItemStack stack) {
        return !target.isEmpty()
                && !stack.isEmpty()
                && stack.getCount() > 0
                && ItemStack.isSameItemSameComponents(target, stack);
    }

    private static boolean canStack(ItemStack existing, ItemStack stack) {
        return !existing.isEmpty()
                && !stack.isEmpty()
                && ItemStack.isSameItemSameComponents(existing, stack)
                && existing.getCount() < existing.getMaxStackSize();
    }

    public int getProgress() {
        return progress;
    }

    public int getRequiredTime() {
        return requiredTime;
    }

    public AbstractCraftingBlockEntity.CraftButtonState getCraftButtonState() {
        AbstractCraftingBlockEntity.CraftButtonState[] values = AbstractCraftingBlockEntity.CraftButtonState.values();
        if (craftButtonState < 0 || craftButtonState >= values.length) {
            return AbstractCraftingBlockEntity.CraftButtonState.NO_RECIPE;
        }
        return values[craftButtonState];
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (isCrafterMenu()) {
            return quickMoveCrafter(player, index);
        }
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < MACHINE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_START + PLAYER_INV_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, INPUT_START, INPUT_START + INPUT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return original;
    }

    private ItemStack quickMoveCrafter(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return original;
        }

        ItemStack stack = slot.getItem();
        original = stack.copy();
        if (index >= INPUT_START && index < INPUT_START + INPUT_COUNT) {
            setTemplateSlot(index, ItemStack.EMPTY);
            return original;
        }
        if (index >= PLAYER_INV_START) {
            boolean moved = stack.getItem() instanceof ModItems.TierUpgradeItem
                    && moveItemStackTo(stack, UPGRADE_START, UPGRADE_START + UPGRADE_COUNT, false);
            if (moved) {
                if (stack.isEmpty()) {
                    slot.setByPlayer(ItemStack.EMPTY);
                } else {
                    slot.setChanged();
                }
            } else {
                setFirstTemplate(stack);
                broadcastChanges();
            }
            return original;
        }

        if (index < MACHINE_SLOT_COUNT && !moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_START + PLAYER_INV_COUNT, true)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    private void setFirstTemplate(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStackHandler handler = blockEntity.getItemHandler();
        for (int i = INPUT_START; i < INPUT_START + INPUT_COUNT; i++) {
            if (ItemStack.isSameItemSameComponents(handler.getStackInSlot(i), stack)) {
                setTemplateSlot(i, stack);
                return;
            }
        }
        for (int i = INPUT_START; i < INPUT_START + INPUT_COUNT; i++) {
            if (handler.getStackInSlot(i).isEmpty()) {
                setTemplateSlot(i, stack);
                return;
            }
        }
        setTemplateSlot(INPUT_START, stack);
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (isCrafterMenu() && slotId >= INPUT_START && slotId < INPUT_START + INPUT_COUNT) {
            handleTemplateClick(slotId, button, clickType, player);
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    private void handleTemplateClick(int slotId, int button, ClickType clickType, Player player) {
        if (clickType == ClickType.PICKUP) {
            ItemStack carried = getCarried();
            setTemplateSlot(slotId, button == 1 || carried.isEmpty() ? ItemStack.EMPTY : carried);
        } else if (clickType == ClickType.QUICK_MOVE || clickType == ClickType.THROW) {
            setTemplateSlot(slotId, ItemStack.EMPTY);
        } else if (clickType == ClickType.SWAP && button >= 0 && button < 9) {
            setTemplateSlot(slotId, player.getInventory().getItem(button));
        }
        broadcastChanges();
    }

    private static ItemStack asTemplateStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack template = stack.copy();
        template.setCount(1);
        return template;
    }

    @Override
    public boolean stillValid(Player player) {
        if (menuBlock == null) {
            return true;
        }
        return stillValid(access, player, menuBlock);
    }

    private static class OutputSlot extends SlotItemHandler {
        public OutputSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }

    private static class GhostSlot extends SlotItemHandler {
        public GhostSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }
    }

    private static class TemplateSlot extends SlotItemHandler {
        public TemplateSlot(ItemStackHandler itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player playerIn) {
            return false;
        }

        @Override
        public int getMaxStackSize() {
            return 1;
        }
    }
}
