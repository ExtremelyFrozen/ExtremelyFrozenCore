package com.extfro.extfrocore.integration.xei.page;

import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class XEIPageRegistry {

    private static final List<Supplier<? extends List<? extends XEIPage>>> PAGE_PROVIDERS = new ArrayList<>();
    private static final List<Consumer<XEIPage>> PAGE_REGISTRARS = new ArrayList<>();
    private static final List<Supplier<? extends List<? extends XEIPageDefinition<?>>>> DEFINITION_PROVIDERS =
            new ArrayList<>();

    private XEIPageRegistry() {}

    public static void registerPageProvider(Supplier<? extends List<? extends XEIPage>> provider) {
        PAGE_PROVIDERS.add(Objects.requireNonNull(provider, "provider"));
    }

    public static void registerPageDefinition(XEIPageDefinition<?> definition) {
        registerPageDefinitions(() -> List.of(definition));
    }

    public static void registerPageDefinitions(Supplier<? extends List<? extends XEIPageDefinition<?>>> provider) {
        DEFINITION_PROVIDERS.add(Objects.requireNonNull(provider, "provider"));
    }

    public static void registerPageRegistrar(Consumer<XEIPage> registrar) {
        PAGE_REGISTRARS.add(Objects.requireNonNull(registrar, "registrar"));
    }

    @Unmodifiable
    public static List<XEIPage> getPages() {
        List<XEIPage> pages = new ArrayList<>();
        pages.addAll(getPageDefinitions());
        for (Supplier<? extends List<? extends XEIPage>> provider : PAGE_PROVIDERS) {
            pages.addAll(provider.get());
        }
        return List.copyOf(pages);
    }

    @Unmodifiable
    public static List<XEIPageDefinition<?>> getPageDefinitions() {
        List<XEIPageDefinition<?>> definitions = new ArrayList<>();
        for (Supplier<? extends List<? extends XEIPageDefinition<?>>> provider : DEFINITION_PROVIDERS) {
            definitions.addAll(provider.get());
        }
        return List.copyOf(definitions);
    }

    public static void registerPages() {
        for (XEIPage page : getPages()) {
            for (Consumer<XEIPage> registrar : PAGE_REGISTRARS) {
                registrar.accept(page);
            }
        }
    }
}
