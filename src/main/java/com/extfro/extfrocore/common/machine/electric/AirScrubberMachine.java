package com.extfro.extfrocore.common.machine.electric;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.data.medicalcondition.MedicalCondition;
import com.extfro.extfrocore.api.machine.SimpleTieredMachine;
import com.extfro.extfrocore.api.recipe.GTRecipe;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;
import com.extfro.extfrocore.common.machine.trait.hazard.EnvironmentalHazardCleanerTrait;
import com.extfro.extfrocore.config.ConfigHolder;
import com.extfro.extfrocore.data.recipe.builder.GTRecipeBuilder;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import static com.extfro.extfrocore.api.EFValues.LV;
import static com.extfro.extfrocore.api.EFValues.VHA;

public class AirScrubberMachine extends SimpleTieredMachine {

    public static final float MIN_CLEANING_PER_OPERATION = 10;

    private MedicalCondition currentRecipeMedicalCondition;

    @Getter
    private float removedLastSecond;

    @Getter
    private final EnvironmentalHazardCleanerTrait cleanerTrait;

    public AirScrubberMachine(BlockEntityCreationInfo info, int tier) {
        super(info, tier, GTMachineUtils.largeTankSizeFunction);
        this.cleanerTrait = attachTrait(new EnvironmentalHazardCleanerTrait(tier / 2, this::validateCleaningOperation));
    }

    @Override
    public boolean regressWhenWaiting() {
        return false;
    }

    public boolean validateCleaningOperation(MedicalCondition condition, float amount) {
        if (this.recipeLogic.isActive()) {
            return false;
        }

        currentRecipeMedicalCondition = condition;

        GTRecipeBuilder builder = GTRecipeTypes.AIR_SCRUBBER_RECIPES.recipeBuilder(condition.id.withSuffix("_autogen"))
                .duration(200).EUt(VHA[LV]);
        condition.recipeModifier.accept(builder);
        return this.recipeLogic.checkMatchedRecipeAvailable(builder.build());
    }

    @Override
    public boolean isRecipeLogicAvailable() {
        // Don't run recipes if hazards are off
        return ConfigHolder.INSTANCE.gameplay.environmentalHazards;
    }

    @Override
    public boolean beforeWorking(@Nullable GTRecipe recipe) {
        if (super.beforeWorking(recipe) && recipe != null) {
            // Sets the amount of hazard to clean based on the recipe tier, not the machine tier
            return cleanerTrait.beginCleaningOperation(currentRecipeMedicalCondition,
                    MIN_CLEANING_PER_OPERATION * (recipe.ocLevel + 1));
        }
        return false;
    }

    @Override
    public boolean onWorking() {
        if (!super.onWorking() || !ConfigHolder.INSTANCE.gameplay.environmentalHazards) {
            return false;
        }
        cleanerTrait.cleanHazard();
        return true;
    }

    @Override
    public void afterWorking() {
        cleanerTrait.endCleaningOperation();
    }
}
