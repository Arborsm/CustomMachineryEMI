package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

public record ChemicalEmiWrapper(RequirementIOMode mode, Object chemical, long amount, double chance, boolean isPerTick,
                                 String tank) implements EmiIngredientWrapper {
}

