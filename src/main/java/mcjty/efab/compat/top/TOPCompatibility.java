package mcjty.efab.compat.top;

import mcjty.efab.EFab;
import mcjty.efab.blockentity.AbstractCraftingBlockEntity;
import mcjty.efab.blockentity.BoilerBlockEntity;
import mcjty.efab.blockentity.CrafterBlockEntity;
import mcjty.efab.blockentity.NamedCrafting;
import mcjty.efab.blockentity.StorageBlockEntity;
import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.ElementAlignment;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.InterModComms;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.InterModEnqueueEvent;

import java.util.Locale;
import java.util.function.Function;

public final class TOPCompatibility {

    private static final String TOP_MODID = "theoneprobe";

    private TOPCompatibility() {
    }

    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded(TOP_MODID)) {
            InterModComms.sendTo(TOP_MODID, "getTheOneProbe", GetTheOneProbe::new);
        }
    }

    public static final class GetTheOneProbe implements Function<ITheOneProbe, Void> {

        @Override
        public Void apply(ITheOneProbe probe) {
            probe.registerProvider(new EFabProbeInfoProvider());
            return null;
        }
    }

    private static final class EFabProbeInfoProvider implements IProbeInfoProvider {

        @Override
        public ResourceLocation getID() {
            return EFab.rl("efab_info");
        }

        @Override
        public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level,
                                 BlockState blockState, IProbeHitData data) {
            BlockPos pos = data.getPos();
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof CrafterBlockEntity crafter) {
                addCrafterInfo(probeInfo, level, pos, crafter);
            } else if (blockEntity instanceof AbstractCraftingBlockEntity crafting) {
                addCraftingInfo(probeInfo, crafting);
            } else if (blockEntity instanceof StorageBlockEntity storage) {
                addCraftingName(probeInfo, storage);
            } else if (blockEntity instanceof BoilerBlockEntity boiler) {
                probeInfo.text(CompoundText.create()
                        .label(Component.translatable("info.efab.temperature"))
                        .info(Component.translatable("info.efab.temperature.value",
                                String.format(Locale.ROOT, "%.1f", boiler.getTemperature()))));
            }
        }

        private static void addCrafterInfo(IProbeInfo probeInfo, Level level, BlockPos pos, CrafterBlockEntity crafter) {
            if (!crafter.isCrafting() && !level.hasNeighborSignal(pos)) {
                addStatus(probeInfo, Component.translatable("info.efab.status.off"), false);
            } else {
                addCraftingInfo(probeInfo, crafter);
            }
            addCraftingName(probeInfo, crafter);
        }

        private static void addCraftingInfo(IProbeInfo probeInfo, AbstractCraftingBlockEntity crafting) {
            if (crafting.isCrafting() && crafting.getRequiredTime() > 0) {
                int progress = Math.min(100, crafting.getProgress() * 100 / crafting.getRequiredTime());
                addStatus(probeInfo, Component.translatable("info.efab.status.progress", progress), false);
            } else {
                AbstractCraftingBlockEntity.CraftButtonState state = crafting.getCraftButtonState();
                boolean error = state != AbstractCraftingBlockEntity.CraftButtonState.NO_INPUT
                        && state != AbstractCraftingBlockEntity.CraftButtonState.READY;
                addStatus(probeInfo, error ? statusFor(state) : Component.translatable("info.efab.status.idle"), error);
            }

            ItemStack output = crafting.getCurrentResult();
            if (!output.isEmpty()) {
                probeInfo.horizontal(probeInfo.defaultLayoutStyle().alignment(ElementAlignment.ALIGN_CENTER))
                        .text(CompoundText.create().label(Component.translatable("info.efab.output")))
                        .item(output);
            }
        }

        private static void addCraftingName(IProbeInfo probeInfo, NamedCrafting namedCrafting) {
            String craftingName = namedCrafting.getCraftingName();
            if (craftingName != null && !craftingName.trim().isEmpty()) {
                probeInfo.text(CompoundText.create()
                        .label(Component.translatable("info.efab.name"))
                        .info(craftingName));
            }
        }

        private static void addStatus(IProbeInfo probeInfo, Component status, boolean error) {
            CompoundText text = CompoundText.create().label(Component.translatable("info.efab.status"));
            if (error) {
                text.error(status);
            } else {
                text.info(status);
            }
            probeInfo.text(text);
        }

        private static Component statusFor(AbstractCraftingBlockEntity.CraftButtonState state) {
            return Component.translatable("info.efab.status." + state.name().toLowerCase(Locale.ROOT));
        }
    }
}
