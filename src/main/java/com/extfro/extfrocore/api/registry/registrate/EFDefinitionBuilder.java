package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class EFDefinitionBuilder<
        D extends EFDefinitionHolder<B, I, BE>,
        B extends Block,
        I extends Item,
        BE extends BlockEntity,
        SELF extends EFDefinitionBuilder<D, B, I, BE, SELF>> {

    protected final EFRegistrate registrate;
    protected final String name;
    protected final Function<ResourceLocation, D> definitionFactory;
    protected final BiFunction<BlockBehaviour.Properties, D, B> blockFactory;
    protected final BiFunction<B, Item.Properties, I> itemFactory;
    protected final Function<BlockEntityCreationInfo, BE> blockEntityFactory;

    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProperties = properties -> properties;
    private NonNullUnaryOperator<Item.Properties> itemProperties = properties -> properties;
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Nullable
    private Consumer<ItemBuilder<? extends Item, ?>> itemBuilder;
    private NonNullConsumer<BlockEntityType<BE>> onBlockEntityRegister = NonNullConsumer.noop();
    @Nullable
    private NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> blockstate;
    @Nullable
    private String langValue;
    protected D value;

    public EFDefinitionBuilder(
                               EFRegistrate registrate,
                               String name,
                               Function<ResourceLocation, D> definitionFactory,
                               BiFunction<BlockBehaviour.Properties, D, B> blockFactory,
                               BiFunction<B, Item.Properties, I> itemFactory,
                               Function<BlockEntityCreationInfo, BE> blockEntityFactory) {
        this.registrate = registrate;
        this.name = name;
        this.definitionFactory = definitionFactory;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
    }

    @SuppressWarnings("unchecked")
    protected SELF self() {
        return (SELF) this;
    }

    public SELF blockProperties(NonNullUnaryOperator<BlockBehaviour.Properties> blockProperties) {
        this.blockProperties = blockProperties;
        return self();
    }

    public SELF itemProperties(NonNullUnaryOperator<Item.Properties> itemProperties) {
        this.itemProperties = itemProperties;
        return self();
    }

    public SELF blockBuilder(Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        this.blockBuilder = blockBuilder;
        return self();
    }

    public SELF itemBuilder(Consumer<ItemBuilder<? extends Item, ?>> itemBuilder) {
        this.itemBuilder = itemBuilder;
        return self();
    }

    public SELF onBlockEntityRegister(NonNullConsumer<BlockEntityType<BE>> onBlockEntityRegister) {
        this.onBlockEntityRegister = onBlockEntityRegister;
        return self();
    }

    public SELF blockstate(NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> blockstate) {
        this.blockstate = blockstate;
        return self();
    }

    public SELF lang(String langValue) {
        this.langValue = langValue;
        return self();
    }

    protected D createDefinition() {
        return definitionFactory.apply(ResourceLocation.fromNamespaceAndPath(registrate.getModid(), name));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public D register() {
        registrate.object(name);
        D definition = createDefinition();

        BlockBuilder<B, ?> block = registrate.block(properties -> blockFactory.apply(properties, definition))
                .properties(blockProperties);
        if (blockstate != null) {
            block.setData(ProviderType.BLOCKSTATE, (ctx, prov) -> blockstate.accept((DataGenContext) ctx, prov));
        }
        if (langValue != null) {
            block.lang(langValue);
        }
        if (blockBuilder != null) {
            blockBuilder.accept(block);
        }
        BlockEntry<B> blockEntry = block.register();

        ItemBuilder<I, ?> item = registrate.item(properties -> itemFactory.apply(blockEntry.get(), properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .properties(itemProperties);
        if (itemBuilder != null) {
            itemBuilder.accept(item);
        }
        var itemEntry = item.register();

        BlockEntityBuilder<BE, ?> blockEntity = registrate
                .blockEntity((type, pos, state) -> blockEntityFactory.apply(new BlockEntityCreationInfo(type, pos, state)));
        blockEntity.onRegister(onBlockEntityRegister);
        blockEntity.validBlock(blockEntry);
        var blockEntityEntry = blockEntity.register();

        definition.setBlock(blockEntry);
        definition.setItem(itemEntry);
        definition.setBlockEntity(blockEntityEntry);
        return value = definition;
    }

    public D get() {
        return value;
    }
}
