package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

/**
 * Wrapper for fuel item ingredients (always input)
 */
public class FuelItemEmiWrapper implements EmiIngredientWrapper {
    @Override
    public RequirementIOMode mode() {
        return RequirementIOMode.INPUT;
    }
}

