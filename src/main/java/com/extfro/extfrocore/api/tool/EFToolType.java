package com.extfro.extfrocore.api.tool;

import java.util.LinkedHashMap;
import java.util.Map;

public record EFToolType(String name) {

    private static final Map<String, EFToolType> TYPES = new LinkedHashMap<>();

    public static final EFToolType SWORD = create("sword");
    public static final EFToolType PICKAXE = create("pickaxe");
    public static final EFToolType SHOVEL = create("shovel");
    public static final EFToolType AXE = create("axe");
    public static final EFToolType HOE = create("hoe");
    public static final EFToolType MINING_HAMMER = create("mining_hammer");
    public static final EFToolType SPADE = create("spade");
    public static final EFToolType SAW = create("saw");
    public static final EFToolType HARD_HAMMER = create("hard_hammer");
    public static final EFToolType WRENCH = create("wrench");
    public static final EFToolType FILE = create("file");
    public static final EFToolType CROWBAR = create("crowbar");
    public static final EFToolType SCREWDRIVER = create("screwdriver");
    public static final EFToolType WIRE_CUTTER = create("wire_cutter");
    public static final EFToolType SCYTHE = create("scythe");
    public static final EFToolType KNIFE = create("knife");
    public static final EFToolType BUTCHERY_KNIFE = create("butchery_knife");
    public static final EFToolType DRILL_LV = create("drill_lv");
    public static final EFToolType DRILL_MV = create("drill_mv");
    public static final EFToolType DRILL_HV = create("drill_hv");
    public static final EFToolType DRILL_EV = create("drill_ev");
    public static final EFToolType DRILL_IV = create("drill_iv");
    public static final EFToolType CHAINSAW_LV = create("chainsaw_lv");
    public static final EFToolType CHAINSAW_HV = create("chainsaw_hv");
    public static final EFToolType CHAINSAW_IV = create("chainsaw_iv");
    public static final EFToolType WRENCH_LV = create("wrench_lv");
    public static final EFToolType WRENCH_HV = create("wrench_hv");
    public static final EFToolType WRENCH_IV = create("wrench_iv");
    public static final EFToolType BUZZSAW = create("buzzsaw");
    public static final EFToolType SCREWDRIVER_LV = create("screwdriver_lv");
    public static final EFToolType SCREWDRIVER_HV = create("screwdriver_hv");
    public static final EFToolType SCREWDRIVER_IV = create("screwdriver_iv");
    public static final EFToolType WIRE_CUTTER_LV = create("wire_cutter_lv");
    public static final EFToolType WIRE_CUTTER_HV = create("wire_cutter_hv");
    public static final EFToolType WIRE_CUTTER_IV = create("wire_cutter_iv");

    public EFToolType {
        TYPES.put(name, this);
    }

    public static EFToolType create(String name) {
        return new EFToolType(name);
    }

    public static Map<String, EFToolType> getTypes() {
        return TYPES;
    }
}
