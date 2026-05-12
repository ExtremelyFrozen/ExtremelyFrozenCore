package com.extfro.extfrocore.api.sync_system.data_transformers.gtceu;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.registry.GTRegistries;
import com.extfro.extfrocore.api.sync_system.data_transformers.ValueTransformer;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Nullable;

public class CoverBehaviorTransformer implements ValueTransformer<CoverBehavior> {

    @Override
    public Tag serializeNBT(@Nullable CoverBehavior value,
                            CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        if (value != null) {
            return serialize(value, context.isClientSync(), context.lookup());
        }
        return new CompoundTag();
    }

    @Override
    public @Nullable CoverBehavior deserializeNBT(Tag tag,
                                                  CoverBehaviorTransformer.TransformerContext<CoverBehavior> context) {
        var compoundTag = ValueTransformer.assertTagType(CompoundTag.class, tag, context);
        if (context.holder() instanceof ICoverable coverable) {
            return deserialize(compoundTag, coverable, context.currentValue(), context.isClientSync(),
                    context.lookup());
        }
        ExtForCore.LOGGER.error("Sync: Object attempting to sync cover does not implement ICoverable {}", context);
        return null;
    }

    private CompoundTag serialize(CoverBehavior cover, boolean isSync, HolderLookup.Provider lookup) {
        var compound = new CompoundTag();

        compound.putInt("side", cover.attachedSide.ordinal());
        compound.putString("coverType", cover.coverDefinition.getId().toString());
        CompoundTag serializedCover = cover.getSyncDataHolder().serializeNBT(lookup, isSync);
        compound.put("data", serializedCover);

        return compound;
    }

    public @Nullable CoverBehavior deserialize(CompoundTag tag, ICoverable holder, @Nullable CoverBehavior cover,
                                               boolean isSync, HolderLookup.Provider lookup) {
        /// Ldlib backwards compat
        if (tag.contains("payload") && tag.contains("uid")) {
            tag.putInt("side", tag.getCompound("uid").getInt("side"));
            tag.putString("coverType", tag.getCompound("uid").getString("id"));
            tag.put("data", tag.getCompound("payload").getCompound("d"));
        }

        Direction side = Direction.values()[tag.getInt("side")];

        if (tag.isEmpty() || tag.getString("coverType").isEmpty()) {
            holder.setCoverAtSide(null, side);
            return null;
        }
        ResourceLocation coverType = ResourceLocation.tryParse(tag.getString("coverType"));
        if (cover == null || cover.coverDefinition.getId() != coverType) {
            var coverReg = GTRegistries.COVERS.get(coverType);
            if (coverReg == null) {
                ExtForCore.LOGGER.error("Error during NBT load: unknown cover type {} ({})", coverType,
                        tag.getString("coverType"));
                return null;
            }
            holder.setCoverAtSide(coverReg.createCoverBehavior(holder, side), side);
        }

        CoverBehavior newCover = holder.getCoverAtSide(side);
        if (newCover == null) return null;
        newCover.getSyncDataHolder().deserializeNBT(lookup, tag.getCompound("data"),
                isSync);

        if (!isSync && newCover.getAttachItem() == ItemStack.EMPTY) {
            ExtForCore.LOGGER.error("Invalid cover save state, this should never happen unless loading corrupted data.");
            holder.setCoverAtSide(null, side);
        }

        return newCover;
    }
}
