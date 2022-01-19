package net.danielgolan.elderion.library.blocks;

import io.netty.buffer.Unpooled;
import net.danielgolan.elderion.library.Author;
import net.danielgolan.elderion.library.ElderionIdentifier;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * A class used to generate multiple variations of a block
 */
public final class VariedBlock {
    private final HashMap<BlockVariation, Block> blocks = new HashMap<>();
    private final HashMap<BlockVariation, BlockItem> items = new HashMap<>();

    private final List<Recipe<?>> recipes = new ArrayList<>();

    private final ElderionIdentifier identifier;

    private VariedBlock(@NotNull Builder builder, ElderionIdentifier identifier) {
        this.identifier = identifier;

        Item.Settings settings = new Item.Settings().rarity(builder.rarity()).group(builder.type());
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

        recipes.forEach(recipe -> {
            if (recipes instanceof StonecuttingRecipe stonecuttingRecipe)
                RecipeSerializer.STONECUTTING.write(new PacketByteBuf(Unpooled.buffer()), stonecuttingRecipe);
        });

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

    /**
     * Builder
     */
    public static final class Builder extends FabricBlockSettings {
        private final HashMap<BlockVariation, Boolean> variations = new HashMap<>();
        private ItemGroup type = ItemGroup.BUILDING_BLOCKS;
        private Rarity rarity = Rarity.COMMON;
        private BlockGenerator generator = BlockGenerator.DEFAULT;
        private boolean enableRecipes = false, enableRevertRecipes = false;
        private VoxelShape box = null;

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

        public Builder type(ItemGroup type) {
            this.type = type;
            return this;
        }

        public Builder rarity(Rarity rarity) {
            this.rarity = rarity;
            return this;
        }

        public ItemGroup type() {
            return type;
        }

        public Rarity rarity() {
            return rarity;
        }

        @Contract("_ -> new")
        public @NotNull VariedBlock build(ElderionIdentifier identifier) {
            return new VariedBlock(this, identifier);
        }

        @Contract("_, _ -> new")
        public @NotNull VariedBlock build(Author author, String path) {
            return build(new ElderionIdentifier(author, path));
        }

        public Builder generator(BlockGenerator generator) {
            this.generator = generator;
            return this;
        }

        public BlockGenerator generator() {
            return generator;
        }

        public Builder noCollision() {
            super.noCollision();
            return this;
        }

        public Builder nonOpaque() {
            super.nonOpaque();
            return this;
        }

        public Builder slipperiness(float value) {
            super.slipperiness(value);
            return this;
        }

        public Builder velocityMultiplier(float velocityMultiplier) {
            super.velocityMultiplier(velocityMultiplier);
            return this;
        }

        public Builder jumpVelocityMultiplier(float jumpVelocityMultiplier) {
            super.jumpVelocityMultiplier(jumpVelocityMultiplier);
            return this;
        }

        public Builder sounds(BlockSoundGroup group) {
            super.sounds(group);
            return this;
        }

        public Builder luminance(ToIntFunction<BlockState> luminanceFunction) {
            super.luminance(luminanceFunction);
            return this;
        }

        public Builder strength(float hardness, float resistance) {
            super.strength(hardness, resistance);
            return this;
        }

        public Builder breakInstantly() {
            super.breakInstantly();
            return this;
        }

        public Builder strength(float strength) {
            super.strength(strength);
            return this;
        }

        public Builder ticksRandomly() {
            super.ticksRandomly();
            return this;
        }

        public Builder dynamicBounds() {
            super.dynamicBounds();
            return this;
        }

        public Builder air() {
            super.air();
            return this;
        }

        public Builder allowsSpawning(AbstractBlock.TypedContextPredicate<EntityType<?>> predicate) {
            super.allowsSpawning(predicate);
            return this;
        }

        public Builder solidBlock(AbstractBlock.ContextPredicate predicate) {
            super.solidBlock(predicate);
            return this;
        }

        public Builder suffocates(AbstractBlock.ContextPredicate predicate) {
            super.suffocates(predicate);
            return this;
        }

        public Builder blockVision(AbstractBlock.ContextPredicate predicate) {
            super.blockVision(predicate);
            return this;
        }

        public Builder postProcess(AbstractBlock.ContextPredicate predicate) {
            super.postProcess(predicate);
            return this;
        }

        public Builder emissiveLighting(AbstractBlock.ContextPredicate predicate) {
            super.emissiveLighting(predicate);
            return this;
        }

        public Builder luminance(int luminance) {
            super.luminance(luminance);
            return this;
        }

        public Builder hardness(float hardness) {
            super.hardness(hardness);
            return this;
        }

        public Builder resistance(float resistance) {
            super.resistance(resistance);
            return this;
        }

        public Builder requiresTool() {
            super.requiresTool();
            return this;
        }

        public Builder mapColor(MapColor color) {
            super.mapColor(color);
            return this;
        }

        @Contract("_ -> this")
        public Builder mapColor(@NotNull DyeColor color) {
            return this.mapColor(color.getMapColor());
        }

        public Builder collidable(boolean collidable) {
            super.collidable(collidable);
            return this;
        }

        public Builder breakByHand(boolean breakByHand) {
            super.breakByHand(breakByHand);
            return this;
        }

        @Deprecated(forRemoval = true)
        public Builder breakByTool(Tag<Item> tag, int miningLevel) {
            super.breakByTool(tag, miningLevel);
            return this;
        }

        @Deprecated(forRemoval = true)
        public Builder breakByTool(Tag<Item> tag) {
            return this.breakByTool(tag, 0);
        }

        public boolean recipesEnabled() {
            return enableRecipes;
        }

        public boolean revertRecipesEnabled() {
            return enableRevertRecipes;
        }

        public Builder recipesEnabled(boolean enableRecipes) {
            return recipesEnabled(enableRecipes, true);
        }

        public Builder recipesEnabled(boolean enableRecipes, boolean enableRevertRecipes) {
            this.enableRecipes = enableRecipes;
            this.enableRevertRecipes = enableRevertRecipes;
            return this;
        }

        public VoxelShape boundingBox() {
            return box;
        }

        public Builder boundingBox(VoxelShape box) {
            this.box = box;
            return this;
        }


    }
}
