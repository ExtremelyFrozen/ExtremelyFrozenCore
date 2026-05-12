package com.extfro.extfrocore.common.data;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.gui.factory.CoverUIFactory;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.lowdragmc.lowdraglib2.gui.holder.ModularUIContainerMenu;

public final class GTMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU,
            ExtForCore.MOD_ID);

    public static final DeferredHolder<MenuType<?>, MenuType<ModularUIContainerMenu>> COVER_UI = MENUS
            .register("cover_ui", () -> IMenuTypeExtension.create(CoverUIFactory::create));

    private GTMenuTypes() {}

    public static void init(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
