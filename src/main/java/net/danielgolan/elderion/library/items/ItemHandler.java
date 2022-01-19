package net.danielgolan.elderion.library.items;

import net.danielgolan.elderion.library.Author;
import net.danielgolan.elderion.library.ElderionIdentifier;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public record ItemHandler<I extends Item>(I item, ElderionIdentifier identifier) {
    public static @NotNull ItemHandler<Item> of(ElderionIdentifier identifier) {
        return of(identifier, ItemGroup.MISC);
    }
    public static @NotNull ItemHandler<Item> of(ElderionIdentifier identifier, ItemGroup group) {
        return of(identifier, new Item.Settings().group(group));
    }
    @Contract("_, _ -> new")
    public static @NotNull ItemHandler<Item> of(ElderionIdentifier identifier, Item.Settings settings) {
        return new ItemHandler<>(new Item(settings), identifier);
    }

    public ItemHandler<I> registerAndGetInstance() {
        register();
        return this;
    }

    public I register() {
        return Registry.register(Registry.ITEM, identifier.toIdentifier(), item());
    }
}
