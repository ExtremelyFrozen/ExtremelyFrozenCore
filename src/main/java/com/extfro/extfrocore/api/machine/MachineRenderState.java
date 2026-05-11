package com.extfro.extfrocore.api.machine;

import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;

public class MachineRenderState extends StateHolder<MachineDefinition, MachineRenderState> {

    public MachineRenderState(
                              MachineDefinition owner,
                              Reference2ObjectArrayMap<Property<?>, Comparable<?>> values,
                              MapCodec<MachineRenderState> propertiesCodec) {
        super(owner, values, propertiesCodec);
    }

    public MachineDefinition getDefinition() {
        return owner;
    }

    public boolean is(MetaMachine machine) {
        return is(machine.getDefinition());
    }

    public boolean is(MachineDefinition definition) {
        return getDefinition() == definition;
    }
}
