package com.extfro.extfrocore.client.model.machine.overlays;

import com.extfro.extfrocore.data.model.MachineModels;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class WorkableOverlaySet {

    public static final ExistingFileHelper.ResourceType TEXTURE =
            new ExistingFileHelper.ResourceType(PackType.CLIENT_RESOURCES, ".png", "textures");

    public static WorkableOverlaySet get(ResourceLocation textureDir, ExistingFileHelper fileHelper) {
        WorkableOverlaySet overlays = new WorkableOverlaySet(textureDir);
        for (OverlayFace overlayFace : OverlayFace.VALUES) {
            String overlayPath = "/" + MachineModels.OVERLAY_PREFIX + overlayFace.getName();
            ResourceLocation normalSprite = textureDir.withSuffix(overlayPath);
            if (!fileHelper.exists(normalSprite, TEXTURE)) {
                overlays.textures.put(overlayFace, StatusTextures.EMPTY);
                continue;
            }

            ResourceLocation activeSprite = fallback(fileHelper, normalSprite.withSuffix("_active"), normalSprite);
            ResourceLocation pausedSprite = fallback(fileHelper, normalSprite.withSuffix("_paused"), normalSprite);
            ResourceLocation normalEmissive = optional(fileHelper, normalSprite.withSuffix("_emissive"));
            ResourceLocation activeEmissive = optional(fileHelper, activeSprite.withSuffix("_emissive"));
            ResourceLocation pausedEmissive = optional(fileHelper, pausedSprite.withSuffix("_emissive"));

            overlays.textures.put(overlayFace, new StatusTextures(normalSprite, activeSprite, pausedSprite,
                    normalEmissive, activeEmissive, pausedEmissive));
        }
        return overlays;
    }

    private static ResourceLocation fallback(ExistingFileHelper fileHelper, ResourceLocation candidate,
                                             ResourceLocation fallback) {
        return fileHelper.exists(candidate, TEXTURE) ? candidate : fallback;
    }

    private static @Nullable ResourceLocation optional(ExistingFileHelper fileHelper, ResourceLocation candidate) {
        return fileHelper.exists(candidate, TEXTURE) ? candidate : null;
    }

    @Getter
    private final ResourceLocation location;
    @Getter
    private final Map<OverlayFace, StatusTextures> textures = new EnumMap<>(OverlayFace.class);

    public WorkableOverlaySet(ResourceLocation location) {
        this.location = location;
    }

    public enum OverlayFace {

        FRONT,
        BACK,
        TOP,
        BOTTOM,
        SIDE;

        public static final OverlayFace[] VALUES = values();

        public static OverlayFace bySide(Direction side) {
            return switch (side) {
                case DOWN -> BOTTOM;
                case UP -> TOP;
                case NORTH -> FRONT;
                case SOUTH -> BACK;
                case WEST, EAST -> SIDE;
            };
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static class StatusTextures {

        public static final StatusTextures EMPTY = new StatusTextures();

        private final Map<WorkableOverlayStatus, ResourceLocation> textures =
                new EnumMap<>(WorkableOverlayStatus.class);
        private final Map<WorkableOverlayStatus, ResourceLocation> emissiveTextures =
                new EnumMap<>(WorkableOverlayStatus.class);

        public StatusTextures(@Nullable ResourceLocation normalSprite,
                              @Nullable ResourceLocation activeSprite,
                              @Nullable ResourceLocation pausedSprite,
                              @Nullable ResourceLocation normalSpriteEmissive,
                              @Nullable ResourceLocation activeSpriteEmissive,
                              @Nullable ResourceLocation pausedSpriteEmissive) {
            textures.put(WorkableOverlayStatus.IDLE, normalSprite);
            emissiveTextures.put(WorkableOverlayStatus.IDLE, normalSpriteEmissive);
            textures.put(WorkableOverlayStatus.WORKING, activeSprite);
            emissiveTextures.put(WorkableOverlayStatus.WORKING, activeSpriteEmissive);
            textures.put(WorkableOverlayStatus.WAITING, activeSprite);
            emissiveTextures.put(WorkableOverlayStatus.WAITING, activeSpriteEmissive);
            textures.put(WorkableOverlayStatus.SUSPENDED, pausedSprite);
            emissiveTextures.put(WorkableOverlayStatus.SUSPENDED, pausedSpriteEmissive);
        }

        private StatusTextures() {
        }

        public @Nullable ResourceLocation getTexture(@NotNull WorkableOverlayStatus status) {
            return textures.get(status);
        }

        public @Nullable ResourceLocation getEmissiveTexture(@NotNull WorkableOverlayStatus status) {
            return emissiveTextures.get(status);
        }
    }
}
