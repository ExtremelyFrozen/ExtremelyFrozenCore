package com.extfro.extfrocore.api.registry.registrate;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

public interface EFDefinitionHolder<B extends Block, I extends Item, BE extends net.minecraft.world.level.block.entity.BlockEntity> {

    ResourceLocation id();

    void setBlock(BlockEntry<B> block);

    void setItem(ItemEntry<I> item);

    void setBlockEntity(RegistryEntry<BlockEntityType<?>, BlockEntityType<BE>> blockEntity);
}
