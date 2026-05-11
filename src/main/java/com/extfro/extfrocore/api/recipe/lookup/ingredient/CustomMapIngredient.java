package com.extfro.extfrocore.api.recipe.lookup.ingredient;

import java.util.Objects;

public class CustomMapIngredient extends AbstractMapIngredient {

    protected final Object value;

    public CustomMapIngredient(Object value) {
        this.value = value;
    }

    @Override
    protected int hash() {
        return Objects.hashCode(value);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && obj instanceof CustomMapIngredient ingredient &&
                Objects.equals(value, ingredient.value);
    }
}
