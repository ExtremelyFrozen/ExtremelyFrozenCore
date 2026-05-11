package com.extfro.extfrocore.api.block;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.tag.EFMaterialTag;
import com.extfro.extfrocore.client.renderer.block.EFMaterialBlockRenderer;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

public class EFMaterialBlock extends Block {

    protected final EFMaterialTag materialTag;
    protected final EFMaterial material;

    public EFMaterialBlock(Properties properties, EFMaterialTag materialTag, EFMaterial material) {
        super(properties);
        this.materialTag = materialTag;
        this.material = material;
        if (ExtForCore.isClientSide()) {
            registerModel();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void registerModel() {
        EFMaterialBlockRenderer.create(this, materialTag.materialIconType(), material.getMaterialIconSet());
    }

    public static BlockColor tintColor() {
        return (state, level, pos, tintIndex) -> {
            if (state.getBlock() instanceof EFMaterialBlock block) {
                return block.material.getLayerARGB(tintIndex);
            }
            return -1;
        };
    }

    @Override
    public String getDescriptionId() {
        return materialTag.getUnlocalizedName(material);
    }

    @Override
    public MutableComponent getName() {
        return materialTag.getLocalizedName(material);
    }
}
