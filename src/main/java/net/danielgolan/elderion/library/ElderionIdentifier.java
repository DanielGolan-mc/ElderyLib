package net.danielgolan.elderion.library;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;

public record ElderionIdentifier(Author author, String path) {
    public ElderionIdentifier(Author author, @NotNull String path) {
        this.author = author;
        this.path = path.toLowerCase();
    }

    @Deprecated
    public @NotNull Identifier toIdentifier() {
        return get();
    }

    public @NotNull Identifier get() {
        return toIdentifier("");
    }

    public @NotNull Identifier toIdentifier(String pathAddition) {
        if (pathAddition == null || pathAddition.isEmpty() || pathAddition.isBlank())
            return isEmpty(author) ? new Identifier(author.modID(), path) :
                    new Identifier(author.modID(), author.name() + '/' + path);
        else return isEmpty(author) ? new Identifier(author.modID(), path + '_' + pathAddition) :
                new Identifier(author.modID(), author.name() + '/' + path + '_' + pathAddition);
    }

    private boolean isEmpty(Author a) {
        return a == null || a.name() == null || a.name().equals("");
    }

    @Override
    public String toString() {
        return toIdentifier().toString();
    }
}
