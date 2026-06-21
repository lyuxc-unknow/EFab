package mcjty.efab.client;

import mcjty.efab.EFab;
import mcjty.efab.menu.StorageMenu;
import mcjty.efab.network.StorageNameRequestPayload;
import mcjty.efab.network.StorageNameSetPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class StorageScreen extends AbstractContainerScreen<StorageMenu> {

    private static final ResourceLocation BACKGROUND = EFab.rl("textures/gui/storage.png");

    private EditBox nameField;
    private boolean applyingServerName;
    private String lastAppliedName = "";

    public StorageScreen(StorageMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 171;
        imageHeight = 176;
        titleLabelX = 6;
        titleLabelY = 4;
        inventoryLabelX = 6;
        inventoryLabelY = 86;
    }

    @Override
    protected void init() {
        super.init();
        nameField = new EditBox(font, leftPos + 5, topPos + 76, 161, 16, Component.translatable("gui.efab.storage.name"));
        nameField.setMaxLength(64);
        nameField.setResponder(name -> {
            if (!applyingServerName) {
                PacketDistributor.sendToServer(new StorageNameSetPayload(menu.getBlockPos(), name));
            }
        });
        addRenderableWidget(nameField);
        PacketDistributor.sendToServer(new StorageNameRequestPayload(menu.getBlockPos()));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        String serverName = ClientStorageNameCache.get(menu.getBlockPos());
        if (serverName != null && !serverName.equals(lastAppliedName)) {
            applyingServerName = true;
            nameField.setValue(serverName);
            applyingServerName = false;
            lastAppliedName = serverName;
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(BACKGROUND, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
