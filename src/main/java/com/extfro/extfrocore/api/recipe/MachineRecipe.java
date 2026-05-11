package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.category.RecipeCategory;
import com.extfro.extfrocore.api.recipe.chance.logic.ChanceLogic;
import com.extfro.extfrocore.api.recipe.content.Content;
import com.extfro.extfrocore.api.recipe.content.ContentModifier;

import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MachineRecipe implements Recipe<RecipeInput> {

    public final MachineRecipeType recipeType;
    @Getter
    @Setter
    @Nullable
    public ResourceLocation id;
    public final Map<RecipeCapability<?>, List<Content>> inputs;
    public final Map<RecipeCapability<?>, List<Content>> outputs;
    public final Map<RecipeCapability<?>, List<Content>> tickInputs;
    public final Map<RecipeCapability<?>, List<Content>> tickOutputs;

    public final Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics;
    public final Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics;

    public final List<RecipeCondition<?>> conditions;
    public final RecipeDataMap data;
    public int duration;
    public int parallels = 1;
    public int subtickParallels = 1;
    public int batchParallels = 1;
    public int ocLevel = 0;
    public final RecipeCategory recipeCategory;
    public int groupColor = -1;

    public MachineRecipe(MachineRecipeType recipeType,
                         Map<RecipeCapability<?>, List<Content>> inputs,
                         Map<RecipeCapability<?>, List<Content>> outputs,
                         Map<RecipeCapability<?>, List<Content>> tickInputs,
                         Map<RecipeCapability<?>, List<Content>> tickOutputs,
                         Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                         List<RecipeCondition<?>> conditions,
                         @NotNull RecipeDataMap data,
                         int duration,
                         @NotNull RecipeCategory recipeCategory,
                         int groupColor) {
        this(recipeType, null, inputs, outputs, tickInputs, tickOutputs,
                inputChanceLogics, outputChanceLogics, tickInputChanceLogics, tickOutputChanceLogics,
                conditions, data, duration, recipeCategory, groupColor);
    }

    public MachineRecipe(MachineRecipeType recipeType,
                         @Nullable ResourceLocation id,
                         Map<RecipeCapability<?>, List<Content>> inputs,
                         Map<RecipeCapability<?>, List<Content>> outputs,
                         Map<RecipeCapability<?>, List<Content>> tickInputs,
                         Map<RecipeCapability<?>, List<Content>> tickOutputs,
                         Map<RecipeCapability<?>, ChanceLogic> inputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> outputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> tickInputChanceLogics,
                         Map<RecipeCapability<?>, ChanceLogic> tickOutputChanceLogics,
                         List<RecipeCondition<?>> conditions,
                         @NotNull RecipeDataMap data,
                         int duration,
                         @NotNull RecipeCategory recipeCategory,
                         int groupColor) {
        this.recipeType = recipeType;
        this.id = id;
        this.inputs = inputs;
        this.outputs = outputs;
        this.tickInputs = tickInputs;
        this.tickOutputs = tickOutputs;
        this.inputChanceLogics = inputChanceLogics;
        this.outputChanceLogics = outputChanceLogics;
        this.tickInputChanceLogics = tickInputChanceLogics;
        this.tickOutputChanceLogics = tickOutputChanceLogics;
        this.conditions = conditions;
        this.data = data;
        this.duration = duration;
        this.recipeCategory = recipeCategory != RecipeCategory.DEFAULT ? recipeCategory : recipeType.getCategory();
        this.groupColor = groupColor;
    }

    public MachineRecipe copy() {
        return copy(ContentModifier.IDENTITY, false);
    }

    public MachineRecipe copy(ContentModifier modifier) {
        return copy(modifier, true);
    }

    public MachineRecipe copy(ContentModifier modifier, boolean modifyDuration) {
        MachineRecipe copied = new MachineRecipe(recipeType, id,
                modifier.applyContents(inputs), modifier.applyContents(outputs),
                modifier.applyContents(tickInputs), modifier.applyContents(tickOutputs),
                new HashMap<>(inputChanceLogics), new HashMap<>(outputChanceLogics),
                new HashMap<>(tickInputChanceLogics), new HashMap<>(tickOutputChanceLogics),
                new ArrayList<>(conditions), data.copy(), duration, recipeCategory, groupColor);
        if (modifyDuration) {
            copied.duration = modifier.apply(this.duration);
        }
        copied.ocLevel = ocLevel;
        copied.parallels = parallels;
        copied.batchParallels = batchParallels;
        copied.subtickParallels = subtickParallels;
        return copied;
    }

    @Override
    public boolean matches(@NotNull RecipeInput input, @NotNull Level level) {
        return false;
    }

    @Override
    public @NotNull ItemStack assemble(@NotNull RecipeInput input, HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.@NotNull Provider registries) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public @NotNull String getGroup() {
        return recipeType.group;
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return recipeType.getSerializer();
    }

    @Override
    public @NotNull RecipeType<?> getType() {
        return recipeType;
    }

    public @Nullable ResourceLocation getRecipeLocation() {
        return id;
    }

    public List<Content> getInputContents(RecipeCapability<?> capability) {
        return inputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getOutputContents(RecipeCapability<?> capability) {
        return outputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickInputContents(RecipeCapability<?> capability) {
        return tickInputs.getOrDefault(capability, Collections.emptyList());
    }

    public List<Content> getTickOutputContents(RecipeCapability<?> capability) {
        return tickOutputs.getOrDefault(capability, Collections.emptyList());
    }

    public boolean hasTick() {
        return !tickInputs.isEmpty() || !tickOutputs.isEmpty();
    }

    public ChanceLogic getChanceLogicForCapability(RecipeCapability<?> cap, IO io, boolean isTick) {
        if (io == IO.OUT) {
            return isTick ? tickOutputChanceLogics.getOrDefault(cap, ChanceLogic.OR) :
                    outputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
        } else if (io == IO.IN) {
            return isTick ? tickInputChanceLogics.getOrDefault(cap, ChanceLogic.OR) :
                    inputChanceLogics.getOrDefault(cap, ChanceLogic.OR);
        }
        return ChanceLogic.OR;
    }

    public int getTotalRuns() {
        return parallels * subtickParallels * batchParallels;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof MachineRecipe recipe)) return false;
        return id != null && id.equals(recipe.id);
    }

    @Override
    public int hashCode() {
        return id == null ? 0 : id.hashCode();
    }

    @Override
    public String toString() {
        return id != null ? id.toString() : "null id";
    }
}
