package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.minecraft.world.item.crafting.Ingredient;

/**
 * Wrapper for item filter ingredients (always input)
 */
public record ItemFilterEmiWrapper(Ingredient ingredient, String slot) implements EmiIngredientWrapper {

    @Override
    public RequirementIOMode mode() {
        return RequirementIOMode.INPUT;
    }
}

