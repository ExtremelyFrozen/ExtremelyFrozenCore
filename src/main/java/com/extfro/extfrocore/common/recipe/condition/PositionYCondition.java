package com.extfro.extfrocore.common.recipe.condition;

import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.common.data.GTRecipeConditions;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class PositionYCondition extends RecipeCondition<PositionYCondition> {

    // spotless:off
    public static final MapCodec<PositionYCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(instance.group(
            Codec.INT.fieldOf("min").forGetter(val -> val.min),
            Codec.INT.fieldOf("max").forGetter(val -> val.max)
    )).apply(instance, PositionYCondition::new));
    // spotless:on

    private int min;
    private int max;

    public PositionYCondition(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public PositionYCondition(boolean isReverse, int min, int max) {
        super(isReverse);
        this.min = min;
        this.max = max;
    }

    @Override
    public RecipeConditionType<PositionYCondition> getType() {
        return GTRecipeConditions.POSITION_Y;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.pos_y.tooltip", this.min, this.max);
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        int y = recipeLogic.getBlockPos().getY();
        return y >= this.min && y <= this.max;
    }

    @Override
    public PositionYCondition createTemplate() {
        return new PositionYCondition();
    }
}
