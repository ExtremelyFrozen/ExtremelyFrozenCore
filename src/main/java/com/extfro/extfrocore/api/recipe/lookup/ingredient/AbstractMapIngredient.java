package com.extfro.extfrocore.api.recipe.lookup.ingredient;

public abstract class AbstractMapIngredient {

    protected final Class<? extends AbstractMapIngredient> objClass;
    private int hash;
    private boolean hashed;

    protected AbstractMapIngredient() {
        objClass = getClass();
    }

    protected abstract int hash();

    @Override
    public final int hashCode() {
        if (!hashed) {
            hash = hash();
            hashed = true;
        }
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof AbstractMapIngredient ingredient) {
            return objClass == ingredient.objClass;
        }
        return false;
    }

    public boolean isSpecialIngredient() {
        return false;
    }
}
