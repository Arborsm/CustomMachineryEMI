package dev.arborsm.custom_machinery_emi.category;

import com.google.common.collect.ImmutableList;
import dev.arborsm.custom_machinery_emi.CustomMachineryEMIPlugin;
import dev.arborsm.custom_machinery_emi.api.EmiStackHelper;
import dev.arborsm.custom_machinery_emi.ars.SourceEmiWidget;
import dev.arborsm.custom_machinery_emi.mekanism.ChemicalEmiWidget;
import dev.arborsm.custom_machinery_emi.mekanism.HeatEmiWidget;
import dev.arborsm.custom_machinery_emi.mekanism.TemperatureEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.EnergyEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.ExperienceEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.FluidTankWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.custommachineryars.client.integration.jei.source.Source;
import es.degrassi.custommachineryars.guielement.SourceGuiElement;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FluidIngredientWrapper;
import fr.frinn.custommachinery.client.integration.jei.wrapper.FuelItemIngredientWrapper;
import fr.frinn.custommachinery.common.component.item.ItemMachineComponent;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.guielement.FluidGuiElement;
import fr.frinn.custommachinery.common.guielement.SlotGuiElement;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import fr.frinn.custommachinerymekanism.client.jei.heat.Heat;
import fr.frinn.custommachinerymekanism.common.guielement.HeatGuiElement;
import mekanism.common.util.UnitDisplayUtils;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomMachineryRecipeCategory extends AbstractEMIRecipeCategory<CustomMachineRecipe> {

    private final List<IJEIIngredientWrapper<?>> wrappers;
    private SlotGuiElement currentSlotElement;

    public CustomMachineryRecipeCategory(CustomMachine machine, EmiRecipeCategory category, CustomMachineRecipe recipe) {
        super(machine, category, recipe);
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
        return EmiStackHelper.extractOutputs(this.wrappers);
    }

    @Override
    protected void addIngredientSlots(WidgetHolder widgets) {
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? 
            this.machine.getGuiElements() : this.machine.getJeiElements();
        
        if(!recipe.getGuiElements().isEmpty())
            elements = recipe.getCustomGuiElements(elements);

        List<IJEIIngredientWrapper<?>> remainingWrappers = new ArrayList<>(this.wrappers);
        
        for(IGuiElement element : elements) {
            if(element instanceof EnergyGuiElement energyElement && 
               element.getType() == Registration.ENERGY_GUI_ELEMENT.get()) {
                EnergyEmiWidget widget = EnergyEmiWidget.fromWrappers(energyElement, remainingWrappers, this.offsetX, this.offsetY);
                if(widget != null) widgets.add(widget);
                continue;
            }
            
            if(element instanceof ExperienceGuiElement experienceElement && 
               element.getType() == Registration.EXPERIENCE_GUI_ELEMENT.get() &&
               experienceElement.getMode().isDisplay()) {
                ExperienceEmiWidget widget = ExperienceEmiWidget.fromWrappers(experienceElement, remainingWrappers, this.offsetX, this.offsetY);
                if(widget != null) widgets.add(widget);
                continue;
            }
            
            if(handleArsNouveauSource(widgets, element, remainingWrappers)) continue;
            if(handleMekanismHeat(widgets, element, remainingWrappers)) continue;
            if(handleMekanismChemical(widgets, element, remainingWrappers)) continue;
            
            if(element instanceof SlotGuiElement slotElement && 
               element.getType() == Registration.SLOT_GUI_ELEMENT.get()) {
                Iterator<IJEIIngredientWrapper<?>> iterator = remainingWrappers.iterator();
                while(iterator.hasNext()) {
                    IJEIIngredientWrapper<?> wrapper = iterator.next();
                    if(canWrapperMatchSlot(wrapper, slotElement)) {
                        int slotX = element.getX() + (element.getWidth() - 16) / 2 - this.offsetX;
                        int slotY = element.getY() + (element.getHeight() - 16) / 2 - this.offsetY;
                        this.currentSlotElement = slotElement;
                        addSlotForWrapper(widgets, wrapper, slotX, slotY);
                        this.currentSlotElement = null;
                        iterator.remove();
                        break;
                    }
                }
            }
            
            if(element instanceof FluidGuiElement fluidElement && 
               element.getType() == Registration.FLUID_GUI_ELEMENT.get()) {
                Iterator<IJEIIngredientWrapper<?>> iterator = remainingWrappers.iterator();
                while(iterator.hasNext()) {
                    IJEIIngredientWrapper<?> wrapper = iterator.next();
                    if(canWrapperMatchFluidSlot(wrapper, fluidElement)) {
                        int slotX = element.getX() - this.offsetX + 1;
                        int slotY = element.getY() - this.offsetY + 1;
                        addFluidSlotForWrapper(widgets, wrapper, slotX, slotY, 
                            element.getWidth() - 2, element.getHeight() - 2);
                        iterator.remove();
                        break;
                    }
                }
            }
        }
    }
    
    private boolean canWrapperMatchSlot(IJEIIngredientWrapper<?> wrapper, SlotGuiElement slot) {
        if (wrapper instanceof fr.frinn.custommachinery.client.integration.jei.wrapper.ItemIngredientWrapper) {
            try {
                Field modeField = wrapper.getClass().getDeclaredField("mode");
                Field slotField = wrapper.getClass().getDeclaredField("slot");
                Field ingredientField = wrapper.getClass().getDeclaredField("ingredient");
                modeField.setAccessible(true);
                slotField.setAccessible(true);
                ingredientField.setAccessible(true);
                
                RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
                String slotId = (String) slotField.get(wrapper);
                Object ingredient = ingredientField.get(wrapper);
                String elementComponentId = slot.getComponentId();
                
                if (elementComponentId.equals(slotId)) return true;
                
                return recipeHelper.getComponentForElement(slot)
                    .map(template -> {
                        if (template.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get()) return false;
                        if (ingredient instanceof net.neoforged.neoforge.common.crafting.SizedIngredient sizedIngredient) {
                            java.util.List<net.minecraft.world.item.ItemStack> items = 
                                java.util.Arrays.stream(sizedIngredient.ingredient().getItems())
                                    .map(item -> item.copyWithCount(sizedIngredient.count()))
                                    .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
                            boolean isInput = mode == RequirementIOMode.INPUT;
                            return template.canAccept(items, isInput, recipeHelper.getDummyManager())
                                && (slotId.isEmpty() || template.getId().equals(slotId));
                        }
                        return false;
                    })
                    .orElse(false);
            } catch (Exception e) {
                return false;
            }
        }
        
        if (wrapper instanceof FuelItemIngredientWrapper) {
            return recipeHelper.getComponentForElement(slot)
                .map(template -> template.getType() == Registration.ITEM_FUEL_MACHINE_COMPONENT.get())
                .orElse(false);
        }
        
        if (wrapper instanceof fr.frinn.custommachinery.client.integration.jei.wrapper.ItemFilterIngredientWrapper) {
            try {
                Field slotField = wrapper.getClass().getDeclaredField("slot");
                slotField.setAccessible(true);
                String slotId = (String) slotField.get(wrapper);
                String elementComponentId = slot.getComponentId();
                
                if (elementComponentId.equals(slotId)) return true;
                
                return recipeHelper.getComponentForElement(slot)
                    .map(template -> template.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get()
                        && (slotId.isEmpty() || template.getId().equals(slotId)))
                    .orElse(false);
            } catch (Exception e) {
                return false;
            }
        }
        
        return false;
    }
    
    private boolean canWrapperMatchFluidSlot(IJEIIngredientWrapper<?> wrapper, FluidGuiElement fluidSlot) {
        if (!(wrapper instanceof FluidIngredientWrapper) || 
            recipeHelper.getComponentForElement(fluidSlot).isEmpty()) {
            return false;
        }
        
        try {
            Field modeField = wrapper.getClass().getDeclaredField("mode");
            Field tankField = wrapper.getClass().getDeclaredField("tank");
            Field ingredientField = wrapper.getClass().getDeclaredField("ingredient");
            modeField.setAccessible(true);
            tankField.setAccessible(true);
            ingredientField.setAccessible(true);
            
            RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
            String tankId = (String) tankField.get(wrapper);
            Object ingredient = ingredientField.get(wrapper);
            String elementComponentId = fluidSlot.getComponentId();
            
            if (!tankId.isEmpty() && !tankId.equals(elementComponentId)) return false;
            
            return recipeHelper.getComponentForElement(fluidSlot)
                .map(template -> {
                    boolean isInput = mode == RequirementIOMode.INPUT;
                    if (ingredient instanceof net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient sizedFluid) {
                        java.util.List<net.neoforged.neoforge.fluids.FluidStack> fluids = 
                            java.util.Arrays.stream(sizedFluid.getFluids())
                                .map(fluid -> fluid.copyWithAmount(sizedFluid.amount()))
                                .toList();
                        return template.canAccept(fluids, isInput, recipeHelper.getDummyManager())
                            && (tankId.isEmpty() || template.getId().equals(tankId));
                    }
                    return false;
                })
                .orElse(false);
        } catch (Exception e) {
            return true;
        }
    }
    
    private void addSlotForWrapper(WidgetHolder widgets, IJEIIngredientWrapper<?> wrapper, int x, int y) {
        var xReal =  x - 1;
        var yReal =  y - 1;
        if (EmiStackHelper.isInput(wrapper)) {
            if (wrapper instanceof FuelItemIngredientWrapper) {
                addFuelSlot(widgets, wrapper, xReal, yReal);
                return;
            }
            List<EmiIngredient> inputs = EmiStackHelper.extractInputs(List.of(wrapper));
            if (!inputs.isEmpty()) {
                widgets.addSlot(inputs.getFirst(), xReal, yReal).drawBack(false);
            }
        } else if (EmiStackHelper.isOutput(wrapper)) {
            List<EmiStack> outputs = EmiStackHelper.extractOutputs(List.of(wrapper));
            if (!outputs.isEmpty()) {
                widgets.addSlot(outputs.getFirst(), xReal, yReal).drawBack(false).recipeContext(this);
            }
        }
    }
    
    private void addFluidSlotForWrapper(WidgetHolder widgets, IJEIIngredientWrapper<?> wrapper, int x, int y, int width, int height) {
        if (EmiStackHelper.isInput(wrapper)) {
            List<EmiIngredient> inputs = EmiStackHelper.extractInputs(List.of(wrapper));
            if (!inputs.isEmpty()) {
                FluidTankWidget slot = new FluidTankWidget(inputs.getFirst(), x, y, width, height, extractFluidCapacity(wrapper));
                widgets.add(slot);
            }
        } else if (EmiStackHelper.isOutput(wrapper)) {
            List<EmiStack> outputs = EmiStackHelper.extractOutputs(List.of(wrapper));
            if (!outputs.isEmpty()) {
                FluidTankWidget slot = new FluidTankWidget(outputs.getFirst(), x, y, width, height, extractFluidCapacity(wrapper));
                slot.setRecipe(this);
                widgets.add(slot);
            }
        }
    }
    
    private long extractFluidCapacity(IJEIIngredientWrapper<?> wrapper) {
        try {
            if (wrapper instanceof FluidIngredientWrapper) {
                Field ingredientField = wrapper.getClass().getDeclaredField("ingredient");
                ingredientField.setAccessible(true);
                Object ingredient = ingredientField.get(wrapper);
                if (ingredient instanceof net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient sizedFluid) {
                    return Math.max(1, sizedFluid.amount());
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return 1000;
    }
    
    private void addFuelSlot(WidgetHolder widgets, IJEIIngredientWrapper<?> wrapper, int x, int y) {
        List<EmiStack> fuelStacks = CustomMachineryEMIPlugin.FUEL_INGREDIENTS.stream()
                .filter(stack -> recipeHelper.getComponentForElement(currentSlotElement)
                        .filter(template -> template instanceof ItemMachineComponent.Template)
                        .map(template -> template.canAccept(stack, true, recipeHelper.getDummyManager()))
                        .orElse(false))
                .map(EmiStack::of)
                .toList();

        int fuelAmount = CustomMachineryEMIPlugin.FUEL_INGREDIENTS.stream()
                .mapToInt(item -> item.getBurnTime(RecipeType.SMELTING))
                .sum();
        
        if (!fuelStacks.isEmpty()) {
            var slot = widgets.addSlot(EmiIngredient.of(fuelStacks), x, y);
            if (fuelAmount > 0) {
                slot.appendTooltip(net.minecraft.network.chat.Component.translatable(
                    "custommachinery.jei.ingredient.fuel.amount", fuelAmount
                ).withStyle(net.minecraft.ChatFormatting.GOLD));
            }
        }
    }
    
    private boolean handleArsNouveauSource(WidgetHolder widgets, IGuiElement element, List<IJEIIngredientWrapper<?>> remainingWrappers) {
        try {
            Class<?> sourceElementClass = Class.forName("es.degrassi.custommachineryars.guielement.SourceGuiElement");
            Class<?> sourceWrapperClass = Class.forName("es.degrassi.custommachineryars.client.integration.jei.wrapper.SourceIngredientWrapper");
            
            if (!sourceElementClass.isInstance(element)) {
                return false;
            }
            
            Iterator<IJEIIngredientWrapper<?>> iterator = remainingWrappers.iterator();
            while(iterator.hasNext()) {
                IJEIIngredientWrapper<?> wrapper = iterator.next();
                if (sourceWrapperClass.isInstance(wrapper)) {
                    SourceEmiWidget widget = getSourceEmiWidget((SourceGuiElement) element, sourceWrapperClass, wrapper);
                    widgets.add(widget);
                    iterator.remove();
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private @NotNull SourceEmiWidget getSourceEmiWidget(SourceGuiElement element, Class<?> sourceWrapperClass, IJEIIngredientWrapper<?> wrapper) throws NoSuchFieldException, IllegalAccessException {
        var modeField = sourceWrapperClass.getDeclaredField("mode");
        var sourceField = sourceWrapperClass.getDeclaredField("source");
        var recipeTimeField = sourceWrapperClass.getDeclaredField("recipeTime");
        modeField.setAccessible(true);
        sourceField.setAccessible(true);
        recipeTimeField.setAccessible(true);

        Object mode = modeField.get(wrapper);
        Object source = sourceField.get(wrapper);
        int recipeTime = (Integer) recipeTimeField.get(wrapper);

        boolean isInput = "INPUT".equals(mode.toString());
        return new SourceEmiWidget(element, (Source) source, recipeTime, isInput, offsetX, offsetY);
    }

    private boolean handleMekanismHeat(WidgetHolder widgets, IGuiElement element, List<IJEIIngredientWrapper<?>> remainingWrappers) {
        try {
            Class<?> heatElementClass = Class.forName("fr.frinn.custommachinerymekanism.common.guielement.HeatGuiElement");
            Class<?> heatWrapperClass = Class.forName("fr.frinn.custommachinerymekanism.client.jei.wrapper.HeatIngredientWrapper");
            Class<?> tempWrapperClass = Class.forName("fr.frinn.custommachinerymekanism.client.jei.wrapper.TemperatureIngredientWrapper");
            
            if (!heatElementClass.isInstance(element)) {
                return false;
            }
            
            Iterator<IJEIIngredientWrapper<?>> iterator = remainingWrappers.iterator();
            while(iterator.hasNext()) {
                IJEIIngredientWrapper<?> wrapper = iterator.next();
                if (heatWrapperClass.isInstance(wrapper)) {
                    widgets.add(getHeatEmiWidget((HeatGuiElement) element, heatWrapperClass, wrapper));
                    iterator.remove();
                    return true;
                }
                if (tempWrapperClass.isInstance(wrapper)) {
                    widgets.add(getTemperatureEmiWidget((HeatGuiElement) element, tempWrapperClass, wrapper));
                    iterator.remove();
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }

    private @NotNull TemperatureEmiWidget getTemperatureEmiWidget(HeatGuiElement element, Class<?> tempWrapperClass, IJEIIngredientWrapper<?> wrapper) throws NoSuchFieldException, IllegalAccessException {
        Field tempField = tempWrapperClass.getDeclaredField("temp");
        Field unitField = tempWrapperClass.getDeclaredField("unit");
        tempField.setAccessible(true);
        unitField.setAccessible(true);
        return new TemperatureEmiWidget(element, (DoubleRange) tempField.get(wrapper), 
            (UnitDisplayUtils.TemperatureUnit) unitField.get(wrapper), offsetX, offsetY);
    }

    private @NotNull HeatEmiWidget getHeatEmiWidget(HeatGuiElement element, Class<?> heatWrapperClass, IJEIIngredientWrapper<?> wrapper) throws NoSuchFieldException, IllegalAccessException {
        Field heatField = heatWrapperClass.getDeclaredField("heat");
        heatField.setAccessible(true);
        return new HeatEmiWidget(element, (Heat) heatField.get(wrapper), offsetX, offsetY);
    }

    private boolean handleMekanismChemical(WidgetHolder widgets, IGuiElement element, List<IJEIIngredientWrapper<?>> remainingWrappers) {
        try {
            Class<?> chemicalElementClass = Class.forName("fr.frinn.custommachinerymekanism.common.guielement.ChemicalGuiElement");
            Class<?> chemicalWrapperClass = Class.forName("fr.frinn.custommachinerymekanism.client.jei.wrapper.ChemicalIngredientWrapper");
            
            if (!chemicalElementClass.isInstance(element)) {
                return false;
            }
            
            Iterator<IJEIIngredientWrapper<?>> iterator = remainingWrappers.iterator();
            while(iterator.hasNext()) {
                IJEIIngredientWrapper<?> wrapper = iterator.next();
                
                if (chemicalWrapperClass.isInstance(wrapper)) {
                    Field modeField = chemicalWrapperClass.getDeclaredField("mode");
                    Field chemicalField = chemicalWrapperClass.getDeclaredField("chemical");
                    Field amountField = chemicalWrapperClass.getDeclaredField("amount");
                    Field chanceField = chemicalWrapperClass.getDeclaredField("chance");
                    Field isPerTickField = chemicalWrapperClass.getDeclaredField("isPerTick");
                    Field tankField = chemicalWrapperClass.getDeclaredField("tank");
                    modeField.setAccessible(true);
                    chemicalField.setAccessible(true);
                    amountField.setAccessible(true);
                    chanceField.setAccessible(true);
                    isPerTickField.setAccessible(true);
                    tankField.setAccessible(true);
                    
                    Object mode = modeField.get(wrapper);
                    Object chemical = chemicalField.get(wrapper);
                    long amount = (Long) amountField.get(wrapper);
                    double chance = (Double) chanceField.get(wrapper);
                    boolean isPerTick = (Boolean) isPerTickField.get(wrapper);
                    String tank = (String) tankField.get(wrapper);
                    
                    String elementComponentId = element.getId();
                    if (!elementComponentId.equals(tank) && !tank.isEmpty()) continue;
                    
                    Class<?> chemicalStackClass = Class.forName("mekanism.api.chemical.ChemicalStack");
                    Class<?> holderClass = Class.forName("net.minecraft.core.Holder");
                    Object holder = holderClass.getMethod("direct", Object.class).invoke(null, chemical);
                    Object chemicalStack = chemicalStackClass.getConstructor(holderClass, long.class).newInstance(holder, amount);
                    
                    widgets.add(new ChemicalEmiWidget(element, chemicalStack, (RequirementIOMode) mode,
                        chance, isPerTick, tank, offsetX, offsetY));
                    iterator.remove();
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        return false;
    }
}
