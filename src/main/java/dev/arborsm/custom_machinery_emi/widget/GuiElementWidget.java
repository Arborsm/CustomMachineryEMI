package dev.arborsm.custom_machinery_emi.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.IJEIElementRenderer;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * Generic widget for rendering GUI elements from Custom Machinery
 */
public class GuiElementWidget extends Widget {
    
    private final IGuiElement element;
    private final IMachineRecipe recipe;
    private final int offsetX, offsetY;
    
    public GuiElementWidget(IGuiElement element, IMachineRecipe recipe, int offsetX, int offsetY) {
        this.element = element;
        this.recipe = recipe;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(
            element.getX() - offsetX, 
            element.getY() - offsetY, 
            element.getWidth(), 
            element.getHeight()
        );
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        if (!element.showInJei())
            return;

        if (!GuiElementJEIRendererRegistry.hasJEIRenderer(element.getType()))
            return;
        
        IJEIElementRenderer<IGuiElement> renderer = GuiElementJEIRendererRegistry.getJEIRenderer(element.getType());
        
        graphics.pose().pushPose();
        graphics.pose().translate(-offsetX, -offsetY, 0);
        renderer.renderElementInJEI(graphics, element, recipe, mouseX, mouseY);
        graphics.pose().popPose();
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        if (!element.showInJei())
            return List.of();
        
        if (!GuiElementJEIRendererRegistry.hasJEIRenderer(element.getType()))
            return List.of();
        
        IJEIElementRenderer<IGuiElement> renderer = GuiElementJEIRendererRegistry.getJEIRenderer(element.getType());
        
        int adjustedX = mouseX + offsetX;
        int adjustedY = mouseY + offsetY;
        
        if (renderer.isHoveredInJei(element, element.getX(), element.getY(), adjustedX, adjustedY)) {
            List<Component> tooltips = renderer.getJEITooltips(element, recipe);
            return tooltips.stream()
                    .map(Component::getVisualOrderText)
                    .map(ClientTooltipComponent::create)
                    .toList();
        }
        
        return List.of();
    }
}

