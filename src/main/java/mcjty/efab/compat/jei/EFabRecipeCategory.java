package mcjty.efab.compat.jei;

import mcjty.efab.EFab;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.recipe.FluidRequirement;
import mcjty.efab.recipe.RecipeTier;
import mcjty.efab.registry.ModItems;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;

import javax.annotation.ParametersAreNonnullByDefault;
import java.text.DecimalFormat;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class EFabRecipeCategory implements IRecipeCategory<EFabRecipe> {

    private static final ResourceLocation BACKGROUND = EFab.rl("textures/gui/grid_jei.png");
    private static final ResourceLocation ICONS = EFab.rl("textures/gui/icons.png");
    private static final int WIDTH = 140;
    private static final int HEIGHT = 110;
    private static final DecimalFormat POWER_FORMAT = new DecimalFormat("#.##");

    private final IDrawable background;
    private final IDrawable icon;

    public EFabRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(BACKGROUND, 29, 16, WIDTH, HEIGHT);
        this.icon = guiHelper.createDrawableItemLike(ModItems.GRID.get());
    }

    @Override
    public RecipeType<EFabRecipe> getRecipeType() {
        return EFabJeiPlugin.EFAB_GRID;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.efab.grid");
    }

    @Override
    public int getWidth() {
        return background.getWidth();
    }

    @Override
    public int getHeight() {
        return background.getHeight();
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, EFabRecipe recipe, IFocusGroup focuses) {
        if (recipe.isShaped()) {
            addShapedInputs(builder, recipe);
        } else {
            addShapelessInputs(builder, recipe);
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 95, 19)
                .setOutputSlotBackground()
                .addItemStack(recipe.getResultItem(Minecraft.getInstance().level.registryAccess()));

        int fluidX = 80;
        int fluidY = infoStartY(recipe);
        for (FluidRequirement requirement : recipe.fluids()) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            builder.addSlot(RecipeIngredientRole.INPUT, fluidX, fluidY)
                    .setFluidRenderer(requirement.amount(), false, 16, 16)
                    .addFluidStack(fluid, requirement.amount());
            fluidX += 20;
        }
    }

    private static void addShapedInputs(IRecipeLayoutBuilder builder, EFabRecipe recipe) {
        for (int y = 0; y < 3; y++) {
            String row = y < recipe.pattern().size() ? recipe.pattern().get(y) : "";
            for (int x = 0; x < 3; x++) {
                if (x >= row.length() || row.charAt(x) == ' ') {
                    continue;
                }
                Ingredient ingredient = recipe.key().get(String.valueOf(row.charAt(x)));
                if (ingredient != null && ingredient != Ingredient.EMPTY) {
                    builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1)
                            .setStandardSlotBackground()
                            .addIngredients(ingredient);
                }
            }
        }
    }

    private static void addShapelessInputs(IRecipeLayoutBuilder builder, EFabRecipe recipe) {
        builder.setShapeless(0, 54);
        for (int i = 0; i < recipe.efabIngredients().size(); i++) {
            int x = i % 3;
            int y = i / 3;
            builder.addSlot(RecipeIngredientRole.INPUT, x * 18 + 1, y * 18 + 1)
                    .setStandardSlotBackground()
                    .addIngredients(recipe.efabIngredients().get(i));
        }
    }

    @Override
    public void draw(EFabRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        guiGraphics.blit(BACKGROUND,0,0,29, 16, WIDTH, HEIGHT);
        int y = 60;
        if (recipe.time() > 0) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.efab.time"), 0, y, 0xFF000000, false);
            guiGraphics.drawString(Minecraft.getInstance().font, formatTime(recipe.time()), 28, y, 0xFF0000AA, false);
            y += 12;
        }
        if (recipe.fePerTick() > 0) {
            guiGraphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.efab.energy"), 0, y, 0xFF000000, false);
            long total = recipe.fePerTick() * (long) recipe.time();
            guiGraphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("jei.efab.energy.detail", formatPower(total), formatPower(recipe.fePerTick())),
                    28, y, 0xFF0000AA, false);
            y += 12;
        }

        int iconX = 0;
        for (RecipeTier tier : recipe.tiers()) {
            guiGraphics.blit(ICONS, iconX, y, tier.getIconX(), tier.getIconY(), 16, 16, 256, 256);
            iconX += 20;
            if (iconX + 18 >= 80) {
                iconX = 0;
                y += 20;
            }
        }

        if (recipe.fluids().size() == 1) {
            FluidRequirement requirement = recipe.fluids().getFirst();
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            guiGraphics.drawString(Minecraft.getInstance().font,
                    fluid.getFluidType().getDescription(),
                    100, infoStartY(recipe) + 5, 0xFF0000AA, false);
        }

        if (!recipe.requirements().isEmpty()) {
            guiGraphics.drawString(Minecraft.getInstance().font,
                    Component.translatable("jei.efab.requirements"),
                    80, requirementsStartY(recipe) + 5, 0xFF0000AA, false);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, EFabRecipe recipe, IRecipeSlotsView recipeSlotsView, double mouseX, double mouseY) {
        int y = infoStartY(recipe);
        int x = 0;
        for (RecipeTier tier : recipe.tiers()) {
            if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                tooltip.add(Component.translatable("jei.efab.tier." + tier.getSerializedName()).withStyle(ChatFormatting.BLUE));
            }
            x += 20;
            if (x + 18 >= 80) {
                x = 0;
                y += 20;
            }
        }

        if (!recipe.requirements().isEmpty()) {
            int requirementsY = requirementsStartY(recipe);
            if (mouseX >= 80 && mouseX < WIDTH && mouseY >= requirementsY && mouseY < requirementsY + 16) {
                tooltip.add(Component.translatable("jei.efab.requirements").withStyle(ChatFormatting.BLUE));
                for (EFabRecipeRequirement requirement : recipe.requirements()) {
                    tooltip.add(requirement.description());
                }
            }
        }
    }

    private static int infoStartY(EFabRecipe recipe) {
        int y = 60;
        if (recipe.time() > 0) {
            y += 12;
        }
        if (recipe.fePerTick() > 0) {
            y += 12;
        }
        return y;
    }

    private static int requirementsStartY(EFabRecipe recipe) {
        int y = infoStartY(recipe);
        if (!recipe.fluids().isEmpty()) {
            y += 20;
        }
        return y;
    }

    private static String formatTime(int ticks) {
        float seconds = ticks / 20.0f;
        if (seconds >= 60.0f) {
            float minutes = seconds / 60.0f;
            if (minutes >= 60.0f) {
                return Component.translatable("gui.efab.time.hours", POWER_FORMAT.format(minutes / 60.0f)).getString();
            }
            return Component.translatable("gui.efab.time.minutes", POWER_FORMAT.format(minutes)).getString();
        }
        return Component.translatable("gui.efab.time.seconds", POWER_FORMAT.format(seconds)).getString();
    }

    private static String formatPower(long amount) {
        if (amount < 10000) {
            return Long.toString(amount);
        }
        if (amount < 1000000) {
            return POWER_FORMAT.format(amount / 1000.0) + "K";
        }
        if (amount < 1000000000L) {
            return POWER_FORMAT.format(amount / 1000000.0) + "M";
        }
        return POWER_FORMAT.format(amount / 1000000000.0) + "G";
    }
}
