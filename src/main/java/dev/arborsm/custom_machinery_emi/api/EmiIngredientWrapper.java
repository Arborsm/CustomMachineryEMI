package dev.arborsm.custom_machinery_emi.api;

import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

/**
 * Base interface for EMI ingredient wrappers.
 * These wrappers are created directly from requirements without JEI dependency.
 */
public interface EmiIngredientWrapper {
    /**
     * Get the mode of this wrapper (INPUT or OUTPUT)
     */
    RequirementIOMode mode();
    
    /**
     * Check if this wrapper represents an input
     */
    default boolean isInput() {
        return mode() == RequirementIOMode.INPUT;
    }
    
    /**
     * Check if this wrapper represents an output
     */
    default boolean isOutput() {
        return mode() == RequirementIOMode.OUTPUT;
    }
}

