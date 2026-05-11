package com.extfro.extfrocore.api.machine.trait;

import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.capability.recipe.IRecipeHandler;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.recipe.MachineRecipe;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeHandlerList {

    public static final RecipeHandlerList NO_DATA = new RecipeHandlerList(IO.NONE);

    public static final Comparator<RecipeHandlerList> COMPARATOR = (first, second) -> {
        int priority = Long.compare(first.getPriority(), second.getPriority());
        if (priority != 0) return priority;
        boolean firstHasContent = first.getTotalContentAmount() > 0;
        boolean secondHasContent = second.getTotalContentAmount() > 0;
        return Boolean.compare(firstHasContent, secondHasContent);
    };

    @Getter
    private final Map<RecipeCapability<?>, List<IRecipeHandler<?>>> handlerMap = new Reference2ObjectOpenHashMap<>();
    private final List<IRecipeHandler<?>> allHandlers = new ArrayList<>();
    private final List<NotifiableRecipeHandlerTrait<?>> allHandlerTraits = new ArrayList<>();

    @Getter
    private final IO handlerIO;
    @Getter
    private int color = -1;
    @Setter
    @Getter
    @NotNull
    private RecipeHandlerGroup group = RecipeHandlerGroupColor.UNDYED;

    protected RecipeHandlerList(IO handlerIO) {
        this.handlerIO = handlerIO;
    }

    public static RecipeHandlerList of(IO io, int color, IRecipeHandler<?>... handlers) {
        RecipeHandlerList list = new RecipeHandlerList(io);
        list.addHandlers(handlers);
        list.setColor(color);
        return list;
    }

    public static RecipeHandlerList of(IO io, IRecipeHandler<?>... handlers) {
        RecipeHandlerList list = new RecipeHandlerList(io);
        list.addHandlers(handlers);
        return list;
    }

    public static RecipeHandlerList of(IO io, Iterable<IRecipeHandler<?>> handlers) {
        RecipeHandlerList list = new RecipeHandlerList(io);
        list.addHandlers(handlers);
        return list;
    }

    public static RecipeHandlerList of(IO io, int color, Iterable<IRecipeHandler<?>> handlers) {
        RecipeHandlerList list = new RecipeHandlerList(io);
        list.addHandlers(handlers);
        list.setColor(color);
        return list;
    }

    public void addHandler(IRecipeHandler<?> handler) {
        addHandlers(List.of(handler));
    }

    public void addHandlers(IRecipeHandler<?>... handlers) {
        addHandlers(Arrays.asList(handlers));
    }

    public void addHandlers(Iterable<IRecipeHandler<?>> handlers) {
        for (IRecipeHandler<?> handler : handlers) {
            handlerMap.computeIfAbsent(handler.getCapability(), ignored -> new ArrayList<>()).add(handler);
            allHandlers.add(handler);
            if (handler instanceof NotifiableRecipeHandlerTrait<?> trait) {
                allHandlerTraits.add(trait);
            }
        }
        if (handlerIO.supports(IO.OUT)) sort();
    }

    private void sort() {
        for (List<IRecipeHandler<?>> handlers : handlerMap.values()) {
            handlers.sort(IRecipeHandler.ENTRY_COMPARATOR);
        }
    }

    public final void setDistinctAndNotify(boolean distinct) {
        setDistinct(distinct, true);
    }

    public final void setDistinct(boolean distinct) {
        setDistinct(distinct, false);
    }

    protected void setDistinct(boolean distinct, boolean notify) {
        boolean currentDistinct = isDistinct();
        if (currentDistinct != distinct) {
            group = currentDistinct ? new RecipeHandlerGroupColor(color) : RecipeHandlerGroupDistinctness.BUS_DISTINCT;
            for (NotifiableRecipeHandlerTrait<?> trait : allHandlerTraits) {
                trait.setDistinct(distinct);
                if (notify) trait.notifyListeners();
            }
        }
    }

    public boolean isDistinct() {
        return group == RecipeHandlerGroupDistinctness.BUS_DISTINCT;
    }

    public void setColor(int color) {
        setColor(color, false);
    }

    public void setColor(int color, boolean notify) {
        this.color = color;
        if (group != RecipeHandlerGroupDistinctness.BUS_DISTINCT) {
            group = new RecipeHandlerGroupColor(color);
        }
        if (notify) {
            for (NotifiableRecipeHandlerTrait<?> trait : allHandlerTraits) {
                trait.notifyListeners();
            }
        }
    }

    public boolean hasCapability(RecipeCapability<?> capability) {
        return handlerMap.containsKey(capability);
    }

    public @NotNull List<IRecipeHandler<?>> getCapability(RecipeCapability<?> capability) {
        return handlerMap.getOrDefault(capability, Collections.emptyList());
    }

    public @NotNull Set<RecipeCapability<?>> getCapabilities() {
        return handlerMap.keySet();
    }

    public boolean doesCapabilityBypassDistinct() {
        for (RecipeCapability<?> capability : getCapabilities()) {
            if (capability.shouldBypassDistinct()) return true;
        }
        return false;
    }

    public boolean isValid(IO externalIO) {
        if (this == NO_DATA || handlerIO == IO.NONE) return false;
        return externalIO == IO.BOTH || handlerIO == IO.BOTH || externalIO == handlerIO;
    }

    public long getPriority() {
        long priority = 0;
        for (IRecipeHandler<?> handler : allHandlers) {
            priority += handler.getPriority();
        }
        return priority;
    }

    public double getTotalContentAmount() {
        double sum = 0;
        for (IRecipeHandler<?> handler : allHandlers) {
            sum += handler.getTotalContentAmount();
        }
        return sum;
    }

    @Contract(pure = true)
    public Map<RecipeCapability<?>, List<Object>> handleRecipe(IO io, MachineRecipe recipe,
                                                               Map<RecipeCapability<?>, List<Object>> contents,
                                                               boolean simulate) {
        if (handlerMap.isEmpty()) return contents;
        var copy = new Reference2ObjectOpenHashMap<>(contents);
        for (var iterator = copy.reference2ObjectEntrySet().fastIterator(); iterator.hasNext();) {
            var entry = iterator.next();
            List<IRecipeHandler<?>> handlers = getCapability(entry.getKey());
            for (IRecipeHandler<?> handler : handlers) {
                List<?> left = handler.handleRecipe(io, recipe, entry.getValue(), simulate);
                if (left == null) {
                    iterator.remove();
                    break;
                }
                entry.setValue(new ArrayList<>(left));
            }
        }
        return copy;
    }

    public List<IRecipeHandler<?>> getHandlersFlat() {
        List<IRecipeHandler<?>> handlers = new ArrayList<>();
        for (var entry : handlerMap.entrySet()) {
            handlers.addAll(entry.getValue());
        }
        return handlers;
    }

    private record Subscription(List<ISubscription> subscriptions) implements ISubscription {

        @Override
        public void unsubscribe() {
            subscriptions.forEach(ISubscription::unsubscribe);
        }
    }

    public ISubscription subscribe(Runnable listener) {
        List<ISubscription> subscriptions = new ArrayList<>(allHandlerTraits.size());
        allHandlerTraits.forEach(trait -> subscriptions.add(trait.addChangedListener(listener)));
        return new Subscription(subscriptions);
    }

    public ISubscription subscribe(Runnable listener, RecipeCapability<?> capability) {
        List<IRecipeHandler<?>> capabilityHandlers = getCapability(capability);
        List<ISubscription> subscriptions = new ArrayList<>(capabilityHandlers.size());
        for (IRecipeHandler<?> handler : capabilityHandlers) {
            if (handler instanceof IRecipeHandlerTrait<?> trait) {
                subscriptions.add(trait.addChangedListener(listener));
            }
        }
        return new Subscription(subscriptions);
    }
}
