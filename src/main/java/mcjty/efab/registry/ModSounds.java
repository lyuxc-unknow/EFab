package mcjty.efab.registry;

import mcjty.efab.EFab;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, EFab.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> MACHINE = register("machine");
    public static final DeferredHolder<SoundEvent, SoundEvent> SPARKS = register("sparks");
    public static final DeferredHolder<SoundEvent, SoundEvent> STEAM = register("steam");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEEPS1 = register("beeps1");
    public static final DeferredHolder<SoundEvent, SoundEvent> BEEPS2 = register("beeps2");

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(EFab.rl(name)));
    }

    private ModSounds() {
    }
}
