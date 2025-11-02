package dev.arborsm.custom_machinery_emi.mekanism;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinerymekanism.client.jei.heat.Heat;
import fr.frinn.custommachinerymekanism.common.guielement.HeatGuiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * EMI widget for rendering Mekanism Heat
 */
public class HeatEmiWidget extends Widget {
    
    private final HeatGuiElement element;
    private final Heat heat;
    private final int x, y, width, height;
    
    public HeatEmiWidget(HeatGuiElement element, Heat heat, int offsetX, int offsetY) {
        this.element = element;
        this.heat = heat;
        this.x = element.getX() - offsetX;
        this.y = element.getY() - offsetY;
        this.width = element.getWidth();
        this.height = element.getHeight();
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 10.0F);
        
        graphics.blit(
            element.getFilledTexture().texture(), 
            x, y, 
            element.getFilledTexture().u(), 
            element.getFilledTexture().v(), 
            width, height, 
            width, height
        );

        graphics.pose().popPose();
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<Component> tooltips = new ArrayList<>();
        
        if (heat.isPerTick()) {
            if (heat.mode() == RequirementIOMode.INPUT) {
                tooltips.add(Component.translatable("custommachinerymekanism.jei.ingredient.heat.pertick.input", heat.amount()));
            } else {
                tooltips.add(Component.translatable("custommachinerymekanism.jei.ingredient.heat.pertick.output", heat.amount()));
            }
        } else {
            if (heat.mode() == RequirementIOMode.INPUT) {
                tooltips.add(Component.translatable("custommachinerymekanism.jei.ingredient.heat.input", heat.amount()));
            } else {
                tooltips.add(Component.translatable("custommachinerymekanism.jei.ingredient.heat.output", heat.amount()));
            }
        }
        
        if (heat.chance() == 0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
        } else if (heat.chance() < 1.0D && heat.chance() > 0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(heat.chance() * 100)));
        }
        
        return tooltips.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }
}

