package dev.arborsm.custom_machinery_emi.category;

import com.google.common.collect.ImmutableList;
import dev.arborsm.custom_machinery_emi.api.EmiStackHelper;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;

import java.util.ArrayList;
import java.util.List;

public class CustomCraftRecipeCategory extends AbstractEMIRecipeCategory<CustomCraftRecipe> {

    private final List<IJEIIngredientWrapper<?>> wrappers;

    public CustomCraftRecipeCategory(CustomMachine machine, EmiRecipeCategory category, CustomCraftRecipe recipe) {
        super(machine, category, recipe);
        
        // Cache ingredient wrappers including the output
        ImmutableList.Builder<IJEIIngredientWrapper<?>> wrappersBuilder = ImmutableList.builder();
        recipe.getDisplayInfoRequirements().forEach(requirement -> 
            wrappersBuilder.addAll(requirement.getJeiIngredientWrappers(recipe))
        );
        this.wrappers = wrappersBuilder.build();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return EmiStackHelper.extractInputs(this.wrappers);
    }

    @Override
    public List<EmiStack> getOutputs() {
        // Always include the crafting output
        List<EmiStack> outputs = new ArrayList<>(EmiStackHelper.extractOutputs(this.wrappers));
        outputs.add(EmiStack.of(recipe.getOutput()));
        return outputs;
    }

    @Override
    protected void addIngredientSlots(WidgetHolder widgets) {
        // Get the GUI elements to display
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? 
            this.machine.getGuiElements() : this.machine.getJeiElements();

        // Find the result slot to display output
        String resultSlotId = machine.getComponentTemplates().stream()
                .filter(template -> template instanceof ItemMachineComponent.Template slotTemplate && 
                                   slotTemplate.getType() == Registration.ITEM_RESULT_MACHINE_COMPONENT.get())
                .findFirst()
                .map(IMachineComponentTemplate::getId)
                .orElse("");

        // Add output slot if found
        for(IGuiElement element : elements) {
            if(element instanceof SlotGuiElement slotElement && 
               slotElement.getComponentId().equals(resultSlotId)) {
                int slotX = element.getX() - this.offsetX + (element.getWidth() - 16) / 2;
                int slotY = element.getY() - this.offsetY + (element.getHeight() - 16) / 2;
                
                widgets.addSlot(EmiStack.of(recipe.getOutput()), slotX, slotY)
                       .recipeContext(this);
                break;
            }
        }
    }
}
