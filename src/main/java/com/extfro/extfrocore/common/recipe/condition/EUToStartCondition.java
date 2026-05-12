package com.extfro.extfrocore.common.recipe.condition;

import com.extfro.extfrocore.api.capability.IEnergyContainer;
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
public class EUToStartCondition extends RecipeCondition<EUToStartCondition> {

    // spotless:off
    public static final MapCodec<EUToStartCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> RecipeCondition.isReverse(instance).and(
            Codec.LONG.fieldOf("eu_to_start").forGetter(val -> val.euToStart)
    ).apply(instance, EUToStartCondition::new));
    // spotless:on

    private long euToStart;

    public EUToStartCondition(long euToStart) {
        this.euToStart = euToStart;
    }

    public EUToStartCondition(boolean isReverse, long euToStart) {
        super(isReverse);
        this.euToStart = euToStart;
    }

    @Override
    public RecipeConditionType<EUToStartCondition> getType() {
        return GTRecipeConditions.EU_TO_START;
    }

    @Override
    public Component getTooltips() {
        return Component.translatable("recipe.condition.eu_to_start.tooltip");
    }

    @Override
    public boolean testCondition(@NotNull GTRecipe recipe, @NotNull RecipeLogic recipeLogic) {
        return recipeLogic.getMachine().getAllTraits().stream()
                .filter(IEnergyContainer.class::isInstance)
                .anyMatch(energyContainer -> ((IEnergyContainer) energyContainer).getEnergyCapacity() > euToStart);
    }

    @Override
    public EUToStartCondition createTemplate() {
        return new EUToStartCondition();
    }
}
