package com.extfro.extfrocore.integration.xei.oreprocessing;

import java.util.function.Consumer;

@FunctionalInterface
public interface OreProcessingDataSource {

    void collectDisplays(Consumer<OreProcessingDisplay> consumer);
}
