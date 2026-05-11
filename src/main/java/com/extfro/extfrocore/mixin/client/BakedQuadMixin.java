package com.extfro.extfrocore.mixin.client;

import com.extfro.extfrocore.client.model.TextureKeyedBakedQuad;

import net.minecraft.client.renderer.block.model.BakedQuad;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(BakedQuad.class)
public class BakedQuadMixin implements TextureKeyedBakedQuad {

    @Unique
    private String extfrocore$textureKey;

    @Override
    public BakedQuad extfrocore$setTextureKey(@Nullable String key) {
        this.extfrocore$textureKey = key;
        return (BakedQuad) (Object) this;
    }

    @Override
    public @Nullable String extfrocore$getTextureKey() {
        return extfrocore$textureKey;
    }
}
