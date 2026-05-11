package com.extfro.extfrocore.api.machine.multiblock;

import net.minecraft.world.level.block.Block;

public interface PartAbility {

    default void register(int tier, Block block) {}
}
