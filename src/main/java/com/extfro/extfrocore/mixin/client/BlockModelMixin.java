package com.extfro.extfrocore.mixin.client;

import com.extfro.extfrocore.client.model.TextureKeyedBakedQuad;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockModel;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = BlockModel.class, priority = 1500)
public class BlockModelMixin {

    @ModifyReturnValue(method = "bakeFace", at = @At("RETURN"))
    private static BakedQuad extfrocore$addTextureKey(BakedQuad quad, BlockElement part, BlockElementFace face) {
        return TextureKeyedBakedQuad.setTextureKey(quad, face.texture());
    }
}
