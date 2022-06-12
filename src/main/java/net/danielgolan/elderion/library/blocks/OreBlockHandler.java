package net.danielgolan.elderion.library.blocks;

import net.danielgolan.elderion.library.ElderionIdentifier;
import net.minecraft.block.*;
import net.minecraft.item.BlockItem;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @see net.danielgolan.elderion.library.blocks.BlockHandler
 * @deprecated {@code ConfiguredFeature} isn't implemented (yet), doesn't work
 */
@Deprecated
public final class OreBlockHandler extends BlockHandler<OreBlockHandler, OreBlock, OreBlockHandler.Builder> {
    private final OreBlock block;
    private final BlockItem item;
    //private final ConfiguredFeature feature = Feature.ORE.configure(new OreFeatureConfig(BASE_STONE));

    private OreBlockHandler(Builder builder, ElderionIdentifier identifier) {
        super(builder, identifier);

        block = new OreBlock(builder, UniformIntProvider.create(builder.minXP, builder.maxXP));
        item = new BlockItem(block, ItemSettingsOf(builder));
    }

    @Override
    public @Nullable OreBlock block() {
        return block;
    }

    @Override
    public @Nullable BlockItem item() {
        return item;
    }

    /**
     * @param material the material of the new block
     * @param color the color of the new block
     * @return a new builder
     */
    @Contract("_, _ -> new")
    public static @NotNull OreBlockHandler.Builder builder(Material material, MapColor color) {
        return new OreBlockHandler.Builder(material, color);
    }

    /**
     * @param settings the base settings to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull OreBlockHandler.Builder of(AbstractBlock.Settings settings) {
        return new OreBlockHandler.Builder(settings);
    }

    /**
     * @param block the base block to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull OreBlockHandler.Builder of(AbstractBlock block) {
        return of(AbstractBlock.Settings.copy(block));
    }

    /**
     * @param block the base block to copy from
     * @return a new builder with default values of these settings
     */
    @Contract("_ -> new")
    public static @NotNull OreBlockHandler.Builder of(@NotNull OreBlockHandler block) {
        return of(block.block());
    }

    public final static class Builder extends BlockHandler.Builder<OreBlockHandler, OreBlock, Builder> {
        private int minXP, maxXP;

        public int minExperienceDrop() {
            return minXP;
        }

        public int maxExperienceDrop() {
            return maxXP;
        }

        public Builder dropExperience(int min, int max) {
            //Validating experience dropping is legal
            this.minXP = Math.max(0, min);
            this.maxXP = Math.max(min, max);

            return this;
        }

        private Builder(Material material, MapColor color) {
            super(material, color);
        }

        private Builder(AbstractBlock.Settings settings) {
            super(settings);
        }

        @Override
        public @NotNull OreBlockHandler build(ElderionIdentifier identifier) {
            return new OreBlockHandler(this, identifier);
        }

        @Override
        protected Builder getBuilderInstance() {
            return this;
        }
    }
}
