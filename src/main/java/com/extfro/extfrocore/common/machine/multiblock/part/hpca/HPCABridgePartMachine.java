package com.extfro.extfrocore.common.machine.multiblock.part.hpca;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComponentTrait;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;

public class HPCABridgePartMachine extends HPCAComponentPartMachine {

    public HPCABridgePartMachine(BlockEntityCreationInfo info) {
        super(info, new HPCAComponentTrait(EFValues.VA[EFValues.IV], EFValues.VA[EFValues.IV], false, true));
    }

    @Override
    public boolean isAdvanced() {
        return true;
    }

    @Override
    public SpriteTexture getComponentIcon() {
        return GuiTextures.HPCA_ICON_BRIDGE_COMPONENT;
    }
}
