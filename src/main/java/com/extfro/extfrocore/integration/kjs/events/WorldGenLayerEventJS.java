package com.extfro.extfrocore.integration.kjs.events;

import com.extfro.extfrocore.api.data.worldgen.SimpleWorldGenLayer;
import com.extfro.extfrocore.integration.kjs.builders.WorldGenLayerBuilder;

import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.function.Consumer;

public class WorldGenLayerEventJS implements KubeEvent {

    @Info("Create a new material icon set with the default parent.")
    public SimpleWorldGenLayer create(String name, Consumer<WorldGenLayerBuilder> consumer) {
        WorldGenLayerBuilder builder = new WorldGenLayerBuilder(name);
        consumer.accept(builder);
        return builder.build();
    }
}
