package mcjty.efab;

import mcjty.efab.client.ClientSetup;
import mcjty.efab.config.EFabConfig;
import mcjty.efab.compat.top.TOPCompatibility;
import mcjty.efab.registry.ModBlockEntities;
import mcjty.efab.registry.ModBlocks;
import mcjty.efab.registry.ModCreativeTabs;
import mcjty.efab.registry.ModItems;
import mcjty.efab.registry.ModMenus;
import mcjty.efab.registry.ModRecipes;
import mcjty.efab.network.ModNetworking;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(EFab.MODID)
public class EFab {

    public static final String MODID = "efab";

    public EFab(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModMenus.MENU_TYPES.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(ModBlockEntities::registerCapabilities);
        modEventBus.addListener(ModNetworking::register);
        modEventBus.addListener(TOPCompatibility::enqueueIMC);

        modContainer.registerConfig(ModConfig.Type.SERVER, EFabConfig.SERVER_SPEC);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientSetup.registerConfigScreen(modContainer);
        }
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
