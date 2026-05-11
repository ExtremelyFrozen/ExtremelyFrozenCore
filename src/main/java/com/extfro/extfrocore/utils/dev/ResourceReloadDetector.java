package com.extfro.extfrocore.utils.dev;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@ApiStatus.Internal
public class ResourceReloadDetector {

    private static final Path GRADLE_DIR = findGradleDir();

    @ApiStatus.Internal
    public static CompletableFuture<Void> regenerateResourcesOnReload(Supplier<CompletableFuture<Void>> reloadFuture) {
        if (!ConfigHolder.INSTANCE.dev.autoRebuildResources || !ExtForCore.isDev() || GRADLE_DIR == null) {
            return reloadFuture.get();
        }
        ProcessBuilder builder = switch (Util.getPlatform()) {
            case WINDOWS -> new ProcessBuilder("cmd.exe", "/c", "gradlew.bat", ":processResources");
            default -> new ProcessBuilder("./gradlew", ":processResources");
        };
        builder.directory(GRADLE_DIR.toFile());
        builder.inheritIO();
        Process process;
        try {
            process = builder.start();
        } catch (IOException exception) {
            ExtForCore.LOGGER.error("Could not run ./gradlew :processResources", exception);
            return reloadFuture.get();
        }
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("extfrocore.debug.resource_rebuild.start"));
        Instant start = Instant.now();
        return process.toHandle().onExit()
                .thenRun(() -> Minecraft.getInstance().player.sendSystemMessage(Component.translatable(
                        "extfrocore.debug.resource_rebuild.done", Duration.between(start, Instant.now()))))
                .thenCompose($ -> reloadFuture.get());
    }

    private static @Nullable Path findGradleDir() {
        Path path = Path.of(".").toAbsolutePath();
        do {
            if (Files.isRegularFile(path.resolve("settings.gradle")) ||
                    Files.isRegularFile(path.resolve("settings.gradle.kts"))) {
                return path;
            }
            path = path.getParent();
        } while (path.getParent() != null);

        return null;
    }
}
