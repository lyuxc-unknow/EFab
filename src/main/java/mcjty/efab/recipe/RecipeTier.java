package mcjty.efab.recipe;

import com.mojang.serialization.Codec;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.util.StringRepresentable;

import java.util.Locale;

@MethodsReturnNonnullByDefault
public enum RecipeTier implements StringRepresentable {
    GEARBOX(16, 0),
    STEAM(0, 0),
    LIQUID(32, 0),
    FE(48, 0),
    COMPUTING(0, 16),
    MANA(96, 0),
    UPGRADE_MAGIC(128, 0),
    UPGRADE_ARMORY(144, 0),
    UPGRADE_POWER(160, 0),
    UPGRADE_DIGITAL(176, 0);

    public static final Codec<RecipeTier> CODEC = StringRepresentable.fromEnum(RecipeTier::values);

    private final int iconX;
    private final int iconY;

    RecipeTier(int iconX, int iconY) {
        this.iconX = iconX;
        this.iconY = iconY;
    }

    public int getIconX() {
        return iconX;
    }

    public int getIconY() {
        return iconY;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
