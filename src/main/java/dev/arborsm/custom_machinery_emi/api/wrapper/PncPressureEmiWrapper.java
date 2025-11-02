package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

public record PncPressureEmiWrapper(RequirementIOMode mode, float minPressure, float maxPressure, int volume,
                                    double chance) implements EmiIngredientWrapper {
}

