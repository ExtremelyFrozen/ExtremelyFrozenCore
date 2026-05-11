package com.extfro.extfrocore.api.recipe;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeCapabilityHolder;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroup;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroupColor;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerGroupDistinctness;
import com.extfro.extfrocore.api.machine.trait.RecipeHandlerList;
import com.extfro.extfrocore.api.recipe.condition.RecipeConditionType;
import com.extfro.extfrocore.api.recipe.content.Content;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RecipeHelper {

    private RecipeHelper() {}

    public static ActionResult matchRecipe(IRecipeCapabilityHolder holder, MachineRecipe recipe) {
        return matchRecipe(holder, recipe, false);
    }

    public static ActionResult matchTickRecipe(IRecipeCapabilityHolder holder, MachineRecipe recipe) {
        return recipe.hasTick() ? matchRecipe(holder, recipe, true) : ActionResult.SUCCESS;
    }

    private static ActionResult matchRecipe(IRecipeCapabilityHolder holder, MachineRecipe recipe, boolean tick) {
        if (!holder.hasCapabilityProxies()) return ActionResult.FAIL_NO_CAPABILITIES;

        ActionResult result = handleRecipe(holder, recipe, IO.IN, tick ? recipe.tickInputs : recipe.inputs,
                Collections.emptyMap(), tick, true);
        if (!result.isSuccess()) return result;

        return handleRecipe(holder, recipe, IO.OUT, tick ? recipe.tickOutputs : recipe.outputs,
                Collections.emptyMap(), tick, true);
    }

    public static ActionResult handleRecipeIO(IRecipeCapabilityHolder holder, MachineRecipe recipe, IO io,
                                              Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasCapabilityProxies() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.inputs : recipe.outputs, chanceCaches, false,
                false);
    }

    public static ActionResult handleTickRecipeIO(IRecipeCapabilityHolder holder, MachineRecipe recipe, IO io,
                                                  Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches) {
        if (!holder.hasCapabilityProxies() || io == IO.BOTH) return ActionResult.FAIL_NO_CAPABILITIES;
        return handleRecipe(holder, recipe, io, io == IO.IN ? recipe.tickInputs : recipe.tickOutputs, chanceCaches,
                true, false);
    }

    public static ActionResult handleRecipe(IRecipeCapabilityHolder holder, MachineRecipe recipe, IO io,
                                            Map<RecipeCapability<?>, List<Content>> contents,
                                            Map<RecipeCapability<?>, Object2IntMap<?>> chanceCaches,
                                            boolean isTick, boolean simulated) {
        RecipeRunner runner = new RecipeRunner(recipe, io, isTick, holder, chanceCaches, simulated);
        ActionResult result = runner.handle(contents);

        if (result.isSuccess() || result.capability() == null) {
            recipe.groupColor = runner.getGroupColor();
            return result;
        }

        String key = "extfrocore.recipe_logic.insufficient_" + (io == IO.IN ? "in" : "out");
        return ActionResult.fail(Component.translatable(key)
                .append(Component.literal(": "))
                .append(result.capability().getName()), result.capability(), io);
    }

    public static ActionResult matchContents(IRecipeCapabilityHolder holder, MachineRecipe recipe) {
        ActionResult match = matchRecipe(holder, recipe);
        if (!match.isSuccess()) return match;
        return matchTickRecipe(holder, recipe);
    }

    public static ActionResult checkConditions(MachineRecipe recipe, @NotNull RecipeLogicContext context) {
        if (recipe.conditions.isEmpty()) return ActionResult.SUCCESS;
        Map<RecipeConditionType<?>, List<RecipeCondition<?>>> or = new Reference2ObjectArrayMap<>();
        for (RecipeCondition<?> condition : recipe.conditions) {
            if (condition.isOr()) {
                or.computeIfAbsent(condition.getType(), ignored -> new ArrayList<>()).add(condition);
            } else if (!condition.check(recipe, context)) {
                return ActionResult.fail(Component.translatable("extfrocore.recipe_logic.condition_fails")
                        .append(Component.literal(": "))
                        .append(condition.getTooltips()), null, null);
            }
        }

        for (List<RecipeCondition<?>> conditions : or.values()) {
            boolean passed = conditions.isEmpty();
            MutableComponent component = Component.translatable("extfrocore.recipe_logic.condition_fails")
                    .append(Component.literal(": "));
            for (RecipeCondition<?> condition : conditions) {
                passed = condition.check(recipe, context);
                if (passed) break;
                component.append(condition.getTooltips());
            }

            if (!passed) {
                return ActionResult.fail(component, null, null);
            }
        }
        return ActionResult.SUCCESS;
    }

    @Contract(pure = true)
    public static MachineRecipe trimRecipeOutputs(MachineRecipe recipe,
                                                  Reference2IntMap<RecipeCapability<?>> trimLimits) {
        if (trimLimits.isEmpty() || trimLimits.values().intStream().allMatch(integer -> integer == -1)) {
            return recipe;
        }

        MachineRecipe copy = recipe.copy();
        copy.outputs.clear();
        copy.outputs.putAll(doTrim(recipe.outputs, trimLimits));
        copy.tickOutputs.clear();
        copy.tickOutputs.putAll(doTrim(recipe.tickOutputs, trimLimits));
        return copy;
    }

    @Contract(pure = true)
    public static Map<RecipeCapability<?>, List<Content>> doTrim(Map<RecipeCapability<?>, List<Content>> current,
                                                                 Reference2IntMap<RecipeCapability<?>> trimLimits) {
        Map<RecipeCapability<?>, List<Content>> outputs = new Reference2ObjectOpenHashMap<>(current.size());

        for (var entry : current.entrySet()) {
            RecipeCapability<?> capability = entry.getKey();
            List<Content> contents = entry.getValue();
            if (contents.isEmpty()) continue;
            int limit = trimLimits.getOrDefault(capability, -1);
            if (limit == 0) continue;

            List<Content> list = outputs.computeIfAbsent(capability, ignored -> new ArrayList<>());
            if (limit == -1) {
                list.addAll(contents);
                continue;
            }

            int added = 0;
            List<Content> chanced = new ArrayList<>();
            for (Content content : contents) {
                if (added == limit) break;
                if (content.isChanced()) {
                    chanced.add(content);
                } else {
                    list.add(content);
                    added++;
                }
            }

            if (added < limit) {
                int remaining = Math.min(chanced.size(), limit - added);
                list.addAll(chanced.subList(0, remaining));
            }
        }

        return outputs;
    }

    public static void addToRecipeHandlerMap(RecipeHandlerGroup key, RecipeHandlerList handler,
                                             Map<RecipeHandlerGroup, List<RecipeHandlerList>> map) {
        if (handler.doesCapabilityBypassDistinct()) {
            map.computeIfAbsent(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT, ignored -> new ArrayList<>())
                    .add(handler);
            return;
        }
        if (key.equals(RecipeHandlerGroupColor.UNDYED)) {
            for (var entry : map.entrySet()) {
                if (entry.getKey().equals(RecipeHandlerGroupDistinctness.BUS_DISTINCT) ||
                        entry.getKey().equals(RecipeHandlerGroupDistinctness.BYPASS_DISTINCT) ||
                        entry.getKey().equals(RecipeHandlerGroupColor.UNDYED)) {
                    continue;
                }
                entry.getValue().add(handler);
            }
        }
        List<RecipeHandlerList> undyed = map.getOrDefault(RecipeHandlerGroupColor.UNDYED, Collections.emptyList());
        map.computeIfAbsent(key, ignored -> new ArrayList<>(undyed)).add(handler);
    }
}
