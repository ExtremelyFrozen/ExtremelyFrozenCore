package com.extfro.extfrocore.api.material.info;

import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.property.EFMaterialPropertyKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EFMaterialFlags {

    private final Set<EFMaterialFlag> flags = new HashSet<>();

    public EFMaterialFlags addFlags(EFMaterialFlag... flags) {
        this.flags.addAll(Arrays.asList(flags));
        return this;
    }

    public void verify(EFMaterial material) {
        flags.addAll(flags.stream()
                .map(flag -> flag.verifyFlag(material))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet()));
    }

    public boolean hasFlag(EFMaterialFlag flag) {
        return flags.contains(flag);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        flags.forEach(flag -> builder.append(flag).append('\n'));
        return builder.toString();
    }

    @Deprecated
    public static final EFMaterialFlag NO_UNIFICATION = new EFMaterialFlag.Builder("no_unification").build();
    public static final EFMaterialFlag DISABLE_MATERIAL_RECIPES = new EFMaterialFlag.Builder("disable_material_recipes").build();
    public static final EFMaterialFlag DECOMPOSITION_BY_ELECTROLYZING = new EFMaterialFlag.Builder("decomposition_by_electrolyzing").build();
    public static final EFMaterialFlag DECOMPOSITION_BY_CENTRIFUGING = new EFMaterialFlag.Builder("decomposition_by_centrifuging").build();
    public static final EFMaterialFlag DISABLE_DECOMPOSITION = new EFMaterialFlag.Builder("disable_decomposition").build();
    public static final EFMaterialFlag EXPLOSIVE = new EFMaterialFlag.Builder("explosive").build();
    public static final EFMaterialFlag FLAMMABLE = new EFMaterialFlag.Builder("flammable").build();
    public static final EFMaterialFlag STICKY = new EFMaterialFlag.Builder("sticky").build();
    public static final EFMaterialFlag PHOSPHORESCENT = new EFMaterialFlag.Builder("phosphorescent").build();
    public static final EFMaterialFlag FIRE_RESISTANT = new EFMaterialFlag.Builder("fire_resistant").build();

    public static final EFMaterialFlag GENERATE_PLATE = new EFMaterialFlag.Builder("generate_plate")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_DENSE = new EFMaterialFlag.Builder("generate_dense")
            .requireFlags(GENERATE_PLATE)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_ROD = new EFMaterialFlag.Builder("generate_rod")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_BOLT_SCREW = new EFMaterialFlag.Builder("generate_bolt_screw")
            .requireFlags(GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_FRAME = new EFMaterialFlag.Builder("generate_frame")
            .requireFlags(GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_GEAR = new EFMaterialFlag.Builder("generate_gear")
            .requireFlags(GENERATE_PLATE, GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag GENERATE_LONG_ROD = new EFMaterialFlag.Builder("generate_long_rod")
            .requireFlags(GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag FORCE_GENERATE_BLOCK = new EFMaterialFlag.Builder("force_generate_block")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag EXCLUDE_BLOCK_CRAFTING_RECIPES = new EFMaterialFlag.Builder("exclude_block_crafting_recipes")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag EXCLUDE_PLATE_COMPRESSOR_RECIPE = new EFMaterialFlag.Builder("exclude_plate_compressor_recipe")
            .requireFlags(GENERATE_PLATE)
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag EXCLUDE_BLOCK_CRAFTING_BY_HAND_RECIPES = new EFMaterialFlag.Builder("exclude_block_crafting_by_hand_recipes")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag MORTAR_GRINDABLE = new EFMaterialFlag.Builder("mortar_grindable")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag NO_WORKING = new EFMaterialFlag.Builder("no_working")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag NO_SMASHING = new EFMaterialFlag.Builder("no_smashing")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag NO_SMELTING = new EFMaterialFlag.Builder("no_smelting")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag NO_ORE_SMELTING = new EFMaterialFlag.Builder("no_ore_smelting")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag NO_ORE_PROCESSING_TAB = new EFMaterialFlag.Builder("no_ore_processing_tab")
            .requireProps(EFMaterialPropertyKey.ORE)
            .build();
    public static final EFMaterialFlag BLAST_FURNACE_CALCITE_DOUBLE = new EFMaterialFlag.Builder("blast_furnace_calcite_double")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag BLAST_FURNACE_CALCITE_TRIPLE = new EFMaterialFlag.Builder("blast_furnace_calcite_triple")
            .requireProps(EFMaterialPropertyKey.DUST)
            .build();
    public static final EFMaterialFlag DISABLE_ALLOY_BLAST = new EFMaterialFlag.Builder("disable_alloy_blast")
            .requireProps(EFMaterialPropertyKey.BLAST, EFMaterialPropertyKey.FLUID)
            .build();
    public static final EFMaterialFlag DISABLE_ALLOY_PROPERTY = new EFMaterialFlag.Builder("disable_alloy_property")
            .requireProps(EFMaterialPropertyKey.BLAST, EFMaterialPropertyKey.FLUID)
            .requireFlags(DISABLE_ALLOY_BLAST)
            .build();
    public static final EFMaterialFlag SOLDER_MATERIAL = new EFMaterialFlag.Builder("solder_material")
            .requireProps(EFMaterialPropertyKey.FLUID)
            .build();
    public static final EFMaterialFlag SOLDER_MATERIAL_BAD = new EFMaterialFlag.Builder("solder_material_bad")
            .requireProps(EFMaterialPropertyKey.FLUID)
            .build();
    public static final EFMaterialFlag SOLDER_MATERIAL_GOOD = new EFMaterialFlag.Builder("solder_material_good")
            .requireProps(EFMaterialPropertyKey.FLUID)
            .build();
    public static final EFMaterialFlag GENERATE_FOIL = new EFMaterialFlag.Builder("generate_foil")
            .requireFlags(GENERATE_PLATE)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_RING = new EFMaterialFlag.Builder("generate_ring")
            .requireFlags(GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_SPRING = new EFMaterialFlag.Builder("generate_spring")
            .requireFlags(GENERATE_LONG_ROD)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_SPRING_SMALL = new EFMaterialFlag.Builder("generate_spring_small")
            .requireFlags(GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_SMALL_GEAR = new EFMaterialFlag.Builder("generate_small_gear")
            .requireFlags(GENERATE_PLATE, GENERATE_ROD)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_FINE_WIRE = new EFMaterialFlag.Builder("generate_fine_wire")
            .requireFlags(GENERATE_FOIL)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_ROTOR = new EFMaterialFlag.Builder("generate_rotor")
            .requireFlags(GENERATE_BOLT_SCREW, GENERATE_RING, GENERATE_PLATE)
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag GENERATE_ROUND = new EFMaterialFlag.Builder("generate_round")
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag IS_MAGNETIC = new EFMaterialFlag.Builder("is_magnetic")
            .requireProps(EFMaterialPropertyKey.INGOT)
            .build();
    public static final EFMaterialFlag CRYSTALLIZABLE = new EFMaterialFlag.Builder("crystallizable")
            .requireProps(EFMaterialPropertyKey.GEM)
            .build();
    public static final EFMaterialFlag GENERATE_LENS = new EFMaterialFlag.Builder("generate_lens")
            .requireFlags(GENERATE_PLATE)
            .requireProps(EFMaterialPropertyKey.GEM)
            .build();
    public static final EFMaterialFlag HIGH_SIFTER_OUTPUT = new EFMaterialFlag.Builder("high_sifter_output")
            .requireProps(EFMaterialPropertyKey.GEM, EFMaterialPropertyKey.ORE)
            .build();
}
