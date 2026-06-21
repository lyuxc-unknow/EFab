package mcjty.efab.client;

import mcjty.efab.EFab;
import mcjty.efab.registry.ModBlockEntities;
import mcjty.efab.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = EFab.MODID, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.EFAB_CRAFTING.get(), EFabCraftingScreen::new);
        event.register(ModMenus.STORAGE.get(), StorageScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GRID.get(), EFabCraftingBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRAFTER.get(), CrafterBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.STEAM_ENGINE.get(), SteamEngineBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.TANK.get(), TankBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MONITOR.get(), MonitorBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        // These OBJ models are drawn directly by the machine renderers and are not referenced by any
        // blockstate, so they must be registered here to get baked.
        event.register(EFabModelRenderer.WHEEL);
        event.register(EFabModelRenderer.PIPE);
    }

    private ClientSetup() {
    }
}
