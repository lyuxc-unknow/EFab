package mcjty.efab.registry;

import mcjty.efab.EFab;
import mcjty.efab.recipe.RecipeTier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(EFab.MODID);

    public static final DeferredItem<BlockItem> BASE = ITEMS.registerSimpleBlockItem(ModBlocks.BASE);
    public static final DeferredItem<BlockItem> GEARBOX = ITEMS.registerSimpleBlockItem(ModBlocks.GEARBOX);
    public static final DeferredItem<BlockItem> PIPES = ITEMS.registerSimpleBlockItem(ModBlocks.PIPES);
    public static final DeferredItem<BlockItem> GRID = ITEMS.registerSimpleBlockItem(ModBlocks.GRID);
    public static final DeferredItem<BlockItem> TANK = ITEMS.registerSimpleBlockItem(ModBlocks.TANK);
    public static final DeferredItem<BlockItem> ADVANCED_TANK = ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_TANK);
    public static final DeferredItem<BlockItem> BOILER = ITEMS.registerSimpleBlockItem(ModBlocks.BOILER);
    public static final DeferredItem<BlockItem> STEAM_ENGINE = ITEMS.registerSimpleBlockItem(ModBlocks.STEAM_ENGINE);
    public static final DeferredItem<BlockItem> FE_CONTROL = ITEMS.registerSimpleBlockItem(ModBlocks.FE_CONTROL);
    public static final DeferredItem<BlockItem> FE_STORAGE = ITEMS.registerSimpleBlockItem(ModBlocks.FE_STORAGE);
    public static final DeferredItem<BlockItem> ADVANCED_FE_STORAGE = ITEMS.registerSimpleBlockItem(ModBlocks.ADVANCED_FE_STORAGE);
    public static final DeferredItem<BlockItem> PROCESSOR = ITEMS.registerSimpleBlockItem(ModBlocks.PROCESSOR);
    public static final DeferredItem<BlockItem> MONITOR = ITEMS.registerSimpleBlockItem(ModBlocks.MONITOR);
    public static final DeferredItem<BlockItem> AUTOCRAFTING_MONITOR = ITEMS.registerSimpleBlockItem(ModBlocks.AUTOCRAFTING_MONITOR);
    public static final DeferredItem<BlockItem> STORAGE = ITEMS.registerSimpleBlockItem(ModBlocks.STORAGE);
    public static final DeferredItem<BlockItem> CRAFTER = ITEMS.registerSimpleBlockItem(ModBlocks.CRAFTER);
    public static final DeferredItem<BlockItem> POWER_OPTIMIZER = ITEMS.registerSimpleBlockItem(ModBlocks.POWER_OPTIMIZER);

    public static final DeferredItem<Item> UPGRADE_ARMORY = ITEMS.registerItem("upgrade_armory",
            properties -> new TierUpgradeItem(RecipeTier.UPGRADE_ARMORY, properties));
    public static final DeferredItem<Item> UPGRADE_MAGIC = ITEMS.registerItem("upgrade_magic",
            properties -> new TierUpgradeItem(RecipeTier.UPGRADE_MAGIC, properties));
    public static final DeferredItem<Item> UPGRADE_POWER = ITEMS.registerItem("upgrade_power",
            properties -> new TierUpgradeItem(RecipeTier.UPGRADE_POWER, properties));
    public static final DeferredItem<Item> UPGRADE_DIGITAL = ITEMS.registerItem("upgrade_digital",
            properties -> new TierUpgradeItem(RecipeTier.UPGRADE_DIGITAL, properties));

    private ModItems() {
    }

    public static class TierUpgradeItem extends Item {
        private final RecipeTier tier;

        public TierUpgradeItem(RecipeTier tier, Properties properties) {
            super(properties.stacksTo(16));
            this.tier = tier;
        }

        public RecipeTier getTier() {
            return tier;
        }

        @Override
        public void appendHoverText(net.minecraft.world.item.ItemStack stack, Item.TooltipContext context,
                                    java.util.List<net.minecraft.network.chat.Component> tooltip,
                                    net.minecraft.world.item.TooltipFlag flag) {
            tooltip.add(net.minecraft.network.chat.Component.translatable(upgradeTooltipKey())
                    .withStyle(net.minecraft.ChatFormatting.GRAY));
            super.appendHoverText(stack, context, tooltip, flag);
        }

        private String upgradeTooltipKey() {
            String name = tier.getSerializedName();
            if (name.startsWith("upgrade_")) {
                name = name.substring("upgrade_".length());
            }
            return "tooltip.efab.upgrade." + name;
        }
    }
}
