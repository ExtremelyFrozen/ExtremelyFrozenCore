package com.extfro.extfrocore.api.material.info;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EFMaterialIconSet {

    public static final Map<String, EFMaterialIconSet> ICON_SETS = new HashMap<>();
    public static final EFMaterialIconSet DULL = new EFMaterialIconSet("dull", null, true);
    public static final EFMaterialIconSet METALLIC = new EFMaterialIconSet("metallic");
    public static final EFMaterialIconSet MAGNETIC = new EFMaterialIconSet("magnetic", METALLIC);
    public static final EFMaterialIconSet SHINY = new EFMaterialIconSet("shiny", METALLIC);
    public static final EFMaterialIconSet BRIGHT = new EFMaterialIconSet("bright", SHINY);
    public static final EFMaterialIconSet DIAMOND = new EFMaterialIconSet("diamond", SHINY);
    public static final EFMaterialIconSet EMERALD = new EFMaterialIconSet("emerald", DIAMOND);
    public static final EFMaterialIconSet GEM_HORIZONTAL = new EFMaterialIconSet("gem_horizontal", EMERALD);
    public static final EFMaterialIconSet GEM_VERTICAL = new EFMaterialIconSet("gem_vertical", EMERALD);
    public static final EFMaterialIconSet RUBY = new EFMaterialIconSet("ruby", EMERALD);
    public static final EFMaterialIconSet OPAL = new EFMaterialIconSet("opal", RUBY);
    public static final EFMaterialIconSet GLASS = new EFMaterialIconSet("glass", RUBY);
    public static final EFMaterialIconSet NETHERSTAR = new EFMaterialIconSet("netherstar", GLASS);
    public static final EFMaterialIconSet FINE = new EFMaterialIconSet("fine");
    public static final EFMaterialIconSet SAND = new EFMaterialIconSet("sand", FINE);
    public static final EFMaterialIconSet WOOD = new EFMaterialIconSet("wood", FINE);
    public static final EFMaterialIconSet ROUGH = new EFMaterialIconSet("rough", FINE);
    public static final EFMaterialIconSet FLINT = new EFMaterialIconSet("flint", ROUGH);
    public static final EFMaterialIconSet LIGNITE = new EFMaterialIconSet("lignite", ROUGH);
    public static final EFMaterialIconSet QUARTZ = new EFMaterialIconSet("quartz", ROUGH);
    public static final EFMaterialIconSet CERTUS = new EFMaterialIconSet("certus", QUARTZ);
    public static final EFMaterialIconSet LAPIS = new EFMaterialIconSet("lapis", QUARTZ);
    public static final EFMaterialIconSet FLUID = new EFMaterialIconSet("fluid");
    public static final EFMaterialIconSet RADIOACTIVE = new EFMaterialIconSet("radioactive", METALLIC);

    private static int idCounter = 0;
    public final String name;
    public final int id;
    public final boolean isRootIconset;
    public final EFMaterialIconSet parentIconset;

    public EFMaterialIconSet(@NotNull String name) {
        this(name, DULL);
    }

    public EFMaterialIconSet(@NotNull String name, @NotNull EFMaterialIconSet parentIconset) {
        this(name, parentIconset, false);
    }

    public EFMaterialIconSet(@NotNull String name, @Nullable EFMaterialIconSet parentIconset, boolean isRootIconset) {
        this.name = name.toLowerCase(Locale.ENGLISH);
        Preconditions.checkArgument(!ICON_SETS.containsKey(this.name), "MaterialIconSet " + this.name + " exists");
        this.id = idCounter++;
        this.isRootIconset = isRootIconset;
        this.parentIconset = parentIconset;
        ICON_SETS.put(this.name, this);
    }

    public static EFMaterialIconSet getByName(@NotNull String name) {
        return ICON_SETS.get(name.toLowerCase(Locale.ENGLISH));
    }

    @Override
    public String toString() {
        return name;
    }
}
