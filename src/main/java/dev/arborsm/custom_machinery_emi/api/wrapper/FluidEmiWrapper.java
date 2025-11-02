package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

/**
 * Wrapper for fluid ingredients
 */
public record FluidEmiWrapper(RequirementIOMode mode, SizedFluidIngredient ingredient, double chance, boolean isPerTick,
                              String tank) implements EmiIngredientWrapper {
}

