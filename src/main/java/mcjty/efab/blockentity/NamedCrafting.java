package mcjty.efab.blockentity;

/**
 * Implemented by block entities that carry a crafting "name" used to match a Crafter
 * to the Storage blocks it is allowed to pull ingredients from.
 */
public interface NamedCrafting {

    String getCraftingName();

    void setCraftingName(String name);
}
