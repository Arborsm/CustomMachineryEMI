package dev.arborsm.custom_machinery_emi.mekanism;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.impl.util.DoubleRange;
import fr.frinn.custommachinerymekanism.common.guielement.HeatGuiElement;
import mekanism.common.MekanismLang;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

import java.util.List;

/**
 * EMI widget for rendering Mekanism Temperature requirements
 */
public class TemperatureEmiWidget extends Widget {
    
    private final HeatGuiElement element;
    private final DoubleRange temp;
    private final TemperatureUnit unit;
    private final int x, y, width, height;
    
    public TemperatureEmiWidget(HeatGuiElement element, DoubleRange temp, TemperatureUnit unit, int offsetX, int offsetY) {
        this.element = element;
        this.temp = temp;
        this.unit = unit;
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
        return List.of(
            ClientTooltipComponent.create(Component.translatable("custommachinerymekanism.requirements.temp.error", temp.toFormattedString()).getVisualOrderText()),
            ClientTooltipComponent.create(MekanismLang.UNIT.translate(unit.getLabel(true).translate()).getVisualOrderText())
        );
    }
}

