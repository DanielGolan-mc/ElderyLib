package net.danielgolan.elderion.library.fluids;

import net.danielgolan.elderion.library.Author;
import net.danielgolan.elderion.library.ElderionIdentifier;
import net.danielgolan.elderion.library.blocks.VariedBlock;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.ModelIdentifier;
import net.minecraft.data.server.FluidTagsProvider;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import org.lwjgl.system.CallbackI;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.function.ToIntFunction;

public class FluidHandler extends FlowableFluid {
    public static final ToIntFunction<WorldView> WATER_FLOW_SPEED = world -> 4;
    public static final ToIntFunction<WorldView> LAVA_FLOW_SPEED = world -> world.getDimension().isUltrawarm() ? 4 : 2;
    public static final ToIntFunction<WorldView> HONEY_FLOW_SPEED = world -> world.getDimension().isUltrawarm() ? 3 : 2;

    public static final ToIntFunction<WorldView> WATER_LEVEL_DECREASE = world -> 1;
    public static final ToIntFunction<WorldView> LAVA_LEVEL_DECREASE = world -> world.getDimension().isUltrawarm() ? 1 : 2;
    public static final ToIntFunction<WorldView> HONEY_LEVEL_DECREASE = world -> 3;

    private static final ToIntFunction<WorldView> WATER_TICK_RATE = world -> 5;
    private static final ToIntFunction<WorldView> LAVA_TICK_RATE = world -> world.getDimension().isUltrawarm() ? 10 : 30;
    private static final ToIntFunction<WorldView> HONEY_TICK_RATE = world -> 20;

    /**
     * When breaking a block inside this fluid, drops will not spawn if this equals to true.
     */
    public final boolean corrodes;
    /**
     * Can you create infinite source of this fluid?
     */
    public final boolean infinite;
    /**
     * When breaking a block inside this fluid, this sound will play.
     */
    public final SoundEvent blockFillSound;
    /**
     * When breaking a block inside this fluid, this particle will show up.
     */
    public final ParticleType<?> blockFillParticle ;
    /**
     * How fast is this fluid travels? The smaller, the slower.
     * <p>
     * {@linkplain #WATER_FLOW_SPEED Water travels at a speed of 4};
     * {@linkplain #LAVA_FLOW_SPEED Lava travels at a speed of 2, or 4 in hot dimensions}.
     */
    public final ToIntFunction<WorldView> flowSpeed;
    /**
     * Any fluid can have varying height, with levels from 1 to 8. Every block the fluid flows, it's level (or height) decreases by this value.
     * <p>
     * {@linkplain #WATER_LEVEL_DECREASE Water drops 1 level per block};
     * {@linkplain #LAVA_LEVEL_DECREASE Lava drops 2 levels per block, or 1 in hot dimensions}.
     */
    public final ToIntFunction<WorldView> levelDecreasePerBlock;
    /**
     * {@linkplain Block} tick rate.
     * <p>
     * {@linkplain #WATER_TICK_RATE Water tick rate is 5};
     * {@linkplain #LAVA_TICK_RATE Lava tick rate is 30, or 10 in hot biomes}.
     */
    public final ToIntFunction<WorldView> tickRate;
    /**
     * The resistance of this fluid to explosions.
     * @see Block#getBlastResistance()
     */
    public final float blastResistance;

    private final ElderionIdentifier identifier;

    private final FlowableFluid still, flowing;
    private final Item bucketItem;
    private final Block block;

    private FluidHandler(Builder builder, ElderionIdentifier identifier) {
        this.identifier = identifier;

        corrodes = builder.corrodes();
        blockFillSound = builder.blockFillSound();
        blockFillParticle = builder.blockFillParticle();
        flowSpeed = builder.flowSpeed();
        infinite = builder.infinite();
        levelDecreasePerBlock = builder.levelDecreasePerBlock();
        tickRate = builder.tickRate();
        blastResistance = builder.blastResistance();

        still = new Delegator(this) {
            @Override
            public int getLevel(FluidState state) {
                return 8;
            }

            @Override
            public boolean isStill(FluidState state) {
                return true;
            }
        };

        bucketItem = new BucketItem(still, new Item.Settings().maxCount(1).recipeRemainder(Items.BUCKET));
        block = new FluidBlock(still, FabricBlockSettings.copy(Blocks.WATER)){ };

        flowing = new Delegator(this) {
            @Override
            public int getLevel(FluidState state) {
                return state.getLevel();
            }

            @Override
            public boolean isStill(FluidState state) {
                return false;
            }
        };
    }

