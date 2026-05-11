package com.extfro.extfrocore.client.renderer.cover;

import com.extfro.extfrocore.api.registry.EFRegistries;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public final class CoverRenderers {

    private CoverRenderers() {}

    @OnlyIn(Dist.CLIENT)
    public static void onResourceManagerReload() {
        ICoverableRenderer.onResourceManagerReload();
        EFRegistries.COVERS.forEach(definition -> {
            if (definition.getCoverRenderer() != null && definition.getCoverRenderer().get() != null) {
                definition.getCoverRenderer().get().onResourceManagerReload();
            }
        });
    }
}
