package com.extfro.extfrocore.data.model.builder;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.client.model.machine.MachineModelLoader;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.CustomLoaderBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class MachineModelBuilder<T extends ModelBuilder<T>> extends CustomLoaderBuilder<T> {

    public static <T extends ModelBuilder<T>> BiFunction<T, ExistingFileHelper, MachineModelBuilder<T>> begin(
            MachineDefinition owner) {
        return (parent, existingFileHelper) -> new MachineModelBuilder<>(parent, existingFileHelper, owner);
    }

    @Getter
    private final MachineDefinition owner;
    @Getter
    private final Map<PartialState<T>, ModelFile> models = new LinkedHashMap<>();
    @Getter
    private final List<PartBuilder> parts = new ArrayList<>();
    private final Set<MachineRenderState> coveredStates = new HashSet<>();

    protected MachineModelBuilder(T parent, ExistingFileHelper existingFileHelper, MachineDefinition owner) {
        super(MachineModelLoader.ID, parent, existingFileHelper, true);
        this.owner = owner;
    }

    @Override
    public @NotNull JsonObject toJson(@NotNull JsonObject json) {
        json = super.toJson(json);
        json.addProperty("machine", owner.getId().toString());

        StateDefinition<MachineDefinition, MachineRenderState> stateDefinition = owner.getStateDefinition();
        Preconditions.checkState(!models.isEmpty() || !parts.isEmpty(),
                "A machine model must have at least one variant or multipart model");
        Set<MachineRenderState> missingStates = new HashSet<>(stateDefinition.getPossibleStates());
        missingStates.removeAll(coveredStates);

        if (!parts.isEmpty()) {
            JsonArray multipart = new JsonArray();
            for (PartBuilder part : parts) {
                missingStates.removeIf(part::matchesState);
                multipart.add(part.toJson());
            }
            json.add("multipart", multipart);
        }

        if (!models.isEmpty()) {
            Preconditions.checkState(missingStates.isEmpty(),
                    "Render state for machine %s does not cover all states. Missing: %s", owner, missingStates);
            JsonObject variants = new JsonObject();
            models.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey(PartialState.comparingByProperties()))
                    .forEach(entry -> variants.add(entry.getKey().toString(), modelToJson(entry.getValue())));
            json.add("variants", variants);
        }
        return json;
    }

    public static JsonElement modelToJson(ModelFile model) {
        return new JsonPrimitive(model.getLocation().toString());
    }

    public static JsonElement configuredModelToJson(ConfiguredModel model, boolean includeWeight) {
        JsonObject modelJson = new JsonObject();
        modelJson.addProperty("model", model.model.getLocation().toString());
        if (model.rotationX != 0) {
            modelJson.addProperty("x", model.rotationX);
        }
        if (model.rotationY != 0) {
            modelJson.addProperty("y", model.rotationY);
        }
        if (model.uvLock) {
            modelJson.addProperty("uvlock", true);
        }
        if (includeWeight && model.weight != ConfiguredModel.DEFAULT_WEIGHT) {
            modelJson.addProperty("weight", model.weight);
        }
        return modelJson;
    }

    public static JsonElement configuredModelsToJson(ConfiguredModel... models) {
        Preconditions.checkNotNull(models, "models must not be null");
        Preconditions.checkArgument(models.length > 0, "models must not be empty");
        if (models.length == 1) {
            return configuredModelToJson(models[0], false);
        }
        JsonArray array = new JsonArray();
        for (ConfiguredModel model : models) {
            array.add(configuredModelToJson(model, true));
        }
        return array;
    }

    public MachineModelBuilder<T> replaceModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkNotNull(state, "state must not be null");
        Preconditions.checkNotNull(model, "model must not be null");
        Preconditions.checkArgument(state.getOwner() == owner,
                "Cannot set model for a different machine. Found: %s, Current: %s", state.getOwner(), owner);
        models.put(state, model);
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            if (state.test(fullState)) {
                coveredStates.add(fullState);
            }
        }
        return this;
    }

    public MachineModelBuilder<T> addModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkArgument(disjointToAll(state),
                "Cannot set model for a state for which a partial match has already been configured");
        return replaceModel(state, model);
    }

    public MachineModelBuilder<T> setModel(PartialState<T> state, ModelFile model) {
        Preconditions.checkArgument(!models.containsKey(state),
                "Cannot set model for a state that has already been configured: %s", state);
        return addModel(state, model);
    }

    private boolean disjointToAll(PartialState<T> newState) {
        return coveredStates.stream().noneMatch(newState);
    }

    public PartialState<T> partialState() {
        return new PartialState<>(owner, this);
    }

    public PartBuilder part(ModelFile model) {
        Preconditions.checkNotNull(model, "model must not be null");
        ConfiguredModel[] models = ConfiguredModel.builder()
                .modelFile(model)
                .build();
        PartBuilder part = new PartBuilder(models);
        parts.add(part);
        return part;
    }

    public PartBuilder part(ResourceLocation model) {
        Preconditions.checkNotNull(model, "model must not be null");
        return part(new ModelFile.ExistingModelFile(model, existingFileHelper));
    }

    public MachineModelBuilder<T> forAllStatesModels(Function<MachineRenderState, ModelFile> mapper) {
        return forAllStatesExcept(mapper);
    }

    public MachineModelBuilder<T> forAllStatesExcept(Function<MachineRenderState, ModelFile> mapper,
                                                     Property<?>... ignored) {
        Set<PartialState<T>> seen = new HashSet<>();
        for (MachineRenderState fullState : owner.getStateDefinition().getPossibleStates()) {
            Map<Property<?>, Comparable<?>> propertyValues = Maps.newLinkedHashMap(fullState.getValues());
            for (Property<?> property : ignored) {
                propertyValues.remove(property);
            }
            PartialState<T> partialState = new PartialState<>(owner, propertyValues, this);
            if (seen.add(partialState)) {
                setModel(partialState, mapper.apply(fullState));
            }
        }
        return this;
    }

    public class PartBuilder {

        private final ConfiguredModel[] models;
        private final Multimap<Property<?>, Comparable<?>> conditions = MultimapBuilder.linkedHashKeys()
                .arrayListValues()
                .build();
        private boolean useOr;

        private PartBuilder(ConfiguredModel[] models) {
            Preconditions.checkNotNull(models, "models must not be null");
            Preconditions.checkArgument(models.length > 0, "models must not be empty");
            this.models = models;
        }

        public PartBuilder useOr() {
            this.useOr = true;
            return this;
        }

        @SafeVarargs
        public final <V extends Comparable<V>> PartBuilder when(Property<V> property, V... values) {
            return condition(property, values);
        }

        @SafeVarargs
        public final <V extends Comparable<V>> PartBuilder condition(Property<V> property, V... values) {
            Preconditions.checkNotNull(property, "property must not be null");
            Preconditions.checkNotNull(values, "values must not be null");
            Preconditions.checkArgument(values.length > 0, "values must not be empty");
            Preconditions.checkArgument(owner.getStateDefinition().getProperties().contains(property),
                    "Property %s not found on machine %s", property, owner);
            Preconditions.checkArgument(!conditions.containsKey(property),
                    "Cannot set condition for property \"%s\" more than once", property.getName());
            for (V value : values) {
                Preconditions.checkArgument(property.getPossibleValues().contains(value),
                        "%s is not a valid value for %s", value, property);
            }
            conditions.putAll(property, Arrays.asList(values));
            return this;
        }

        public MachineModelBuilder<T> end() {
            return MachineModelBuilder.this;
        }

        public JsonObject toJson() {
            JsonObject json = new JsonObject();
            if (!conditions.isEmpty()) {
                json.add("when", conditionsToJson());
            }
            json.add("apply", configuredModelsToJson(models));
            return json;
        }

        protected boolean matchesState(MachineRenderState state) {
            if (state.getDefinition() != owner) {
                return false;
            }
            if (conditions.isEmpty()) {
                return true;
            }
            boolean matched = !useOr;
            for (Map.Entry<Property<?>, Comparable<?>> entry : conditions.entries()) {
                boolean contains = state.getValue(entry.getKey()) == entry.getValue();
                if (useOr) {
                    matched |= contains;
                } else {
                    matched &= contains;
                }
            }
            return matched;
        }

        @SuppressWarnings({ "rawtypes", "unchecked" })
        private JsonObject conditionsToJson() {
            JsonObject conditionJson = new JsonObject();
            for (Map.Entry<Property<?>, java.util.Collection<Comparable<?>>> entry : conditions.asMap().entrySet()) {
                StringBuilder values = new StringBuilder();
                for (Comparable<?> value : entry.getValue()) {
                    if (!values.isEmpty()) {
                        values.append('|');
                    }
                    values.append(((Property) entry.getKey()).getName(value));
                }
                conditionJson.addProperty(entry.getKey().getName(), values.toString());
            }
            if (!useOr) {
                return conditionJson;
            }
            JsonArray anyConditions = new JsonArray();
            for (Map.Entry<String, JsonElement> entry : conditionJson.entrySet()) {
                JsonObject single = new JsonObject();
                single.add(entry.getKey(), entry.getValue());
                anyConditions.add(single);
            }
            JsonObject wrapped = new JsonObject();
            wrapped.add("OR", anyConditions);
            return wrapped;
        }
    }

    public static class PartialState<B extends ModelBuilder<B>> implements Predicate<MachineRenderState> {

        @Getter
        private final MachineDefinition owner;
        @Getter
        private final SortedMap<Property<?>, Comparable<?>> setStates;
        @Nullable
        private final MachineModelBuilder<B> outerBuilder;

        private PartialState(MachineDefinition owner, @Nullable MachineModelBuilder<B> outerBuilder) {
            this(owner, Map.of(), outerBuilder);
        }

        private PartialState(MachineDefinition owner, Map<Property<?>, Comparable<?>> setStates,
                             @Nullable MachineModelBuilder<B> outerBuilder) {
            this.owner = owner;
            this.outerBuilder = outerBuilder;
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                Property<?> property = entry.getKey();
                Comparable<?> value = entry.getValue();
                Preconditions.checkArgument(owner.getStateDefinition().getProperties().contains(property),
                        "Property %s not found on machine %s", property, owner);
                Preconditions.checkArgument(property.getPossibleValues().contains(value),
                        "%s is not a valid value for %s", value, property);
            }
            this.setStates = Maps.newTreeMap(Comparator.comparing(Property::getName));
            this.setStates.putAll(setStates);
        }

        public <V extends Comparable<V>> PartialState<B> with(Property<V> property, V value) {
            Preconditions.checkArgument(!setStates.containsKey(property), "Property %s has already been set",
                    property);
            Map<Property<?>, Comparable<?>> newState = new HashMap<>(setStates);
            newState.put(property, value);
            return new PartialState<>(owner, newState, outerBuilder);
        }

        private void checkValidOwner() {
            Preconditions.checkNotNull(outerBuilder,
                    "Partial MachineRenderState must have a valid owner to perform this action");
        }

        public MachineModelBuilder<B> setModel(ModelFile model) {
            checkValidOwner();
            return outerBuilder.setModel(this, model);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PartialState<?> that = (PartialState<?>) o;
            return owner.equals(that.owner) && setStates.equals(that.setStates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(owner, setStates);
        }

        @Override
        public boolean test(MachineRenderState state) {
            if (state.getDefinition() != owner) {
                return false;
            }
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (state.getValue(entry.getKey()) != entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Property<?>, Comparable<?>> entry : setStates.entrySet()) {
                if (!builder.isEmpty()) {
                    builder.append(',');
                }
                builder.append(entry.getKey().getName())
                        .append('=')
                        .append(((Property) entry.getKey()).getName(entry.getValue()));
            }
            return builder.toString();
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        public static Comparator<PartialState<?>> comparingByProperties() {
            return (left, right) -> {
                SortedSet<Property<?>> propertyUniverse = new TreeSet<>(left.getSetStates().comparator().reversed());
                propertyUniverse.addAll(left.getSetStates().keySet());
                propertyUniverse.addAll(right.getSetStates().keySet());

                for (Property property : propertyUniverse) {
                    boolean leftHas = left.getSetStates().containsKey(property);
                    boolean rightHas = right.getSetStates().containsKey(property);
                    if (leftHas != rightHas) {
                        return leftHas ? -1 : 1;
                    }
                    if (leftHas) {
                        Comparable leftValue = left.getSetStates().get(property);
                        Comparable rightValue = right.getSetStates().get(property);
                        int comparison = property.getName(leftValue).compareTo(property.getName(rightValue));
                        if (comparison != 0) {
                            return comparison;
                        }
                    }
                }
                return 0;
            };
        }
    }
}
