package net.danielgolan.elderion.library.blocks;

import net.danielgolan.elderion.library.ElderionIdentifier;
import net.danielgolan.elderion.library.Elderly;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A class used to generate multiple variations of a block
 */
public final class VariedBlock extends BlockHandler<VariedBlock, Block, VariedBlock.Builder> implements Elderly.RecipeManager {
    private final HashMap<BlockVariation, Block> blocks = new HashMap<>();
    private final HashMap<BlockVariation, BlockItem> items = new HashMap<>();

    private final List<Recipe<?>> recipes = new ArrayList<>();

    private VariedBlock(@NotNull Builder builder, ElderionIdentifier identifier) {
        super(builder, identifier);

        FabricItemSettings settings = ItemSettingsOf(builder);
        blocks.put(BlockVariation.BLOCK, builder.generator().generate(builder));
        items.put(BlockVariation.BLOCK, new BlockItem(block(), settings));

        builder.variations.forEach((variation, bool) -> {
            if (variation == BlockVariation.BLOCK) return;

            blocks.put(variation, builder.generator().generateVariation(builder, block(), variation));
            items.put(variation, new BlockItem(block(variation), settings));

            if (builder.recipesEnabled() && variation.RECIPE_RESULT > 0) {
                //creation of normal recipe
                StonecuttingRecipe stonecuttingRecipe = new StonecuttingRecipe(identifier.toIdentifier(variation.SUFFIX),
                        variation.SUFFIX, Ingredient.ofItems(item()), new ItemStack(item(variation),
                        variation.RECIPE_RESULT));

                recipes.add(stonecuttingRecipe);

                //creation of additional recipe
                if (builder.revertRecipesEnabled() && variation.RECIPE_RESULT == 1) {
                    StonecuttingRecipe revertRecipe = new StonecuttingRecipe(identifier.toIdentifier("revert" + variation.SUFFIX),
                            identifier.toString(), Ingredient.ofItems(item(variation)), new ItemStack(item(), 1));

                    recipes.add(revertRecipe);
                }
            }
        });
    }

    /**
     * Registers all the contents of this class
     * @return {@code this}, to allow easy builder compatibility
     */
    public VariedBlock register() {
        for (BlockVariation variation : BlockVariation.values()) {
            if (block(variation) == null) continue;

            Identifier identifier = this.identifier.toIdentifier(variation.SUFFIX);

            Registry.register(Registry.BLOCK, identifier, block(variation));
            Registry.register(Registry.ITEM, identifier, item(variation));
        }

        Elderly.RecipeManager.addRecipes(recipes);

        return this;
    }

    /**
     * @return original base {@link Block}
     */
    public Block block() {
        return block(BlockVariation.BLOCK);
    }

    /**
     * @param variation the variation of the block
     * @return the {@link Block} assigned to the variation provided
     */
    public Block block(BlockVariation variation) {
        return blocks.get(variation);
    }

    /**
     * @return the original base block's item
     */
    public BlockItem item() {
        return item(BlockVariation.BLOCK);
    }

    /**
     * @param variation the variation
     * @return the {@link BlockItem} for the block {@link #block(BlockVariation)}
     */
    public BlockItem item(BlockVariation variation) {
        return items.get(variation);
    }

    /**
     * @param material the material of the new block
     * @param color the color of the new block
     * @return a new builder
     */
    @Contract("_, _ -> new")
    public static @NotNull Builder builder(Material material, MapColor color) {
        return new Builder(material, color);
    }

    /**
     * @param settings the base settings to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull Builder of(AbstractBlock.Settings settings) {
        return new Builder(settings);
    }

    /**
     * @param block the base block to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull Builder of(AbstractBlock block) {
        return of(AbstractBlock.Settings.copy(block));
    }

    /**
     * @param block the base block to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull Builder of(@NotNull VariedBlock block) {
        return of(block.block());
    }

    public static final class Builder extends BlockHandler.Builder<VariedBlock, Block, Builder> {
        private final HashMap<BlockVariation, Boolean> variations = new HashMap<>();
        private BlockGenerator generator = BlockGenerator.DEFAULT;
        private boolean enableRecipes = false, enableRevertRecipes = false;

        private Builder(Material material, MapColor color) {
            super(material, color);
        }

        private Builder(AbstractBlock.Settings settings) {
            super(settings);
        }

        /**
         * @param variations block variations to enable
         */
        @Contract("_ -> this")
        public Builder enable(BlockVariation @NotNull ... variations) {
            for (BlockVariation variation : variations)
                this.variations.put(variation, true);
            return this;
        }

        /**
         * @param variations block variations to disable
         */
        @Contract("_ -> this")
        public Builder disable(BlockVariation @NotNull ... variations) {
            for (BlockVariation variation : variations)
                this.variations.put(variation, false);
            return this;
        }

        public boolean isEnabled(BlockVariation variation) {
            return variations.get(variation);
        }

        @Contract("_ -> new")
        public @NotNull VariedBlock build(ElderionIdentifier identifier) {
            return new VariedBlock(this, identifier);
        }


        public Builder generator(BlockGenerator generator) {
            this.generator = generator;
            return this;
        }

        public BlockGenerator generator() {
            return generator;
        }

        public Builder recipesEnabled(boolean enableRecipes) {
            return recipesEnabled(enableRecipes, true);
        }

        public Builder recipesEnabled(boolean enableRecipes, boolean enableRevertRecipes) {
            this.enableRecipes = enableRecipes;
            this.enableRevertRecipes = enableRevertRecipes;
            return this;
        }

        public boolean revertRecipesEnabled() {
            return enableRevertRecipes;
        }

        public boolean recipesEnabled() {
            return enableRecipes;
        }

        @Override
        protected Builder getBuilderInstance() {
            return this;
        }
    }
}
