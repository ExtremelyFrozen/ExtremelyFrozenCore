package com.extfro.extfrocore.api.registry.registrate;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BlockEntityBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import java.util.function.Supplier;

public class EFBlockBuilder<T extends Block, P> extends BlockBuilder<T, P> {

    public static <T extends Block, P> EFBlockBuilder<T, P> create(
                                                                   AbstractRegistrate<?> owner,
                                                                   P parent,
                                                                   String name,
                                                                   BuilderCallback callback,
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return new EFBlockBuilder<>(owner, parent, name, callback, factory, BlockBehaviour.Properties::of)
                .defaultBlockstate()
                .defaultLoot()
                .defaultLang();
    }

    protected EFBlockBuilder(
                             AbstractRegistrate<?> owner,
                             P parent,
                             String name,
                             BuilderCallback callback,
                             NonNullFunction<BlockBehaviour.Properties, T> factory,
                             NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, factory, initialProperties);
    }

    public EFBlockBuilder<T, P> exBlockstate(
                                             NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> cons) {
        return setDataGeneric(ProviderType.BLOCKSTATE, (ctx, prov) -> cons.accept(ctx, prov));
    }

    @Override
    public EFBlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> func) {
        return (EFBlockBuilder<T, P>) super.properties(func);
    }

    @Override
    public EFBlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
        return (EFBlockBuilder<T, P>) super.initialProperties(block);
    }

    @SuppressWarnings("removal")
    @Override
    public EFBlockBuilder<T, P> addLayer(Supplier<Supplier<RenderType>> layer) {
        return (EFBlockBuilder<T, P>) super.addLayer(layer);
    }

    @Override
    public EFBlockBuilder<T, P> simpleItem() {
        return (EFBlockBuilder<T, P>) super.simpleItem();
    }

    @Override
    public <BE extends BlockEntity> EFBlockBuilder<T, P> simpleBlockEntity(
                                                                           BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return (EFBlockBuilder<T, P>) super.simpleBlockEntity(factory);
    }

    @Override
    public <BE extends BlockEntity> BlockEntityBuilder<BE, BlockBuilder<T, P>> blockEntity(
                                                                                           BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return super.blockEntity(factory);
    }

    @Override
    public EFBlockBuilder<T, P> color(NonNullSupplier<Supplier<BlockColor>> colorHandler) {
        return (EFBlockBuilder<T, P>) super.color(colorHandler);
    }

    @Override
    public EFBlockBuilder<T, P> defaultBlockstate() {
        return (EFBlockBuilder<T, P>) super.defaultBlockstate();
    }

    @Override
    public EFBlockBuilder<T, P> blockstate(
                                           NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cons) {
        return (EFBlockBuilder<T, P>) setData(ProviderType.BLOCKSTATE, cons);
    }

    @Override
    public EFBlockBuilder<T, P> defaultLang() {
        return (EFBlockBuilder<T, P>) super.defaultLang();
    }

    @Override
    public EFBlockBuilder<T, P> lang(String name) {
        return (EFBlockBuilder<T, P>) super.lang(name);
    }

    @Override
    public EFBlockBuilder<T, P> defaultLoot() {
        return (EFBlockBuilder<T, P>) super.defaultLoot();
    }

    @Override
    public EFBlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, T> cons) {
        return (EFBlockBuilder<T, P>) super.loot(cons);
    }

    @Override
    public EFBlockBuilder<T, P> recipe(
                                       NonNullBiConsumer<DataGenContext<Block, T>, RegistrateRecipeProvider> cons) {
        return (EFBlockBuilder<T, P>) super.recipe(cons);
    }

    public <D extends RegistrateProvider> EFBlockBuilder<T, P> setDataGeneric(
                                                                              ProviderType<? extends D> type,
                                                                              NonNullBiConsumer<DataGenContext<Block, ? extends Block>, D> cons) {
        getOwner().setDataGenerator(this, type, prov -> cons.accept(DataGenContext.from(this), prov));
        return this;
    }

    @Override
    public <D extends RegistrateProvider> EFBlockBuilder<T, P> setData(
                                                                       ProviderType<? extends D> type,
                                                                       NonNullBiConsumer<DataGenContext<Block, T>, D> cons) {
        return (EFBlockBuilder<T, P>) super.setData(type, cons);
    }
}
