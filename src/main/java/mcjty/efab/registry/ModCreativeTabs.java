package mcjty.efab.registry;

import mcjty.efab.EFab;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, EFab.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN =
            CREATIVE_MODE_TABS.register("main", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.efab"))
                    .icon(() -> new ItemStack(ModBlocks.GRID.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BASE.get());
                        output.accept(ModItems.GEARBOX.get());
                        output.accept(ModItems.PIPES.get());
                        output.accept(ModItems.GRID.get());
                        output.accept(ModItems.TANK.get());
                        output.accept(ModItems.ADVANCED_TANK.get());
                        output.accept(ModItems.BOILER.get());
                        output.accept(ModItems.STEAM_ENGINE.get());
                        output.accept(ModItems.FE_CONTROL.get());
                        output.accept(ModItems.FE_STORAGE.get());
                        output.accept(ModItems.ADVANCED_FE_STORAGE.get());
                        output.accept(ModItems.PROCESSOR.get());
                        output.accept(ModItems.MONITOR.get());
                        output.accept(ModItems.AUTOCRAFTING_MONITOR.get());
                        output.accept(ModItems.STORAGE.get());
                        output.accept(ModItems.CRAFTER.get());
                        output.accept(ModItems.POWER_OPTIMIZER.get());
                        output.accept(ModItems.UPGRADE_ARMORY.get());
                        output.accept(ModItems.UPGRADE_MAGIC.get());
                        output.accept(ModItems.UPGRADE_POWER.get());
                        output.accept(ModItems.UPGRADE_DIGITAL.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
