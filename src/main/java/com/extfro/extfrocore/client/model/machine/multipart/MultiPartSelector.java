package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.client.model.machine.variant.MultiVariantModel;

import net.minecraft.client.resources.model.ModelState;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.StateDefinition;

import com.google.common.collect.Streams;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class MultiPartSelector implements ModelState {

    private final PartCondition condition;
    @Getter
    private final MultiVariantModel variant;

    public MultiPartSelector(PartCondition condition, MultiVariantModel variant) {
        this.condition = condition;
        this.variant = variant;
    }

    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> definition) {
        return condition.getPredicate(definition);
    }

    public static class Deserializer implements JsonDeserializer<MultiPartSelector> {

        @Override
        public MultiPartSelector deserialize(JsonElement json, Type type, JsonDeserializationContext context)
                throws JsonParseException {
            return fromJson(json, context);
        }

        public static MultiPartSelector fromJson(JsonElement json, JsonDeserializationContext context)
                throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return new MultiPartSelector(getSelector(jsonObject),
                    context.deserialize(jsonObject.get("apply"), MultiVariantModel.class));
        }

        private static PartCondition getSelector(JsonObject json) {
            return json.has("when") ? getCondition(GsonHelper.getAsJsonObject(json, "when")) : PartCondition.TRUE;
        }

        private static PartCondition getCondition(JsonObject json) {
            Set<Map.Entry<String, JsonElement>> entries = json.entrySet();
            if (entries.isEmpty()) {
                throw new JsonParseException("No elements found in selector");
            }
            if (entries.size() == 1) {
                if (json.has(OrPartCondition.TOKEN)) {
                    List<PartCondition> conditions = Streams.stream(GsonHelper.getAsJsonArray(json,
                                    OrPartCondition.TOKEN))
                            .map(element -> getCondition(element.getAsJsonObject()))
                            .toList();
                    return new OrPartCondition(conditions);
                }
                if (json.has(AndPartCondition.TOKEN)) {
                    List<PartCondition> conditions = Streams.stream(GsonHelper.getAsJsonArray(json,
                                    AndPartCondition.TOKEN))
                            .map(element -> getCondition(element.getAsJsonObject()))
                            .toList();
                    return new AndPartCondition(conditions);
                }
                return getKeyValueCondition(entries.iterator().next());
            }
            return new AndPartCondition(entries.stream()
                    .map(MultiPartSelector.Deserializer::getKeyValueCondition)
                    .collect(Collectors.toList()));
        }

        private static PartCondition getKeyValueCondition(Map.Entry<String, JsonElement> entry) {
            return new KeyValuePartCondition(entry.getKey(), entry.getValue().getAsString());
        }
    }
}
