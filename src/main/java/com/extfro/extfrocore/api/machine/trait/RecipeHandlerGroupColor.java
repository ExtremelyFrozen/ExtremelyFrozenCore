package com.extfro.extfrocore.api.machine.trait;

public record RecipeHandlerGroupColor(int color) implements RecipeHandlerGroup {

    public static final RecipeHandlerGroupColor UNDYED = new RecipeHandlerGroupColor(-1);
}
