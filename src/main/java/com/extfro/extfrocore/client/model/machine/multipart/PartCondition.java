package com.extfro.extfrocore.client.model.machine.multipart;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;

import net.minecraft.world.level.block.state.StateDefinition;

import java.util.function.Predicate;

@FunctionalInterface
public interface PartCondition {

    PartCondition TRUE = definition -> state -> true;
    PartCondition FALSE = definition -> state -> false;

    Predicate<MachineRenderState> getPredicate(StateDefinition<MachineDefinition, MachineRenderState> definition);
}
