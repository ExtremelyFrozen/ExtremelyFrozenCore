package com.extfro.extfrocore.api.sync_system;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.cover.filter.SimpleFluidFilter;
import com.extfro.extfrocore.api.cover.filter.SimpleItemFilter;

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

    public static final Supplier<DataComponentType<SimpleItemFilter>> SIMPLE_ITEM_FILTER = COMPONENTS.register(
            "simple_item_filter",
            () -> DataComponentType.<SimpleItemFilter>builder().persistent(SimpleItemFilter.CODEC).build());

    public static final Supplier<DataComponentType<SimpleFluidFilter>> SIMPLE_FLUID_FILTER = COMPONENTS.register(
            "simple_fluid_filter",
            () -> DataComponentType.<SimpleFluidFilter>builder().persistent(SimpleFluidFilter.CODEC).build());

    public static final Supplier<DataComponentType<String>> TAG_FILTER_EXPRESSION = COMPONENTS.register(
            "tag_filter_expression",
            () -> DataComponentType.<String>builder().persistent(com.mojang.serialization.Codec.STRING).build());
}
