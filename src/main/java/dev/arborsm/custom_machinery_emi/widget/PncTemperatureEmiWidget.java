package dev.arborsm.custom_machinery_emi.widget;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.wrapper.PncTemperatureEmiWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.impl.util.IntRange;
import fr.frinn.custommachinery.impl.util.Restriction;
import fr.frinn.custommachinerypnc.common.guielement.HeatGuiElement;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PncTemperatureEmiWidget extends Widget {

    private final IntRange range;
    private final WidgetTemperature temperatureWidget;
    private final int x, y, width, height;
    
    public PncTemperatureEmiWidget(HeatGuiElement element, IntRange range, int offsetX, int offsetY) {
        this.range = range;
        this.x = element.getX() - offsetX;
        this.y = element.getY() - offsetY;
        this.width = element.getWidth();
        this.height = element.getHeight();
        
        int minTemp = range.getRestrictions().stream()
            .mapToInt(Restriction::lowerBound)
            .min()
            .orElse(0);
        int maxTemp = range.getRestrictions().stream()
            .mapToInt(Restriction::upperBound)
            .max()
            .orElse(100);
        
        int centerTemp = (minTemp + maxTemp) / 2 + 273;
        this.temperatureWidget = new WidgetTemperature(0, 0, TemperatureRange.of(minTemp + 273, maxTemp + 273), centerTemp, 10);
        this.temperatureWidget.autoScaleForTemperature();
        this.temperatureWidget.setX(x);
        this.temperatureWidget.setY(y);
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 10.0F);
        
        this.temperatureWidget.render(graphics, mouseX, mouseY, delta);
        
        graphics.pose().popPose();
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return List.of(
            ClientTooltipComponent.create(
                Component.translatable("custommachinerypnc.requirements.temperature.error", 
                    range.toFormattedString()).getVisualOrderText()
            )
        );
    }
    
    public static PncTemperatureEmiWidget fromWrappers(HeatGuiElement element, List<EmiIngredientWrapper> wrappers, int offsetX, int offsetY) {
        for (EmiIngredientWrapper wrapper : wrappers) {
            if (wrapper instanceof PncTemperatureEmiWrapper(IntRange range)) {
                return new PncTemperatureEmiWidget(element, range, offsetX, offsetY);
            }
        }
        return null;
    }
}

