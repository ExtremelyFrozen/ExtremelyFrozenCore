package com.extfro.extfrocore.integration.kjs.builders.prefix;

import com.extfro.extfrocore.api.block.OreBlock;
import com.extfro.extfrocore.api.data.chemical.material.Material;
import com.extfro.extfrocore.api.data.chemical.material.info.MaterialIconType;
import com.extfro.extfrocore.api.data.tag.TagPrefix;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.integration.kjs.helpers.GTResourceLocation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.function.Supplier;

import static com.extfro.extfrocore.api.data.tag.TagPrefix.Conditions.hasOreProperty;
import static com.extfro.extfrocore.integration.kjs.Validator.*;

@Accessors(fluent = true, chain = true)
public class OreTagPrefixBuilder extends TagPrefixBuilder {

    @Setter
    public transient Supplier<BlockState> stateSupplier;
    @Setter
    public transient Supplier<Material> materialSupplier;
    @Setter
    public transient ResourceLocation baseModelLocation;
    @Setter
    public transient Supplier<BlockBehaviour.Properties> templateProperties;
    @Setter
    public transient boolean doubleDrops = false;
    @Setter
    public transient boolean isSand = false;
    @Setter
    public transient boolean shouldDropAsItem = false;

    public OreTagPrefixBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
    }

    @Override
    public TagPrefix create(String id) {
        return new TagPrefix(id)
                .defaultTagPath("ores/%s")
                .prefixOnlyTagPath("ores_in_ground/%s")
                .unformattedTagPath("ores")
                .materialIconType(MaterialIconType.ore)
                .unificationEnabled(true)
                .blockConstructor(OreBlock::new)
                .generationCondition(hasOreProperty);
    }

    @Override
    public TagPrefix createObject() {
        validate(this.id,
                errorIfNull(stateSupplier, "stateSupplier"),
                onlySetDefault(templateProperties, () -> {
                    templateProperties = () -> GTBlocks.copy(stateSupplier.get().getBlock().properties(),
                            BlockBehaviour.Properties.of());
                }),
                errorIfNull(baseModelLocation, "baseModelLocation"));

        return base.registerOre(stateSupplier, materialSupplier, templateProperties, baseModelLocation,
                doubleDrops, isSand, shouldDropAsItem);
    }
}
