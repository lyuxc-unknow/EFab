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
    public static final ModConfigSpec.DoubleValue MAX_BOILER_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue AMBIENT_BOILER_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue BOILER_RISE_TEMPERATURE;
    public static final ModConfigSpec.DoubleValue BOILER_COOL_TEMPERATURE;

    public static final ModConfigSpec.IntValue MAX_SPEEDUP_BONUS;
    public static final ModConfigSpec.IntValue MAX_PIPE_SPEED_BONUS;
    public static final ModConfigSpec.IntValue CRAFTER_DELAY;
    public static final ModConfigSpec.IntValue TICKS_ALLOWED_WITHOUT_FE;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.translation("efab.configuration.section.efab.server.toml.crafting").push("crafting");
        VANILLA_CRAFTING_ALLOWED = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.vanillaCraftingAllowed")
                .comment("If true the EFab grid and crafter can also use normal vanilla crafting recipes.")
                .define("vanillaCraftingAllowed", true);
        DEFAULT_CRAFT_TIME = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.defaultCraftTime")
                .comment("Default craft time in ticks for vanilla recipes.")
                .defineInRange("defaultCraftTime", 40, 1, 1_000_000);
        MAX_FE_USAGE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.maxFeUsage")
                .comment("Maximum FE/t the crafting grid may pull from EFab energy blocks.")
                .defineInRange("maxFeUsage", 1000, 1, 1_000_000);
        WATER_STEAM_START_AMOUNT = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.waterSteamStartAmount")
                .comment("Water amount in mB required to enable steam-tier recipes.")
                .defineInRange("waterSteamStartAmount", 100, 1, 1_000_000);
        WATER_STEAM_CRAFTING_CONSUMPTION = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.waterSteamCraftingConsumption")
                .comment("Water in mB consumed per tick during a steam crafting operation (scaled by the speed bonus).")
                .defineInRange("waterSteamCraftingConsumption", 5, 0, 1_000_000);
        MAX_BOILER_TEMPERATURE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.maxBoilerTemperature")
                .comment("Maximum boiler temperature in C. Boilers generate steam at 100C and above.")
                .defineInRange("maxBoilerTemperature", 200.0, 100.0, 1000.0);
        AMBIENT_BOILER_TEMPERATURE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.ambientBoilerTemperature")
                .comment("Ambient boiler temperature in C.")
                .defineInRange("ambientBoilerTemperature", 20.0, 0.0, 100.0);
        BOILER_RISE_TEMPERATURE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.boilerRiseTemperature")
                .comment("Temperature gained per tick while the boiler has a heat source below.")
                .defineInRange("boilerRiseTemperature", 0.5, 0.0, 100.0);
        BOILER_COOL_TEMPERATURE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.boilerCoolTemperature")
                .comment("Temperature lost per tick while the boiler has no heat source below.")
                .defineInRange("boilerCoolTemperature", 0.3, 0.0, 100.0);
        MAX_SPEEDUP_BONUS = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.maxSpeedupBonus")
                .comment("Maximum speed bonus on a craft from adding multiple gearboxes/processors/steam engines/FE controls.")
                .defineInRange("maxSpeedupBonus", 4, 1, 64);
        MAX_PIPE_SPEED_BONUS = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.maxPipeSpeedBonus")
                .comment("Maximum speed bonus on a liquid craft from adding multiple pipes.")
                .defineInRange("maxPipeSpeedBonus", 2, 1, 64);
        CRAFTER_DELAY = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.crafterDelay")
                .comment("Number of ticks between every automatic craft operation of the Crafter.")
                .defineInRange("crafterDelay", 10, 1, 100_000);
        TICKS_ALLOWED_WITHOUT_FE = builder
                .translation("efab.configuration.section.efab.server.toml.crafting.ticksAllowedWithoutFe")
                .comment("Ticks a craft may run starved of FE before it aborts (-1 to pause indefinitely).")
                .defineInRange("ticksAllowedWithoutFe", 1, -1, 1_000_000);
        builder.pop();

        builder.translation("efab.configuration.section.efab.server.toml.storage").push("storage");
        TANK_CAPACITY = builder
                .translation("efab.configuration.section.efab.server.toml.storage.tankCapacity")
                .defineInRange("tankCapacity", 16000, 1, 1_000_000_000);
        ADVANCED_TANK_CAPACITY = builder
                .translation("efab.configuration.section.efab.server.toml.storage.advancedTankCapacity")
                .defineInRange("advancedTankCapacity", 64000, 1, 1_000_000_000);
        FE_STORAGE_CAPACITY = builder
                .translation("efab.configuration.section.efab.server.toml.storage.feStorageCapacity")
                .defineInRange("feStorageCapacity", 100000, 1, 1_000_000_000);
        ADVANCED_FE_STORAGE_CAPACITY = builder
                .translation("efab.configuration.section.efab.server.toml.storage.advancedFeStorageCapacity")
                .defineInRange("advancedFeStorageCapacity", 1000000, 1, 1_000_000_000);
        FE_CONTROL_CAPACITY = builder
                .translation("efab.configuration.section.efab.server.toml.storage.feControlCapacity")
                .defineInRange("feControlCapacity", 10000, 1, 1_000_000_000);
        FE_CONTROL_FLOW = builder
                .translation("efab.configuration.section.efab.server.toml.storage.feControlFlow")
                .comment("FE/t an FE Control block can contribute to a craft.")
                .defineInRange("feControlFlow", 100, 1, 1_000_000_000);
        FE_STORAGE_FLOW = builder
                .translation("efab.configuration.section.efab.server.toml.storage.feStorageFlow")
                .comment("FE/t an FE Storage block can contribute to a craft.")
                .defineInRange("feStorageFlow", 1000, 1, 1_000_000_000);
        ADVANCED_FE_STORAGE_FLOW = builder
                .translation("efab.configuration.section.efab.server.toml.storage.advancedFeStorageFlow")
                .comment("FE/t an Advanced FE Storage block can contribute to a craft.")
                .defineInRange("advancedFeStorageFlow", 10000, 1, 1_000_000_000);
        builder.pop();

        SERVER_SPEC = builder.build();
    }

    private EFabConfig() {
    }
}
