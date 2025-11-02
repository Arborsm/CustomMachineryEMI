package dev.arborsm.custom_machinery_emi.widget;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.wrapper.PncPressureEmiWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinerypnc.common.guielement.PressureGuiElement;
import me.desht.pneumaticcraft.client.render.pressure_gauge.PressureGaugeRenderer2D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

import static dev.arborsm.custom_machinery_emi.widget.PncHeatEmiWidget.getClientTooltipComponents;

public class PncPressureEmiWidget extends Widget {

    private final PncPressureEmiWrapper wrapper;
    private final float danger;
    private final float critical;
    private final int x, y, width, height;
    
    public PncPressureEmiWidget(PressureGuiElement element, PncPressureEmiWrapper wrapper, float danger, float critical, int offsetX, int offsetY) {
        this.wrapper = wrapper;
        this.danger = danger;
        this.critical = critical;
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
        
        float scaleX = this.width / 40f;
        float scaleY = this.height / 40f;
        graphics.pose().translate(this.x + this.width / 2f, this.y + this.height / 2f, 0);
        graphics.pose().scale(scaleX, scaleY, 1);
        
        float avgPressure = (wrapper.minPressure() + wrapper.maxPressure()) / 2f;
        
        PressureGaugeRenderer2D.drawPressureGauge(
            graphics, 
            Minecraft.getInstance().font, 
            -1, 
            critical, 
            danger, 
            wrapper.minPressure(),
            avgPressure, 
            0, 
            0
        );
        
        graphics.pose().popPose();
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        List<Component> tooltips = new ArrayList<>();
        
        if (wrapper.volume() > 0) {
            String volume = Utils.format(wrapper.volume());
            if (wrapper.isInput()) {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.pressure.input", 
                    wrapper.minPressure(), wrapper.maxPressure(), volume));
            } else {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.pressure.output", 
                    wrapper.minPressure(), wrapper.maxPressure(), volume));
            }
        } else {
            if (wrapper.isInput()) {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.pressure.range", 
                    wrapper.minPressure(), wrapper.maxPressure()));
            } else {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.pressure.output.range", 
                    wrapper.minPressure(), wrapper.maxPressure()));
            }
        }

        return getClientTooltipComponents(tooltips, wrapper.chance());
    }
    
    public static PncPressureEmiWidget fromWrappers(PressureGuiElement element, List<EmiIngredientWrapper> wrappers, float danger, float critical, int offsetX, int offsetY) {
        for (EmiIngredientWrapper wrapper : wrappers) {
            if (wrapper instanceof PncPressureEmiWrapper pressureWrapper) {
                return new PncPressureEmiWidget(element, pressureWrapper, danger, critical, offsetX, offsetY);
            }
        }
        return null;
    }
}

