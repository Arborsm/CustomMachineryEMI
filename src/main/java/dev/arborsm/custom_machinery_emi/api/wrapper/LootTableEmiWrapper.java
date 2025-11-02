package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import net.minecraft.resources.ResourceLocation;

/**
 * Wrapper for loot table ingredients (always output)
 */
public record LootTableEmiWrapper(ResourceLocation lootTable) implements EmiIngredientWrapper {

    @Override
    public RequirementIOMode mode() {
        return RequirementIOMode.OUTPUT;
    }
}

