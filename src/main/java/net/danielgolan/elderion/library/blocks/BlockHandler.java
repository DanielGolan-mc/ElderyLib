package net.danielgolan.elderion.library.blocks;

import net.danielgolan.elderion.library.Author;
import net.danielgolan.elderion.library.ElderionIdentifier;
import net.fabricmc.fabric.api.item.v1.CustomDamageHandler;
import net.fabricmc.fabric.api.item.v1.EquipmentSlotProvider;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Rarity;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.shape.VoxelShape;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.ToIntFunction;

/**
 * @param <T> a reference to implementations
 * @param <B> a reference to implementations {@link Block} class
 * @param <E> a reference to implementations {@link Builder} class
 */
public abstract sealed class BlockHandler<T extends BlockHandler<T, B, E>, B extends Block, E extends BlockHandler.Builder<T, B, E>>
        permits OreBlockHandler, VariedBlock {
    public abstract B block();
    public abstract BlockItem item();

    public T register() {
        Registry.register(Registry.BLOCK, identifier.get(), block());
        Registry.register(Registry.ITEM, identifier.get(), item());
        return getInstance();
    }

    public final ElderionIdentifier identifier;

    protected BlockHandler(E builder, ElderionIdentifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @see net.minecraft.block.AbstractBlock.Settings
     * @see net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings
     * @see BlockHandler
     */
    public static abstract sealed class Builder<T extends BlockHandler<T, B, E>, B extends Block, E extends BlockHandler.Builder<T, B, E>>
            extends FabricBlockSettings permits OreBlockHandler.Builder, VariedBlock.Builder {
        private ItemGroup type = ItemGroup.BUILDING_BLOCKS;
        private Rarity rarity = Rarity.COMMON;
        private VoxelShape box = null;
        private EquipmentSlotProvider equipmentSlot;
        private CustomDamageHandler customDamage;

        protected Builder(Material material, MapColor color) {
            super(material, color);
        }

        protected Builder(AbstractBlock.Settings settings) {
            super(settings);
        }

        public ItemGroup type() {
            return type;
        }

        public E type(ItemGroup type) {
            this.type = type;
            return getBuilderInstance();
        }

        public Rarity rarity() {
            return rarity;
        }

        public E rarity(Rarity rarity) {
            this.rarity = rarity;
            return getBuilderInstance();
        }

        public VoxelShape boundingBox() {
            return box;
        }

        public E boundingBox(VoxelShape box) {
            this.box = box;
            return getBuilderInstance();
        }

        public E noCollision() {
            super.noCollision();
            return getBuilderInstance();
        }

        public E nonOpaque() {
            super.nonOpaque();
            return getBuilderInstance();
        }

        public E slipperiness(float value) {
            super.slipperiness(value);
            return getBuilderInstance();
        }

        public E velocityMultiplier(float velocityMultiplier) {
            super.velocityMultiplier(velocityMultiplier);
            return getBuilderInstance();
        }

        public E jumpVelocityMultiplier(float jumpVelocityMultiplier) {
            super.jumpVelocityMultiplier(jumpVelocityMultiplier);
            return getBuilderInstance();
        }

        public E sounds(BlockSoundGroup group) {
            super.sounds(group);
            return getBuilderInstance();
        }

        public E luminance(ToIntFunction<BlockState> luminanceFunction) {
            super.luminance(luminanceFunction);
            return getBuilderInstance();
        }

        public E strength(float hardness, float resistance) {
            super.strength(hardness, resistance);
            return getBuilderInstance();
        }

        public E breakInstantly() {
            super.breakInstantly();
            return getBuilderInstance();
        }

        public E strength(float strength) {
            super.strength(strength);
            return getBuilderInstance();
        }

        public E ticksRandomly() {
            super.ticksRandomly();
            return getBuilderInstance();
        }

        public E dynamicBounds() {
            super.dynamicBounds();
            return getBuilderInstance();
        }

        public E air() {
            super.air();
            return getBuilderInstance();
        }

        public E allowsSpawning(AbstractBlock.TypedContextPredicate<EntityType<?>> predicate) {
            super.allowsSpawning(predicate);
            return getBuilderInstance();
        }

        public E solidBlock(AbstractBlock.ContextPredicate predicate) {
            super.solidBlock(predicate);
            return getBuilderInstance();
        }

        public E suffocates(AbstractBlock.ContextPredicate predicate) {
            super.suffocates(predicate);
            return getBuilderInstance();
        }

        public E blockVision(AbstractBlock.ContextPredicate predicate) {
            super.blockVision(predicate);
            return getBuilderInstance();
        }

        public E postProcess(AbstractBlock.ContextPredicate predicate) {
            super.postProcess(predicate);
            return getBuilderInstance();
        }

        public E emissiveLighting(AbstractBlock.ContextPredicate predicate) {
            super.emissiveLighting(predicate);
            return getBuilderInstance();
        }

        public E luminance(int luminance) {
            super.luminance(luminance);
            return getBuilderInstance();
        }

        public E hardness(float hardness) {
            super.hardness(hardness);
            return getBuilderInstance();
        }

        public E resistance(float resistance) {
            super.resistance(resistance);
            return getBuilderInstance();
        }

        public E requiresTool() {
            super.requiresTool();
            return getBuilderInstance();
        }

        public E mapColor(MapColor color) {
            super.mapColor(color);
            return getBuilderInstance();
        }

        @Contract("_ -> this")
        public E mapColor(@NotNull DyeColor color) {
            return this.mapColor(color.getMapColor());
        }

        public E collidable(boolean collidable) {
            super.collidable(collidable);
            return getBuilderInstance();
        }

        public EquipmentSlotProvider equipmentSlot() {
            return equipmentSlot;
        }

        public E equipmentSlot(EquipmentSlotProvider equipmentSlot) {
            this.equipmentSlot = equipmentSlot;
            return getBuilderInstance();
        }

        public CustomDamageHandler customDamage() {
            return customDamage;
        }

        public E customDamage(CustomDamageHandler customDamage) {
            this.customDamage = customDamage;
            return getBuilderInstance();
        }

        /**
         * used to get the builder's instance. Can be overridden if needed for custom advanced implementations.
         */
        protected E getBuilderInstance() {
            try {
                return (E) this;
            } catch (ClassCastException e) {
                System.err.println("ElderlyLib Error! We don't handle our blocks well! That's must be us or another mod using this library!");
                throw e;
            }
        }

        @Contract("_ -> new")
        public abstract @NotNull T build(ElderionIdentifier identifier);

        @Contract("_, _ -> new")
        public final @NotNull T build(Author author, String path) {
            return build(new ElderionIdentifier(author, path));
        }
    }

    @Contract("_, _ -> param1")
    protected final <S extends FabricItemSettings> @NotNull S apply(@NotNull S settings, @NotNull E builder) {
        settings.rarity(builder.rarity()).group(builder.type());

        if (builder.equipmentSlot() != null) settings.equipmentSlot(builder.equipmentSlot());
        if (builder.customDamage() != null) settings.customDamage(builder.customDamage());

        return settings;
    }

    protected final @NotNull FabricItemSettings ItemSettingsOf(@NotNull E builder) {
        FabricItemSettings settings = new FabricItemSettings().rarity(builder.rarity()).group(builder.type());

        if (builder.equipmentSlot() != null) settings.equipmentSlot(builder.equipmentSlot());
        if (builder.customDamage() != null) settings.customDamage(builder.customDamage());

        return settings;
    }

    /**
     * used to get the handler's instance. Can be overridden if needed for custom advanced implementations.
     */
    protected T getInstance() {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            System.err.println("ElderlyLib Error! Somebody doesn't handle their blocks well! That's must be us or another mod using this library!");
            throw e;
        }
    }
}
