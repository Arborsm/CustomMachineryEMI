package dev.arborsm.custom_machinery_emi.api.wrapper;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;

public class PncHeatEmiWrapper implements EmiIngredientWrapper {
    private final RequirementIOMode mode;
    private final int amount;
    private final double chance;
    private final boolean isPerTick;
    
    public PncHeatEmiWrapper(RequirementIOMode mode, int amount, double chance, boolean isPerTick) {
        this.mode = mode;
        this.amount = amount;
        this.chance = chance;
        this.isPerTick = isPerTick;
    }
    
    @Override
    public RequirementIOMode getMode() {
        return mode;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public double getChance() {
        return chance;
    }
    
    public boolean isPerTick() {
        return isPerTick;
    }
}

