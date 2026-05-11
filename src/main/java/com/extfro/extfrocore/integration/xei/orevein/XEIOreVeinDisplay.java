package com.extfro.extfrocore.integration.xei.orevein;

import com.extfro.extfrocore.api.data.worldgen.OreDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockfluid.BedrockFluidDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.BedrockOreDefinition;
import com.extfro.extfrocore.api.data.worldgen.bedrockore.WeightedMaterial;
import com.extfro.extfrocore.api.data.worldgen.generator.VeinGenerator;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.material.Fluid;

import java.util.List;
import java.util.Set;

public sealed interface XEIOreVeinDisplay permits XEIOreVeinDisplay.OreVein,
                                          XEIOreVeinDisplay.BedrockOreVein, XEIOreVeinDisplay.BedrockFluidVein {

    ResourceLocation id();

    Type type();

    String translationKey();

    int weight();

    Set<ResourceKey<Level>> dimensions();

    boolean canGenerate();

    enum Type {
        ORE,
        BEDROCK_ORE,
        BEDROCK_FLUID
    }

    record OreVein(
                   ResourceLocation id,
                   OreDefinition definition,
                   String translationKey,
                   int weight,
                   Set<ResourceKey<Level>> dimensions,
                   HeightRangePlacement heightRange,
                   float density,
                   List<WeightedItemStack> outputs)
            implements XEIOreVeinDisplay {

        public static OreVein of(ResourceLocation id, OreDefinition definition) {
            return new OreVein(
                    id,
                    definition,
                    id.toLanguageKey("ore_vein"),
                    definition.weight(),
                    copyDimensions(definition.dimensionFilter()),
                    definition.heightRange(),
                    definition.density(),
                    itemOutputs(definition.veinGenerator()));
        }

        @Override
        public Type type() {
            return Type.ORE;
        }

        @Override
        public boolean canGenerate() {
            return definition.canGenerate();
        }
    }

    record BedrockOreVein(
                          ResourceLocation id,
                          BedrockOreDefinition definition,
                          String translationKey,
                          int weight,
                          Set<ResourceKey<Level>> dimensions,
                          int size,
                          int depletionAmount,
                          int depletionChance,
                          int depletedYield,
                          List<WeightedMaterial> materials)
            implements XEIOreVeinDisplay {

        public static BedrockOreVein of(ResourceLocation id, BedrockOreDefinition definition) {
            return new BedrockOreVein(
                    id,
                    definition,
                    id.toLanguageKey("bedrock_ore"),
                    definition.weight(),
                    copyDimensions(definition.dimensionFilter()),
                    definition.size(),
                    definition.depletionAmount(),
                    definition.depletionChance(),
                    definition.depletedYield(),
                    List.copyOf(definition.materials()));
        }

        @Override
        public Type type() {
            return Type.BEDROCK_ORE;
        }

        @Override
        public boolean canGenerate() {
            return definition.canGenerate();
        }
    }

    record BedrockFluidVein(
                            ResourceLocation id,
                            BedrockFluidDefinition definition,
                            String translationKey,
                            int weight,
                            Set<ResourceKey<Level>> dimensions,
                            int minimumYield,
                            int maximumYield,
                            int depletionAmount,
                            int depletionChance,
                            int depletedYield,
                            Fluid fluid)
            implements XEIOreVeinDisplay {

        public static BedrockFluidVein of(ResourceLocation id, BedrockFluidDefinition definition) {
            return new BedrockFluidVein(
                    id,
                    definition,
                    id.toLanguageKey("bedrock_fluid"),
                    definition.getWeight(),
                    copyDimensions(definition.getDimensionFilter()),
                    definition.getMinimumYield(),
                    definition.getMaximumYield(),
                    definition.getDepletionAmount(),
                    definition.getDepletionChance(),
                    definition.getDepletedYield(),
                    definition.getStoredFluid());
        }

        @Override
        public Type type() {
            return Type.BEDROCK_FLUID;
        }

        @Override
        public boolean canGenerate() {
            return definition.canGenerate();
        }
    }

    record WeightedItemStack(ItemStack stack, int weight) {}

    private static Set<ResourceKey<Level>> copyDimensions(Set<ResourceKey<Level>> dimensions) {
        return dimensions == null ? Set.of() : Set.copyOf(dimensions);
    }

    private static List<WeightedItemStack> itemOutputs(VeinGenerator generator) {
        return generator.getAllEntries().stream()
                .map(entry -> new WeightedItemStack(entry.state().getBlock().asItem().getDefaultInstance(),
                        entry.chance()))
                .filter(output -> !output.stack().isEmpty())
                .toList();
    }
}
