package com.extfro.extfrocore.client.model;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public interface TextureKeyedBakedQuad {

    @ApiStatus.Internal
    default BakedQuad extfrocore$setTextureKey(@Nullable String key) {
        return (BakedQuad) this;
    }

    default @Nullable String extfrocore$getTextureKey() {
        return null;
    }

    static BakedQuad setTextureKey(BakedQuad quad, @Nullable String key) {
        return ((TextureKeyedBakedQuad) (Object) quad).extfrocore$setTextureKey(key);
    }

    static @Nullable String getTextureKey(BakedQuad quad) {
        return ((TextureKeyedBakedQuad) (Object) quad).extfrocore$getTextureKey();
    }
}
