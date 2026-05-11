package com.extfro.extfrocore.api.sync_system;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class SyncedComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS = DeferredRegister.create(
            Registries.DATA_COMPONENT_TYPE, ExtForCore.MOD_ID);

    public static final Supplier<DataComponentType<SyncTagMap>> BLOCK_ITEM_DATA = COMPONENTS.register(
            "block_item_data",
            () -> DataComponentType.<SyncTagMap>builder().persistent(SyncTagMap.CODEC).build());
}
