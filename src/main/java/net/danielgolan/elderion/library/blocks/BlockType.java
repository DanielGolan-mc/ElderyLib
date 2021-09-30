package net.danielgolan.elderion.library.blocks;

import net.minecraft.item.ItemGroup;

public enum BlockType {
    BUILDING(ItemGroup.BUILDING_BLOCKS), DECORATION(ItemGroup.DECORATIONS);

    public final ItemGroup GROUP;

    BlockType(ItemGroup group) {
        GROUP = group;
    }
}
