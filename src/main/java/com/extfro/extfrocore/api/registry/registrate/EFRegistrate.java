package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.block.EFMaterialBlock;
import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.item.EFMaterialBlockItem;
import com.extfro.extfrocore.api.item.EFMaterialItem;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.material.EFMaterial;
import com.extfro.extfrocore.api.material.info.EFMaterialIconType;
import com.extfro.extfrocore.api.material.tag.EFMaterialTag;
import com.extfro.extfrocore.api.registry.EFRegistries;
import com.extfro.extfrocore.api.sound.EFSoundEntry;
import com.extfro.extfrocore.client.renderer.cover.ICoverRenderer;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.RegisterEvent;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

public class EFRegistrate extends AbstractRegistrate<EFRegistrate> {

    private static final Map<String, EFRegistrate> EXISTING_REGISTRATES = new java.util.HashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    private RegistryEntry<CreativeModeTab, CreativeModeTab> currentTab;
    private final List<EFSoundEntry> soundEntries = new ArrayList<>();
    private static final Map<RegistryEntry<?, ?>, RegistryEntry<CreativeModeTab, CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    protected EFRegistrate(String modId) {
        super(modId);
    }

    public static EFRegistrate create(String modId) {
        return create(modId, true);
    }

    public static EFRegistrate create(String modId, boolean registerEvents) {
        return innerCreate(modId, registerEvents, registerEvents);
    }

    public static EFRegistrate createIgnoringListenerErrors(String modId) {
        return innerCreate(modId, true, false);
    }

