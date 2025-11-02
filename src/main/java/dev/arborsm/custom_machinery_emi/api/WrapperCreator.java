package dev.arborsm.custom_machinery_emi.api;

import dev.arborsm.custom_machinery_emi.api.wrapper.*;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.requirement.*;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.fml.ModList;

import java.lang.reflect.Field;
import java.util.Optional;

import es.degrassi.custommachineryars.requirement.SourceRequirement;
import es.degrassi.custommachineryars.requirement.SourceRequirementPerTick;
import fr.frinn.custommachinerymekanism.common.requirement.ChemicalRequirement;
import fr.frinn.custommachinerymekanism.common.requirement.ChemicalPerTickRequirement;
import fr.frinn.custommachinerymekanism.common.requirement.HeatRequirement;
import fr.frinn.custommachinerymekanism.common.requirement.HeatPerTickRequirement;
import fr.frinn.custommachinerymekanism.common.requirement.TemperatureRequirement;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

public class WrapperCreator {

    public static Optional<EmiIngredientWrapper> createWrapper(RecipeRequirement<?, ?> requirement, IMachineRecipe recipe) {
        Object req = requirement.requirement();
        if (req instanceof ItemRequirement itemReq) {
            return Optional.of(new ItemEmiWrapper(itemReq.mode(), itemReq.ingredient(), requirement.chance(), false, itemReq.slot(), true));
        }

        if (req instanceof FluidRequirement(
                RequirementIOMode mode,
                SizedFluidIngredient ingredient, String tank
        )) {
            return Optional.of(new FluidEmiWrapper(mode, ingredient, requirement.chance(), false, tank));
        }

        if (req instanceof FluidPerTickRequirement(
                RequirementIOMode mode,
                SizedFluidIngredient ingredient, String tank
        )) {
            return Optional.of(new FluidEmiWrapper(mode, ingredient, requirement.chance(), true, tank));
        }

        if (req instanceof EnergyRequirement(
                RequirementIOMode mode, int amount
        )) {
            return Optional.of(new EnergyEmiWrapper(mode, amount, requirement.chance(), false, recipe.getRecipeTime()));
        }

        if (req instanceof EnergyPerTickRequirement(
                RequirementIOMode mode, int amount
        )) {
            return Optional.of(new EnergyEmiWrapper(mode, amount, requirement.chance(), true, recipe.getRecipeTime()));
        }

        if (req instanceof ExperienceRequirement(
                RequirementIOMode mode, int amount, Experience.Form form
        )) {
            Experience experience = new Experience(amount, requirement.chance(), false, form);
            return Optional.of(new ExperienceEmiWrapper(mode, experience, recipe.getRecipeTime()));
        }

        if (req instanceof ExperiencePerTickRequirement(
                RequirementIOMode mode, int amount, Experience.Form form
        )) {
            Experience experience = new Experience(amount, requirement.chance(), true, form);
            return Optional.of(new ExperienceEmiWrapper(mode, experience, recipe.getRecipeTime()));
        }

        if (req instanceof LootTableRequirement lootTableReq) {
            try {
                Field field = LootTableRequirement.class.getDeclaredField("lootTable");
                field.setAccessible(true);
                ResourceLocation lootTable = (ResourceLocation) field.get(lootTableReq);
                return Optional.of(new LootTableEmiWrapper(lootTable));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        if (req instanceof ItemFilterRequirement(Ingredient ingredient, String slot
        )) {
            return Optional.of(new ItemFilterEmiWrapper(ingredient, slot));
        }

        if (req instanceof FuelRequirement) {
            return Optional.of(new FuelItemEmiWrapper());
        }

        if (ModList.get().isLoaded("custommachineryars")) {
            Optional<EmiIngredientWrapper> arsResult = handleArsNouveauSource(req, recipe);
            if (arsResult.isPresent()) {
                return arsResult;
            }
        }

        if (ModList.get().isLoaded("custommachinerymekanism")) {
            Optional<EmiIngredientWrapper> chemicalResult = handleMekanismChemical(req, requirement);
            if (chemicalResult.isPresent()) {
                return chemicalResult;
            }

            Optional<EmiIngredientWrapper> heatResult = handleMekanismHeat(req, requirement);
            if (heatResult.isPresent()) {
                return heatResult;
            }

            Optional<EmiIngredientWrapper> tempResult = handleMekanismTemperature(req);
            if (tempResult.isPresent()) {
                return tempResult;
            }
        }

        return Optional.empty();
    }

    private static Optional<EmiIngredientWrapper> handleArsNouveauSource(Object req, IMachineRecipe recipe) {
        if (req instanceof SourceRequirement sourceReq) {
            return Optional.of(new SourceEmiWrapper(sourceReq.getMode(), sourceReq.source(), false, recipe.getRecipeTime()));
        }

        if (req instanceof SourceRequirementPerTick sourcePerTickReq) {
            return Optional.of(new SourceEmiWrapper(sourcePerTickReq.getMode(), sourcePerTickReq.source(), true, recipe.getRecipeTime()));
        }

        return Optional.empty();
    }

    private static Optional<EmiIngredientWrapper> handleMekanismChemical(Object req, RecipeRequirement<?, ?> requirement) {
        if (req instanceof ChemicalRequirement chemicalReq) {
            return Optional.of(new ChemicalEmiWrapper(chemicalReq.getMode(), chemicalReq.chemical(), chemicalReq.amount(), requirement.chance(), false, chemicalReq.tank()));
        }

        if (req instanceof ChemicalPerTickRequirement chemicalPerTickReq) {
            return Optional.of(new ChemicalEmiWrapper(chemicalPerTickReq.getMode(), chemicalPerTickReq.chemical(), chemicalPerTickReq.amount(), requirement.chance(), true, chemicalPerTickReq.tank()));
        }

        return Optional.empty();
    }

    private static Optional<EmiIngredientWrapper> handleMekanismHeat(Object req, RecipeRequirement<?, ?> requirement) {
        if (req instanceof HeatRequirement heatReq) {
            return Optional.of(new HeatEmiWrapper(heatReq.getMode(), heatReq.amount(), requirement.chance(), false));
        }

        if (req instanceof HeatPerTickRequirement heatPerTickReq) {
            try {
                Field amountField = HeatPerTickRequirement.class.getDeclaredField("amount");
                amountField.setAccessible(true);
                double amount = amountField.getDouble(heatPerTickReq);
                return Optional.of(new HeatEmiWrapper(heatPerTickReq.getMode(), amount, requirement.chance(), true));
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private static Optional<EmiIngredientWrapper> handleMekanismTemperature(Object req) {
        if (req instanceof TemperatureRequirement(
                DoubleRange temp,
                UnitDisplayUtils.TemperatureUnit unit
        )) {
            return Optional.of(new TemperatureEmiWrapper(temp, unit));
        }

        return Optional.empty();
    }
}

