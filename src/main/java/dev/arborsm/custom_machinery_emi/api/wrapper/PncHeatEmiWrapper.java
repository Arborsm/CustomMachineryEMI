package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

public record PncHeatEmiWrapper(RequirementIOMode mode, int amount, double chance,
                                boolean isPerTick) implements EmiIngredientWrapper {
}

