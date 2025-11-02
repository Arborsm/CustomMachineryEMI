package dev.arborsm.custom_machinery_emi.api;

import dev.arborsm.custom_machinery_emi.api.wrapper.*;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper class to extract EMI ingredients and stacks from our wrapper system.
 * No longer uses reflection or JEI wrapper classes.
 */
public class EmiStackHelper {

    /**
     * Extract input ingredients from a list of wrappers
     */
    public static List<EmiIngredient> extractInputs(List<EmiIngredientWrapper> wrappers) {
        List<EmiIngredient> inputs = new ArrayList<>();
        for (EmiIngredientWrapper wrapper : wrappers) {
            if (!wrapper.isInput()) continue;

            switch (wrapper) {
                case ItemEmiWrapper itemWrapper -> extractItemInput(itemWrapper, inputs);
                case FluidEmiWrapper fluidWrapper -> extractFluidInput(fluidWrapper, inputs);
                case ItemFilterEmiWrapper filterWrapper -> extractItemFilterInput(filterWrapper, inputs);
                default -> {
                }
            }
        }
        return inputs;
    }

    /**
     * Extract output stacks from a list of wrappers
     */
    public static List<EmiStack> extractOutputs(List<EmiIngredientWrapper> wrappers) {
        List<EmiStack> outputs = new ArrayList<>();
        
        for (EmiIngredientWrapper wrapper : wrappers) {
            if (!wrapper.isOutput()) continue;

            switch (wrapper) {
                case ItemEmiWrapper itemWrapper -> extractItemOutput(itemWrapper, outputs);
                case FluidEmiWrapper fluidWrapper -> extractFluidOutput(fluidWrapper, outputs);
                case LootTableEmiWrapper lootTableWrapper -> extractLootTableOutput(lootTableWrapper, outputs);
                default -> {
                }
            }
        }
        
        return outputs;
    }
    
    private static void extractItemInput(ItemEmiWrapper wrapper, List<EmiIngredient> inputs) {
        SizedIngredient ingredient = wrapper.ingredient();
        List<EmiStack> stacks = Arrays.stream(ingredient.ingredient().getItems())
                .map(item -> EmiStack.of(item.copyWithCount(ingredient.count())))
                .collect(Collectors.toList());
        if (!stacks.isEmpty()) {
            inputs.add(EmiIngredient.of(stacks));
        }
    }
    
    private static void extractItemOutput(ItemEmiWrapper wrapper, List<EmiStack> outputs) {
        SizedIngredient ingredient = wrapper.ingredient();
        ItemStack[] items = ingredient.ingredient().getItems();
        if (items.length > 0) {
            EmiStack stack = EmiStack.of(items[0].copyWithCount(ingredient.count()));
            double chance = wrapper.chance();
            if (chance < 1.0) {
                stack = stack.setChance((float) chance);
            }
            outputs.add(stack);
        }
    }
    
    private static void extractFluidInput(FluidEmiWrapper wrapper, List<EmiIngredient> inputs) {
        SizedFluidIngredient ingredient = wrapper.ingredient();
        List<EmiStack> stacks = Arrays.stream(ingredient.getFluids())
                .map(fluid -> EmiStack.of(fluid.getFluid(), ingredient.amount()))
                .collect(Collectors.toList());
        if (!stacks.isEmpty()) {
            inputs.add(EmiIngredient.of(stacks));
        }
    }
    
    private static void extractFluidOutput(FluidEmiWrapper wrapper, List<EmiStack> outputs) {
        SizedFluidIngredient ingredient = wrapper.ingredient();
        double chance = wrapper.chance();
        Arrays.stream(ingredient.getFluids())
                .map(fluid -> {
                    EmiStack stack = EmiStack.of(fluid.getFluid(), ingredient.amount());
                    if (chance < 1.0) {
                        stack = stack.setChance((float) chance);
                    }
                    return stack;
                })
                .forEach(outputs::add);
    }
    
    private static void extractItemFilterInput(ItemFilterEmiWrapper wrapper, List<EmiIngredient> inputs) {
        Ingredient ingredient = wrapper.ingredient();
        List<EmiStack> stacks = Arrays.stream(ingredient.getItems())
                .map(EmiStack::of)
                .collect(Collectors.toList());
        if (!stacks.isEmpty()) {
            inputs.add(EmiIngredient.of(stacks));
        }
    }
    
    private static void extractLootTableOutput(LootTableEmiWrapper wrapper, List<EmiStack> outputs) {
        // Get all possible loot items from the loot table
        List<LootTableHelper.LootData> loots = LootTableHelper.getLootsForTable(wrapper.lootTable());
        for (LootTableHelper.LootData lootData : loots) {
            EmiStack stack = EmiStack.of(lootData.stack());
            double chance = lootData.chance();
            if (chance < 1.0) {
                stack = stack.setChance((float) chance);
            }
            outputs.add(stack);
        }
    }
}

