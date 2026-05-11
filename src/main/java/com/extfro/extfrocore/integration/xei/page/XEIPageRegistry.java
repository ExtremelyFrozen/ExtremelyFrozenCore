package com.extfro.extfrocore.integration.xei.page;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class XEIPageRegistry {

    private static final List<Supplier<? extends List<? extends XEIPage>>> PAGE_PROVIDERS = new ArrayList<>();
    private static final List<Consumer<XEIPage>> PAGE_REGISTRARS = new ArrayList<>();

    private XEIPageRegistry() {}

    public static void registerPageProvider(Supplier<? extends List<? extends XEIPage>> provider) {
        PAGE_PROVIDERS.add(provider);
    }

    public static void registerPageRegistrar(Consumer<XEIPage> registrar) {
        PAGE_REGISTRARS.add(registrar);
    }

    @Unmodifiable
    public static List<XEIPage> getPages() {
        List<XEIPage> pages = new ArrayList<>();
        for (Supplier<? extends List<? extends XEIPage>> provider : PAGE_PROVIDERS) {
            pages.addAll(provider.get());
        }
        return List.copyOf(pages);
    }

    public static void registerPages() {
        for (XEIPage page : getPages()) {
            for (Consumer<XEIPage> registrar : PAGE_REGISTRARS) {
                registrar.accept(page);
            }
        }
    }
}
