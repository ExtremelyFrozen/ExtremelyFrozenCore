package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.item.CoverItem;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;

public final class CoverDefinitionHolder {

    private final ResourceLocation id;
    private RegistryEntry<CoverDefinition, CoverDefinition> definition;
    private ItemEntry<CoverItem> item;

    public CoverDefinitionHolder(ResourceLocation id) {
        this.id = id;
    }

    public ResourceLocation id() {
        return id;
    }

    public CoverDefinition get() {
        return definition.get();
    }

    public ItemEntry<CoverItem> item() {
        return item;
    }

    void setDefinition(RegistryEntry<CoverDefinition, CoverDefinition> definition) {
        this.definition = definition;
    }

    void setItem(ItemEntry<CoverItem> item) {
        this.item = item;
    }
}
