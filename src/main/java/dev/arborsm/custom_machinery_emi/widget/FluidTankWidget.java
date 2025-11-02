package dev.arborsm.custom_machinery_emi.widget;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.stack.FluidEmiStack;
import dev.emi.emi.api.widget.Bounds;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.platform.EmiAgnos;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

/**
 * Custom fluid tank widget that renders fluid across the entire rectangle area
 */
public class FluidTankWidget extends SlotWidget {

    private final long capacity;
    private EmiRecipe recipe;
    
    public FluidTankWidget(EmiIngredient stack, int x, int y, int width, int height, long capacity) {
        super(stack, x, y);
        this.capacity = capacity;
        // Disable background and set custom bounds
        this.bounds = new Bounds(x, y, width, height);
        drawBack(false);
    }
    
    @Override
    public Bounds getBounds() {
        return this.bounds;
    }
    
    @Override
    public SlotWidget backgroundTexture(ResourceLocation id, int u, int v) {
        return super.backgroundTexture(id, u, v);
    }
    
    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void drawStack(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        EmiIngredient ingredient = this.getStack();
        
        for (EmiStack stack : ingredient.getEmiStacks()) {
            Object key = stack.getKey();
            if (key instanceof Fluid fluid) {
                FluidEmiStack fes = new FluidEmiStack(fluid, stack.getComponentChanges(), ingredient.getAmount());
                boolean floaty = EmiAgnos.isFloatyFluid(fes);
                Bounds bounds = this.getBounds();
                int x = bounds.x() + 1;
                int y = bounds.y() + 1;
                int w = bounds.width() - 2;
                int h = bounds.height() - 2;
                
                // Calculate filled height based on capacity
                int filledHeight = Math.max(1, (int) Math.min((long) h, fes.getAmount() * (long) h / this.capacity));
                int sy = floaty ? y : y + h;
                
                // Render fluid in 16x16 tiles across the entire rectangle
                for (int oy = 0; oy < filledHeight; oy += 16) {
                    int rh = Math.min(16, filledHeight - oy);
                    
                    for (int ox = 0; ox < w; ox += 16) {
                        int rw = Math.min(16, w - ox);
                        if (floaty) {
                            // Flowing down (from top)
                            EmiAgnos.renderFluid(fes, graphics.pose(), x + ox, sy + oy, delta, 0, 0, rw, rh);
                        } else {
                            // Flowing up (from bottom)
                            EmiAgnos.renderFluid(fes, graphics.pose(), x + ox, sy + (oy + rh) * -1, delta, 0, 16 - rh, rw, rh);
                        }
                    }
                }
                
                return;
            }
        }
    }
    
    /**
     * Set the recipe context for output slots
     */
    public void setRecipe(EmiRecipe recipe) {
        this.recipe = recipe;
    }
    
    /**
     * Get the recipe context (for tooltips and recipe tree)
     */
    public EmiRecipe getRecipe() {
        return recipe;
    }
}