    /**
     * Call from {@link ModInitializer#onInitialize()}.
     * @return {@link FluidHandler this}
     */
    public FluidHandler register() {
        Registry.register(Registry.FLUID, identifier.toIdentifier(), still);
        Registry.register(Registry.FLUID, identifier.toIdentifier("flows"), flowing);
        Registry.register(Registry.ITEM, identifier.toIdentifier("bucket"), bucketItem);
        Registry.register(Registry.BLOCK, identifier.toIdentifier(), block);
        return this;
    }

    public Fluid getStill() {
        return still;
    }
    public Fluid getFlowing() {
        return flowing;
    }
    public Item getBucketItem() {
        return bucketItem;
    }

    protected BlockState toBlockState(FluidState state) {
        return block.getDefaultState().with(Properties.LEVEL_15, FlowableFluid.getBlockStateLevel(state));
    }
    public boolean matchesType(Fluid fluid) {
        return fluid == getStill() || fluid == getFlowing();
    }
    protected boolean isInfinite() {
        return infinite;
    }
    protected void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
        final BlockEntity entity = state.hasBlockEntity() ? world.getBlockEntity(pos) : null;

        if (!corrodes) Block.dropStacks(state, world, pos, entity);
    }
    protected boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }
    protected int getFlowSpeed(WorldView world) {
        return flowSpeed.applyAsInt(world);
    }
    protected int getLevelDecreasePerBlock(WorldView world) {
        return levelDecreasePerBlock.applyAsInt(world);
    }
    public int getTickRate(WorldView world) {
        return tickRate.applyAsInt(world);
    }
    public float getBlastResistance() {
        return blastResistance;
    }

    public int getLevel(FluidState state) {
        return 0;
    }

    public boolean isStill(FluidState state) {
        return false;
    }

    public abstract static class Delegator extends FlowableFluid {
        public final FluidHandler source;

        public Delegator(FluidHandler source) {
            this.source = source;
        }

        @Override
        public Fluid getStill() {
            return source.getStill();
        }

        @Override
        public Fluid getFlowing() {
            return source.getFlowing();
        }

        @Override
        public Item getBucketItem() {
            return source.getBucketItem();
        }

        @Override
        public BlockState toBlockState(FluidState state) {
            return source.toBlockState(state);
        }

        @Override
        public boolean matchesType(Fluid fluid) {
            return source.matchesType(fluid);
        }

        @Override
        public boolean isInfinite() {
            return source.isInfinite();
        }

        @Override
        public void beforeBreakingBlock(WorldAccess world, BlockPos pos, BlockState state) {
            source.beforeBreakingBlock(world, pos, state);
        }

        @Override
        public boolean canBeReplacedWith(FluidState state, BlockView world, BlockPos pos, Fluid fluid, Direction direction) {
            return source.canBeReplacedWith(state, world, pos, fluid, direction);
        }

        @Override
        public int getFlowSpeed(WorldView world) {
            return source.getFlowSpeed(world);
        }

        @Override
        public int getLevelDecreasePerBlock(WorldView world) {
            return source.getLevelDecreasePerBlock(world);
        }

        @Override
        public int getTickRate(WorldView world) {
            return source.getTickRate(world);
        }

        @Override
        public float getBlastResistance() {
            return source.getBlastResistance();
        }
    }

    public static Builder builder() {
        return new Builder();
    }
    public static final class Builder {
        /**
         * When breaking a block inside this fluid, drops will not spawn if this equals to true.
         */
        public boolean corrodes;
        /**
         * Can you create infinite source of this fluid?
         */
        public boolean infinite;
        /**
         * When breaking a block inside this fluid, this sound will play.
         */
        public SoundEvent blockFillSound = null;
        /**
         * When breaking a block inside this fluid, this particle will show up.
         */
        public ParticleType<?> blockFillParticle = null;
        /**
         * How fast is this fluid travels? The smaller, the slower.
         * <p>
         * {@linkplain #WATER_FLOW_SPEED Water travels at a speed of 4};
         * {@linkplain #LAVA_FLOW_SPEED Lava travels at a speed of 2, or 4 in hot dimensions}.
         */
        public ToIntFunction<WorldView> flowSpeed = WATER_FLOW_SPEED;
        /**
         * Any fluid can have varying height, with levels from 1 to 8. Every block the fluid flows, it's level (or height) decreases by this value.
         * <p>
         * {@linkplain #WATER_LEVEL_DECREASE Water drops 1 level per block};
         * {@linkplain #LAVA_LEVEL_DECREASE Lava drops 2 levels per block, or 1 in hot dimensions}.
         */
        public ToIntFunction<WorldView> levelDecreasePerBlock = WATER_LEVEL_DECREASE;
        /**
         * {@linkplain Block} tick rate.
         * <p>
         * {@linkplain #WATER_TICK_RATE Water tick rate is 5};
         * {@linkplain #LAVA_TICK_RATE Lava tick rate is 30, or 10 in hot biomes}.
         */
        public ToIntFunction<WorldView> tickRate = WATER_TICK_RATE;

        /**
         * The resistance of this fluid to explosions.
         * @see Block#getBlastResistance()
         */
        public float blastResistance = 100;

        private Builder() { }

        public boolean corrodes() {
            return corrodes;
        }

        public Builder corrodes(boolean corrodes) {
            this.corrodes = corrodes;
            return this;
        }

        public SoundEvent blockFillSound() {
            return blockFillSound;
        }

        public Builder blockFillSound(SoundEvent blockFillSound) {
            this.blockFillSound = blockFillSound;
            return this;
        }

        public ParticleType<?> blockFillParticle() {
            return blockFillParticle;
        }

        public Builder blockFillParticle(ParticleType<?> blockFillEffect) {
            this.blockFillParticle = blockFillEffect;
            return this;
        }

        public ToIntFunction<WorldView> flowSpeed() {
            return flowSpeed;
        }

        public Builder flowSpeed(ToIntFunction<WorldView> flowSpeed) {
            if (flowSpeed != null)
                this.flowSpeed = flowSpeed;
            return this;
        }

        public boolean infinite() {
            return infinite;
        }

        public Builder infinite(boolean infinite) {
            this.infinite = infinite;
            return this;
        }

        public ToIntFunction<WorldView> levelDecreasePerBlock() {
            return levelDecreasePerBlock;
        }

        public Builder levelDecreasePerBlock(ToIntFunction<WorldView> levelDecreasePerBlock) {
            if (levelDecreasePerBlock != null)
                this.levelDecreasePerBlock = levelDecreasePerBlock;
            return this;
        }

        public ToIntFunction<WorldView> tickRate() {
            return tickRate;
        }

        public Builder tickRate(ToIntFunction<WorldView> tickRate) {
            if (tickRate != null)
                this.tickRate = tickRate;
            return this;
        }

        public float blastResistance() {
            return blastResistance;
        }

        public Builder blastResistance(float blastResistance) {
            this.blastResistance = blastResistance;
            return this;
        }

        public static Builder of(FluidHandler handler) {
            return new Builder()
                    .blastResistance(handler.blastResistance)
                    .blockFillParticle(handler.blockFillParticle)
                    .blockFillSound(handler.blockFillSound)
                    .corrodes(handler.corrodes)
                    .flowSpeed(handler.flowSpeed)
                    .infinite(handler.isInfinite())
                    .levelDecreasePerBlock(handler.levelDecreasePerBlock)
                    .tickRate(handler.tickRate);
        }

        public FluidHandler build(ElderionIdentifier identifier) {
            return new FluidHandler(this, identifier);
        }

        public FluidHandler build(Author author, String path) {
            return build(new ElderionIdentifier(author, path));
        }
    }
}
