package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * Wrapper for item ingredients
 */
public record ItemEmiWrapper(RequirementIOMode mode, SizedIngredient ingredient, double chance, boolean useDurability,
                             String slot, boolean showRequireSlot) implements EmiIngredientWrapper {
}

