package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import com.google.common.base.Splitter;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

public class KeyValuePartCondition implements PartCondition {

    private static final Splitter PIPE_SPLITTER = Splitter.on('|').omitEmptyStrings();
    private final String key;
    private final String value;

    public KeyValuePartCondition(String key, String value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> def) {
        Property<?> property = def.getProperty(key);
        if (property == null) {
            throw new RuntimeException(String.format(Locale.ROOT, "Unknown property '%s' on machine '%s'", key,
                    def.getOwner()));
        }
        String parsedValue = value;
        boolean invert = !parsedValue.isEmpty() && parsedValue.charAt(0) == '!';
        if (invert) {
            parsedValue = parsedValue.substring(1);
        }

        List<String> values = PIPE_SPLITTER.splitToList(parsedValue);
        if (values.isEmpty()) {
            throw new RuntimeException(String.format(Locale.ROOT, "Empty value '%s' for property '%s' on machine '%s'",
                    value, key, def.getOwner()));
        }

        Predicate<MachineRenderState> predicate;
        if (values.size() == 1) {
            predicate = getStatePredicate(def, property, parsedValue);
        } else {
            List<Predicate<MachineRenderState>> parsed = values.stream()
                    .map(string -> getStatePredicate(def, property, string))
                    .toList();
            predicate = state -> parsed.stream().anyMatch(p -> p.test(state));
        }
        return invert ? predicate.negate() : predicate;
    }

    private Predicate<MachineRenderState> getStatePredicate(StateDefinition<MachineDefinition, MachineRenderState> def,
                                                            Property<?> property, String value) {
        Optional<?> optional = property.getValue(value);
        if (optional.isEmpty()) {
            throw new RuntimeException(String.format(Locale.ROOT,
                    "Unknown value '%s' for property '%s' on '%s' in '%s'", value, key, def.getOwner(), this.value));
        }
        return state -> state.getValue(property).equals(optional.get());
    }

    @Override
    public String toString() {
        return "KeyValueCondition{" + "key='" + key + "', value='" + value + "'}";
    }
}
