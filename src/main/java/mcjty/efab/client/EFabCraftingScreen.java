package mcjty.efab.client;

import mcjty.efab.EFab;
import mcjty.efab.blockentity.AbstractCraftingBlockEntity.CraftButtonState;
import mcjty.efab.blockentity.CrafterBlockEntity;
import mcjty.efab.menu.EFabCraftingMenu;
import mcjty.efab.network.CraftingActionPayload;
import mcjty.efab.network.CraftingStatusRequestPayload;
import mcjty.efab.network.StorageNameRequestPayload;
import mcjty.efab.network.StorageNameSetPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class EFabCraftingScreen extends AbstractContainerScreen<EFabCraftingMenu> {

    private static final ResourceLocation BACKGROUND = EFab.rl("textures/gui/grid.png");
    private static final int CRAFT_BUTTON_X = 84;
    private static final int CRAFT_BUTTON_Y = 24;
    private static final int CRAFT_BUTTON_WIDTH = 40;
    private static final int CRAFT_BUTTON_HEIGHT = 16;

    private int requestTicker;
    private Button craftButton;
    private EditBox nameField;
    private boolean applyingServerName;
    private String lastAppliedName = "";

    public EFabCraftingScreen(EFabCraftingMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth = 171;
        imageHeight = 176;
        titleLabelX = 6;
        titleLabelY = 4;
        inventoryLabelX = 6;
        inventoryLabelY = 86;
    }

    private boolean isCrafter() {
        return menu.getBlockEntity() instanceof CrafterBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (isCrafter()) {
            nameField = new EditBox(font, leftPos + 79, topPos + 24, 46, 12, Component.translatable("gui.efab.crafter.name"));
            nameField.setMaxLength(64);
            nameField.setHint(Component.translatable("gui.efab.crafter.name"));
            nameField.setTooltip(Tooltip.create(Component.translatable("tooltip.efab.crafter.name")));
            nameField.setResponder(name -> {
                if (!applyingServerName) {
                    PacketDistributor.sendToServer(new StorageNameSetPayload(menu.getBlockEntity().getBlockPos(), name));
                }
            });
            addRenderableWidget(nameField);
            PacketDistributor.sendToServer(new StorageNameRequestPayload(menu.getBlockEntity().getBlockPos()));
        } else {
            craftButton = Button.builder(Component.translatable("gui.efab.start"), button -> sendCraftAction())
                    .bounds(leftPos + CRAFT_BUTTON_X, topPos + CRAFT_BUTTON_Y, CRAFT_BUTTON_WIDTH, CRAFT_BUTTON_HEIGHT)
                    .tooltip(Tooltip.create(Component.translatable("tooltip.efab.start")))
                    .build();
            addRenderableWidget(craftButton);
        }
        requestStatus();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (++requestTicker >= 20) {
            requestTicker = 0;
            requestStatus();
        }
        updateCraftButton();
        if (nameField != null) {
            String serverName = ClientStorageNameCache.get(menu.getBlockEntity().getBlockPos());
            if (serverName != null && !serverName.equals(lastAppliedName)) {
                applyingServerName = true;
                nameField.setValue(serverName);
                applyingServerName = false;
                lastAppliedName = serverName;
            }
        }
    }

    private void requestStatus() {
        PacketDistributor.sendToServer(new CraftingStatusRequestPayload(menu.getBlockEntity().getBlockPos()));
    }

    private void sendCraftAction() {
        PacketDistributor.sendToServer(new CraftingActionPayload(menu.getBlockEntity().getBlockPos(), hasShiftDown()));
    }

    private void updateCraftButton() {
        if (craftButton == null) {
            return;
        }
        int required = menu.getRequiredTime();
        if (required > 0) {
            craftButton.active = false;
            craftButton.setMessage(Component.literal(Math.min(100, menu.getProgress() * 100 / required) + "%"));
            craftButton.setTooltip(Tooltip.create(Component.translatable("tooltip.efab.start.crafting")));
        } else {
            CraftButtonState state = menu.getCraftButtonState();
            craftButton.active = state == CraftButtonState.READY;
            craftButton.setMessage(Component.translatable("gui.efab.start"));
            craftButton.setTooltip(Tooltip.create(Component.translatable(tooltipKey(state))));
        }
    }

    private static String tooltipKey(CraftButtonState state) {
        return switch (state) {
            case READY -> "tooltip.efab.start";
            case NO_INPUT -> "tooltip.efab.start.no_input";
            case MISSING_INGREDIENTS -> "tooltip.efab.start.missing_ingredients";
            case NO_RECIPE -> "tooltip.efab.start.no_recipe";
            case OUTPUT_FULL -> "tooltip.efab.start.output_full";
            case MISSING_FE_CONTROL -> "tooltip.efab.start.missing_fe_control";
            case MISSING_REQUIREMENTS -> "tooltip.efab.start.missing_requirements";
            case MISSING_FLUIDS -> "tooltip.efab.start.missing_fluids";
            case MISSING_ENERGY -> "tooltip.efab.start.missing_energy";
        };
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        updateCraftButton();
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderGhostResult(guiGraphics);
        renderTooltip(guiGraphics, mouseX, mouseY);
        renderGhostResultTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        guiGraphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    private void renderGhostResult(GuiGraphics guiGraphics) {
        CraftingStatus status = ClientCraftingStatusCache.get(menu.getBlockEntity().getBlockPos());
        if (status == null || status.result().isEmpty()) {
            return;
        }
        ItemStack result = status.result();
        guiGraphics.renderFakeItem(result, leftPos + 95, topPos + 45);
        guiGraphics.renderItemDecorations(font, result, leftPos + 95, topPos + 45);
    }

    private void renderGhostResultTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!isHovering(95, 45, 16, 16, mouseX, mouseY)) {
            return;
        }
        CraftingStatus status = ClientCraftingStatusCache.get(menu.getBlockEntity().getBlockPos());
        if (status != null && !status.result().isEmpty()) {
            guiGraphics.renderTooltip(font, status.result(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, Component.empty(), inventoryLabelX, inventoryLabelY, 0x404040, false);
        String timeLeft = getTimeLeftText();
        if (!timeLeft.isEmpty()) {
            guiGraphics.drawString(font, timeLeft, 88, 11, 0x404040, false);
        }
    }

    private String getTimeLeftText() {
        int required = menu.getRequiredTime();
        if (required <= 0) {
            return "";
        }
        return formatTime(Math.max(0, required - menu.getProgress()));
    }

    private static String formatTime(int ticks) {
        float seconds = ticks / 20.0f;
        if (seconds >= 60.0f) {
            float minutes = seconds / 60.0f;
            if (minutes >= 60.0f) {
                float hours = minutes / 60.0f;
                return Component.translatable("gui.efab.time.hours", formatNumber(hours)).getString();
            }
            return Component.translatable("gui.efab.time.minutes", formatNumber(minutes)).getString();
        }
        return Component.translatable("gui.efab.time.seconds", formatNumber(seconds)).getString();
    }

    private static String formatNumber(float value) {
        if (Math.abs(value - Math.round(value)) < 0.05f) {
            return Integer.toString(Math.round(value));
        }
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

}
