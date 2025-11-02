package dev.arborsm.custom_machinery_emi.api;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import fr.frinn.custommachinery.api.crafting.IMachineRecipe;
import fr.frinn.custommachinery.common.crafting.craft.CustomCraftRecipe;
import fr.frinn.custommachinery.common.crafting.machine.CustomMachineRecipe;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class EMIRecipeTypes {

    private static final Map<Class<? extends IMachineRecipe>, Map<ResourceLocation, EmiRecipeCategory>> CATEGORIES = new HashMap<>();

    @Nullable
    public static <T extends IMachineRecipe> EmiRecipeCategory get(Class<T> recipeClass, ResourceLocation id) {
        Map<ResourceLocation, EmiRecipeCategory> map = CATEGORIES.get(recipeClass);
        if(map == null)
            return null;
        return map.get(id);
    }

    @Nullable
    public static EmiRecipeCategory machine(ResourceLocation id) {
        return get(CustomMachineRecipe.class, id);
    }

    @Nullable
    public static EmiRecipeCategory craft(ResourceLocation id) {
        return get(CustomCraftRecipe.class, id);
    }

    @Nullable
    public static EmiRecipeCategory fromID(ResourceLocation id) {
        return CATEGORIES.values().stream()
                .filter(map -> map.containsKey(id))
                .findFirst()
                .map(map -> map.get(id))
                .orElse(null);
    }

    public static <T extends IMachineRecipe> void create(ResourceLocation id, EmiRecipeCategory category, Class<T> recipeClass) {
        CATEGORIES.computeIfAbsent(recipeClass, c -> new HashMap<>()).put(id, category);
    }
}


