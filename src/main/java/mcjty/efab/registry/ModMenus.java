package mcjty.efab.registry;

import mcjty.efab.EFab;
import mcjty.efab.menu.EFabCraftingMenu;
import mcjty.efab.menu.StorageMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, EFab.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<EFabCraftingMenu>> EFAB_CRAFTING =
            MENU_TYPES.register("efab_crafting", () -> IMenuTypeExtension.create(EFabCraftingMenu::new));

    public static final DeferredHolder<MenuType<?>, MenuType<StorageMenu>> STORAGE =
            MENU_TYPES.register("storage", () -> IMenuTypeExtension.create(StorageMenu::new));

    private ModMenus() {
    }
}
