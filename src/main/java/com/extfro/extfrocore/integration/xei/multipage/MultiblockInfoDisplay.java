package com.extfro.extfrocore.integration.xei.multipage;

import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public record MultiblockInfoDisplay(MultiblockMachineDefinition definition,
                                    @Unmodifiable List<MultiblockInfoPage> pages) {

    public MultiblockInfoDisplay {
        pages = List.copyOf(pages);
    }

    public static MultiblockInfoDisplay fromDefinition(MultiblockMachineDefinition definition) {
        List<MultiblockInfoPage> pages = new ArrayList<>();
        var shapes = definition.getMatchingShapes();
        for (int i = 0; i < shapes.size(); i++) {
            pages.add(new MultiblockInfoPage(i, shapes.get(i)));
        }
        return new MultiblockInfoDisplay(definition, pages);
    }

    public ResourceLocation id() {
        return definition.getId();
    }

    public Component title() {
        return Component.translatable(definition.getDescriptionId());
    }

    public ItemStack icon() {
        return definition.asStack();
    }

    public boolean hasPages() {
        return !pages.isEmpty();
    }
}
