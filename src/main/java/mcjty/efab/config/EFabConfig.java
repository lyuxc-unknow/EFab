package mcjty.efab.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class EFabConfig {

    public static final ModConfigSpec SERVER_SPEC;

    public static final ModConfigSpec.BooleanValue VANILLA_CRAFTING_ALLOWED;
    public static final ModConfigSpec.IntValue DEFAULT_CRAFT_TIME;
    public static final ModConfigSpec.IntValue TANK_CAPACITY;
    public static final ModConfigSpec.IntValue ADVANCED_TANK_CAPACITY;
    public static final ModConfigSpec.IntValue FE_STORAGE_CAPACITY;
    public static final ModConfigSpec.IntValue ADVANCED_FE_STORAGE_CAPACITY;
    public static final ModConfigSpec.IntValue FE_CONTROL_CAPACITY;
    public static final ModConfigSpec.IntValue FE_CONTROL_FLOW;
    public static final ModConfigSpec.IntValue FE_STORAGE_FLOW;
    public static final ModConfigSpec.IntValue ADVANCED_FE_STORAGE_FLOW;
    public static final ModConfigSpec.IntValue MAX_FE_USAGE;
    public static final ModConfigSpec.IntValue WATER_STEAM_START_AMOUNT;
    public static final ModConfigSpec.IntValue WATER_STEAM_CRAFTING_CONSUMPTION;

    public static final ModConfigSpec.IntValue MAX_SPEEDUP_BONUS;
    public static final ModConfigSpec.IntValue MAX_PIPE_SPEED_BONUS;
    public static final ModConfigSpec.IntValue CRAFTER_DELAY;
    public static final ModConfigSpec.IntValue TICKS_ALLOWED_WITHOUT_FE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("crafting");
        VANILLA_CRAFTING_ALLOWED = builder
                .comment("If true the EFab grid and crafter can also use normal vanilla crafting recipes.")
                .define("vanillaCraftingAllowed", true);
        DEFAULT_CRAFT_TIME = builder
                .comment("Default craft time in ticks for vanilla recipes.")
                .defineInRange("defaultCraftTime", 40, 1, 1_000_000);
        MAX_FE_USAGE = builder
                .comment("Maximum FE/t the crafting grid may pull from EFab energy blocks.")
                .defineInRange("maxFeUsage", 1000, 1, 1_000_000);
        WATER_STEAM_START_AMOUNT = builder
                .comment("Water amount in mB required to enable steam-tier recipes.")
                .defineInRange("waterSteamStartAmount", 100, 1, 1_000_000);
        WATER_STEAM_CRAFTING_CONSUMPTION = builder
                .comment("Water in mB consumed per tick during a steam crafting operation (scaled by the speed bonus).")
                .defineInRange("waterSteamCraftingConsumption", 5, 0, 1_000_000);
        MAX_SPEEDUP_BONUS = builder
                .comment("Maximum speed bonus on a craft from adding multiple gearboxes/processors/steam engines/FE controls.")
                .defineInRange("maxSpeedupBonus", 4, 1, 64);
        MAX_PIPE_SPEED_BONUS = builder
                .comment("Maximum speed bonus on a liquid craft from adding multiple pipes.")
                .defineInRange("maxPipeSpeedBonus", 2, 1, 64);
        CRAFTER_DELAY = builder
                .comment("Number of ticks between every automatic craft operation of the Crafter.")
                .defineInRange("crafterDelay", 10, 1, 100_000);
        TICKS_ALLOWED_WITHOUT_FE = builder
                .comment("Ticks a craft may run starved of FE before it aborts (-1 to pause indefinitely).")
                .defineInRange("ticksAllowedWithoutFe", 1, -1, 1_000_000);
        builder.pop();

        builder.push("storage");
        TANK_CAPACITY = builder.defineInRange("tankCapacity", 16000, 1, 1_000_000_000);
        ADVANCED_TANK_CAPACITY = builder.defineInRange("advancedTankCapacity", 64000, 1, 1_000_000_000);
        FE_STORAGE_CAPACITY = builder.defineInRange("feStorageCapacity", 100000, 1, 1_000_000_000);
        ADVANCED_FE_STORAGE_CAPACITY = builder.defineInRange("advancedFeStorageCapacity", 1000000, 1, 1_000_000_000);
        FE_CONTROL_CAPACITY = builder.defineInRange("feControlCapacity", 10000, 1, 1_000_000_000);
        FE_CONTROL_FLOW = builder
                .comment("FE/t an FE Control block can contribute to a craft.")
                .defineInRange("feControlFlow", 100, 1, 1_000_000_000);
        FE_STORAGE_FLOW = builder
                .comment("FE/t an FE Storage block can contribute to a craft.")
                .defineInRange("feStorageFlow", 1000, 1, 1_000_000_000);
        ADVANCED_FE_STORAGE_FLOW = builder
                .comment("FE/t an Advanced FE Storage block can contribute to a craft.")
                .defineInRange("advancedFeStorageFlow", 10000, 1, 1_000_000_000);
        builder.pop();

        SERVER_SPEC = builder.build();
    }

    private EFabConfig() {
    }
}
