package dev.arborsm.custom_machinery_emi.widget;

import dev.arborsm.custom_machinery_emi.api.EmiIngredientWrapper;
import dev.arborsm.custom_machinery_emi.api.wrapper.ExperienceEmiWrapper;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.common.guielement.ExperienceGuiElement;
import fr.frinn.custommachinery.common.util.Color;
import fr.frinn.custommachinery.common.util.ExperienceUtils;
import fr.frinn.custommachinery.common.util.Utils;
import fr.frinn.custommachinery.impl.integration.jei.Experience;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Custom EMI widget for rendering experience in recipes
 */
public class ExperienceEmiWidget extends Widget {
    
    private final ExperienceGuiElement element;
    private final Experience experience;
    private final int x, y, width, height;
    private final List<Component> tooltip;
    
    private ExperienceEmiWidget(ExperienceGuiElement element, Experience experience, int x, int y, List<Component> tooltip) {
        this.element = element;
        this.experience = experience;
        this.x = x;
        this.y = y;
        this.width = element.getWidth() - 2;
        this.height = element.getHeight() - 2;
        this.tooltip = tooltip;
    }
    
    /**
     * Try to create an ExperienceEmiWidget from the wrappers list
     * @return The widget if successful, null otherwise
     */
    @Nullable
    public static ExperienceEmiWidget fromWrappers(ExperienceGuiElement element, List<EmiIngredientWrapper> wrappers, int offsetX, int offsetY) {
        Iterator<EmiIngredientWrapper> iterator = wrappers.iterator();
        while(iterator.hasNext()) {
            EmiIngredientWrapper wrapper = iterator.next();
            if(wrapper instanceof ExperienceEmiWrapper(RequirementIOMode mode, Experience experience1, int recipeTime)) {
                // Build tooltip
                List<Component> tooltip = new ArrayList<>();
                String amount = Utils.format(experience1.xp());
                if(experience1.isPoints()) {
                    if(experience1.isPerTick()) {
                        String totalExperience = Utils.format(experience1.xp() * recipeTime);
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.pertick.input", totalExperience, "XP", amount, "XP"));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.pertick.output", totalExperience, "XP", amount, "XP"));
                    } else {
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.input", amount, "XP"));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.output", amount, "XP"));
                    }
                } else {
                    if(experience1.isPerTick()) {
                        String totalExperience = Utils.format(experience1.getLevels() * recipeTime);
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.pertick.input", totalExperience, "Level(s)", amount, "Level(s)"));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.pertick.output", totalExperience, "Level(s)", amount, "Level(s)"));
                    } else {
                        if(mode == RequirementIOMode.INPUT)
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.input", amount, "Level(s)"));
                        else
                            tooltip.add(Component.translatable("custommachinery.jei.ingredient.xp.output", amount, "Level(s)"));
                    }
                }
                
                int x = element.getX() - offsetX + 1;
                int y = element.getY() - offsetY + 1;
                
                iterator.remove();
                return new ExperienceEmiWidget(element, experience1, x, y, tooltip);
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
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        
        if (element.getMode().isDisplayBar()) {
            // Render experience level text
            int levels = experience.isLevels() ? experience.xp() : ExperienceUtils.getLevelFromXp(experience.xp());
            String levelText = String.valueOf(levels);
            int xPos = width / 2 - Minecraft.getInstance().font.width(levelText) / 2;
            graphics.drawString(Minecraft.getInstance().font, levelText, xPos, 0, 0x80FF20, true);
            
            // Render experience bar background
            graphics.fill(0, height - 3, width, height, 0xFF000000);
            
            // Render experience bar fill
            if (experience.isPoints()) {
                int level = ExperienceUtils.getLevelFromXp(experience.xp());
                int xpDiff = experience.xp() - ExperienceUtils.getXpFromLevel(level);
                if (xpDiff > 0) {
                    double percent = (double) xpDiff / ExperienceUtils.getXpNeededForNextLevel(level);
                    graphics.fill(1, height - 2, 1 + Math.max((int) Math.ceil(width * percent) - 2, 0), height - 1, 0xFF80FF20);
                }
            }
        } else {
            // Simple colored rectangle for non-bar mode
            graphics.fill(0, 0, width, height, Color.fromColors(1, 7, 186, 7).getARGB());
        }
        
        graphics.pose().popPose();
    }

    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        return tooltip.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).toList();
    }
}

