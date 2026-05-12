package com.extfro.extfrocore.common.machine.multiblock.part.hpca;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComponentTrait;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComputationProviderTrait;

import net.minecraft.MethodsReturnNonnullByDefault;

import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import lombok.Getter;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class HPCAComputationPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCAComputationPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, createHPCATrait(advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(boolean isAdvanced) {
        int upkeepEUt = EFValues.VA[isAdvanced ? EFValues.IV : EFValues.EV];
        int maxEUt = EFValues.VA[isAdvanced ? EFValues.ZPM : EFValues.LuV];
        int cooling = isAdvanced ? 4 : 2;
        int cwu = isAdvanced ? 16 : 4;
        return new HPCAComputationProviderTrait(upkeepEUt, maxEUt, true, false, cwu, cooling);
    }

    @Override
    public SpriteTexture getComponentIcon() {
        if (hpcaComponentTrait.isDamaged()) {
            return advanced ? GuiTextures.HPCA_ICON_DAMAGED_ADVANCED_COMPUTATION_COMPONENT :
                    GuiTextures.HPCA_ICON_DAMAGED_COMPUTATION_COMPONENT;
        }
        return advanced ? GuiTextures.HPCA_ICON_ADVANCED_COMPUTATION_COMPONENT :
                GuiTextures.HPCA_ICON_COMPUTATION_COMPONENT;
    }
}
