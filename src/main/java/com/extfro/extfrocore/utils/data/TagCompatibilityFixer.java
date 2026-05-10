package com.extfro.extfrocore.utils.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TagCompatibilityFixer {

    public static Tag stripLDLibPayloadWrapper(Tag tag) {
        if (!(tag instanceof CompoundTag compoundTag)) return tag;
        if (compoundTag.contains("p") && compoundTag.contains("t")) {
            return compoundTag.getCompound("p");
        }
        if (compoundTag.contains("t", Tag.TAG_COMPOUND)) {
            return compoundTag.getCompound("t").getCompound("p");
        }
        return compoundTag;
    }
}
