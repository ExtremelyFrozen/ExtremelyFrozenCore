package com.extfro.extfrocore.api.item;

import com.extfro.extfrocore.api.item.component.IComponentCapability;
import com.extfro.extfrocore.api.item.component.IItemComponent;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.extensions.IItemExtension;

import java.util.List;

public interface IComponentItem extends ItemLike, IItemExtension {

    List<IItemComponent> getComponents();

    void attachComponents(IItemComponent... components);

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        for (IItemComponent component : getComponents()) {
            if (component instanceof IComponentCapability componentCapability) {
                componentCapability.attachCapabilities(event, this.asItem());
            }
        }
    }

    default void fillItemCategory(CreativeModeTab category, NonNullList<ItemStack> items) {}
}
