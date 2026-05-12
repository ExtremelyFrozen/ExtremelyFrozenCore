package com.extfro.extfrocore.integration.kjs.builders.machine;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.registry.registrate.GTRegistrate;
import com.extfro.extfrocore.api.registry.registrate.MachineBuilder;
import com.extfro.extfrocore.api.registry.registrate.MultiblockMachineBuilder;
import com.extfro.extfrocore.common.data.machines.GTMachineUtils;
import com.extfro.extfrocore.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;

import com.google.common.base.Preconditions;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.generator.KubeAssetGenerator;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

import static com.extfro.extfrocore.api.EFValues.*;

@Accessors(fluent = true, chain = true)
public class KJSTieredMultiblockBuilder extends BuilderBase<@Nullable MultiblockMachineDefinition @NotNull []>
                                        implements IMachineBuilderKJS {

    private final MultiblockMachineBuilder[] builders = new MultiblockMachineBuilder[TIER_COUNT];

    @Setter
    public transient int[] tiers = GTMachineUtils.ELECTRIC_TIERS;
    @Setter
    public transient TieredCreationFunction machine;
    @Setter
    public transient DefinitionFunction definition = (tier, def) -> def.tier(tier);

    public KJSTieredMultiblockBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.dummyBuilder = true;
    }

    public KJSTieredMultiblockBuilder(ResourceLocation id, TieredCreationFunction machine) {
        super(GTResourceLocation.implicitAsGtceu(id));
        this.machine = machine;
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
    public String getTranslationKeyGroup() {
        return "block";
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        for (int tier : tiers) {
            MachineDefinition def = object[tier];
            if (def != null && def.getLangValue() != null) {
                lang.add(def.getId().getNamespace(), def.getDescriptionId(), def.getLangValue());
            }
        }
    }

    @Override
    public @Nullable MultiblockMachineDefinition @NotNull [] createObject() {
        Preconditions.checkNotNull(tiers, "Tiers can't be null!");
        Preconditions.checkArgument(tiers.length > 0, "tiers must have at least one tier!");
        Preconditions.checkNotNull(machine, "You must set a machine creation function! " +
                "example: `builder.machine((holder, tier) => new SimpleTieredMachine(holder, tier, t => t * 3200)`");
        Preconditions.checkNotNull(definition, "You must set a definition function! " +
                "See GTMachines for examples");
        MultiblockMachineDefinition[] definitions = new MultiblockMachineDefinition[TIER_COUNT];
        for (final int tier : tiers) {
            String tierName = VN[tier].toLowerCase(Locale.ROOT);
            MultiblockMachineBuilder<MultiblockMachineDefinition, ?> builder = GTRegistrate
                    .createIgnoringListenerErrors(this.id.getNamespace())
                    .multiblock(String.format("%s_%s", tierName, this.id.getPath()),
                            holder -> machine.create(holder, tier));

            builder.workableTieredHullModel(id.withPrefix("block/machines/"))
                    .tier(tier);
            this.definition.apply(tier, builder);
            this.builders[tier] = builder;
            definitions[tier] = builder.register();
        }
        return definitions;
    }

    @FunctionalInterface
    public interface TieredCreationFunction {

        MultiblockControllerMachine create(BlockEntityCreationInfo info, int tier);
    }

    @FunctionalInterface
    public interface DefinitionFunction {

        void apply(int tier, MachineBuilder<?, ?> builder);
    }
}
