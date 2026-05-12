package com.extfro.extfrocore.common.machine.multiblock.part.hpca;

import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCAComponentTrait;
import com.extfro.extfrocore.common.machine.trait.hpca.HPCACoolantProviderTrait;

import com.lowdragmc.lowdraglib2.gui.texture.ResourceTexture;
import lombok.Getter;

public class HPCACoolerPartMachine extends HPCAComponentPartMachine {

    @Getter
    private final boolean advanced;

    public HPCACoolerPartMachine(BlockEntityCreationInfo info, boolean advanced) {
        super(info, createHPCATrait(advanced));
        this.advanced = advanced;
    }

    public static HPCAComponentTrait createHPCATrait(boolean isAdvanced) {
        int upkeepEU = isAdvanced ? EFValues.VA[EFValues.IV] : 0;
        int coolingAmount = isAdvanced ? 2 : 1;
        int maxCoolant = isAdvanced ? 8 : 0;
        return new HPCACoolantProviderTrait(upkeepEU, upkeepEU, false, false, coolingAmount, maxCoolant,
                isAdvanced);
    }

    @Override
    public ResourceTexture getComponentIcon() {
        return advanced ? GuiTextures.HPCA_ICON_ACTIVE_COOLER_COMPONENT : GuiTextures.HPCA_ICON_HEAT_SINK_COMPONENT;
    }
}
