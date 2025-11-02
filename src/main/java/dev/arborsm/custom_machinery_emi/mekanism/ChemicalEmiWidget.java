package dev.arborsm.custom_machinery_emi.mekanism;

import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.Widget;
import fr.frinn.custommachinery.CustomMachinery;
import fr.frinn.custommachinery.api.requirement.RequirementIOMode;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.text.EnumColor;
import mekanism.client.recipe_viewer.emi.ChemicalEmiStack;
import mekanism.client.render.MekanismRenderer;
import mekanism.common.MekanismLang;
import mekanism.common.util.text.TextUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * EMI widget for rendering Mekanism Chemical stacks
 */
public class ChemicalEmiWidget extends Widget {
    
    private final Object chemicalElement; // ChemicalGuiElement
    private final RequirementIOMode mode;
    private final double chance;
    private final boolean isPerTick;
    private final String tank;
    private final int x, y, width, height;
    private final ChemicalEmiStack stack;
    
    public ChemicalEmiWidget(Object chemicalElement, Object chemicalStack, RequirementIOMode mode, 
                            double chance, boolean isPerTick, String tank, int offsetX, int offsetY) {
        this.chemicalElement = chemicalElement;
        this.mode = mode;
        this.chance = chance;
        this.isPerTick = isPerTick;
        this.tank = tank;
        this.stack = new ChemicalEmiStack((ChemicalStack)chemicalStack);
        
        try {
            Class<?> elementClass = chemicalElement.getClass();
            int elemX = (Integer) elementClass.getMethod("getX").invoke(chemicalElement);
            int elemY = (Integer) elementClass.getMethod("getY").invoke(chemicalElement);
            int elemWidth = (Integer) elementClass.getMethod("getWidth").invoke(chemicalElement);
            int elemHeight = (Integer) elementClass.getMethod("getHeight").invoke(chemicalElement);
            
            this.x = elemX - offsetX;
            this.y = elemY - offsetY;
            this.width = elemWidth;
            this.height = elemHeight;
        } catch (Exception e) {
            throw new RuntimeException("Failed to get ChemicalGuiElement dimensions", e);
        }
    }
    
    @Override
    public Bounds getBounds() {
        return new Bounds(x, y, width, height);
    }
    
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        try {
            Class<?> elementClass = chemicalElement.getClass();
            Object texture = elementClass.getMethod("getTexture").invoke(chemicalElement);
            Class<?> textureClass = texture.getClass();
            
            Object textureLoc = textureClass.getMethod("texture").invoke(texture);
            int u = (Integer) textureClass.getMethod("u").invoke(texture);
            int v = (Integer) textureClass.getMethod("v").invoke(texture);
            
            graphics.blit(
                (net.minecraft.resources.ResourceLocation) textureLoc,
                x, y,
                u, v,
                width, height,
                width, height
            );
            
            graphics.pose().pushPose();
            graphics.pose().translate(0.0F, 0.0F, 10.0F);

            int color = stack.getKey().getTint();
            TextureAtlasSprite sprite = MekanismRenderer.getChemicalTexture(stack.getStack());
            float red = MekanismRenderer.getRed(color);
            float green = MekanismRenderer.getGreen(color);
            float blue = MekanismRenderer.getBlue(color);
            graphics.blit(x + 1, y + 1, 0, width - 2, height - 2, sprite, red, green, blue, 1);

            graphics.pose().popPose();
        } catch (Exception e) {
            CustomMachinery.LOGGER.error("Failed to render ChemicalEmiWidget", e);
        }
    }
    
    @Override
    public List<ClientTooltipComponent> getTooltip(int mouseX, int mouseY) {
        var fTooltips = stack.getTooltip();
        List<Component> tooltips = new ArrayList<>();

        var formattedAmount = MekanismLang.GENERIC_MB.translateColored(EnumColor.GRAY, TextUtils.format(stack.getAmount()));

        tooltips.add(formattedAmount);

        if (isPerTick) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.pertick"));
        }

        if (chance == 0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance.0").withStyle(ChatFormatting.DARK_RED));
        } else if (chance != 1.0) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.chance", (int)(chance * 100)));
        }

        if (!tank.isEmpty() && Minecraft.getInstance().options.advancedItemTooltips) {
            tooltips.add(Component.translatable("custommachinery.jei.ingredient.fluid.specificTank").withStyle(ChatFormatting.DARK_RED));
        }

        fTooltips.addAll(tooltips.stream()
                .map(Component::getVisualOrderText)
                .map(ClientTooltipComponent::create)
                .toList());
        return fTooltips;
    }
}

