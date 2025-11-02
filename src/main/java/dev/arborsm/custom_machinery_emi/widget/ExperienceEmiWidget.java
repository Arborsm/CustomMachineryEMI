package dev.arborsm.custom_machinery_emi.widget;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.api.integration.jei.IJEIIngredientWrapper;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import fr.frinn.custommachinery.client.integration.jei.wrapper.ExperienceIngredientWrapper;
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

import java.lang.reflect.Field;
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
    public static ExperienceEmiWidget fromWrappers(ExperienceGuiElement element, List<IJEIIngredientWrapper<?>> wrappers, int offsetX, int offsetY) {
        Iterator<IJEIIngredientWrapper<?>> iterator = wrappers.iterator();
        while(iterator.hasNext()) {
            IJEIIngredientWrapper<?> wrapper = iterator.next();
            if(wrapper instanceof ExperienceIngredientWrapper) {
                try {
                    Field experienceField = ExperienceIngredientWrapper.class.getDeclaredField("experience");
                    experienceField.setAccessible(true);
                    Experience experience = (Experience) experienceField.get(wrapper);
                    
                    Field modeField = ExperienceIngredientWrapper.class.getDeclaredField("mode");
                    modeField.setAccessible(true);
                    RequirementIOMode mode = (RequirementIOMode) modeField.get(wrapper);
                    
                    Field recipeTimeField = ExperienceIngredientWrapper.class.getDeclaredField("recipeTime");
                    recipeTimeField.setAccessible(true);
                    int recipeTime = (Integer) recipeTimeField.get(wrapper);
                    
                    // Build tooltip
                    List<Component> tooltip = new ArrayList<>();
                    String amount = Utils.format(experience.xp());
                    if(experience.isPoints()) {
                        if(experience.isPerTick()) {
                            String totalExperience = Utils.format(experience.xp() * recipeTime);
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
                        if(experience.isPerTick()) {
                            String totalExperience = Utils.format(experience.getLevels() * recipeTime);
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
                    return new ExperienceEmiWidget(element, experience, x, y, tooltip);
                } catch (Exception e) {
                    // Failed to extract experience data
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

