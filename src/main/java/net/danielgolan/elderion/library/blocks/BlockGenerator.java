package net.danielgolan.elderion.library.blocks;

import net.minecraft.block.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

public interface BlockGenerator {
    BlockGenerator DEFAULT = (settings, original, variation) -> switch (variation) {
        case STAIRS -> generateStairs(original, settings);
        case BLOCK -> original;
        case WALL -> new WallBlock(settings);
        case SLAB -> new SlabBlock(settings);
        case FENCE -> new FenceBlock(settings);
        case FENCE_GATE -> new FenceGateBlock(settings);
    };

    Block generateVariation(VariedBlock.Builder builder, Block original, BlockVariation variation);

    default Block generate(VariedBlock.Builder builder) {
        return new Block(builder) {
            @Override
            public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
                return builder.boundingBox() == null ? super.getOutlineShape(state, world, pos, context) :
                        builder.boundingBox();
            }
        };
    }

    static StairsBlock generateStairs(Block original, AbstractBlock.Settings settings) {
        return new StairsGenerator(original.getDefaultState(), settings);
    }

    static StairsBlock generateStairs(BlockState state, AbstractBlock.Settings settings) {
        return new StairsGenerator(state, settings);
    }

    final class StairsGenerator extends StairsBlock {
        private StairsGenerator(BlockState state, Settings settings) {
            super(state, settings);
        }
    }
}
