package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;

import com.google.common.collect.Streams;

import java.util.List;
import java.util.function.Predicate;

public class AndPartCondition implements PartCondition {

    public static final String TOKEN = "AND";
    private final Iterable<? extends PartCondition> conditions;

    public AndPartCondition(Iterable<? extends PartCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> def) {
        List<Predicate<MachineRenderState>> predicates = Streams.stream(conditions)
                .map(condition -> condition.getPredicate(def))
                .toList();
        return state -> predicates.stream().allMatch(predicate -> predicate.test(state));
    }
}
