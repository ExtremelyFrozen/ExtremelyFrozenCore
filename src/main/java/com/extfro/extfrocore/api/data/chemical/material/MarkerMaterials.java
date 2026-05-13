package com.extfro.extfrocore.api.data.chemical.material;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.world.item.DyeColor;

import com.google.common.collect.HashBiMap;

public class MarkerMaterials {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void register() {
        Color.Colorless.toString();
        Empty.toString();
    }

    /**
     * Marker materials without category
     */
    public static final MarkerMaterial Empty = new MarkerMaterial(ExtForCore.id("empty"));

    /**
     * Color materials
     */
    public static class Color {

        /**
         * Can be used only by direct specifying
         * Means absence of color on TagPrefix
         * Often a default value for color prefixes
         */
        public static final MarkerMaterial Colorless = new MarkerMaterial(ExtForCore.id("colorless"));

        public static final MarkerMaterial White = new MarkerMaterial(ExtForCore.id("white"));
        public static final MarkerMaterial Orange = new MarkerMaterial(ExtForCore.id("orange"));
        public static final MarkerMaterial Magenta = new MarkerMaterial(ExtForCore.id("magenta"));
        public static final MarkerMaterial LightBlue = new MarkerMaterial(ExtForCore.id("light_blue"));
        public static final MarkerMaterial Yellow = new MarkerMaterial(ExtForCore.id("yellow"));
        public static final MarkerMaterial Lime = new MarkerMaterial(ExtForCore.id("lime"));
        public static final MarkerMaterial Pink = new MarkerMaterial(ExtForCore.id("pink"));
        public static final MarkerMaterial Gray = new MarkerMaterial(ExtForCore.id("gray"));
        public static final MarkerMaterial LightGray = new MarkerMaterial(ExtForCore.id("light_gray"));
        public static final MarkerMaterial Cyan = new MarkerMaterial(ExtForCore.id("cyan"));
        public static final MarkerMaterial Purple = new MarkerMaterial(ExtForCore.id("purple"));
        public static final MarkerMaterial Blue = new MarkerMaterial(ExtForCore.id("blue"));
        public static final MarkerMaterial Brown = new MarkerMaterial(ExtForCore.id("brown"));
        public static final MarkerMaterial Green = new MarkerMaterial(ExtForCore.id("green"));
        public static final MarkerMaterial Red = new MarkerMaterial(ExtForCore.id("red"));
        public static final MarkerMaterial Black = new MarkerMaterial(ExtForCore.id("black"));

        /**
         * Arrays containing all possible color values (without Colorless!)
         */
        public static final MarkerMaterial[] VALUES = new MarkerMaterial[] {
                White, Orange, Magenta, LightBlue, Yellow, Lime, Pink, Gray, LightGray, Cyan, Purple, Blue, Brown,
                Green, Red, Black
        };

        /**
         * Gets color by it's name
         * Name format is equal to DyeColor
         */
        public static MarkerMaterial valueOf(String string) {
            for (MarkerMaterial color : VALUES) {
                if (color.getName().equals(string)) {
                    return color;
                }
            }
            return null;
        }

        /**
         * Contains associations between MC DyeColor and Color MarkerMaterial
         */
        public static final HashBiMap<DyeColor, MarkerMaterial> COLORS = HashBiMap.create();

        static {
            for (var color : DyeColor.values()) {
                COLORS.put(color, Color.valueOf(color.getName()));
            }
        }
    }
}
