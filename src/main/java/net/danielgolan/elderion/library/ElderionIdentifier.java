package net.danielgolan.elderion.library;

import net.minecraft.util.Identifier;

public record ElderionIdentifier(Author author, String path) {
    public ElderionIdentifier(Author author, String path) {
        this.author = author;
        this.path = path.toLowerCase();
    }

    public Identifier toIdentifier() {
        return toIdentifier("");
    }

    public Identifier toIdentifier(String pathAddition) {
        return isEmpty(author) ? new Identifier(author.modID(),path + pathAddition) :
        new Identifier(author.modID(), author.name() + '/' + path + pathAddition);
    }

    private boolean isEmpty(Author a) {
        return a == null || a.name() == null ||a.name().equals("");
    }
}
