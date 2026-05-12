package com.extfro.extfrocore.api.block;

import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.tag.TagPrefix;

import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.client.renderer.IBlockRendererProvider;
import com.lowdragmc.lowdraglib2.client.renderer.IRenderer;
import org.jetbrains.annotations.Nullable;

public class RendererMaterialBlock extends MaterialBlock implements IBlockRendererProvider {

    public final IRenderer renderer;

    public RendererMaterialBlock(Properties properties, TagPrefix tagPrefix, Material material,
                                 @Nullable IRenderer renderer) {
        super(properties, tagPrefix, material, false);
        this.renderer = renderer;
    }

    @Nullable
    @Override
    @OnlyIn(Dist.CLIENT)
    public IRenderer getRenderer(BlockState state) {
        return renderer;
    }
}
