package com.extfro.extfrocore.data.pack.event;

import com.extfro.extfrocore.data.pack.EFDynamicResourcePack;

import net.minecraft.data.models.blockstates.BlockStateGenerator;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.fml.event.IModBusEvent;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class EFRegisterDynamicResourcesEvent extends Event implements IModBusEvent {

    @ApiStatus.Internal
    public EFRegisterDynamicResourcesEvent() {
    }

    public void addResource(ResourceLocation location, JsonElement object) {
        EFDynamicResourcePack.addResource(location, object);
    }

    public void addResource(ResourceLocation location, byte[] data) {
        EFDynamicResourcePack.addResource(location, data);
    }

    public void addBlockModel(ResourceLocation location, JsonElement object) {
        EFDynamicResourcePack.addBlockModel(location, object);
    }

    public void addBlockModel(ResourceLocation location, Supplier<JsonElement> object) {
        EFDynamicResourcePack.addBlockModel(location, object);
    }

    public void addBlockModel(BlockModelBuilder builder) {
        EFDynamicResourcePack.addBlockModel(builder);
    }

    public void addItemModel(ResourceLocation location, JsonElement object) {
        EFDynamicResourcePack.addItemModel(location, object);
    }

    public void addItemModel(ResourceLocation location, Supplier<JsonElement> object) {
        EFDynamicResourcePack.addItemModel(location, object);
    }

    public void addItemModel(ItemModelBuilder builder) {
        EFDynamicResourcePack.addItemModel(builder);
    }

    public <T extends ModelBuilder<T>> void addModel(T builder) {
        EFDynamicResourcePack.addModel(builder);
    }

    public void addModel(ResourceLocation location, JsonElement object) {
        EFDynamicResourcePack.addModel(location, object);
    }

    public void addModel(ResourceLocation location, Supplier<JsonElement> object) {
        EFDynamicResourcePack.addModel(location, object);
    }

    public void addBlockState(ResourceLocation location, JsonElement stateJson) {
        EFDynamicResourcePack.addBlockState(location, stateJson);
    }

    public void addBlockState(ResourceLocation location, Supplier<JsonElement> stateJson) {
        EFDynamicResourcePack.addBlockState(location, stateJson);
    }

    public void addBlockState(BlockStateGenerator generator) {
        EFDynamicResourcePack.addBlockState(generator);
    }
}
