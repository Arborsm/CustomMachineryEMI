package dev.arborsm.custom_machinery_emi.category;

import com.google.common.collect.ImmutableList;
import dev.arborsm.custom_machinery_emi.CustomMachineryEMIPlugin;
import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.EmiStackHelper;
import dev.arborsm.custom_machinery_emi.api.WrapperCreator;
import dev.arborsm.custom_machinery_emi.api.wrapper.*;
import dev.arborsm.custom_machinery_emi.widget.SourceEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.ChemicalEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.HeatEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.TemperatureEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.EnergyEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.ExperienceEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.FluidTankWidget;
import dev.arborsm.custom_machinery_emi.widget.PncHeatEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.PncPressureEmiWidget;
import dev.arborsm.custom_machinery_emi.widget.PncTemperatureEmiWidget;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import es.degrassi.custommachineryars.client.integration.jei.source.Source;
import es.degrassi.custommachineryars.guielement.SourceGuiElement;
import fr.frinn.custommachinery.api.component.IMachineComponentTemplate;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
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
import fr.frinn.custommachinerymekanism.common.guielement.ChemicalGuiElement;
import fr.frinn.custommachinerymekanism.common.guielement.HeatGuiElement;
import fr.frinn.custommachinerypnc.common.component.PressureMachineComponent;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalStack;
import mekanism.common.util.UnitDisplayUtils;
import fr.frinn.custommachinerypnc.common.guielement.PressureGuiElement;
import net.minecraft.world.item.crafting.RecipeType;

