package com.extfro.extfrocore.client.model;

import com.extfro.extfrocore.client.util.QuadTransformers;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.data.ModelData;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TextureOverrideModel<T extends BakedModel> extends BakedModelWrapper<T> {

    public static final IQuadTransformer OVERLAY_OFFSET = QuadTransformers.offset(0.002f);

    @Getter
    protected final @NotNull Map<String, TextureAtlasSprite> textureOverrides;

    public TextureOverrideModel(T child, @NotNull Map<String, TextureAtlasSprite> textureOverrides) {
        super(child);
        this.textureOverrides = textureOverrides;
    }

    public BakedModel getChild() {
        return originalModel;
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
                                             @NotNull RandomSource rand, @NotNull ModelData extraData,
                                             @Nullable RenderType renderType) {
        return retextureQuads(super.getQuads(state, side, rand, extraData, renderType), textureOverrides);
    }

    public static List<BakedQuad> retextureQuads(List<BakedQuad> quads,
                                                 Map<String, TextureAtlasSprite> overrides) {
        List<BakedQuad> newQuads = new LinkedList<>();
        for (BakedQuad quad : quads) {
            String textureKey = TextureKeyedBakedQuad.getTextureKey(quad);
            if (textureKey == null || textureKey.isEmpty()) {
                continue;
            }
            if (textureKey.charAt(0) == '#') {
                textureKey = textureKey.substring(1);
            }

            TextureAtlasSprite replacement = overrides.get(textureKey);
            newQuads.add(replacement == null ? quad : QuadTransformers.setSprite(quad, replacement));
        }
        return newQuads;
    }
}
