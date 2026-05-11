package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.content.IContentSerializer;

public class BasicRecipeCapability<T> extends RecipeCapability<T> {

    public BasicRecipeCapability(String name, int color, boolean doRenderSlot, int sortIndex,
                                 IContentSerializer<T> serializer) {
        super(name, color, doRenderSlot, sortIndex, serializer);
    }

    @Override
    public T copyWithModifier(T content, ContentModifier modifier) {
        return switch (content) {
            case Integer value -> serializer.contentClass().cast(modifier.apply(value));
            case Long value -> serializer.contentClass().cast(modifier.apply(value));
            case Float value -> serializer.contentClass().cast(modifier.apply(value));
            case Double value -> serializer.contentClass().cast(modifier.apply(value));
            default -> copyInner(content);
        };
    }
}
