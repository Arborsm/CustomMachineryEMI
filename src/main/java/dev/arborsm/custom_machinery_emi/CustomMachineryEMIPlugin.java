package dev.arborsm.custom_machinery_emi;

import com.google.common.collect.Lists;
import dev.arborsm.custom_machinery_emi.api.EMIRecipeTypes;
import dev.arborsm.custom_machinery_emi.category.CustomCraftRecipeCategory;
import dev.arborsm.custom_machinery_emi.category.CustomMachineryRecipeCategory;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiInitRegistry;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.init.CustomMachineItem;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.fml.ModList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@EmiEntrypoint
public class CustomMachineryEMIPlugin implements EmiPlugin {

    public static final Map<ResourceLocation, EmiRecipeCategory> CATEGORIES = new HashMap<>();
    public static final List<ItemStack> FUEL_INGREDIENTS = Lists.newArrayList();

    @Override
    public void initialize(EmiInitRegistry registry) {
        // Initialize fuel ingredients list
        FUEL_INGREDIENTS.clear();
        BuiltInRegistries.ITEM.stream()
                .map(Item::getDefaultInstance)
                .filter(stack -> stack.getBurnTime(RecipeType.SMELTING) > 0)
                .forEach(FUEL_INGREDIENTS::add);
        
        if (!ModList.get().isLoaded("jei")) {
            GuiElementJEIRendererRegistry.init();
        }
        
        CustomMachinery.LOGGER.info("EMI Plugin initialized with {} fuel ingredients", FUEL_INGREDIENTS.size());
    }

    @Override
    public void register(EmiRegistry registry) {
        if(Minecraft.getInstance().level == null)
            return;

        // Register categories for each machine
        CATEGORIES.clear();
        CustomMachinery.MACHINES.forEach((id, machine) -> {
            EmiRecipeCategory category = null;
            ItemStack icon = CustomMachineItem.makeMachineItem(id);
            
            if(machine.getProcessorTemplate().getType() == Registration.MACHINE_PROCESSOR.get()) {
                category = new EmiRecipeCategory(id, EmiStack.of(icon));
                EMIRecipeTypes.create(id, category, CustomMachineRecipe.class);
            } else if(machine.getProcessorTemplate().getType() == Registration.CRAFT_PROCESSOR.get()) {
                category = new EmiRecipeCategory(id, EmiStack.of(icon));
                EMIRecipeTypes.create(id, category, CustomCraftRecipe.class);
            }

            if(category != null) {
                registry.addCategory(category);
                CATEGORIES.put(machine.getId(), category);
            }
        });

        // Register machine recipes
        Map<ResourceLocation, List<CustomMachineRecipe>> machineRecipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(CustomMachineRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .collect(Collectors.groupingBy(CustomMachineRecipe::getMachineId));
        
        machineRecipes.forEach((id, list) -> {
            EmiRecipeCategory category = EMIRecipeTypes.machine(id);
            if(category != null && CustomMachinery.MACHINES.containsKey(id)) {
                CustomMachine machine = CustomMachinery.MACHINES.get(id);
                list.forEach(recipe -> {
                    CustomMachineryRecipeCategory emiRecipe = new CustomMachineryRecipeCategory(machine, category, recipe);
                    registry.addRecipe(emiRecipe);
                });
            }
        });

        // Register craft recipes
        Map<ResourceLocation, List<CustomCraftRecipe>> craftRecipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(Registration.CUSTOM_CRAFT_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(CustomCraftRecipe::showInJei)
                .sorted(Comparators.JEI_PRIORITY_COMPARATOR.reversed())
                .collect(Collectors.groupingBy(CustomCraftRecipe::getMachineId));
        
        craftRecipes.forEach((id, list) -> {
            EmiRecipeCategory category = EMIRecipeTypes.craft(id);
            if(category != null && CustomMachinery.MACHINES.containsKey(id)) {
                CustomMachine machine = CustomMachinery.MACHINES.get(id);
                list.forEach(recipe -> {
                    CustomCraftRecipeCategory emiRecipe = new CustomCraftRecipeCategory(machine, category, recipe);
                    registry.addRecipe(emiRecipe);
                });
            }
        });

        // Register workstations (catalysts)
        CustomMachinery.MACHINES.forEach((id, machine) -> machine.getRecipeIds().forEach(recipeId -> {
            EmiRecipeCategory category = EMIRecipeTypes.fromID(recipeId);
            if (category != null) {
                List<ResourceLocation> catalysts = machine.getCatalysts();
                if (!catalysts.contains(id)) {
                    registry.addWorkstation(category, EmiStack.of(CustomMachineItem.makeMachineItem(id)));
                }
                machine.getCatalysts().forEach(catalyst -> {
                    if (CustomMachinery.MACHINES.containsKey(catalyst)) {
                        registry.addWorkstation(category, EmiStack.of(CustomMachineItem.makeMachineItem(catalyst)));
                    } else if (BuiltInRegistries.ITEM.containsKey(catalyst)) {
                        registry.addWorkstation(category, EmiStack.of(BuiltInRegistries.ITEM.get(catalyst).getDefaultInstance()));
                    }
                });
            }
        }));
    }
}
