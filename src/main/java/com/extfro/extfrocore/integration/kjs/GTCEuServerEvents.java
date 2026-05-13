package com.extfro.extfrocore.integration.kjs;

import com.extfro.extfrocore.integration.kjs.events.GTBedrockFluidVeinEventJS;
import com.extfro.extfrocore.integration.kjs.events.GTBedrockOreVeinEventJS;
import com.extfro.extfrocore.integration.kjs.events.GTOreVeinEventJS;
import com.extfro.extfrocore.integration.kjs.events.RegisterCapesEventJS;

import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;

public interface GTCEuServerEvents {

    EventGroup GROUP = EventGroup.of("GTCEuServerEvents");

    EventHandler ORE_VEIN_MODIFICATION = GROUP.server("oreVeins", () -> GTOreVeinEventJS.class);
    EventHandler FLUID_VEIN_MODIFICATION = GROUP.server("fluidVeins", () -> GTBedrockFluidVeinEventJS.class);
    EventHandler BEDROCK_ORE_VEIN_MODIFICATION = GROUP.server("bedrockOreVeins", () -> GTBedrockOreVeinEventJS.class);

    EventHandler REGISTER_CAPES = GROUP.server("registerCapes", () -> RegisterCapesEventJS.class);
}
