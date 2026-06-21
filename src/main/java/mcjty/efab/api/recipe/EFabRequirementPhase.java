package mcjty.efab.api.recipe;

public enum EFabRequirementPhase {
    /**
     * Called once when a timed craft begins. Use this for upfront consumables.
     */
    START,

    /**
     * Called whenever the machine advances. The amount is scaled by the current
     * speed bonus, so total tick consumption stays close to the recipe time.
     */
    TICK,

    /**
     * Called once immediately before item and fluid ingredients are consumed and
     * the output is inserted.
     */
    FINISH
}
