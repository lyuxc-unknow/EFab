package mcjty.efab.network;

import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import mcjty.efab.blockentity.NamedCrafting;
import mcjty.efab.client.ClientCraftingStatusCache;
import mcjty.efab.client.CraftingStatus;
import mcjty.efab.client.ClientStorageNameCache;
import mcjty.efab.menu.EFabCraftingMenu;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class ModNetworking {

    private static final String VERSION = "1";

    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(VERSION);
        registrar.playToServer(CraftingActionPayload.TYPE, CraftingActionPayload.STREAM_CODEC, ModNetworking::handleCraftingAction);
        registrar.playToServer(CraftingStatusRequestPayload.TYPE, CraftingStatusRequestPayload.STREAM_CODEC, ModNetworking::handleStatusRequest);
        registrar.playToServer(CrafterTemplatePayload.TYPE, CrafterTemplatePayload.STREAM_CODEC, ModNetworking::handleCrafterTemplate);
        registrar.playToServer(GridTransferPayload.TYPE, GridTransferPayload.STREAM_CODEC, ModNetworking::handleGridTransfer);
        registrar.playToServer(StorageNameRequestPayload.TYPE, StorageNameRequestPayload.STREAM_CODEC, ModNetworking::handleStorageNameRequest);
        registrar.playToServer(StorageNameSetPayload.TYPE, StorageNameSetPayload.STREAM_CODEC, ModNetworking::handleStorageNameSet);
        registrar.playToClient(CraftingStatusPayload.TYPE, CraftingStatusPayload.STREAM_CODEC, ModNetworking::handleStatus);
        registrar.playToClient(StorageNamePayload.TYPE, StorageNamePayload.STREAM_CODEC, ModNetworking::handleStorageName);
    }

    private static void handleCraftingAction(CraftingActionPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof AbstractCraftingBlockEntity blockEntity) {
            blockEntity.startCraft(payload.repeat());
        }
    }

    private static void handleStatusRequest(CraftingStatusRequestPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof AbstractCraftingBlockEntity blockEntity) {
            context.reply(new CraftingStatusPayload(
                    payload.pos(),
                    blockEntity.getProgress(),
                    blockEntity.getRequiredTime(),
                    blockEntity.getCurrentResult()
            ));
        }
    }

    private static void handleStatus(CraftingStatusPayload payload, IPayloadContext context) {
        ClientCraftingStatusCache.put(payload.pos(), new CraftingStatus(
                payload.progress(),
                payload.requiredTime(),
                payload.result()
        ));
    }

    private static void handleCrafterTemplate(CrafterTemplatePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.containerMenu instanceof EFabCraftingMenu menu
                && menu.isCrafterMenu()
                && menu.getBlockEntity() != null
                && menu.getBlockEntity().getBlockPos().equals(payload.pos())
                && menu.stillValid(player)) {
            menu.setTemplateSlots(payload.stacks());
        }
    }

    private static void handleGridTransfer(GridTransferPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.containerMenu instanceof EFabCraftingMenu menu
                && !menu.isCrafterMenu()
                && menu.getBlockEntity() != null
                && menu.getBlockEntity().getBlockPos().equals(payload.pos())
                && menu.stillValid(player)) {
            menu.fillGridFromPlayer(payload.stacks());
        }
    }

    private static void handleStorageNameRequest(StorageNameRequestPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof NamedCrafting blockEntity) {
            context.reply(new StorageNamePayload(payload.pos(), blockEntity.getCraftingName()));
        }
    }

    private static void handleStorageNameSet(StorageNameSetPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (player.level().getBlockEntity(payload.pos()) instanceof NamedCrafting blockEntity) {
            blockEntity.setCraftingName(payload.name());
            context.reply(new StorageNamePayload(payload.pos(), blockEntity.getCraftingName()));
        }
    }

    private static void handleStorageName(StorageNamePayload payload, IPayloadContext context) {
        ClientStorageNameCache.put(payload.pos(), payload.name());
    }

    private ModNetworking() {
    }
}
