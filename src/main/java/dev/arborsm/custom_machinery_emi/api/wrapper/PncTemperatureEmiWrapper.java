package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.impl.util.IntRange;

public record PncTemperatureEmiWrapper(IntRange range) implements EmiIngredientWrapper {

    @Override
    public RequirementIOMode mode() {
        return RequirementIOMode.INPUT;
    }
}

