package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import mekanism.common.util.UnitDisplayUtils;

/**
 * Wrapper for Mekanism Temperature ingredients
 */
public record TemperatureEmiWrapper(DoubleRange temp,
                                    UnitDisplayUtils.TemperatureUnit unit) implements EmiIngredientWrapper {

    @Override
    public RequirementIOMode mode() {
        return RequirementIOMode.INPUT; // Temperature is always input
    }
}