import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CustomMachineryRecipeCategory extends AbstractEMIRecipeCategory<CustomMachineRecipe> {

    private final List<EmiIngredientWrapper> wrappers;
    private SlotGuiElement currentSlotElement;

    public CustomMachineryRecipeCategory(CustomMachine machine, EmiRecipeCategory category, CustomMachineRecipe recipe) {
        super(machine, category, recipe);
        ImmutableList.Builder<EmiIngredientWrapper> wrappersBuilder = ImmutableList.builder();
        recipe.getDisplayInfoRequirements().forEach(requirement ->
                WrapperCreator.createWrapper(requirement, recipe).ifPresent(wrappersBuilder::add));
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

        List<EmiIngredientWrapper> remainingWrappers = new ArrayList<>(this.wrappers);
        
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
            if(handlePncHeat(widgets, element, remainingWrappers)) continue;
            if(handlePncPressure(widgets, element, remainingWrappers)) continue;
            
            if(element instanceof SlotGuiElement slotElement && 
               element.getType() == Registration.SLOT_GUI_ELEMENT.get()) {
                Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
                while(iterator.hasNext()) {
                    EmiIngredientWrapper wrapper = iterator.next();
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
                Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
                while(iterator.hasNext()) {
                    EmiIngredientWrapper wrapper = iterator.next();
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
    
    private boolean canWrapperMatchSlot(EmiIngredientWrapper wrapper, SlotGuiElement slot) {
        if (wrapper instanceof ItemEmiWrapper itemWrapper) {
            String slotId = itemWrapper.slot();
            String elementComponentId = slot.getComponentId();
            
            if (elementComponentId.equals(slotId)) return true;
            
            return recipeHelper.getComponentForElement(slot)
                .map(template -> {
                    if (template.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get()) return false;
                    net.neoforged.neoforge.common.crafting.SizedIngredient sizedIngredient = itemWrapper.ingredient();
                    java.util.List<net.minecraft.world.item.ItemStack> items = 
                        java.util.Arrays.stream(sizedIngredient.ingredient().getItems())
                            .map(item -> item.copyWithCount(sizedIngredient.count()))
                            .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
                    boolean isInput = itemWrapper.mode() == RequirementIOMode.INPUT;
                    return template.canAccept(items, isInput, recipeHelper.getDummyManager())
                        && (slotId.isEmpty() || template.getId().equals(slotId));
                })
                .orElse(false);
        }
        
        if (wrapper instanceof FuelItemEmiWrapper) {
            return recipeHelper.getComponentForElement(slot)
                .map(template -> template.getType() == Registration.ITEM_FUEL_MACHINE_COMPONENT.get())
                .orElse(false);
        }
        
        if (wrapper instanceof ItemFilterEmiWrapper filterWrapper) {
            String slotId = filterWrapper.slot();
            String elementComponentId = slot.getComponentId();
            
            if (elementComponentId.equals(slotId)) return true;
            
            return recipeHelper.getComponentForElement(slot)
                .map(template -> template.getType() == Registration.ITEM_FILTER_MACHINE_COMPONENT.get()
                    && (slotId.isEmpty() || template.getId().equals(slotId)))
                .orElse(false);
        }
        
        return false;
    }
    
    private boolean canWrapperMatchFluidSlot(EmiIngredientWrapper wrapper, FluidGuiElement fluidSlot) {
        if (!(wrapper instanceof FluidEmiWrapper fluidWrapper) || 
            recipeHelper.getComponentForElement(fluidSlot).isEmpty()) {
            return false;
        }
        
        String tankId = fluidWrapper.tank();
        String elementComponentId = fluidSlot.getComponentId();
        
        if (!tankId.isEmpty() && !tankId.equals(elementComponentId)) return false;
        
        return recipeHelper.getComponentForElement(fluidSlot)
            .map(template -> {
                boolean isInput = fluidWrapper.mode() == RequirementIOMode.INPUT;
                net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient sizedFluid = fluidWrapper.ingredient();
                java.util.List<net.neoforged.neoforge.fluids.FluidStack> fluids = 
                    java.util.Arrays.stream(sizedFluid.getFluids())
                        .map(fluid -> fluid.copyWithAmount(sizedFluid.amount()))
                        .toList();
                return template.canAccept(fluids, isInput, recipeHelper.getDummyManager())
                    && (tankId.isEmpty() || template.getId().equals(tankId));
            })
            .orElse(false);
    }
    
    private void addSlotForWrapper(WidgetHolder widgets, EmiIngredientWrapper wrapper, int x, int y) {
        var xReal =  x - 1;
        var yReal =  y - 1;
        if (wrapper.isInput()) {
            if (wrapper instanceof FuelItemEmiWrapper) {
                addFuelSlot(widgets, xReal, yReal);
                return;
            }
            List<EmiIngredient> inputs = EmiStackHelper.extractInputs(List.of(wrapper));
            if (!inputs.isEmpty()) {
                widgets.addSlot(inputs.getFirst(), xReal, yReal).drawBack(false);
            }
        } else if (wrapper.isOutput()) {
            List<EmiStack> outputs = EmiStackHelper.extractOutputs(List.of(wrapper));
            if (!outputs.isEmpty()) {
                widgets.addSlot(outputs.getFirst(), xReal, yReal).drawBack(false).recipeContext(this);
            }
        }
    }
    
    private void addFluidSlotForWrapper(WidgetHolder widgets, EmiIngredientWrapper wrapper, int x, int y, int width, int height) {
        if (wrapper.isInput()) {
            List<EmiIngredient> inputs = EmiStackHelper.extractInputs(List.of(wrapper));
            if (!inputs.isEmpty()) {
                FluidTankWidget slot = new FluidTankWidget(inputs.getFirst(), x, y, width, height, extractFluidCapacity(wrapper));
                widgets.add(slot);
            }
        } else if (wrapper.isOutput()) {
            List<EmiStack> outputs = EmiStackHelper.extractOutputs(List.of(wrapper));
            if (!outputs.isEmpty()) {
                FluidTankWidget slot = new FluidTankWidget(outputs.getFirst(), x, y, width, height, extractFluidCapacity(wrapper));
                slot.setRecipe(this);
                widgets.add(slot);
            }
        }
    }
    
    private long extractFluidCapacity(EmiIngredientWrapper wrapper) {
        if (wrapper instanceof FluidEmiWrapper fluidWrapper) {
            return Math.max(1, fluidWrapper.ingredient().amount());
        }
        return 1000;
    }
    
    private void addFuelSlot(WidgetHolder widgets, int x, int y) {
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
    
    private boolean handleArsNouveauSource(WidgetHolder widgets, IGuiElement element, List<EmiIngredientWrapper> remainingWrappers) {
        if (!ModList.get().isLoaded("custommachineryars")) {
            return false;
        }

        if (!(element instanceof SourceGuiElement sourceGuiElement)) {
            return false;
        }

        Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if (wrapper instanceof dev.arborsm.custom_machinery_emi.api.wrapper.SourceEmiWrapper sourceWrapper) {
                var source = new Source(sourceWrapper.amount(), sourceWrapper.isPerTick());
                SourceEmiWidget widget = new SourceEmiWidget(sourceGuiElement, source, sourceWrapper.recipeTime(), sourceWrapper.isInput(), offsetX, offsetY);
                widgets.add(widget);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private boolean handleMekanismHeat(WidgetHolder widgets, IGuiElement element, List<EmiIngredientWrapper> remainingWrappers) {
        if (!ModList.get().isLoaded("custommachinerymekanism")) {
            return false;
        }

        if (!(element instanceof HeatGuiElement heatGuiElement)) {
            return false;
        }

        Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
        while (iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if (wrapper instanceof HeatEmiWrapper(
                    RequirementIOMode mode, double amount, double chance, boolean isPerTick
            )) {
                var heat = new Heat(amount, chance, isPerTick, mode);
                var widget = new HeatEmiWidget(heatGuiElement, heat, offsetX, offsetY);
                widgets.add(widget);
                iterator.remove();
                return true;
            } else if (wrapper instanceof TemperatureEmiWrapper(
                    DoubleRange temp, UnitDisplayUtils.TemperatureUnit unit
            )) {
                TemperatureEmiWidget widget = new TemperatureEmiWidget(heatGuiElement, temp, unit, offsetX, offsetY);
                widgets.add(widget);
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private boolean handleMekanismChemical(WidgetHolder widgets, IGuiElement element, List<EmiIngredientWrapper> remainingWrappers) {
        if (!ModList.get().isLoaded("custommachinerymekanism")) {
            return false;
        }

        if (!(element instanceof ChemicalGuiElement)) {
            return false;
        }

        Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if (wrapper instanceof ChemicalEmiWrapper(
                    RequirementIOMode mode, Chemical chemical, long amount, double chance,
                    boolean isPerTick, String tank
            )) {
                var chemicalStack = new ChemicalStack(MekanismAPI.CHEMICAL_REGISTRY.wrapAsHolder(chemical), amount);
                ChemicalEmiWidget widget = new ChemicalEmiWidget(element, chemicalStack,
                        mode,
                        chance,
                        isPerTick,
                        tank,
                        offsetX, offsetY);
                widgets.add(widget);
                iterator.remove();
                return true;
            }
        }

        return false;
    }

    private boolean handlePncHeat(WidgetHolder widgets, IGuiElement element, List<EmiIngredientWrapper> remainingWrappers) {
        if (!ModList.get().isLoaded("custommachinerypnc")) {
            return false;
        }
        
        if (!(element instanceof fr.frinn.custommachinerypnc.common.guielement.HeatGuiElement)) {
            return false;
        }
        
        Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if (wrapper instanceof PncHeatEmiWrapper) {
                PncHeatEmiWidget widget = PncHeatEmiWidget.fromWrappers(
                    (fr.frinn.custommachinerypnc.common.guielement.HeatGuiElement) element,
                    remainingWrappers,
                    offsetX, offsetY,
                    recipe.getRecipeTime()
                );
                if (widget != null) {
                    widgets.add(widget);
                    iterator.remove();
                    return true;
                }
            } else if (wrapper instanceof PncTemperatureEmiWrapper) {
                PncTemperatureEmiWidget widget = PncTemperatureEmiWidget.fromWrappers(
                    (fr.frinn.custommachinerypnc.common.guielement.HeatGuiElement) element,
                    remainingWrappers,
                    offsetX, offsetY
                );
                if (widget != null) {
                    widgets.add(widget);
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handlePncPressure(WidgetHolder widgets, IGuiElement element, List<EmiIngredientWrapper> remainingWrappers) {
        if (!ModList.get().isLoaded("custommachinerypnc")) {
            return false;
        }
        
        if (!(element instanceof PressureGuiElement)) {
            return false;
        }

        IMachineComponentTemplate<?> template =
                recipeHelper.getComponentForElement((fr.frinn.custommachinery.api.guielement.IComponentGuiElement<?>) element).orElse(null);

        float danger = 4.0f;
        float critical = 5.0f;

        if (template instanceof PressureMachineComponent.Template pressureTemplate) {
            danger = pressureTemplate.danger();
            critical = pressureTemplate.critical();
        }

        Iterator<EmiIngredientWrapper> iterator = remainingWrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if (wrapper instanceof PncPressureEmiWrapper) {
                PncPressureEmiWidget widget = PncPressureEmiWidget.fromWrappers(
                        (PressureGuiElement) element,
                        remainingWrappers,
                        danger,
                        critical,
                        offsetX, offsetY
                );
                if (widget != null) {
                    widgets.add(widget);
                    iterator.remove();
                    return true;
                }
            }
        }
        return false;
    }
}
