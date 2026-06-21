package mcjty.efab.tooltip;

import mcjty.efab.config.EFabConfig;
import mcjty.efab.registry.ModBlocks;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * Builds the localized hover tooltips for EFab blocks and items. All text comes from the
 * language files (keys under {@code tooltip.efab.*}); colors are applied here so the strings
 * stay translation-friendly.
 */
public final class EFabTooltips {

    private EFabTooltips() {
    }

    private static void line(List<Component> tooltip, String key, ChatFormatting color, Object... args) {
        tooltip.add(Component.translatable(key, args).withStyle(color));
    }

    private static void speedup(List<Component> tooltip, int max) {
        if (max > 1) {
            line(tooltip, "tooltip.efab.speedup", ChatFormatting.GOLD, max);
        }
    }

    public static void appendBlockTooltip(Block block, List<Component> tooltip) {
        if (block == ModBlocks.GRID.get()) {
            line(tooltip, "tooltip.efab.grid.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.grid.2", ChatFormatting.WHITE);
        } else if (block == ModBlocks.BASE.get()) {
            line(tooltip, "tooltip.efab.base.1", ChatFormatting.WHITE);
        } else if (block == ModBlocks.GEARBOX.get()) {
            line(tooltip, "tooltip.efab.gearbox.1", ChatFormatting.WHITE);
            speedup(tooltip, EFabConfig.MAX_SPEEDUP_BONUS.get());
        } else if (block == ModBlocks.PROCESSOR.get()) {
            line(tooltip, "tooltip.efab.processor.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.processor.2", ChatFormatting.WHITE);
            speedup(tooltip, EFabConfig.MAX_SPEEDUP_BONUS.get());
        } else if (block == ModBlocks.STEAM_ENGINE.get()) {
            line(tooltip, "tooltip.efab.steamengine.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.steamengine.2", ChatFormatting.WHITE);
            speedup(tooltip, EFabConfig.MAX_SPEEDUP_BONUS.get());
        } else if (block == ModBlocks.BOILER.get()) {
            line(tooltip, "tooltip.efab.boiler.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.boiler.2", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.boiler.3", ChatFormatting.WHITE);
        } else if (block == ModBlocks.PIPES.get()) {
            line(tooltip, "tooltip.efab.pipes.1", ChatFormatting.WHITE);
            speedup(tooltip, EFabConfig.MAX_PIPE_SPEED_BONUS.get());
        } else if (block == ModBlocks.TANK.get()) {
            line(tooltip, "tooltip.efab.tank.1", ChatFormatting.WHITE, EFabConfig.TANK_CAPACITY.get());
            line(tooltip, "tooltip.efab.tank.2", ChatFormatting.WHITE);
        } else if (block == ModBlocks.ADVANCED_TANK.get()) {
            line(tooltip, "tooltip.efab.tank.1", ChatFormatting.WHITE, EFabConfig.ADVANCED_TANK_CAPACITY.get());
            line(tooltip, "tooltip.efab.tank.2", ChatFormatting.WHITE);
        } else if (block == ModBlocks.FE_CONTROL.get()) {
            line(tooltip, "tooltip.efab.fe.store", ChatFormatting.WHITE, EFabConfig.FE_CONTROL_CAPACITY.get());
            line(tooltip, "tooltip.efab.fe.flow", ChatFormatting.WHITE, EFabConfig.FE_CONTROL_FLOW.get());
            speedup(tooltip, EFabConfig.MAX_SPEEDUP_BONUS.get());
        } else if (block == ModBlocks.FE_STORAGE.get()) {
            line(tooltip, "tooltip.efab.fe.store", ChatFormatting.WHITE, EFabConfig.FE_STORAGE_CAPACITY.get());
            line(tooltip, "tooltip.efab.fe.flow", ChatFormatting.WHITE, EFabConfig.FE_STORAGE_FLOW.get());
        } else if (block == ModBlocks.ADVANCED_FE_STORAGE.get()) {
            line(tooltip, "tooltip.efab.fe.store", ChatFormatting.WHITE, EFabConfig.ADVANCED_FE_STORAGE_CAPACITY.get());
            line(tooltip, "tooltip.efab.fe.flow", ChatFormatting.WHITE, EFabConfig.ADVANCED_FE_STORAGE_FLOW.get());
        } else if (block == ModBlocks.STORAGE.get()) {
            line(tooltip, "tooltip.efab.storage.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.name_match", ChatFormatting.GOLD);
        } else if (block == ModBlocks.CRAFTER.get()) {
            line(tooltip, "tooltip.efab.crafter.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.crafter.2", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.crafter.3", ChatFormatting.YELLOW);
            line(tooltip, "tooltip.efab.name_match", ChatFormatting.GOLD);
        } else if (block == ModBlocks.MONITOR.get()) {
            line(tooltip, "tooltip.efab.monitor.1", ChatFormatting.WHITE);
        } else if (block == ModBlocks.AUTOCRAFTING_MONITOR.get()) {
            line(tooltip, "tooltip.efab.autocrafting_monitor.1", ChatFormatting.WHITE);
        } else if (block == ModBlocks.POWER_OPTIMIZER.get()) {
            line(tooltip, "tooltip.efab.power_optimizer.1", ChatFormatting.WHITE);
            line(tooltip, "tooltip.efab.power_optimizer.2", ChatFormatting.WHITE);
        }
    }
}
