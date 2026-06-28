package mcjty.efab.blockentity;

import mcjty.efab.api.recipe.EFabCraftingContext;
import mcjty.efab.api.recipe.EFabRecipeRequirement;
import mcjty.efab.api.recipe.EFabRequirementPhase;
import mcjty.efab.block.TierProvider;
import mcjty.efab.config.EFabConfig;
import mcjty.efab.registry.ModItems;
import mcjty.efab.registry.ModBlocks;
import mcjty.efab.registry.ModSounds;
import mcjty.efab.recipe.EFabRecipe;
import mcjty.efab.recipe.EFabRecipeInput;
import mcjty.efab.recipe.FluidRequirement;
import mcjty.efab.recipe.RecipeTier;
import mcjty.efab.menu.EFabCraftingMenu;
import mcjty.efab.registry.ModRecipes;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class AbstractCraftingBlockEntity extends BlockEntity implements ServerTickingBlockEntity, MenuProvider {

    public enum CraftButtonState {
        NO_INPUT,
        MISSING_INGREDIENTS,
        NO_RECIPE,
        OUTPUT_FULL,
        MISSING_FE_CONTROL,
        MISSING_REQUIREMENTS,
        MISSING_FLUIDS,
        MISSING_ENERGY,
        READY
    }

    protected static final int INPUT_SLOTS = 9;
    protected static final int OUTPUT_START = 9;
    protected static final int OUTPUT_SLOTS = 3;
    protected static final int UPGRADE_START = 12;
    protected static final int UPGRADE_SLOTS = 9;
    protected static final int GHOST_SLOT = 21;
    protected static final int SLOT_COUNT = 22;
    private static final int MACHINE_SOUND_TICKS = 50;
    private static final int SPARKS_SOUND_TICKS = 25;
    private static final int STEAM_SOUND_TICKS = 50;
    private static final int BEEPS_SOUND_TICKS = 8;
    private static final float MACHINE_SOUND_VOLUME = 1.0F;
    private static final float SPARKS_SOUND_VOLUME = 0.7F;
    private static final float STEAM_SOUND_VOLUME = 1.0F;
    private static final float BEEPS_SOUND_VOLUME = 0.2F;

    private final ItemStackHandler items = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    protected int progress;
    protected int requiredTime = -1;
    protected boolean crafting;
    protected boolean repeatCrafting;
    private int feWarning;
    private int machineSoundCooldown;
    private int sparksSoundCooldown;
    private int steamSoundCooldown;
    private int beepsSoundCooldown;
    private final RandomSource soundRandom = RandomSource.create();

    protected AbstractCraftingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public IItemHandler getItems(Direction direction) {
        return items;
    }

    public ItemStackHandler getItemHandler() {
        return items;
    }

    public void dropContents() {
        if (level == null || level.isClientSide) {
            return;
        }
        for (int i = 0; i < items.getSlots(); i++) {
            if (!shouldDropSlot(i)) {
                continue;
            }
            ItemStack stack = items.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack.copy());
                items.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    protected boolean shouldDropSlot(int slot) {
        return slot != GHOST_SLOT;
    }

    public int getProgress() {
        return progress;
    }

    public int getRequiredTime() {
        return requiredTime;
    }

    public boolean isCrafting() {
        return crafting;
    }

    public CraftButtonState getCraftButtonState() {
        if (level == null) {
            return CraftButtonState.NO_RECIPE;
        }
        Optional<EFabRecipe> recipe = findRecipe(level);
        if (recipe.isEmpty()) {
            if (isInputEmpty()) {
                return CraftButtonState.NO_INPUT;
            }
            return hasPartialRecipe() ? CraftButtonState.MISSING_INGREDIENTS : CraftButtonState.NO_RECIPE;
        }

        EFabRecipe efabRecipe = recipe.get();
        if (!hasRoom(efabRecipe)) {
            return CraftButtonState.OUTPUT_FULL;
        }
        if (needsFeControl(efabRecipe) && !hasFeControl()) {
            return CraftButtonState.MISSING_FE_CONTROL;
        }
        if (!hasTiers(efabRecipe)) {
            return CraftButtonState.MISSING_REQUIREMENTS;
        }
        if (!hasRequiredFluids(efabRecipe)) {
            return CraftButtonState.MISSING_FLUIDS;
        }
        MachineCounts counts = scan();
        int bonus = Math.max(1, speedBonus(efabRecipe, counts));
        if (!hasRecipeRequirements(efabRecipe, EFabRequirementPhase.START, 1, bonus)
                || !hasRecipeRequirements(efabRecipe, EFabRequirementPhase.TICK, bonus, bonus)
                || !hasRecipeRequirements(efabRecipe, EFabRequirementPhase.FINISH, 1, bonus)) {
            return CraftButtonState.MISSING_REQUIREMENTS;
        }
        int energyPerTick = efabRecipe.fePerTick() * bonus;
        if (energyPerTick > 0 && extractEnergy(energyPerTick, true) < energyPerTick) {
            return CraftButtonState.MISSING_ENERGY;
        }
        return CraftButtonState.READY;
    }

    public ItemStack getCurrentResult() {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        return findRecipe(level)
                .map(recipe -> recipe.getResultItem(level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    public void requestImmediateCraft() {
        if (level == null || level.isClientSide) {
            return;
        }
        findRecipe(level).ifPresent(recipe -> {
            MachineCounts counts = scan();
            int bonus = Math.max(1, speedBonus(recipe, counts));
            int tickAmount = Math.max(1, recipeRequiredTime(recipe, bonus) * bonus);
            if (canCraft(recipe)
                    && hasRecipeRequirements(recipe, EFabRequirementPhase.START, 1, bonus)
                    && hasRecipeRequirements(recipe, EFabRequirementPhase.TICK, tickAmount, bonus)) {
                int energy = recipe.fePerTick() * Math.max(1, recipe.time());
                if (energy <= 0 || extractEnergy(energy, true) >= energy) {
                    if (!consumeRecipeRequirements(recipe, EFabRequirementPhase.START, 1, bonus)) {
                        return;
                    }
                    if (!consumeRecipeRequirements(recipe, EFabRequirementPhase.TICK, tickAmount, bonus)) {
                        return;
                    }
                    if (energy > 0) {
                        extractEnergy(energy, false);
                    }
                    if (craft(recipe)) {
                        progress = 0;
                        requiredTime = -1;
                    }
                }
            }
        });
    }

    public void startCraft(boolean repeat) {
        if (level == null || level.isClientSide || crafting) {
            return;
        }
        findRecipe(level).ifPresent(recipe -> beginCraft(recipe, repeat));
    }

    protected boolean beginCraft(EFabRecipe recipe, boolean repeat) {
        MachineCounts counts = scan();
        int bonus = Math.max(1, speedBonus(recipe, counts));
        if (!canCraft(recipe) || !hasRecipeRequirements(recipe, EFabRequirementPhase.START, 1, bonus)) {
            return false;
        }
        if (!consumeRecipeRequirements(recipe, EFabRequirementPhase.START, 1, bonus)) {
            return false;
        }
        repeatCrafting = repeat;
        crafting = true;
        progress = 0;
        feWarning = 0;
        requiredTime = recipeRequiredTime(recipe, bonus);
        setChanged();
        updateNearbyMonitors();
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new EFabCraftingMenu(containerId, playerInventory, this);
    }

    @Override
    public void serverTick() {
        if (level == null || level.isClientSide) {
            return;
        }
        tickSoundCooldowns();
        if (!crafting) {
            updateNearbyMonitors();
            return;
        }

        Optional<EFabRecipe> recipe = findRecipe(level);
        if (recipe.isEmpty() || !canCraft(recipe.get())) {
            stopCrafting();
            updateNearbyMonitors();
            return;
        }

        EFabRecipe efabRecipe = recipe.get();
        MachineCounts counts = scan();
        int bonus = speedBonus(efabRecipe, counts);
        requiredTime = recipeRequiredTime(efabRecipe, bonus);
        int fePerTick = efabRecipe.fePerTick() * bonus;
        int requirementTickAmount = Math.max(1, bonus);

        if (counts.powerOptimizer() && fePerTick > 0) {
            // Power optimizer: progress as fast as stored power (and steam) allows, ignoring per-tick flow caps.
            boolean advanced = false;
            boolean blockedByRequiredResource = false;
            int startProgress = progress;
            while (progress < requiredTime) {
                if (extractEnergyUncapped(fePerTick, true) < fePerTick) {
                    break;
                }
                if (requiresSteam(efabRecipe) && drainFluid(steamWater(bonus), true) < steamWater(bonus).getAmount()) {
                    blockedByRequiredResource = true;
                    break;
                }
                if (!hasRecipeRequirements(efabRecipe, EFabRequirementPhase.TICK, requirementTickAmount, bonus)) {
                    blockedByRequiredResource = true;
                    break;
                }
                if (!consumeRecipeRequirements(efabRecipe, EFabRequirementPhase.TICK, requirementTickAmount, bonus)) {
                    blockedByRequiredResource = true;
                    break;
                }
                extractEnergyUncapped(fePerTick, false);
                if (requiresSteam(efabRecipe)) {
                    drainFluid(steamWater(bonus), false);
                }
                progress++;
                advanced = true;
            }
            if (advanced) {
                feWarning = 0;
                updateCraftingSounds(efabRecipe, startProgress);
            } else if (blockedByRequiredResource || !waitForResources()) {
                stopCrafting();
                updateNearbyMonitors();
                return;
            }
        } else {
            if (fePerTick > 0 && extractEnergy(fePerTick, true) < fePerTick) {
                if (!waitForResources()) {
                    stopCrafting();
                }
                updateNearbyMonitors();
                return;
            }
            if (!hasRecipeRequirements(efabRecipe, EFabRequirementPhase.TICK, requirementTickAmount, bonus)) {
                stopCrafting();
                updateNearbyMonitors();
                return;
            }
            if (requiresSteam(efabRecipe)) {
                FluidStack water = steamWater(bonus);
                if (drainFluid(water, true) < water.getAmount()) {
                    stopCrafting();
                    updateNearbyMonitors();
                    return;
                }
            }
            if (!consumeRecipeRequirements(efabRecipe, EFabRequirementPhase.TICK, requirementTickAmount, bonus)) {
                stopCrafting();
                updateNearbyMonitors();
                return;
            }
            int startProgress = progress;
            if (fePerTick > 0) {
                extractEnergy(fePerTick, false);
            }
            feWarning = 0;
            if (requiresSteam(efabRecipe)) {
                drainFluid(steamWater(bonus), false);
            }
            progress++;
            updateCraftingSounds(efabRecipe, startProgress);
        }

        if (requiresSteam(efabRecipe)) {
            driveSteamAnimation();
        }

        if (progress >= requiredTime) {
            if (!craft(efabRecipe)) {
                stopCrafting();
                updateNearbyMonitors();
                return;
            }
            boolean repeat = repeatCrafting;
            if (repeat) {
                Optional<EFabRecipe> nextRecipe = findRecipe(level);
                if (nextRecipe.isPresent() && beginCraft(nextRecipe.get(), true)) {
                } else {
                    stopCrafting();
                }
            } else {
                stopCrafting();
            }
        }
        updateNearbyMonitors();
    }

    // Returns true if the craft should keep waiting (paused) instead of aborting because of missing FE.
    private boolean waitForResources() {
        int allowed = EFabConfig.TICKS_ALLOWED_WITHOUT_FE.get();
        if (allowed < 0) {
            return true;
        }
        feWarning++;
        return feWarning <= allowed;
    }

    protected boolean canCraft(EFabRecipe recipe) {
        MachineCounts counts = scan();
        int bonus = Math.max(1, speedBonus(recipe, counts));
        return hasRoom(recipe)
                && hasTiers(recipe)
                && hasRequiredFluids(recipe)
                && hasRecipeRequirements(recipe, EFabRequirementPhase.TICK, bonus, bonus)
                && hasRecipeRequirements(recipe, EFabRequirementPhase.FINISH, 1, bonus);
    }

    protected void stopCrafting() {
        crafting = false;
        repeatCrafting = false;
        progress = 0;
        requiredTime = -1;
        feWarning = 0;
        setChanged();
    }

    private void tickSoundCooldowns() {
        if (machineSoundCooldown > 0) {
            machineSoundCooldown--;
        }
        if (sparksSoundCooldown > 0) {
            sparksSoundCooldown--;
        }
        if (steamSoundCooldown > 0) {
            steamSoundCooldown--;
        }
        if (beepsSoundCooldown > 0) {
            beepsSoundCooldown--;
        }
    }

    private void updateCraftingSounds(EFabRecipe recipe, int startProgress) {
        Set<RecipeTier> tiers = recipe.tiers();
        if (tiers.contains(RecipeTier.STEAM) && steamSoundCooldown <= 0) {
            playCraftingSound(ModSounds.STEAM.get(), STEAM_SOUND_VOLUME);
            emitSteamBurstAtRandomBoiler();
            steamSoundCooldown = STEAM_SOUND_TICKS;
        }
        if (tiers.contains(RecipeTier.GEARBOX) && machineSoundCooldown <= 0) {
            playCraftingSound(ModSounds.MACHINE.get(), MACHINE_SOUND_VOLUME);
            machineSoundCooldown = MACHINE_SOUND_TICKS;
        }
        if (tiers.contains(RecipeTier.COMPUTING) && beepsSoundCooldown <= 0 && shouldPlayShortSound(startProgress)) {
            playCraftingSound(soundRandom.nextBoolean() ? ModSounds.BEEPS1.get() : ModSounds.BEEPS2.get(), BEEPS_SOUND_VOLUME);
            beepsSoundCooldown = BEEPS_SOUND_TICKS;
        }
        if ((recipe.fePerTick() > 0 || tiers.contains(RecipeTier.FE)) && sparksSoundCooldown <= 0 && shouldPlayShortSound(startProgress)) {
            playCraftingSound(ModSounds.SPARKS.get(), SPARKS_SOUND_VOLUME);
            emitFeControlSparks();
            sparksSoundCooldown = SPARKS_SOUND_TICKS;
        }
    }

    private boolean shouldPlayShortSound(int startProgress) {
        return startProgress == 0 || soundRandom.nextFloat() < 0.04F;
    }

    private void playCraftingSound(SoundEvent sound, float volume) {
        level.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, 1.0F);
    }

    private void emitSteamBurstAtRandomBoiler() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<BlockPos> boilers = new ArrayList<>();
        for (BlockPos pos : craftingArea()) {
            if (isValidSteamBoiler(pos)) {
                boilers.add(pos.immutable());
            }
        }
        if (!boilers.isEmpty()) {
            emitBoilerSteamBurst(serverLevel, boilers.get(soundRandom.nextInt(boilers.size())));
        }
    }

    private void emitFeControlSparks() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<BlockPos> controls = new ArrayList<>();
        for (BlockPos pos : craftingArea()) {
            if (level.getBlockState(pos).is(ModBlocks.FE_CONTROL.get())) {
                controls.add(pos.immutable());
            }
        }
        if (controls.isEmpty()) {
            return;
        }

        BlockPos pos = controls.get(soundRandom.nextInt(controls.size()));
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.65;
        double z = pos.getZ() + 0.5;
        serverLevel.sendParticles(ParticleTypes.ELECTRIC_SPARK, x, y, z, 12, 0.35, 0.25, 0.35, 0.03);
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.1, z, 2, 0.15, 0.05, 0.15, 0.005);
    }

    protected Iterable<BlockPos> craftingArea() {
        return BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3));
    }

    protected Optional<EFabRecipe> findRecipe(Level level) {
        EFabRecipeInput input = new EFabRecipeInput(inputStacks(), 3, 3);
        for (RecipeHolder<EFabRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.EFAB_TYPE.get())) {
            EFabRecipe recipe = holder.value();
            if (recipe.matches(input, level)) {
                return Optional.of(recipe);
            }
        }
        return Optional.empty();
    }

    private boolean isInputEmpty() {
        for (int i = 0; i < INPUT_SLOTS; i++) {
            if (!items.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasPartialRecipe() {
        EFabRecipeInput input = new EFabRecipeInput(inputStacks(), 3, 3);
        for (RecipeHolder<EFabRecipe> holder : level.getRecipeManager().getAllRecipesFor(ModRecipes.EFAB_TYPE.get())) {
            EFabRecipe recipe = holder.value();
            if (recipe.isShaped() ? partiallyMatchesShaped(recipe, input) : partiallyMatchesShapeless(recipe, input)) {
                return true;
            }
        }
        return false;
    }

    private static boolean partiallyMatchesShaped(EFabRecipe recipe, EFabRecipeInput input) {
        boolean missingRequiredSlot = false;
        boolean usedAnyIngredient = false;
        for (int y = 0; y < 3; y++) {
            String row = y < recipe.pattern().size() ? recipe.pattern().get(y) : "";
            for (int x = 0; x < 3; x++) {
                Ingredient ingredient = Ingredient.EMPTY;
                if (x < row.length() && row.charAt(x) != ' ') {
                    ingredient = recipe.key().getOrDefault(String.valueOf(row.charAt(x)), Ingredient.EMPTY);
                }
                ItemStack stack = input.getItem(y * 3 + x);
                if (ingredient == Ingredient.EMPTY) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else if (stack.isEmpty()) {
                    missingRequiredSlot = true;
                } else if (ingredient.test(stack)) {
                    usedAnyIngredient = true;
                } else {
                    return false;
                }
            }
        }
        return usedAnyIngredient && missingRequiredSlot;
    }

    private static boolean partiallyMatchesShapeless(EFabRecipe recipe, EFabRecipeInput input) {
        List<Ingredient> remaining = new ArrayList<>(recipe.efabIngredients());
        boolean usedAnyIngredient = false;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (int j = 0; j < remaining.size(); j++) {
                if (remaining.get(j).test(stack)) {
                    remaining.remove(j);
                    usedAnyIngredient = true;
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                return false;
            }
        }
        return usedAnyIngredient && !remaining.isEmpty();
    }

    protected List<ItemStack> inputStacks() {
        List<ItemStack> stacks = new ArrayList<>(INPUT_SLOTS);
        for (int i = 0; i < INPUT_SLOTS; i++) {
            stacks.add(items.getStackInSlot(i).copy());
        }
        return stacks;
    }

    protected boolean hasRoom(EFabRecipe recipe) {
        ItemStack result = recipe.getResultItem(level.registryAccess());
        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_SLOTS; slot++) {
            ItemStack current = items.getStackInSlot(slot);
            if (current.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(current, result)
                    && current.getCount() + result.getCount() <= current.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    // ---- Multiblock scan & speed bonus ----------------------------------------------------------

    protected record MachineCounts(int gearboxes, int processors, int steamEngines, int feControls,
                                   int pipes, boolean powerOptimizer) {
    }

    protected MachineCounts scan() {
        int gearboxes = 0;
        int processors = 0;
        int steamEngines = 0;
        int feControls = 0;
        int pipes = 0;
        boolean powerOptimizer = false;
        for (BlockPos pos : craftingArea()) {
            BlockState state = level.getBlockState(pos);
            if (state.is(ModBlocks.GEARBOX.get())) {
                gearboxes++;
            } else if (state.is(ModBlocks.PROCESSOR.get())) {
                processors++;
            } else if (state.is(ModBlocks.STEAM_ENGINE.get())) {
                steamEngines++;
            } else if (state.is(ModBlocks.FE_CONTROL.get())) {
                feControls++;
            } else if (state.is(ModBlocks.PIPES.get())) {
                pipes++;
            } else if (state.is(ModBlocks.POWER_OPTIMIZER.get())) {
                powerOptimizer = true;
            }
        }
        return new MachineCounts(gearboxes, processors, steamEngines, feControls, pipes, powerOptimizer);
    }

    protected int speedBonus(EFabRecipe recipe, MachineCounts counts) {
        int max = EFabConfig.MAX_SPEEDUP_BONUS.get();
        int pipeMax = EFabConfig.MAX_PIPE_SPEED_BONUS.get();
        Set<RecipeTier> tiers = recipe.tiers();
        int bonus = 1;
        if (tiers.contains(RecipeTier.GEARBOX) && counts.gearboxes() > 1) {
            bonus = Math.clamp(counts.gearboxes(), bonus, max);
        }
        if (tiers.contains(RecipeTier.STEAM) && counts.steamEngines() > 1) {
            bonus = Math.clamp(counts.steamEngines(), bonus, max);
        }
        if (tiers.contains(RecipeTier.FE) && counts.feControls() > 1) {
            bonus = Math.clamp(counts.feControls(), bonus, max);
        }
        if (tiers.contains(RecipeTier.COMPUTING) && counts.processors() > 1) {
            bonus = Math.clamp(counts.processors(), bonus, max);
        }
        if (tiers.contains(RecipeTier.LIQUID) && counts.pipes() > 1) {
            bonus = Math.clamp(counts.pipes(), bonus, pipeMax);
        }
        return bonus;
    }

    // ---- Tiers / steam --------------------------------------------------------------------------

    protected boolean hasTiers(EFabRecipe recipe) {
        Set<RecipeTier> present = EnumSet.noneOf(RecipeTier.class);
        boolean[] hasFeControl = {false};
        BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3)).forEach(pos -> {
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof TierProvider tierProvider) {
                for (RecipeTier tier : tierProvider.getTiers()) {
                    if (tier != RecipeTier.STEAM) {
                        present.add(tier);
                    }
                }
            }
            if (state.is(ModBlocks.FE_CONTROL.get())) {
                present.add(RecipeTier.FE);
                hasFeControl[0] = true;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TankBlockEntity) {
                present.add(RecipeTier.LIQUID);
            }
        });
        for (int slot = UPGRADE_START; slot < UPGRADE_START + UPGRADE_SLOTS; slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            if (stack.getItem() instanceof ModItems.TierUpgradeItem upgradeItem) {
                present.add(upgradeItem.getTier());
            }
        }
        if (requiresSteam(recipe) && hasSteamSetup()) {
            present.add(RecipeTier.STEAM);
        }
        return present.containsAll(recipe.tiers()) && (!needsFeControl(recipe) || hasFeControl[0]);
    }

    private boolean hasFeControl() {
        for (BlockPos pos : craftingArea()) {
            if (level.getBlockState(pos).is(ModBlocks.FE_CONTROL.get())) {
                return true;
            }
        }
        return false;
    }

    private static boolean needsFeControl(EFabRecipe recipe) {
        return recipe.fePerTick() > 0 || recipe.tiers().contains(RecipeTier.FE);
    }

    protected boolean hasProcessor() {
        for (BlockPos pos : craftingArea()) {
            if (level.getBlockState(pos).is(ModBlocks.PROCESSOR.get())) {
                return true;
            }
        }
        return false;
    }

    protected static boolean requiresSteam(EFabRecipe recipe) {
        return recipe.tiers().contains(RecipeTier.STEAM);
    }

    private FluidStack steamWater(int bonus) {
        int amount = Math.max(0, EFabConfig.WATER_STEAM_CRAFTING_CONSUMPTION.get() * bonus);
        return new FluidStack(Fluids.WATER, amount);
    }

    private boolean hasSteamSetup() {
        if (!hasWaterTankInCraftingNetwork()) {
            return false;
        }
        for (BlockPos boilerPos : BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3))) {
            if (isValidSteamBoiler(boilerPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isValidSteamBoiler(BlockPos boilerPos) {
        BlockState boilerState = level.getBlockState(boilerPos);
        if (!boilerState.is(ModBlocks.BOILER.get()) || !boilerState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return false;
        }

        BlockState engineState = level.getBlockState(boilerPos.above());
        if (!engineState.is(ModBlocks.STEAM_ENGINE.get()) || !engineState.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return false;
        }
        if (boilerState.getValue(HorizontalDirectionalBlock.FACING) != engineState.getValue(HorizontalDirectionalBlock.FACING)) {
            return false;
        }

        return level.getBlockEntity(boilerPos) instanceof BoilerBlockEntity boiler && boiler.canMakeSteam();
    }

    // ---- Steam animation (flywheel spin + boiler steam) -----------------------------------------

    /**
     * While a steam recipe is actively progressing, keep nearby steam-engine flywheels spinning and
     * puff steam from the front of every valid boiler. Mirrors the original GridTE behaviour.
     */
    private void driveSteamAnimation() {
        boolean emitSteam = level instanceof ServerLevel && progress % 4 == 0;
        for (BlockPos pos : craftingArea()) {
            if (level.getBlockEntity(pos) instanceof SteamEngineBlockEntity steamEngine) {
                steamEngine.keepWorking();
            }
            if (emitSteam && isValidSteamBoiler(pos)) {
                emitBoilerSteam((ServerLevel) level, pos);
            }
        }
    }

    private void emitBoilerSteam(ServerLevel serverLevel, BlockPos boilerPos) {
        Direction facing = serverLevel.getBlockState(boilerPos).getValue(HorizontalDirectionalBlock.FACING);
        double x = boilerPos.getX() + 0.5 + facing.getStepX() * 0.55;
        double y = boilerPos.getY() + 0.55;
        double z = boilerPos.getZ() + 0.5 + facing.getStepZ() * 0.55;
        serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 2, 0.05, 0.02, 0.05, 0.01);
    }

    private void emitBoilerSteamBurst(ServerLevel serverLevel, BlockPos boilerPos) {
        Direction facing = serverLevel.getBlockState(boilerPos).getValue(HorizontalDirectionalBlock.FACING);
        double x = boilerPos.getX() + 0.5 + facing.getStepX() * 0.55;
        double y = boilerPos.getY() + 0.55;
        double z = boilerPos.getZ() + 0.5 + facing.getStepZ() * 0.55;
        serverLevel.sendParticles(ParticleTypes.CLOUD, x, y, z, 12, 0.18, 0.10, 0.18, 0.02);
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y + 0.05, z, 4, 0.12, 0.04, 0.12, 0.01);
    }

    private boolean hasWaterTankInCraftingNetwork() {
        int requiredWater = EFabConfig.WATER_STEAM_START_AMOUNT.get();
        FluidStack required = new FluidStack(Fluids.WATER, requiredWater);
        return drainFluid(required, true) >= requiredWater;
    }

    protected boolean hasRequiredFluids(EFabRecipe recipe) {
        for (FluidRequirement requirement : recipe.fluids()) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            FluidStack required = new FluidStack(fluid, requirement.amount());
            if (drainFluid(required, true) < requirement.amount()) {
                return false;
            }
        }
        return true;
    }

    protected boolean craft(EFabRecipe recipe) {
        int bonus = Math.max(1, speedBonus(recipe, scan()));
        if (!hasRecipeRequirements(recipe, EFabRequirementPhase.FINISH, 1, bonus)
                || !consumeRecipeRequirements(recipe, EFabRequirementPhase.FINISH, 1, bonus)) {
            return false;
        }
        consumeCraftIngredients(recipe);
        for (FluidRequirement requirement : recipe.fluids()) {
            Fluid fluid = BuiltInRegistries.FLUID.get(requirement.fluid());
            drainFluid(new FluidStack(fluid, requirement.amount()), false);
        }
        insertOutput(recipe.getResultItem(level.registryAccess()));
        setChanged();
        return true;
    }

    protected boolean hasRecipeRequirements(EFabRecipe recipe, EFabRequirementPhase phase, int amount, int speedBonus) {
        return applyRecipeRequirements(recipe, phase, amount, speedBonus, true);
    }

    protected boolean consumeRecipeRequirements(EFabRecipe recipe, EFabRequirementPhase phase, int amount, int speedBonus) {
        return applyRecipeRequirements(recipe, phase, amount, speedBonus, false);
    }

    private boolean applyRecipeRequirements(EFabRecipe recipe, EFabRequirementPhase phase, int amount, int speedBonus, boolean simulate) {
        if (recipe.requirements().isEmpty()) {
            return true;
        }
        EFabCraftingContext context = craftingContext(speedBonus, recipeRequiredTime(recipe, speedBonus));
        int safeAmount = Math.max(1, amount);
        for (EFabRecipeRequirement requirement : recipe.requirements()) {
            if (!requirement.apply(context, phase, safeAmount, simulate)) {
                return false;
            }
        }
        return true;
    }

    private EFabCraftingContext craftingContext(int speedBonus, int contextRequiredTime) {
        return new EFabCraftingContext() {
            @Override
            public Level level() {
                return level;
            }

            @Override
            public BlockEntity machine() {
                return AbstractCraftingBlockEntity.this;
            }

            @Override
            public BlockPos origin() {
                return worldPosition;
            }

            @Override
            public Iterable<BlockPos> craftingArea() {
                return AbstractCraftingBlockEntity.this.craftingArea();
            }

            @Override
            public boolean automated() {
                return isAutomatedMachine();
            }

            @Override
            public int progress() {
                return progress;
            }

            @Override
            public int requiredTime() {
                return contextRequiredTime;
            }

            @Override
            public int speedBonus() {
                return Math.max(1, speedBonus);
            }
        };
    }

    protected boolean isAutomatedMachine() {
        return false;
    }

    private static int recipeRequiredTime(EFabRecipe recipe, int bonus) {
        return Math.max(1, recipe.time() / Math.max(1, bonus));
    }

    /**
     * Remove the recipe's item ingredients. The grid consumes from its own input slots; the
     * Crafter overrides this to pull from named Storage blocks (its input slots are a template).
     */
    protected void consumeCraftIngredients(EFabRecipe recipe) {
        consumeIngredients(recipe);
    }

    private void consumeIngredients(EFabRecipe recipe) {
        if (recipe.isShaped()) {
            for (int y = 0; y < 3; y++) {
                String row = y < recipe.pattern().size() ? recipe.pattern().get(y) : "";
                for (int x = 0; x < 3; x++) {
                    if (x < row.length() && row.charAt(x) != ' ') {
                        items.extractItem(y * 3 + x, 1, false);
                    }
                }
            }
            return;
        }

        List<Ingredient> remaining = new ArrayList<>(recipe.efabIngredients());
        for (int slot = 0; slot < INPUT_SLOTS && !remaining.isEmpty(); slot++) {
            ItemStack stack = items.getStackInSlot(slot);
            for (int i = 0; i < remaining.size(); i++) {
                if (remaining.get(i).test(stack)) {
                    items.extractItem(slot, 1, false);
                    remaining.remove(i);
                    break;
                }
            }
        }
    }

    protected void insertOutput(ItemStack output) {
        for (int slot = OUTPUT_START; slot < OUTPUT_START + OUTPUT_SLOTS; slot++) {
            output = items.insertItem(slot, output, false);
            if (output.isEmpty()) {
                return;
            }
        }
    }

    // ---- Monitors -------------------------------------------------------------------------------

    protected void updateNearbyMonitors() {
        String status = getStatusLine();
        String result = getResultLine();
        List<String> autoLines = collectCraftingStatusLines();
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3))) {
            if (level.getBlockEntity(pos) instanceof MonitorBlockEntity monitor) {
                if (monitor.isAutoCraftingMonitor()) {
                    monitor.setLines(autoLines);
                } else {
                    monitor.setCraftStatus(status, result);
                }
            }
        }
    }

    static String tr(String key, Object... args) {
        return Component.translatable(key, args).getString();
    }

    private String getStatusLine() {
        if (requiredTime <= 0) {
            return "  " + tr("status.efab.idle");
        }
        return "  " + Math.min(100, progress * 100 / requiredTime) + "%";
    }

    private String getResultLine() {
        ItemStack result = getCurrentResult();
        if (result.isEmpty()) {
            return "  " + tr("status.efab.none");
        }
        String name = result.getHoverName().getString();
        return "  " + (name.length() > 15 ? name.substring(0, 15) + "..." : name);
    }

    private List<String> collectCraftingStatusLines() {
        List<String> lines = new ArrayList<>();
        lines.add(tr("status.efab.crafters"));
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3))) {
            if (level.getBlockEntity(pos) instanceof AbstractCraftingBlockEntity craftingBlockEntity) {
                ItemStack result = craftingBlockEntity.getCurrentResult();
                String name = result.isEmpty() ? tr("status.efab.none_short") : result.getHoverName().getString();
                if (name.length() > 11) {
                    name = name.substring(0, 11) + "...";
                }
                lines.add(craftingBlockEntity.requiredTime > 0
                        ? "* " + Math.min(100, craftingBlockEntity.progress * 100 / craftingBlockEntity.requiredTime) + "% " + name
                        : "* " + tr("status.efab.idle") + " " + name);
                if (lines.size() >= MonitorBlockEntity.MAX_LINES) {
                    break;
                }
            }
        }
        return lines;
    }

    // ---- Energy & fluids ------------------------------------------------------------------------

    protected int extractEnergy(int amount, boolean simulate) {
        int remaining = amount;
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3))) {
            IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, null);
            if (storage != null) {
                remaining -= storage.extractEnergy(remaining, simulate);
                if (remaining <= 0) {
                    return amount;
                }
            }
        }
        return amount - remaining;
    }

    // Extract ignoring per-tick flow caps. Used by the Power Optimizer.
    protected int extractEnergyUncapped(int amount, boolean simulate) {
        int remaining = amount;
        for (BlockPos pos : craftingArea()) {
            if (level.getBlockEntity(pos) instanceof EnergyBlockEntity energy) {
                remaining -= energy.extractIgnoringLimit(remaining, simulate);
                if (remaining <= 0) {
                    return amount;
                }
            }
        }
        return amount - remaining;
    }

    protected int drainFluid(FluidStack stack, boolean simulate) {
        int remaining = stack.getAmount();
        Set<IFluidHandler> seenHandlers = Collections.newSetFromMap(new IdentityHashMap<>());
        for (BlockPos pos : BlockPos.betweenClosed(worldPosition.offset(-3, -2, -3), worldPosition.offset(3, 2, 3))) {
            IFluidHandler handler = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
            if (handler != null && seenHandlers.add(handler)) {
                FluidStack request = stack.copyWithAmount(remaining);
                FluidStack drained = handler.drain(request, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
                remaining -= drained.getAmount();
                if (remaining <= 0) {
                    return stack.getAmount();
                }
            }
        }
        return stack.getAmount() - remaining;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", items.serializeNBT(registries));
        tag.putInt("Progress", progress);
        tag.putInt("RequiredTime", requiredTime);
        tag.putBoolean("Crafting", crafting);
        tag.putBoolean("RepeatCrafting", repeatCrafting);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items.deserializeNBT(registries, tag.getCompound("Items"));
        progress = tag.getInt("Progress");
        requiredTime = tag.getInt("RequiredTime");
        crafting = tag.getBoolean("Crafting");
        repeatCrafting = tag.getBoolean("RepeatCrafting");
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
