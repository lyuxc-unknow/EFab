package mcjty.efab.menu;

import mcjty.efab.blockentity.StorageBlockEntity;
import mcjty.efab.registry.ModMenus;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class StorageMenu extends AbstractContainerMenu {

    public static final int STORAGE_SLOT_COUNT = 27;
    private static final int PLAYER_INV_START = STORAGE_SLOT_COUNT;
    private static final int PLAYER_INV_COUNT = 36;

    private final StorageBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Block menuBlock;
    private final BlockPos blockPos;

    public StorageMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        this(containerId, playerInventory, lookupBlockEntity(playerInventory, extraData));
    }

    public StorageMenu(int containerId, Inventory playerInventory, StorageBlockEntity blockEntity) {
        super(ModMenus.STORAGE.get(), containerId);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());
        this.menuBlock = blockEntity.getBlockState().getBlock();
        this.blockPos = blockEntity.getBlockPos();

        ItemStackHandler handler = blockEntity.getItemHandler();
        addStorageSlots(handler);
        addPlayerSlots(playerInventory);
    }

    private static @Nullable StorageBlockEntity lookupBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf extraData) {
        BlockPos pos = extraData.readBlockPos();
        if (playerInventory.player.level().getBlockEntity(pos) instanceof StorageBlockEntity storageBlockEntity) {
            return storageBlockEntity;
        }
        return null;
    }

    private void addStorageSlots(ItemStackHandler handler) {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new SlotItemHandler(handler, row * 9 + col, 6 + col * 18, 20 + row * 18));
            }
        }
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

    public StorageBlockEntity getBlockEntity() {
        return blockEntity;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack original = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot.hasItem()) {
            ItemStack stack = slot.getItem();
            original = stack.copy();
            if (index < STORAGE_SLOT_COUNT) {
                if (!moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_START + PLAYER_INV_COUNT, true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!moveItemStackTo(stack, 0, STORAGE_SLOT_COUNT, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        if (menuBlock == null) {
            return true;
        }
        return stillValid(access, player, menuBlock);
    }
}
