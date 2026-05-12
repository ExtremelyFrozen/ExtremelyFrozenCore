package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.registry.GTRegistries;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditableMachineUI implements IEditableUI<UIElement, MetaMachine> {

    @Getter
    final String groupName;
    @Getter
    final ResourceLocation uiPath;
    final Supplier<UIElement> widgetSupplier;
    final BiConsumer<UIElement, MetaMachine> binder;
    @Nullable
    private CompoundTag customUICache;

    public EditableMachineUI(String groupName, ResourceLocation uiPath, Supplier<UIElement> widgetSupplier,
                             BiConsumer<UIElement, MetaMachine> binder) {
        this.groupName = groupName;
        this.uiPath = uiPath;
        this.widgetSupplier = widgetSupplier;
        this.binder = binder;
    }

    public UIElement createDefault() {
        return widgetSupplier.get();
    }

    public void setupUI(UIElement template, MetaMachine machine) {
        binder.accept(template, machine);
    }

    //////////////////////////////////////
    // ******** GUI *********//
    //////////////////////////////////////

    @Nullable
    public UIElement createCustomUI() {
        if (hasCustomUI()) {
            var nbt = getCustomUI();
            var group = new UIElement();
            group.deserializeNBT(GTRegistries.builtinRegistry(), nbt.getCompound("root"));
            group.layout(layout -> layout.left(0).top(0));
            return group;
        }
        return null;
    }

    public CompoundTag getCustomUI() {
        if (this.customUICache == null) {
            ResourceManager resourceManager = null;
            if (ExtForCore.isClientSide()) {
                resourceManager = Minecraft.getInstance().getResourceManager();
            } else if (ExtForCore.getMinecraftServer() != null) {
                resourceManager = ExtForCore.getMinecraftServer().getResourceManager();
            }
            if (resourceManager == null) {
                this.customUICache = new CompoundTag();
            } else {
                try {
                    var resource = resourceManager
                            .getResourceOrThrow(ResourceLocation.fromNamespaceAndPath(uiPath.getNamespace(),
                                    "ui/machine/%s.mui".formatted(uiPath.getPath())));
                    try (InputStream inputStream = resource.open()) {
                        try (DataInputStream dataInputStream = new DataInputStream(inputStream);) {
                            this.customUICache = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
                        }
                    }
                } catch (Exception e) {
                    this.customUICache = new CompoundTag();
                }
                if (this.customUICache == null) {
                    this.customUICache = new CompoundTag();
                }
            }
        }
        return this.customUICache;
    }

    public boolean hasCustomUI() {
        return !getCustomUI().isEmpty();
    }

    public void reloadCustomUI() {
        this.customUICache = null;
    }
}
