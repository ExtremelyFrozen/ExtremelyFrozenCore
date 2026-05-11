package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.item.CoverItem;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.client.renderer.cover.ICoverRenderer;
import com.extfro.extfrocore.client.renderer.cover.SimpleCoverRenderer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class CoverBuilder {

    protected final EFRegistrate registrate;
    protected final String name;
    protected final CoverDefinition.CoverBehaviourProvider behaviorCreator;
    protected Supplier<Supplier<ICoverRenderer>> coverRenderer;

    private NonNullUnaryOperator<Item.Properties> itemProperties = properties -> properties;
    @Nullable
    private Consumer<ItemBuilder<? extends CoverItem, ?>> itemBuilder;
    @Nullable
    private String langValue;
    @Nullable
    private ResourceLocation itemTexture;

    public CoverBuilder(EFRegistrate registrate, String name,
                        CoverDefinition.CoverBehaviourProvider behaviorCreator,
                        Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        this.registrate = registrate;
        this.name = name;
        this.behaviorCreator = behaviorCreator;
        this.coverRenderer = coverRenderer;
    }

    public CoverBuilder itemProperties(NonNullUnaryOperator<Item.Properties> itemProperties) {
        this.itemProperties = itemProperties;
        return this;
    }

    public CoverBuilder itemBuilder(@Nullable Consumer<ItemBuilder<? extends CoverItem, ?>> itemBuilder) {
        this.itemBuilder = itemBuilder;
        return this;
    }

    public CoverBuilder lang(String langValue) {
        this.langValue = langValue;
        return this;
    }

    public CoverBuilder itemTexture(ResourceLocation itemTexture) {
        this.itemTexture = itemTexture;
        return this;
    }

    public CoverBuilder renderer(Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        this.coverRenderer = coverRenderer;
        return this;
    }

    public CoverDefinitionHolder register() {
        CoverDefinitionHolder holder = new CoverDefinitionHolder(registrate.makeResourceLocation(name));
        CoverDefinition definition = new CoverDefinition(holder.id(), behaviorCreator, coverRenderer);
        var definitionEntry = registrate.simple(name, EFRegistries.COVER_REGISTRY, () -> definition);
        holder.setDefinition(definitionEntry);

        ItemBuilder<CoverItem, ?> item = registrate.item(name, properties -> new CoverItem(properties, definition))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model((ctx, provider) -> provider.generated(ctx::getEntry, itemTexture != null ? itemTexture :
                        registrate.makeResourceLocation("item/cover/" + name)))
                .properties(itemProperties);
        if (langValue != null) {
            item.lang(langValue);
        }
        if (itemBuilder != null) {
            itemBuilder.accept(item);
        }
        holder.setItem(item.register());
        definition.setItem(holder.item());
        return holder;
    }

    public static Supplier<Supplier<ICoverRenderer>> simpleCoverRenderer(EFRegistrate registrate, String name) {
        return () -> () -> new SimpleCoverRenderer(registrate.makeResourceLocation("block/cover/" + name));
    }

    static String tieredName(String name, String tierName) {
        return name + "." + tierName.toLowerCase(Locale.ROOT);
    }
}
