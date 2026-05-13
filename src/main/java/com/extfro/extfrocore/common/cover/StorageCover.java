package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.fancy.IFancyConfigurator;
import com.extfro.extfrocore.api.machine.MachineCoverContainer;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StorageCover extends CoverBehavior implements IUICover {

    @SaveField
    @SyncToClient
    public final CustomItemStackHandler inventory;
    private final int SIZE = 18;

    public StorageCover(@NotNull CoverDefinition definition, @NotNull ICoverable coverableView,
                        @NotNull Direction attachedSide) {
        super(definition, coverableView, attachedSide);
        inventory = new CustomItemStackHandler(SIZE) {

            @Override
            public int getSlotLimit(int slot) {
                return 1;
            }
        };

        inventory.setOnContentsChanged(() -> syncDataHolder.markClientSyncFieldDirty("inventory"));
    }

    @Override
    @NotNull
    public List<ItemStack> getAdditionalDrops() {
        var list = super.getAdditionalDrops();
        for (int slot = 0; slot < SIZE; slot++) {
            list.add(inventory.getStackInSlot(slot));
        }
        return list;
    }

    @Override
    public boolean canAttach() {
        if (!(coverHolder instanceof MachineCoverContainer)) return false;
        for (var dir : Direction.values()) {
            if (coverHolder.hasCover(dir) && coverHolder.getCoverAtSide(dir) instanceof StorageCover)
                return false;
        }
        return super.canAttach();
    }

    @Override
    public UIElement createUIElement() {
        final var group = new UIElement().layout(layout -> layout.width(126).height(87));

        Label title = new Label();
        title.setValue(Component.translatable(getUITitle()));
        title.layout(layout -> layout.left(10).top(5).width(110).height(10));
        title.textStyle(style -> style.textColor(0x404040).textShadow(false));
        group.addChild(title);

        addInventorySlots(group, 21);

        return group;
    }

    private String getUITitle() {
        return "cover.storage.title";
    }

    @Override
    public @Nullable IFancyConfigurator getConfigurator() {
        return new StorageCoverConfigurator();
    }

    private class StorageCoverConfigurator implements IFancyConfigurator {

        @Override
        public Component getTitle() {
            return Component.translatable("cover.storage.title");
        }

        @Override
        public IGuiTexture getIcon() {
            return GuiTextures.STORAGE_ICON;
        }

        @Override
        public UIElement createConfigurator() {
            final var group = new UIElement().layout(layout -> layout.width(126).height(66));

            addInventorySlots(group, 3);
            return group;
        }
    }

    private void addInventorySlots(UIElement group, int topOffset) {
        for (int slot = 0; slot < SIZE; slot++) {
            int left = 7 + (slot % 6) * 18;
            int top = topOffset + (slot / 6) * 18;
            ItemSlot itemSlot = new ItemSlot().bind(inventory, slot);
            itemSlot.layout(layout -> layout.left(left).top(top).width(18).height(18));
            itemSlot.style(style -> style.background(GuiTextures.SLOT));
            group.addChild(itemSlot);
        }
    }
}
