package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

/**
 * Wrapper for energy ingredients
 */
public record EnergyEmiWrapper(RequirementIOMode mode, int amount, double chance, boolean isPerTick,
                               int recipeTime) implements EmiIngredientWrapper {
}

