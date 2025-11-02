package dev.arborsm.custom_machinery_emi.api;

import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.wrapper.*;
import fr.frinn.custommachinery.common.util.LootTableHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EmiStackHelper {

    private static Field itemModeField;
    private static Field itemIngredientField;
    private static Field itemChanceField;
    private static Field fluidModeField;
    private static Field fluidIngredientField;
    private static Field fluidChanceField;
    private static Field lootTableField;
    private static Field itemFilterIngredientField;
    
    static {
        try {
            itemModeField = ItemIngredientWrapper.class.getDeclaredField("mode");
            itemModeField.setAccessible(true);
            itemIngredientField = ItemIngredientWrapper.class.getDeclaredField("ingredient");
            itemIngredientField.setAccessible(true);
            itemChanceField = ItemIngredientWrapper.class.getDeclaredField("chance");
            itemChanceField.setAccessible(true);
            
            fluidModeField = FluidIngredientWrapper.class.getDeclaredField("mode");
            fluidModeField.setAccessible(true);
            fluidIngredientField = FluidIngredientWrapper.class.getDeclaredField("ingredient");
            fluidIngredientField.setAccessible(true);
            fluidChanceField = FluidIngredientWrapper.class.getDeclaredField("chance");
            fluidChanceField.setAccessible(true);
            
            lootTableField = LootTableIngredientWrapper.class.getDeclaredField("lootTable");
            lootTableField.setAccessible(true);
            
            itemFilterIngredientField = ItemFilterIngredientWrapper.class.getDeclaredField("ingredient");
            itemFilterIngredientField.setAccessible(true);
        } catch (Exception e) {
            // Ignore
        }
    }

    public static List<EmiIngredient> extractInputs(List<IJEIIngredientWrapper<?>> wrappers) {
        List<EmiIngredient> inputs = new ArrayList<>();
        for (IJEIIngredientWrapper<?> wrapper : wrappers) {
            if (wrapper instanceof ItemIngredientWrapper) {
                extractItemInput(wrapper, inputs);
            } else if (wrapper instanceof FluidIngredientWrapper) {
                extractFluidInput(wrapper, inputs);
            } else if (wrapper instanceof ItemFilterIngredientWrapper) {
                extractItemFilterInput(wrapper, inputs);
            }
        }
        return inputs;
    }

    public static List<EmiStack> extractOutputs(List<IJEIIngredientWrapper<?>> wrappers) {
        List<EmiStack> outputs = new ArrayList<>();
        
        for (IJEIIngredientWrapper<?> wrapper : wrappers) {
            if (wrapper instanceof ItemIngredientWrapper) {
                extractItemOutput(wrapper, outputs);
            } else if (wrapper instanceof FluidIngredientWrapper) {
                extractFluidOutput(wrapper, outputs);
            } else if (wrapper instanceof LootTableIngredientWrapper) {
                extractLootTableOutput(wrapper, outputs);
            }
        }
        
        return outputs;
    }
    
    private static void extractItemInput(IJEIIngredientWrapper<?> wrapper, List<EmiIngredient> inputs) {
        try {
            if (itemModeField == null || itemIngredientField == null) return;
            
            RequirementIOMode mode = (RequirementIOMode) itemModeField.get(wrapper);
            if (mode == RequirementIOMode.INPUT) {
                Object ingredient = itemIngredientField.get(wrapper);
                if (ingredient instanceof SizedIngredient sizedIngredient) {
                    List<EmiStack> stacks = Arrays.stream(sizedIngredient.ingredient().getItems())
                            .map(item -> EmiStack.of(item.copyWithCount(sizedIngredient.count())))
                            .collect(Collectors.toList());
                    if (!stacks.isEmpty()) {
                        inputs.add(EmiIngredient.of(stacks));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    private static void extractItemOutput(IJEIIngredientWrapper<?> wrapper, List<EmiStack> outputs) {
        try {
            if (itemModeField == null || itemIngredientField == null) return;
            
            RequirementIOMode mode = (RequirementIOMode) itemModeField.get(wrapper);
            if (mode == RequirementIOMode.OUTPUT) {
                Object ingredient = itemIngredientField.get(wrapper);
                double chance = itemChanceField != null ? (Double) itemChanceField.get(wrapper) : 1.0;
                
                if (ingredient instanceof SizedIngredient sizedIngredient) {
                    ItemStack[] items = sizedIngredient.ingredient().getItems();
                    if (items.length > 0) {
                        EmiStack stack = EmiStack.of(items[0].copyWithCount(sizedIngredient.count()));
                        // Apply chance using EMI's native probability system
                        if (chance < 1.0) {
                            stack = stack.setChance((float) chance);
                        }
                        outputs.add(stack);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    private static void extractFluidInput(IJEIIngredientWrapper<?> wrapper, List<EmiIngredient> inputs) {
        try {
            if (fluidModeField == null || fluidIngredientField == null) return;
            
            RequirementIOMode mode = (RequirementIOMode) fluidModeField.get(wrapper);
            if (mode == RequirementIOMode.INPUT) {
                Object ingredient = fluidIngredientField.get(wrapper);
                if (ingredient instanceof SizedFluidIngredient sizedFluidIngredient) {
                    List<EmiStack> stacks = Arrays.stream(sizedFluidIngredient.getFluids())
                            .map(fluid -> EmiStack.of(fluid.getFluid(), sizedFluidIngredient.amount()))
                            .collect(Collectors.toList());
                    if (!stacks.isEmpty()) {
                        inputs.add(EmiIngredient.of(stacks));
                    }
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    private static void extractFluidOutput(IJEIIngredientWrapper<?> wrapper, List<EmiStack> outputs) {
        try {
            if (fluidModeField == null || fluidIngredientField == null) return;
            
            RequirementIOMode mode = (RequirementIOMode) fluidModeField.get(wrapper);
            if (mode == RequirementIOMode.OUTPUT) {
                Object ingredient = fluidIngredientField.get(wrapper);
                double chance = fluidChanceField != null ? (Double) fluidChanceField.get(wrapper) : 1.0;
                
                if (ingredient instanceof SizedFluidIngredient sizedFluidIngredient) {
                    Arrays.stream(sizedFluidIngredient.getFluids())
                            .map(fluid -> {
                                EmiStack stack = EmiStack.of(fluid.getFluid(), sizedFluidIngredient.amount());
                                // Apply chance using EMI's native probability system
                                if (chance < 1.0) {
                                    stack = stack.setChance((float) chance);
                                }
                                return stack;
                            })
                            .forEach(outputs::add);
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    private static void extractItemFilterInput(IJEIIngredientWrapper<?> wrapper, List<EmiIngredient> inputs) {
        try {
            if (itemFilterIngredientField == null) return;
            
            Object ingredient = itemFilterIngredientField.get(wrapper);
            if (ingredient instanceof Ingredient vanillaIngredient) {
                List<EmiStack> stacks = Arrays.stream(vanillaIngredient.getItems())
                        .map(EmiStack::of)
                        .collect(Collectors.toList());
                if (!stacks.isEmpty()) {
                    inputs.add(EmiIngredient.of(stacks));
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    private static void extractLootTableOutput(IJEIIngredientWrapper<?> wrapper, List<EmiStack> outputs) {
        try {
            if (lootTableField == null) return;
            
            Object lootTable = lootTableField.get(wrapper);
            if (lootTable instanceof ResourceLocation lootTableId) {
                // Get all possible loot items from the loot table
                List<LootTableHelper.LootData> loots = LootTableHelper.getLootsForTable(lootTableId);
                for (LootTableHelper.LootData lootData : loots) {
                    EmiStack stack = EmiStack.of(lootData.stack());
                    // Apply loot table chance using EMI's native probability system
                    double chance = lootData.chance();
                    if (chance < 1.0) {
                        stack = stack.setChance((float) chance);
                    }
                    outputs.add(stack);
                }
            }
        } catch (Exception e) {
            // Ignore reflection errors silently
        }
    }
    
    /**
     * Check if a wrapper is for input
     */
    public static boolean isInput(IJEIIngredientWrapper<?> wrapper) {
        // ItemFilterIngredientWrapper and FuelItemIngredientWrapper are always inputs
        if (wrapper instanceof ItemFilterIngredientWrapper || wrapper instanceof FuelItemIngredientWrapper) {
            return true;
        }
        
        try {
            Field modeField = null;
            if (wrapper instanceof ItemIngredientWrapper) {
                modeField = itemModeField;
            } else if (wrapper instanceof FluidIngredientWrapper) {
                modeField = fluidModeField;
            }
            
            if (modeField != null) {
                RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
                return mode == RequirementIOMode.INPUT;
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
    
    /**
     * Check if a wrapper is for output
     */
    public static boolean isOutput(IJEIIngredientWrapper<?> wrapper) {
        // LootTableIngredientWrapper is always output
        if (wrapper instanceof LootTableIngredientWrapper) {
            return true;
        }
        
        try {
            Field modeField = null;
            if (wrapper instanceof ItemIngredientWrapper) {
                modeField = itemModeField;
            } else if (wrapper instanceof FluidIngredientWrapper) {
                modeField = fluidModeField;
            }
            
            if (modeField != null) {
                RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
                return mode == RequirementIOMode.OUTPUT;
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
}

