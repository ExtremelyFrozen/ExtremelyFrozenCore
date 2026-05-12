package com.extfro.extfrocore.common.recipe.condition;

import com.extfro.extfrocore.api.machine.trait.RecipeLogic;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.common.data.GTRecipeConditions;
import com.extfro.extfrocore.common.machine.trait.ExhaustVentMachineTrait;

import net.minecraft.network.chat.Component;

import com.mojang.serialization.MapCodec;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

@NoArgsConstructor
public class VentCondition extends RecipeCondition<VentCondition> {

    public static final MapCodec<VentCondition> CODEC = RecipeCondition.simpleCodec(VentCondition::new);
    public final static VentCondition INSTANCE = new VentCondition();

    public VentCondition(boolean isReverse) {
        super(isReverse);
    }

    @Override
    public RecipeConditionType<VentCondition> getType() {
        return GTRecipeConditions.VENT;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.steam_vent.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        var ventTrait = recipeLogic.getMachine().getTrait(ExhaustVentMachineTrait.TYPE);
        if (recipeLogic.getProgress() % 10 == 0 && ventTrait != null) {
            return !(ventTrait.isNeedsVenting() && ventTrait.isVentingBlocked());
        }
        return true;
    }

    @Override
    public VentCondition createTemplate() {
        return new VentCondition();
    }
}
