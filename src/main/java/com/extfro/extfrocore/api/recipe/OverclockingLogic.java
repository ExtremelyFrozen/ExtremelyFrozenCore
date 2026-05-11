package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.recipe.content.ContentModifier;
import com.extfro.extfrocore.api.recipe.modifier.ModifierFunction;
import com.extfro.extfrocore.api.recipe.modifier.ParallelLogic;

import com.google.common.math.IntMath;
import org.jetbrains.annotations.NotNull;

import java.math.RoundingMode;

@FunctionalInterface
public interface OverclockingLogic {

    double STD_VOLTAGE_FACTOR = 4.0;
    double PERFECT_HALF_VOLTAGE_FACTOR = 2.0;
    double STD_DURATION_FACTOR = 0.5;
    double STD_DURATION_FACTOR_INV = 2.0;
    double PERFECT_DURATION_FACTOR = 0.25;
    double PERFECT_DURATION_FACTOR_INV = 4.0;
    double PERFECT_HALF_DURATION_FACTOR = 0.5;
    double PERFECT_HALF_DURATION_FACTOR_INV = 2.0;
    int COIL_EUT_DISCOUNT_TEMPERATURE = 900;

    OverclockingLogic PERFECT_OVERCLOCK = create(PERFECT_DURATION_FACTOR, STD_VOLTAGE_FACTOR, false);
    OverclockingLogic NON_PERFECT_OVERCLOCK = create(STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, false);
    OverclockingLogic PERFECT_OVERCLOCK_SUBTICK = create(PERFECT_DURATION_FACTOR, STD_VOLTAGE_FACTOR, true);
    OverclockingLogic NON_PERFECT_OVERCLOCK_SUBTICK = create(STD_DURATION_FACTOR, STD_VOLTAGE_FACTOR, true);

    OCResult runOverclockingLogic(@NotNull OCParams ocParams, long maxValue);

    static OverclockingLogic create(double durationFactor, double valueFactor, boolean subtick) {
        if (subtick) return (params, maxValue) -> subTickParallelOC(params, maxValue, durationFactor, valueFactor);
        return (params, maxValue) -> standardOC(params, maxValue, durationFactor, valueFactor);
    }

    default @NotNull ModifierFunction getModifier(com.extfro.extfrocore.api.machine.MetaMachine machine,
                                                  MachineRecipe recipe,
                                                  long recipeValue,
                                                  long maxValue,
                                                  int recipeTier,
                                                  int maximumTier,
                                                  boolean shouldParallel) {
        if (recipeValue == 0) return ModifierFunction.IDENTITY;
        int overclocks = maximumTier - recipeTier;
        if (overclocks == 0) return ModifierFunction.IDENTITY;
        int maxParallels;
        if (!shouldParallel || this == PERFECT_OVERCLOCK || this == NON_PERFECT_OVERCLOCK) {
            maxParallels = 1;
        } else {
            int lg = IntMath.log2(Math.max(1, recipe.duration), RoundingMode.FLOOR) / 2;
            if (lg > overclocks) {
                maxParallels = 16;
            } else {
                long potential = (1L << Math.min(30, 2 * Math.max(0, overclocks - lg))) + 1;
                maxParallels = ParallelLogic.getParallelAmount(machine, recipe, saturatedCast(potential));
            }
        }
        OCParams params = new OCParams(recipeValue, recipe.duration, overclocks, maxParallels);
        return runOverclockingLogic(params, maxValue).toModifier();
    }

