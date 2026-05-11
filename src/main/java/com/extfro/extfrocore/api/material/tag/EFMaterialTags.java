package com.extfro.extfrocore.api.material.tag;

import com.extfro.extfrocore.api.material.info.EFMaterialIconType;

public final class EFMaterialTags {

    public static final long UNIT = 3_628_800L;

    public static final EFMaterialTag DUST = new EFMaterialTag("dust", EFMaterialIconType.dust, UNIT);
    public static final EFMaterialTag INGOT = new EFMaterialTag("ingot", EFMaterialIconType.ingot, UNIT);
    public static final EFMaterialTag GEM = new EFMaterialTag("gem", EFMaterialIconType.gem, UNIT);
    public static final EFMaterialTag NUGGET = new EFMaterialTag("nugget", EFMaterialIconType.nugget, UNIT / 9);
    public static final EFMaterialTag PLATE = new EFMaterialTag("plate", EFMaterialIconType.plate, UNIT);
    public static final EFMaterialTag BLOCK = new EFMaterialTag("block", EFMaterialIconType.block, UNIT * 9);

    private EFMaterialTags() {}
}
