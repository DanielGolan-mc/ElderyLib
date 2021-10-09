package net.danielgolan.elderion.library.blocks;

public enum BlockVariation {
    BLOCK(),
    FENCE("fence"),
    FENCE_GATE("fence_gate"),
    SLAB("slab", 2),
    STAIRS("stairs"),
    WALL("wall");

    public final String SUFFIX;
    public final int RECIPE_RESULT;

    BlockVariation(String suffix, int stone_cutter_result) {
        SUFFIX = suffix;
        RECIPE_RESULT = stone_cutter_result;
    }

    BlockVariation(String suffix) {
        this(suffix, 1);
    }

    BlockVariation() {
        this("", 0);
    }

}
