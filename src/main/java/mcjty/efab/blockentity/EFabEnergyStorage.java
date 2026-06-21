package mcjty.efab.blockentity;

import net.neoforged.neoforge.energy.EnergyStorage;

public class EFabEnergyStorage extends EnergyStorage {

    private final Runnable changed;

    public EFabEnergyStorage(int capacity, int maxTransfer, Runnable changed) {
        super(capacity, maxTransfer, maxTransfer);
        this.changed = changed;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        int received = super.receiveEnergy(maxReceive, simulate);
        if (received > 0 && !simulate) {
            changed.run();
        }
        return received;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        int extracted = super.extractEnergy(maxExtract, simulate);
        if (extracted > 0 && !simulate) {
            changed.run();
        }
        return extracted;
    }

    public void setEnergy(int energy) {
        this.energy = Math.clamp(energy, 0, capacity);
    }

    /**
     * Extract energy ignoring the per-tick transfer limit. Used by the Power Optimizer
     * which removes the normal FE/t throttle on crafting.
     */
    public int extractIgnoringLimit(int maxExtract, boolean simulate) {
        int extracted = Math.clamp(maxExtract, 0, energy);
        if (extracted > 0 && !simulate) {
            energy -= extracted;
            changed.run();
        }
        return extracted;
    }
}
