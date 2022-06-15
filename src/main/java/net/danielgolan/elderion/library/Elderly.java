package net.danielgolan.elderion.library;

import net.danielgolan.elderion.library.blocks.VariedBlock;
import net.fabricmc.api.ModInitializer;
import net.minecraft.recipe.Recipe;

import java.util.ArrayList;
import java.util.List;

public class Elderly implements ModInitializer {
    private static final List<List<Recipe<?>>> recipes = new ArrayList<>();

    @Override
    public void onInitialize() {
    }

    public interface RecipeManager {
        static void addRecipes(List<Recipe<?>> recipes){
            Elderly.recipes.add(recipes);
        }
    }
}
