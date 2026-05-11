package com.extfro.extfrocore.api.capability.recipe;

import com.extfro.extfrocore.api.machine.trait.RecipeHandlerList;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public interface IRecipeCapabilityHolder {

    default boolean hasCapabilityProxies() {
        return !getCapabilitiesProxy().isEmpty();
    }

    @NotNull
    Map<IO, List<RecipeHandlerList>> getCapabilitiesProxy();

    @NotNull
    Map<IO, Map<RecipeCapability<?>, List<IRecipeHandler<?>>>> getCapabilitiesFlat();

    @NotNull
    default List<RecipeHandlerList> getCapabilitiesForIO(IO io) {
        return getCapabilitiesProxy().getOrDefault(io, Collections.emptyList());
    }

    @NotNull
    default List<IRecipeHandler<?>> getCapabilitiesFlat(IO io, RecipeCapability<?> capability) {
        return getCapabilitiesFlat()
                .getOrDefault(io, Collections.emptyMap())
                .getOrDefault(capability, Collections.emptyList());
    }

    default void addHandlerList(RecipeHandlerList handlerList) {
        if (handlerList == RecipeHandlerList.NO_DATA) return;
        IO io = handlerList.getHandlerIO();
        getCapabilitiesProxy().computeIfAbsent(io, ignored -> new ArrayList<>()).add(handlerList);
        var entrySet = handlerList.getHandlerMap().entrySet();
        var inner = getCapabilitiesFlat().computeIfAbsent(io,
                ignored -> new Reference2ObjectOpenHashMap<>(entrySet.size()));
        for (var entry : entrySet) {
            List<IRecipeHandler<?>> handlers = entry.getValue();
            inner.computeIfAbsent(entry.getKey(), ignored -> new ArrayList<>(handlers.size())).addAll(handlers);
        }
    }
}
