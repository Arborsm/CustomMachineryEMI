package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import mekanism.api.chemical.Chemical;

public record ChemicalEmiWrapper(RequirementIOMode mode, Chemical chemical, long amount, double chance, boolean isPerTick,
                                 String tank) implements EmiIngredientWrapper {
}