    static OCResult standardOC(OCParams params, long maxValue, double durationFactor, double valueFactor) {
        double duration = params.duration;
        double value = params.value;
        int overclocks = params.ocAmount;
        int ocLevel = 0;
        while (overclocks-- > 0) {
            double potentialValue = value * valueFactor;
            if (potentialValue > maxValue) break;
            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) break;
            duration = potentialDuration;
            value = potentialValue;
            ocLevel++;
        }
        return new OCResult(Math.pow(valueFactor, ocLevel), Math.pow(durationFactor, ocLevel), ocLevel, 1);
    }

    static OCResult subTickNonParallelOC(OCParams params, long maxValue, double durationFactor, double valueFactor) {
        double duration = params.duration;
        double value = params.value;
        int overclocks = params.ocAmount;
        int ocLevel = 0;
        double valueMultiplier = 1;
        double durationMultiplier = 1;
        while (overclocks-- > 0) {
            double potentialValue = value * valueFactor;
            if (potentialValue > maxValue || potentialValue < 1) break;
            valueMultiplier *= valueFactor;
            double potentialDuration = duration * durationFactor;
            if (potentialDuration < 1) {
                potentialValue = value * durationFactor;
                if (potentialValue > maxValue || potentialValue < 1) break;
                valueMultiplier *= durationFactor;
            } else {
                duration = potentialDuration;
                durationMultiplier *= durationFactor;
            }
            value = potentialValue;
            ocLevel++;
        }
        return new OCResult(valueMultiplier, durationMultiplier, ocLevel, 1);
    }

    static OCResult subTickParallelOC(OCParams params, long maxValue, double durationFactor, double valueFactor) {
        double duration = params.duration;
        double value = params.value;
        int overclocks = params.ocAmount;
        int maxParallels = params.maxParallels;
        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;
        while (overclocks-- > 0) {
            double potentialValue = value * valueFactor;
            if (potentialValue > maxValue) break;
            if (shouldParallel || duration * durationFactor < 1) {
                double potentialParallel = parallel / durationFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= durationFactor;
                durationMultiplier *= durationFactor;
            }
            value = potentialValue;
            ocLevel++;
        }
        return new OCResult(Math.pow(valueFactor, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    static OCResult heatingCoilOC(OCParams params, long maxValue, int recipeTemp, int machineTemp) {
        int perfectOCAmount = getCoilDiscountAmount(recipeTemp, machineTemp) / 2;
        double duration = params.duration;
        double value = params.value;
        int overclocks = params.ocAmount;
        int maxParallels = params.maxParallels;
        double parallel = 1;
        boolean shouldParallel = false;
        int ocLevel = 0;
        double durationMultiplier = 1;
        while (overclocks-- > 0) {
            boolean perfect = perfectOCAmount-- > 0;
            double potentialValue = value * STD_VOLTAGE_FACTOR;
            if (potentialValue > maxValue) break;
            double durationFactor = perfect ? PERFECT_DURATION_FACTOR : STD_DURATION_FACTOR;
            if (shouldParallel || duration * durationFactor < 1) {
                double parallelFactor = perfect ? PERFECT_DURATION_FACTOR_INV : STD_DURATION_FACTOR_INV;
                double potentialParallel = parallel * parallelFactor;
                if (potentialParallel > maxParallels) break;
                parallel = potentialParallel;
                shouldParallel = true;
            } else {
                duration *= durationFactor;
                durationMultiplier *= durationFactor;
            }
            value = potentialValue;
            ocLevel++;
        }
        return new OCResult(Math.pow(STD_VOLTAGE_FACTOR, ocLevel), durationMultiplier, ocLevel, (int) parallel);
    }

    static int getCoilDiscountAmount(int recipeTemp, int machineTemp) {
        return Math.max(0, (machineTemp - recipeTemp) / COIL_EUT_DISCOUNT_TEMPERATURE);
    }

    static double getCoilValueDiscount(int recipeTemp, int machineTemp) {
        if (recipeTemp < COIL_EUT_DISCOUNT_TEMPERATURE) return 1;
        int amount = getCoilDiscountAmount(recipeTemp, machineTemp);
        if (amount < 1) return 1;
        return Math.min(1, Math.pow(0.95, amount));
    }

    private static int saturatedCast(long value) {
        if (value > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (value < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) value;
    }

    record OCParams(long value, int duration, int ocAmount, int maxParallels) {}

    record OCResult(double valueMultiplier, double durationMultiplier, int ocLevel, int parallels) {

        public ModifierFunction toModifier() {
            return ModifierFunction.builder()
                    .modifyAllContents(ContentModifier.multiplier(parallels))
                    .durationMultiplier(durationMultiplier)
                    .addOCs(ocLevel)
                    .subtickParallels(parallels)
                    .build();
        }
    }
}
