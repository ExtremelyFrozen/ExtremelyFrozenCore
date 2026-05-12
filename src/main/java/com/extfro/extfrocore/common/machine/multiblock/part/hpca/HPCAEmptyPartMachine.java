package com.extfro.extfrocore.common.machine.multiblock.part.hpca;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComponentTrait;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;

public class HPCAEmptyPartMachine extends HPCAComponentPartMachine {

    public HPCAEmptyPartMachine(BlockEntityCreationInfo info) {
        super(info, new HPCAComponentTrait(0, 0, false, false));
    }

    @Override
    public boolean isAdvanced() {
        return false;
    }

    @Override
    public SpriteTexture getComponentIcon() {
        return GuiTextures.HPCA_ICON_EMPTY_COMPONENT;
    }
}
