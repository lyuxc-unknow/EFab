package mcjty.efab.registry;

import com.mojang.datafixers.DSL;
import mcjty.efab.EFab;
import mcjty.efab.blockentity.BoilerBlockEntity;
import mcjty.efab.blockentity.CrafterBlockEntity;
import mcjty.efab.blockentity.EnergyBlockEntity;
import mcjty.efab.blockentity.GridBlockEntity;
import mcjty.efab.blockentity.MonitorBlockEntity;
import mcjty.efab.blockentity.StorageBlockEntity;
import mcjty.efab.blockentity.SteamEngineBlockEntity;
import mcjty.efab.blockentity.TankBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, EFab.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<EnergyBlockEntity>> ENERGY_STORAGE =
            BLOCK_ENTITY_TYPES.register("energy_storage", () -> BlockEntityType.Builder.of(
                    EnergyBlockEntity::new,
                    ModBlocks.FE_CONTROL.get(),
                    ModBlocks.FE_STORAGE.get(),
                    ModBlocks.ADVANCED_FE_STORAGE.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TankBlockEntity>> TANK =
            BLOCK_ENTITY_TYPES.register("tank", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new TankBlockEntity(pos, state, 16000),
                    ModBlocks.TANK.get(),
                    ModBlocks.ADVANCED_TANK.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StorageBlockEntity>> STORAGE =
            BLOCK_ENTITY_TYPES.register("storage", () -> BlockEntityType.Builder.of(
                    StorageBlockEntity::new,
                    ModBlocks.STORAGE.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GridBlockEntity>> GRID =
            BLOCK_ENTITY_TYPES.register("grid", () -> BlockEntityType.Builder.of(
                    GridBlockEntity::new,
                    ModBlocks.GRID.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CrafterBlockEntity>> CRAFTER =
            BLOCK_ENTITY_TYPES.register("crafter", () -> BlockEntityType.Builder.of(
                    CrafterBlockEntity::new,
                    ModBlocks.CRAFTER.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SteamEngineBlockEntity>> STEAM_ENGINE =
            BLOCK_ENTITY_TYPES.register("steamengine", () -> BlockEntityType.Builder.of(
                    SteamEngineBlockEntity::new,
                    ModBlocks.STEAM_ENGINE.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BoilerBlockEntity>> BOILER =
            BLOCK_ENTITY_TYPES.register("boiler", () -> BlockEntityType.Builder.of(
                    BoilerBlockEntity::new,
                    ModBlocks.BOILER.get()
            ).build(DSL.emptyPartType()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MonitorBlockEntity>> MONITOR =
            BLOCK_ENTITY_TYPES.register("monitor", () -> BlockEntityType.Builder.of(
                    (pos, state) -> new MonitorBlockEntity(pos, state, false),
                    ModBlocks.MONITOR.get(),
                    ModBlocks.AUTOCRAFTING_MONITOR.get()
            ).build(DSL.emptyPartType()));

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK, ENERGY_STORAGE.get(),
                EnergyBlockEntity::getEnergy);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, TANK.get(),
                TankBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, STORAGE.get(),
                StorageBlockEntity::getItems);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GRID.get(),
                GridBlockEntity::getItems);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CRAFTER.get(),
                CrafterBlockEntity::getItems);
    }

    private ModBlockEntities() {
    }
}
