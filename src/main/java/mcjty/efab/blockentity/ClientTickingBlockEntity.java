package mcjty.efab.blockentity;

/**
 * Implemented by block entities that need a client-side tick (for example to drive a renderer
 * animation such as the steam engine flywheel or the crafter pistons).
 */
public interface ClientTickingBlockEntity {
    void clientTick();
}
