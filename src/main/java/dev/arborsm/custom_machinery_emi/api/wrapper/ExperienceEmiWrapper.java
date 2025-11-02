package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.impl.integration.jei.Experience;

/**
 * Wrapper for experience ingredients
 */
public record ExperienceEmiWrapper(RequirementIOMode mode, Experience experience,
                                   int recipeTime) implements EmiIngredientWrapper {
}