    private static EFRegistrate innerCreate(String modId, boolean registerEvents, boolean requireValidEventBus) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new EFRegistrate(modId);
        if (registerEvents) {
            Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
            if (requireValidEventBus) {
                modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                    String message = "# [EFRegistrate] Failed to register event listeners for mod " + modId + " #";
                    String hashtags = "#".repeat(message.length());
                    ExtForCore.LOGGER.fatal(hashtags);
                    ExtForCore.LOGGER.fatal(message);
                    ExtForCore.LOGGER.fatal(hashtags);
                });
            } else {
                IEventBus eventBus = modEventBus.orElse(ExtForCore.modBus);
                if (eventBus != null) {
                    registrate.registerEventListeners(eventBus);
                }
            }
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    public ResourceLocation makeResourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(getModid(), path);
    }

    public EFSoundEntryBuilder sound(String name) {
        return new EFSoundEntryBuilder(this, ResourceLocation.fromNamespaceAndPath(getModid(), name));
    }

    public EFSoundEntryBuilder sound(ResourceLocation name) {
        return new EFSoundEntryBuilder(this, name);
    }

    EFSoundEntry registerSoundEntry(EFSoundEntry entry) {
        if (!entry.getId().getNamespace().equals(getModid())) {
            throw new IllegalArgumentException(
                    "Sound entry namespace " + entry.getId().getNamespace() + " does not match registrate owner " +
                            getModid());
        }
        soundEntries.add(entry);
        entry.register(soundEvent -> simple(soundEvent.getLocation().getPath(), Registries.SOUND_EVENT,
                () -> soundEvent));
        return entry;
    }

    public List<EFSoundEntry> getSoundEntries() {
        return Collections.unmodifiableList(soundEntries);
    }

    public EFMaterial.Builder material(String name) {
        return material(ResourceLocation.fromNamespaceAndPath(getModid(), name));
    }

    public EFMaterial.Builder material(ResourceLocation name) {
        return new EFMaterial.Builder(name);
    }

    public RegistryEntry<CoverDefinition, CoverDefinition> simpleCover(String name,
                                                                       CoverDefinition.CoverBehaviourProvider behaviorCreator,
                                                                       Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        return simple(name, EFRegistries.COVER_REGISTRY,
                () -> new CoverDefinition(makeResourceLocation(name), behaviorCreator, coverRenderer));
    }

    public RegistryEntry<CoverDefinition, CoverDefinition> simpleCover(String name,
                                                                       CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        return simpleCover(name, behaviorCreator, CoverBuilder.simpleCoverRenderer(this, name));
    }

    public CoverBuilder cover(String name,
                              CoverDefinition.CoverBehaviourProvider behaviorCreator,
                              Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        return new CoverBuilder(this, name, behaviorCreator, coverRenderer);
    }

    public CoverBuilder cover(String name, CoverDefinition.CoverBehaviourProvider behaviorCreator) {
        return cover(name, behaviorCreator, CoverBuilder.simpleCoverRenderer(this, name));
    }

    public CoverBuilder tieredCover(String name,
                                    CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                    int tier,
                                    Supplier<Supplier<ICoverRenderer>> coverRenderer) {
        return cover(name, (definition, coverable, side) -> behaviorCreator.create(definition, coverable, side, tier),
                coverRenderer);
    }

    public CoverBuilder tieredCover(String name,
                                    CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                    int tier) {
        return tieredCover(name, behaviorCreator, tier, CoverBuilder.simpleCoverRenderer(this, name));
    }

    public CoverDefinitionHolder[] tieredCovers(String name,
                                                CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                IntFunction<String> tierName,
                                                int... tiers) {
        CoverDefinitionHolder[] holders = new CoverDefinitionHolder[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            holders[i] = tieredCover(CoverBuilder.tieredName(name, tierName.apply(tier)), behaviorCreator, tier)
                    .register();
        }
        return holders;
    }

    public CoverDefinitionHolder[] tieredCovers(String name,
                                                CoverDefinition.TieredCoverBehaviourProvider behaviorCreator,
                                                IntFunction<String> tierName,
                                                IntFunction<Supplier<Supplier<ICoverRenderer>>> coverRenderer,
                                                int... tiers) {
        CoverDefinitionHolder[] holders = new CoverDefinitionHolder[tiers.length];
        for (int i = 0; i < tiers.length; i++) {
            int tier = tiers[i];
            holders[i] = tieredCover(CoverBuilder.tieredName(name, tierName.apply(tier)), behaviorCreator, tier,
                    coverRenderer.apply(tier)).register();
        }
        return holders;
    }

    public ItemEntry<EFMaterialItem> materialItem(EFMaterialTag materialTag, EFMaterial material) {
        return materialItem(materialTag.getRegisteredName(material), materialTag, material);
    }

    public ItemEntry<EFMaterialItem> materialItem(String name, EFMaterialTag materialTag, EFMaterial material) {
        EFMaterialIconType iconType = materialTag.materialIconType();
        return item(name, properties -> new EFMaterialItem(properties, materialTag, material))
                .lang(materialTag.getDefaultEnglishName(material))
                .model((ctx, provider) -> provider.withExistingParent(ctx.getName(),
                        iconType.getItemModelPath(material.getMaterialIconSet(), true)))
                .color(() -> () -> EFMaterialItem.tintColor(material))
                .register();
    }

    public BlockEntry<EFMaterialBlock> materialBlock(EFMaterialTag materialTag, EFMaterial material) {
        return materialBlock(materialTag.getRegisteredName(material), materialTag, material);
    }

    public BlockEntry<EFMaterialBlock> materialBlock(String name, EFMaterialTag materialTag, EFMaterial material) {
        EFMaterialIconType iconType = materialTag.materialIconType();
        return block(name, properties -> new EFMaterialBlock(properties, materialTag, material))
                .lang(materialTag.getDefaultEnglishName(material))
                .blockstate((ctx, provider) -> provider.simpleBlock(ctx.getEntry(),
                        provider.models().getExistingFile(iconType.getBlockModelPath(material.getMaterialIconSet(), true))))
                .color(() -> EFMaterialBlock::tintColor)
                .item((block, properties) -> new EFMaterialBlockItem(block, properties, materialTag, material))
                .model((ctx, provider) -> provider.blockItem(ctx::getEntry))
                .color(() -> () -> EFMaterialBlockItem.tintColor(material))
                .build()
                .register();
    }

    public <D extends EFDefinitionHolder<B, I, BE>, B extends Block, I extends Item, BE extends BlockEntity> EFDefinitionBuilder<D, B, I, BE, ?> definition(
                                                                                                                                                            String name,
                                                                                                                                                            Function<ResourceLocation, D> definitionFactory,
                                                                                                                                                            BiFunction<BlockBehaviour.Properties, D, B> blockFactory,
                                                                                                                                                            BiFunction<B, Item.Properties, I> itemFactory,
                                                                                                                                                            Function<com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo, BE> blockEntityFactory) {
        return new EFDefinitionBuilder<>(this, name, definitionFactory, blockFactory, itemFactory, blockEntityFactory);
    }

    public <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION, ?> machine(
                                                                                        String name,
                                                                                        Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                        BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                                                                                        BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                        Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        return new MachineBuilder<>(this, name, definitionFactory, blockFactory, itemFactory, blockEntityFactory);
    }

    public MachineBuilder<MachineDefinition, ?> machine(
                                                        String name, Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        return new MachineBuilder<>(this, name, MachineDefinition::new,
                MetaMachineBlock::new, MetaMachineItem::new, blockEntityFactory);
    }

    public MultiblockMachineBuilder<MultiblockMachineDefinition, ?> multiblock(
                                                                               String name,
                                                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, MetaMachineBlock> blockFactory,
                                                                               BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                               Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        return new MultiblockMachineBuilder<>(this, name, blockFactory, itemFactory, blockEntityFactory);
    }

    public MultiblockMachineBuilder<MultiblockMachineDefinition, ?> multiblock(
                                                                               String name, Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        return new MultiblockMachineBuilder<>(this, name, MetaMachineBlock::new, MetaMachineItem::new,
                blockEntityFactory);
    }

    public void registerRegistrate(IEventBus bus) {
        registerEventListeners(bus);
    }

    @Override
    public EFRegistrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
            setModEventBus(bus);

            Consumer<RegisterEvent> onRegister = this::onRegister;
            Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
            bus.addListener(EventPriority.LOW, onRegister);
            bus.addListener(EventPriority.LOWEST, onRegisterLate);
            bus.addListener(this::onBuildCreativeModeTabContents);

            OneTimeEventReceiver.addModListener(this, FMLCommonSetupEvent.class, ignored -> {
                OneTimeEventReceiver.unregister(this, onRegister, RegisterEvent.class);
                OneTimeEventReceiver.unregister(this, onRegisterLate, RegisterEvent.class);
            });
            if (DatagenModLoader.isRunningDataGen()) {
                OneTimeEventReceiver.addModListener(this, GatherDataEvent.class, this::onData);
            }
        }
        return this;
    }

    protected <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(
                                                                                             P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        ResourceKey<CreativeModeTab> key = ResourceKey.create(
                Registries.CREATIVE_MODE_TAB, ResourceLocation.fromNamespaceAndPath(getModid(), name));
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream()
                            .findFirst()
                            .map(ItemEntry::cast)
                            .map(ItemEntry::asStack)
                            .orElse(new ItemStack(Items.AIR)))
                    .title(this.addLang("itemGroup", key.location(), RegistrateLangProvider.toEnglishName(name)));
            config.accept(builder);
            return builder.build();
        });
    }

    @Override
    public <T extends Block> EFBlockBuilder<T, EFRegistrate> block(
                                                                   NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, factory);
    }

    @Override
    public <T extends Block> EFBlockBuilder<T, EFRegistrate> block(
                                                                   String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(this, name, factory);
    }

    @Override
    public <T extends Block, P> EFBlockBuilder<T, P> block(
                                                           P parent, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return block(parent, currentName(), factory);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T extends Block, P> EFBlockBuilder<T, P> block(
                                                           P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (EFBlockBuilder<T, P>) (BlockBuilder) this.<Block, T, P, BlockBuilder<T, P>>entry(
                name, callback -> EFBlockBuilder.create(this, parent, name, callback, factory));
    }

    public RegistryEntry<CreativeModeTab, CreativeModeTab> creativeModeTab() {
        return this.currentTab;
    }

    public void creativeModeTab(Supplier<RegistryEntry<CreativeModeTab, CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void creativeModeTab(@Nullable RegistryEntry<CreativeModeTab, CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?, ?> entry, RegistryEntry<CreativeModeTab, CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?, ?> entry, @Nullable RegistryEntry<CreativeModeTab, CreativeModeTab> tab) {
        if (tab == null) {
            TAB_LOOKUP.remove(entry);
        } else {
            TAB_LOOKUP.put(entry, tab);
        }
    }

    @Override
    protected <R, T extends R> RegistryEntry<R, T> accept(
                                                          String name,
                                                          ResourceKey<? extends Registry<R>> type,
                                                          Builder<R, T, ?, ?> builder,
                                                          NonNullSupplier<? extends T> creator,
                                                          NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    @Override
    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(
                                                                                       P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        return createCreativeModeTab(parent, name, config);
    }
}
