package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.impl.util.IntRange;

public class PncTemperatureEmiWrapper implements EmiIngredientWrapper {
    private final IntRange range;
    
    public PncTemperatureEmiWrapper(IntRange range) {
        this.range = range;
    }
    
    @Override
    public RequirementIOMode getMode() {
        return RequirementIOMode.INPUT;
    }
    
    public IntRange getRange() {
        return range;
    }
}

