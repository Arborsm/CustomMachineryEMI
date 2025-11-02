package dev.arborsm.custom_machinery_emi.category;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.datafixers.util.Pair;
import dev.arborsm.custom_machinery_emi.api.RecipeHelper;
import dev.arborsm.custom_machinery_emi.widget.GuiElementWidget;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.api.guielement.IGuiElement;
import fr.frinn.custommachinery.api.integration.jei.DisplayInfoTemplate;
import fr.frinn.custommachinery.api.requirement.RecipeRequirement;
import fr.frinn.custommachinery.client.integration.jei.RequirementDisplayInfo;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import fr.frinn.custommachinery.common.init.Registration;
import fr.frinn.custommachinery.common.machine.CustomMachine;
import fr.frinn.custommachinery.common.util.Comparators;
import fr.frinn.custommachinery.impl.integration.jei.GuiElementJEIRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractEMIRecipeCategory<T extends IMachineRecipe> implements EmiRecipe {

    protected static final int ICON_SIZE = 10;

    protected CustomMachine machine;
    protected final EmiRecipeCategory category;
    protected final RecipeHelper recipeHelper;
    protected final LoadingCache<RecipeRequirement<?, ?>, RequirementDisplayInfo> infoCache;
    protected int offsetX;
    protected int offsetY;
    protected int width;
    protected int height;
    protected boolean hasInfoRow;
    protected int rowY;
    protected int maxIconPerRow;
    protected T recipe;

    public AbstractEMIRecipeCategory(CustomMachine machine, EmiRecipeCategory category, T recipe) {
        this.machine = machine;
        this.category = category;
        this.recipe = recipe;
        this.recipeHelper = new RecipeHelper(machine);
        this.infoCache = CacheBuilder.newBuilder().build(new CacheLoader<>() {
            @Override
            public @NotNull RequirementDisplayInfo load(@NotNull RecipeRequirement<?, ?> requirement) {
                RequirementDisplayInfo info = new RequirementDisplayInfo();
                requirement.getDisplayInfo(info);
                DisplayInfoTemplate template = requirement.info;
                if(template != null) {
                    if(!template.getTooltips().isEmpty())
                        info.getTooltips().clear();
                    template.build(info);
                }
                return info;
            }
        });
        this.setupRecipeDimensions();
    }

    //Find the minimal size for the recipe layout
    private void setupRecipeDimensions() {
        if(Minecraft.getInstance().level == null)
            return;
        int minX = Integer.MAX_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxX = 0;
        int maxY = 0;
        List<IGuiElement> elements = this.machine.getJeiElements().isEmpty() ? this.machine.getGuiElements() : this.machine.getJeiElements();
        for(IGuiElement element : elements) {
            if(!GuiElementJEIRendererRegistry.hasJEIRenderer(element.getType()) || !element.showInJei())
                continue;
            minX = Math.min(minX, element.getX());
            minY = Math.min(minY, element.getY());
            maxX = Math.max(maxX, element.getX() + element.getWidth());
            maxY = Math.max(maxY, element.getY() + element.getHeight());
        }

        this.rowY = Math.max(maxY - minY, 20);
        this.offsetX = Math.max(minX, 0);
        this.offsetY = Math.max(minY, 0);
        this.width = Math.max(maxX - minX, 20);
        this.maxIconPerRow = this.width / (ICON_SIZE + 2);
        long maxDisplayRequirement = Minecraft.getInstance().level.getRecipeManager().getAllRecipesFor(Registration.CUSTOM_MACHINE_RECIPE.get())
                .stream()
                .map(RecipeHolder::value)
                .filter(rec -> rec.getMachineId().equals(this.machine.getId()) && rec.showInJei())
                .mapToLong(rec -> rec.getDisplayInfoRequirements().stream().map(this.infoCache).filter(RequirementDisplayInfo::shouldRender).count())
                .max()
                .orElse(0);
        this.hasInfoRow = maxDisplayRequirement != 0;
        int rows = this.hasInfoRow ? Math.toIntExact(maxDisplayRequirement) / this.maxIconPerRow + 1 : 0;
        this.height = this.rowY + (ICON_SIZE + 2) * rows;
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return this.category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return this.recipe.getMachineId();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return List.of();
    }

    @Override
    public int getDisplayWidth() {
        return this.width;
    }

    @Override
    public int getDisplayHeight() {
        return this.height;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        // Get the GUI elements to display
        List<IGuiElement> elements = machine.getJeiElements().isEmpty() ? machine.getGuiElements() : machine.getJeiElements();
        if(recipe instanceof CustomMachineRecipe machineRecipe && !machineRecipe.getGuiElements().isEmpty())
            elements = machineRecipe.getCustomGuiElements(elements);

        // GUI element
        elements.stream()
                .filter(element -> GuiElementJEIRendererRegistry.hasJEIRenderer(element.getType()) && element.showInJei())
                .sorted(Comparators.GUI_ELEMENTS_COMPARATOR.reversed())
                .forEach(element -> {
                    widgets.add(new GuiElementWidget(element, recipe, offsetX, offsetY));
                });

        // line
        if(hasInfoRow) {
            widgets.addDrawable(0, rowY, width, 1, (graphics, mouseX, mouseY, delta) -> {
                graphics.fill(-3, 0, width + 3, 1, 0x30000000);
            });
        }

        // ingredient slots
        addIngredientSlots(widgets);
        
        // info widgets
        if(this.hasInfoRow) {
            int index = 0;
            int row = 0;
            for(RecipeRequirement<?, ?> requirement : this.recipe.getDisplayInfoRequirements()) {
                RequirementDisplayInfo info = this.infoCache.getUnchecked(requirement);
                if(info.shouldRender()) {
                    int x = index * (ICON_SIZE + 2) - 2;
                    int y = this.rowY + 2 + (ICON_SIZE + 2) * row;
                    if(++index >= this.maxIconPerRow) {
                        index = 0;
                        row++;
                    }
                    // tooltip
                    final RequirementDisplayInfo finalInfo = info;
                    widgets.addDrawable(x, y, ICON_SIZE, ICON_SIZE, (graphics, mx, my, delta) -> {
                        finalInfo.renderIcon(graphics, ICON_SIZE);
                    }).tooltipText(finalInfo.getTooltips().stream()
                            .filter(pair -> pair.getSecond().shouldDisplay(Minecraft.getInstance().player, Minecraft.getInstance().options.advancedItemTooltips))
                            .map(Pair::getFirst)
                            .collect(Collectors.toList()));
                }
            }
        }
    }
    
    /**
     * Add ingredient slots to the widget holder
     * Subclasses should override this to add their specific ingredient displays
     */
    protected abstract void addIngredientSlots(WidgetHolder widgets);
}

