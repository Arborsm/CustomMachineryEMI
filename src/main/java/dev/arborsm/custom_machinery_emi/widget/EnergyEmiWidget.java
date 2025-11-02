package dev.arborsm.custom_machinery_emi.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.client.integration.jei.wrapper.EnergyIngredientWrapper;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.Energy;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom EMI widget for rendering energy in recipes
 */
public class EnergyEmiWidget extends Widget {
    
    private final EnergyGuiElement element;
    private final int x, y, width, height;
    private final List<Component> tooltip;
    
    private EnergyEmiWidget(EnergyGuiElement element, int x, int y, List<Component> tooltip) {
        this.element = element;
        this.x = x;
        this.y = y;
        this.width = element.getWidth() - 2;
        this.height = element.getHeight() - 2;
        this.tooltip = tooltip;
    }
    
    /**
     * Try to create an EnergyEmiWidget from the wrappers list
     * @return The widget if successful, null otherwise
     */
    @Nullable
    public static EnergyEmiWidget fromWrappers(EnergyGuiElement element, List<IJEIIngredientWrapper<?>> wrappers, int offsetX, int offsetY) {
        Iterator<IJEIIngredientWrapper<?>> iterator = wrappers.iterator();
        while(iterator.hasNext()) {
            IJEIIngredientWrapper<?> wrapper = iterator.next();
            if(wrapper instanceof EnergyIngredientWrapper) {
                try {
                    Field energyField = EnergyIngredientWrapper.class.getDeclaredField("energy");
                    energyField.setAccessible(true);
                    Energy energy = (Energy) energyField.get(wrapper);
                    
                    Field modeField = EnergyIngredientWrapper.class.getDeclaredField("mode");
                    modeField.setAccessible(true);
                    RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
                    
                    Field recipeTimeField = EnergyIngredientWrapper.class.getDeclaredField("recipeTime");
                    recipeTimeField.setAccessible(true);
                    int recipeTime = (Integer) recipeTimeField.get(wrapper);
                    
                    // Build tooltip
                    List<Component> tooltip = new ArrayList<>();
                    String amount = Utils.format(energy.amount());
                    Component unit = Component.translatable("unit.energy.forge");
                    if(energy.isPerTick()) {
                        String totalEnergy = Utils.format(energy.amount() * recipeTime);
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.pertick.input", totalEnergy, unit, amount, unit));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.pertick.output", totalEnergy, unit, amount, unit));
                    } else {
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.input", amount, unit));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.output", amount, unit));
                    }
                    
                    int x = element.getX() - offsetX + 1;
                    int y = element.getY() - offsetY + 1;
                    
                    iterator.remove();
                    return new EnergyEmiWidget(element, x, y, tooltip);
                } catch (Exception e) {
                    // Failed to extract energy data
                }
            }
        }
        return null;
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        int elementWidth = element.getWidth();
        int elementHeight = element.getHeight();
        
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        
        // Translate to make sure the filled texture is rendered on top of empty texture
        graphics.pose().translate(0, 0, 10);
        ClientHandler.blit(graphics, element.getFilledTexture(), -1, -1, elementWidth, elementHeight);
        
        graphics.pose().popPose();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }
}

