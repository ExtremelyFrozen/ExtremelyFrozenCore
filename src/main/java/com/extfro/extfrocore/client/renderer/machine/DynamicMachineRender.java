package com.extfro.extfrocore.client.renderer.machine;

import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.feature.IMachineFeature;
import com.extfro.extfrocore.client.model.machine.IMachineRendererModel;
import com.extfro.extfrocore.client.model.machine.MachineModel;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;

public abstract class DynamicMachineRender<T extends IMachineFeature, S extends DynamicMachineRender<T, S>>
                                          implements Comparable<DynamicMachineRender<T, S>>, IMachineRendererModel<T> {

    public static final Codec<DynamicMachineRender<?, ?>> CODEC = DynamicMachineRenderManager.TYPE_CODEC
            .dispatchStable(DynamicMachineRender::getType, DynamicMachineRenderType::codec);

    protected MachineModel parent;

    public abstract DynamicMachineRenderType<T, S> getType();

    public MachineModel getParent() {
        return parent;
    }

    public void setParent(MachineModel parent) {
        this.parent = parent;
    }

    @Override
    public MachineDefinition getDefinition() {
        return parent.getDefinition();
    }

    @Override
    public int compareTo(@NotNull DynamicMachineRender<T, S> other) {
        return getType().compareTo(other.getType());
    }

    @Override
    public boolean isBlockEntityRenderer() {
        return true;
    }
}
