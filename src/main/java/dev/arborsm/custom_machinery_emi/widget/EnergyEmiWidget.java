package dev.arborsm.custom_machinery_emi.widget;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.wrapper.EnergyEmiWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.ClientHandler;
import fr.frinn.custommachinery.common.guielement.EnergyGuiElement;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

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
    public static EnergyEmiWidget fromWrappers(EnergyGuiElement element, List<EmiIngredientWrapper> wrappers, int offsetX, int offsetY) {
        Iterator<EmiIngredientWrapper> iterator = wrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if(wrapper instanceof EnergyEmiWrapper energyWrapper) {
                // Build tooltip
                List<Component> tooltip = new ArrayList<>();
                int amount = energyWrapper.amount();
                String amountStr = Utils.format(amount);
                Component unit = Component.translatable("unit.energy.forge");
                RequirementIOMode mode = energyWrapper.mode();
                
                if(energyWrapper.isPerTick()) {
                    String totalEnergy = Utils.format(amount * energyWrapper.recipeTime());
                    if(mode == RequirementIOMode.INPUT)
                        tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.pertick.input", totalEnergy, unit, amountStr, unit));
                    else
                        tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.pertick.output", totalEnergy, unit, amountStr, unit));
                } else {
                    if(mode == RequirementIOMode.INPUT)
                        tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.input", amountStr, unit));
                    else
                        tooltip.add(Component.translatable("custommachinery.jei.ingredient.energy.output", amountStr, unit));
                }
                
                int x = element.getX() - offsetX + 1;
                int y = element.getY() - offsetY + 1;
                
                iterator.remove();
                return new EnergyEmiWidget(element, x, y, tooltip);
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

