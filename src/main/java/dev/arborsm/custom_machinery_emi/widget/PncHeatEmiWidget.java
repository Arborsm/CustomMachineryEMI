package dev.arborsm.custom_machinery_emi.widget;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.wrapper.PncHeatEmiWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinerypnc.common.guielement.HeatGuiElement;
import me.desht.pneumaticcraft.api.crafting.TemperatureRange;
import me.desht.pneumaticcraft.client.gui.widget.WidgetTemperature;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class PncHeatEmiWidget extends Widget {

    private final PncHeatEmiWrapper wrapper;
    private final int recipeTime;
    private final WidgetTemperature temperatureWidget;
    private final int x, y, width, height;
    
    public PncHeatEmiWidget(HeatGuiElement element, PncHeatEmiWrapper wrapper, int recipeTime, int offsetX, int offsetY) {
        this.wrapper = wrapper;
        this.recipeTime = recipeTime;
        this.x = element.getX() - offsetX;
        this.y = element.getY() - offsetY;
        this.width = element.getWidth();
        this.height = element.getHeight();
        
        int temperature = wrapper.amount() + 273;
        this.temperatureWidget = new WidgetTemperature(0, 0, TemperatureRange.of(273, 373), temperature, 10);
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
        List<Component> tooltips = new ArrayList<>();
        String amount = Utils.format(wrapper.amount());
        
        if (wrapper.isPerTick()) {
            String totalHeat = Utils.format(wrapper.amount() * recipeTime);
            if (wrapper.isInput()) {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.heat.pertick.input", totalHeat, amount));
            } else {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.heat.pertick.output", totalHeat, amount));
            }
        } else {
            if (wrapper.isInput()) {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.heat.input", amount));
            } else {
                tooltips.add(Component.translatable("custommachinerypnc.jei.ingredient.heat.output", amount));
            }
        }

        return getClientTooltipComponents(tooltips, wrapper.chance());
    }

    @NotNull
    static List<ClientTooltipComponent> getClientTooltipComponents(List<Component> tooltips, double chance) {
        if (chance == 0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(net.minecraft.ChatFormatting.DARK_RED));
        } else if (chance < 1.0D && chance > 0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(chance * 100)));
        }

        return tooltips.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList();
    }

    public static PncHeatEmiWidget fromWrappers(HeatGuiElement element, List<EmiIngredientWrapper> wrappers, int offsetX, int offsetY, int recipeTime) {
        for (EmiIngredientWrapper wrapper : wrappers) {
            if (wrapper instanceof PncHeatEmiWrapper heatWrapper) {
                return new PncHeatEmiWidget(element, heatWrapper, recipeTime, offsetX, offsetY);
            }
        }
        return null;
    }
}

