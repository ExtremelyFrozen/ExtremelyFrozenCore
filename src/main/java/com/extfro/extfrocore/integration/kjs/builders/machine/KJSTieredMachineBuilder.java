package com.extfro.extfrocore.integration.kjs.builders.machine;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.registry.registrate.GTRegistrate;
import com.extfro.extfrocore.api.registry.registrate.MachineBuilder;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;
import com.extfro.extfrocore.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

import static com.extfro.extfrocore.api.EFValues.*;
import static com.extfro.extfrocore.utils.FormattingUtil.toEnglishName;

@Accessors(fluent = true, chain = true)
public class KJSTieredMachineBuilder extends BuilderBase<@Nullable MachineDefinition @NotNull []>
                                     implements IMachineBuilderKJS {

    private final MachineBuilder<?, ?>[] builders = new MachineBuilder[TIER_COUNT];

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);
    @Setter
    public transient Int2IntFunction tankScalingFunction = GTMachineUtils.defaultTankSizeFunction;
    @Setter
    public transient boolean addDefaultTooltips = true;
    @Setter
    public transient boolean addDefaultModel = true;
    @Setter
    public transient boolean isGenerator = false;

    public KJSTieredMachineBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.addDefaultTooltips = false;
        this.addDefaultModel = false;
        this.dummyBuilder = true;
    }

    public KJSTieredMachineBuilder(ResourceLocation id, TieredCreationFunction machine, boolean isGenerator) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.machine = machine;
        this.isGenerator = isGenerator;
        this.dummyBuilder = true;
    }

    @Override
    public void generateMachineModels() {
        for (int tier : this.tiers) {
            generateMachineModel(this.builders[tier], this.object[tier]);
        }
    }

    @Override
    public void generateAssets(KubeAssetGenerator generator) {
        for (int tier : this.tiers) {
            MachineDefinition definition = this.object[tier];
            if (definition == null) continue;

            final ResourceLocation id = definition.getId();
            generator.itemModel(id, gen -> gen.parent(id.withPrefix("block/machine/")));
        }
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        for (int tier : this.tiers) {
            MachineDefinition def = this.object[tier];
            if (def != null && def.getLangValue() != null) {
                lang.add(ExtForCore.MOD_ID, def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public @Nullable MachineDefinition @NotNull [] createObject() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");
        @Nullable
        MachineDefinition @NotNull [] definitions = new MachineDefinition[TIER_COUNT];
        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            final Int2IntFunction tankFunction = Objects.requireNonNullElse(tankScalingFunction,
                    GTMachineUtils.defaultTankSizeFunction);

            MachineBuilder<?, ?> builder = GTRegistrate.createIgnoringListenerErrors(this.id.getNamespace())
                    .machine(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.create(holder, tier, tankFunction));

            builder.langValue("%s %s %s".formatted(VLVH[tier], toEnglishName(this.id.getPath()), VLVT[tier]))
                    .tier(tier);
            if (this.addDefaultModel) {
                builder.workableTieredHullModel(id.withPrefix("block/machines/"));
            }
            this.definition.apply(tier, builder);

            if (builder.recipeTypes().length > 0) {
                GTRecipeType recipeType = builder.recipeTypes()[0];
                if (tankScalingFunction != null && addDefaultTooltips) {
                    builder.tooltips(
                            GTMachineUtils.workableTiered(tier, EFValues.V[tier], EFValues.V[tier] * 64, recipeType,
                                    tankScalingFunction.applyAsInt(tier), !isGenerator));
                }
            }

            this.builders[tier] = builder;
            definitions[tier] = builder.register();
        }
        return definitions;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MetaMachine create(BlockEntityCreationInfo info, int tier, Int2IntFunction tankScaling);
    }

    @FunctionalInterface
    public interface CreationFunction<T extends MetaMachine> {

        T create(BlockEntityCreationInfo info);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?, ?> builder);
    }
}
