package com.extfro.extfrocore.api.recipe.modifier;

import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.MachineRecipe;
import com.extfro.extfrocore.api.recipe.RecipeCondition;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;

import net.minecraft.network.chat.Component;

import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@FunctionalInterface
public interface ModifierFunction {

    ModifierFunction NULL = recipe -> null;
    ModifierFunction IDENTITY = ModifierFunction.builder().build();

    Component DEFAULT_FAILURE = Component.translatable("extfrocore.recipe_modifier.default_fail");

    static ModifierFunction cancel(Component reason) {
        return new ModifierFunction() {

            @Override
            public @Nullable MachineRecipe apply(@NotNull MachineRecipe recipe) {
                return null;
            }

            @Override
            public Component getFailReason() {
                return reason;
            }
        };
    }

    static FunctionBuilder builder() {
        return new FunctionBuilder();
    }

    @Contract(pure = true)
    @Nullable
    MachineRecipe apply(@NotNull MachineRecipe recipe);

    default ModifierFunction compose(@NotNull ModifierFunction before) {
        return recipe -> applySafe(before.apply(recipe));
    }

    default ModifierFunction andThen(@NotNull ModifierFunction after) {
        return recipe -> after.applySafe(apply(recipe));
    }

    private MachineRecipe applySafe(@Nullable MachineRecipe recipe) {
        if (recipe == null) return null;
        return apply(recipe);
    }

    default Component getFailReason() {
        return DEFAULT_FAILURE;
    }

    @Setter
    @Accessors(chain = true, fluent = true)
    final class FunctionBuilder {

        private int parallels = 1;
        private int subtickParallels = 1;
        private int batchParallels = 1;
        private int addOCs;
        private ContentModifier durationModifier = ContentModifier.IDENTITY;
        private ContentModifier inputModifier = ContentModifier.IDENTITY;
        private ContentModifier outputModifier = ContentModifier.IDENTITY;
        private ContentModifier tickInputModifier = ContentModifier.IDENTITY;
        private ContentModifier tickOutputModifier = ContentModifier.IDENTITY;
        private final List<RecipeCondition<?>> addedConditions = new ArrayList<>();

        public FunctionBuilder conditions(RecipeCondition<?>... conditions) {
            addedConditions.addAll(Arrays.asList(conditions));
            return this;
        }

        public FunctionBuilder modifyAllContents(ContentModifier modifier) {
            inputModifier = modifier;
            outputModifier = modifier;
            tickInputModifier = modifier;
            tickOutputModifier = modifier;
            return this;
        }

        public FunctionBuilder durationMultiplier(double multiplier) {
            durationModifier = ContentModifier.multiplier(multiplier);
            return this;
        }

        public ModifierFunction build() {
            if (parallels == 0 || subtickParallels == 0 || batchParallels == 0) return NULL;
            return recipe -> {
                var newConditions = new ArrayList<>(recipe.conditions);
                newConditions.addAll(addedConditions);
                var copied = new MachineRecipe(recipe.recipeType, recipe.id,
                        inputModifier.applyContents(recipe.inputs),
                        outputModifier.applyContents(recipe.outputs),
                        tickInputModifier.applyContents(recipe.tickInputs),
                        tickOutputModifier.applyContents(recipe.tickOutputs),
                        new HashMap<>(recipe.inputChanceLogics), new HashMap<>(recipe.outputChanceLogics),
                        new HashMap<>(recipe.tickInputChanceLogics), new HashMap<>(recipe.tickOutputChanceLogics),
                        newConditions, recipe.data.copy(), recipe.duration, recipe.recipeCategory, recipe.groupColor);
                copied.parallels = recipe.parallels * parallels;
                copied.subtickParallels = recipe.subtickParallels * subtickParallels;
                copied.batchParallels = recipe.batchParallels * batchParallels;
                copied.ocLevel = recipe.ocLevel + addOCs;
                copied.duration = Math.max(1, durationModifier.apply(recipe.duration));
                return copied;
            };
        }

        @SuppressWarnings("unused")
        private static Map<RecipeCapability<?>, List<Content>> applyContents(ContentModifier modifier,
                                                                             Map<RecipeCapability<?>, List<Content>> contents) {
            return modifier.applyContents(contents);
        }
    }
}
