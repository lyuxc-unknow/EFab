package mcjty.efab.registry;

import mcjty.efab.EFab;
import mcjty.efab.block.GridBlock;
import mcjty.efab.block.HorizontalEFabEntityBlock;
import mcjty.efab.block.HorizontalTierPartBlock;
import mcjty.efab.block.NonFullHorizontalEFabEntityBlock;
import mcjty.efab.block.NonFullHorizontalTierPartBlock;
import mcjty.efab.block.TankBlock;
import mcjty.efab.block.TieredNonFullHorizontalEFabEntityBlock;
import mcjty.efab.block.TierPartBlock;
import mcjty.efab.blockentity.*;
import mcjty.efab.config.EFabConfig;
import mcjty.efab.recipe.RecipeTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(EFab.MODID);

    private static BlockBehaviour.Properties machineProperties() {
        return BlockBehaviour.Properties.of()
                .mapColor(MapColor.METAL)
                .requiresCorrectToolForDrops()
                .strength(3.0F, 6.0F);
    }

    private static BlockBehaviour.Properties nonFullMachineProperties() {
        return machineProperties().noOcclusion();
    }

    public static final DeferredBlock<Block> BASE = BLOCKS.register("base",
            () -> new TierPartBlock(machineProperties()));
    public static final DeferredBlock<Block> GEARBOX = BLOCKS.register("gearbox",
            () -> new TierPartBlock(machineProperties(), RecipeTier.GEARBOX));
    public static final DeferredBlock<Block> PIPES = BLOCKS.register("pipes",
            () -> new NonFullHorizontalTierPartBlock(nonFullMachineProperties()));
    public static final DeferredBlock<Block> BOILER = BLOCKS.register("boiler",
            () -> new NonFullHorizontalTierPartBlock(nonFullMachineProperties(), RecipeTier.STEAM));
    public static final DeferredBlock<Block> STEAM_ENGINE = BLOCKS.register("steamengine",
            () -> new TieredNonFullHorizontalEFabEntityBlock(nonFullMachineProperties(),
                    SteamEngineBlockEntity::new, RecipeTier.STEAM));
    public static final DeferredBlock<Block> PROCESSOR = BLOCKS.register("processor",
            () -> new HorizontalTierPartBlock(machineProperties(), RecipeTier.COMPUTING));
    public static final DeferredBlock<Block> MONITOR = BLOCKS.register("monitor",
            () -> new TieredNonFullHorizontalEFabEntityBlock(nonFullMachineProperties(),
                    (pos, state) -> new MonitorBlockEntity(pos, state, false), RecipeTier.COMPUTING));
    public static final DeferredBlock<Block> AUTOCRAFTING_MONITOR = BLOCKS.register("autocrafting_monitor",
            () -> new TieredNonFullHorizontalEFabEntityBlock(nonFullMachineProperties(),
                    (pos, state) -> new MonitorBlockEntity(pos, state, true), RecipeTier.COMPUTING));
    public static final DeferredBlock<Block> POWER_OPTIMIZER = BLOCKS.register("power_optimizer",
            () -> new NonFullHorizontalTierPartBlock(nonFullMachineProperties()));
    public static final DeferredBlock<Block> FE_CONTROL = BLOCKS.register("fe_control",
            () -> new NonFullHorizontalEFabEntityBlock(nonFullMachineProperties(), EnergyBlockEntity::new));
    public static final DeferredBlock<Block> FE_STORAGE = BLOCKS.register("fe_storage",
            () -> new HorizontalEFabEntityBlock(machineProperties(), EnergyBlockEntity::new));
    public static final DeferredBlock<Block> ADVANCED_FE_STORAGE = BLOCKS.register("advanced_fe_storage",
            () -> new HorizontalEFabEntityBlock(machineProperties(), EnergyBlockEntity::new));
    public static final DeferredBlock<Block> STORAGE = BLOCKS.register("storage",
            () -> new HorizontalEFabEntityBlock(machineProperties(), StorageBlockEntity::new));
    public static final DeferredBlock<Block> TANK = BLOCKS.register("tank",
            () -> new TankBlock(nonFullMachineProperties(), (pos, state) ->
                    new TankBlockEntity(pos, state, EFabConfig.TANK_CAPACITY.get())));
    public static final DeferredBlock<Block> ADVANCED_TANK = BLOCKS.register("advanced_tank",
            () -> new TankBlock(nonFullMachineProperties(), (pos, state) ->
                    new TankBlockEntity(pos, state, EFabConfig.ADVANCED_TANK_CAPACITY.get())));
    public static final DeferredBlock<Block> GRID = BLOCKS.register("grid",
            () -> new GridBlock(nonFullMachineProperties(), GridBlockEntity::new));
    public static final DeferredBlock<Block> CRAFTER = BLOCKS.register("crafter",
            () -> new NonFullHorizontalEFabEntityBlock(nonFullMachineProperties(), CrafterBlockEntity::new));

    private ModBlocks() {
    }
}
