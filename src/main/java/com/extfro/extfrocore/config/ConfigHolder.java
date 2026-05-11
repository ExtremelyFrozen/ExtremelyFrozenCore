package com.extfro.extfrocore.config;

import com.extfro.extfrocore.ExtForCore;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;
import org.jetbrains.annotations.ApiStatus;

@Config(id = ExtForCore.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    @ApiStatus.Internal
    public static dev.toma.configuration.config.ConfigHolder<ConfigHolder> INTERNAL_INSTANCE;

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null || INTERNAL_INSTANCE == null) {
                INTERNAL_INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.YAML);
                INSTANCE = INTERNAL_INSTANCE.getConfigInstance();
            }
        }
    }

    @Configurable
    public DeveloperConfigs dev = new DeveloperConfigs();

    public static class DeveloperConfigs {

        @Configurable
        @Configurable.Comment({ "Executes ./gradlew :processResources when F3+T is pressed",
                "Only works in a development environment", "Default: false" })
        public boolean autoRebuildResources = false;
    }
}
