package dev.arborsm.custom_machinery_emi.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import es.degrassi.custommachineryars.client.integration.jei.source.Source;
import es.degrassi.custommachineryars.guielement.SourceGuiElement;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.common.util.Utils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.List;

/**
 * EMI widget for rendering Ars Nouveau Source
 */
public class SourceEmiWidget extends Widget {
    private final Source source;
    private final int recipeTime;
    private final boolean isInput;
    private final int x, y, width, height;
    
    public SourceEmiWidget(SourceGuiElement element, Source source, int recipeTime, boolean isInput, int offsetX, int offsetY) {
        this.source = source;
        this.recipeTime = recipeTime;
        this.isInput = isInput;
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
        
        try {
            Class<?> widgetClass = Class.forName("es.degrassi.custommachineryars.client.render.element.SourceGuiElementWidget");
            Method renderMethod = widgetClass.getDeclaredMethod("renderSource",
                PoseStack.class, int.class, int.class, int.class, int.class, int.class);
            double fillPercent = 1.0d;
            int manaHeight = (int)(fillPercent * (double)(this.height - 4));
            renderMethod.invoke(null, graphics.pose(), manaHeight, x + 2, y + 2, this.width - 4, this.height - 4);
        } catch (Exception e) {
            CustomMachinery.LOGGER.error("Error while rendering SourceGuiElementWidget", e);
        }
        
        graphics.pose().popPose();
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        String amount = Utils.format(source.getAmount());
        Component component;
        
        if (source.isPerTick()) {
            String totalSource = Utils.format(source.getAmount() * recipeTime);
            if (isInput) {
                component = Component.translatable("custommachineryars.jei.ingredient.source.pertick.input", totalSource, amount);
            } else {
                component = Component.translatable("custommachineryars.jei.ingredient.source.pertick.output", totalSource, amount);
            }
        } else {
            if (isInput) {
                component = Component.translatable("custommachineryars.jei.ingredient.source.input", amount);
            } else {
                component = Component.translatable("custommachineryars.jei.ingredient.source.output", amount);
            }
        }
        
        return List.of(ClientTooltipComponent.create(component.getVisualOrderText()));
    }
}

