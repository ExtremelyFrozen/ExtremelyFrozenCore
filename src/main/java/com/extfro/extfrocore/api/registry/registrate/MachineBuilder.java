package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.data.RotationState;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.machine.MachineDefinition;
import com.extfro.extfrocore.api.machine.MachineRenderState;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.multiblock.PartAbility;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.registry.EFRegistries;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class MachineBuilder<DEFINITION extends MachineDefinition, TYPE extends MachineBuilder<DEFINITION, TYPE>> {

    protected final EFRegistrate registrate;
    protected final String name;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory;
    protected final BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory;
    protected Function<ResourceLocation, DEFINITION> definition;

    @Nullable
    private MachineBuilder.ModelInitializer model = null;
    @Nullable
    private NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> blockModel = null;
    protected final Map<Property<?>, @Nullable Comparable<?>> modelProperties = new IdentityHashMap<>();
    private VoxelShape shape = Shapes.block();
    private RotationState rotationState = RotationState.NON_Y_AXIS;
    private boolean allowExtendedFacing;
    private boolean renderMultiblockWorldPreview = true;
    private boolean renderMultiblockXEIPreview = true;
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = properties -> properties;
    private NonNullUnaryOperator<Item.Properties> itemProp = properties -> properties;
    @Nullable
    private java.util.function.Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Nullable
    private java.util.function.Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    private NonNullConsumer<BlockEntityType<MetaMachine>> onBlockEntityRegister = NonNullConsumer.noop();
    private MachineRecipeType[] recipeTypes = new MachineRecipeType[0];
    private int tier;
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();
    private int paintingColor = -1;
    private BiFunction<ItemStack, Integer, Integer> itemColor = (itemStack, tintIndex) -> -1;
    private PartAbility[] abilities = new PartAbility[0];
    private final List<Component> tooltips = new ArrayList<>();
    @Nullable
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    private boolean allowCoverOnFront;
    @Nullable
    private Supplier<BlockState> appearance;
    @Nullable
    private String langValue;

    public MachineBuilder(
                          EFRegistrate registrate,
                          String name,
                          Function<ResourceLocation, DEFINITION> definition,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                          BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        this.registrate = registrate;
        this.name = name;
        this.definition = definition;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
    }

    @SuppressWarnings("unchecked")
    public TYPE getThis() {
        return (TYPE) this;
    }

    public TYPE blockModel(
                           NonNullBiConsumer<DataGenContext<Block, ? extends Block>, RegistrateBlockstateProvider> blockModel) {
        this.blockModel = blockModel;
        return getThis();
    }

    public TYPE shape(VoxelShape shape) {
        this.shape = shape;
        return getThis();
    }

    public TYPE rotationState(RotationState rotationState) {
        this.rotationState = rotationState;
        return getThis();
    }

    public TYPE allowExtendedFacing(boolean allowExtendedFacing) {
        this.allowExtendedFacing = allowExtendedFacing;
        return getThis();
    }

    public TYPE renderMultiblockWorldPreview(boolean renderMultiblockWorldPreview) {
        this.renderMultiblockWorldPreview = renderMultiblockWorldPreview;
        return getThis();
    }

    public TYPE renderMultiblockXEIPreview(boolean renderMultiblockXEIPreview) {
        this.renderMultiblockXEIPreview = renderMultiblockXEIPreview;
        return getThis();
    }

    public TYPE multiblockPreviewRenderer(boolean multiBlockWorldPreview, boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return getThis();
    }

    public TYPE blockProp(NonNullUnaryOperator<BlockBehaviour.Properties> blockProp) {
        this.blockProp = blockProp;
        return getThis();
    }

    public TYPE itemProp(NonNullUnaryOperator<Item.Properties> itemProp) {
        this.itemProp = itemProp;
        return getThis();
    }

    public TYPE blockBuilder(@Nullable java.util.function.Consumer<BlockBuilder<? extends Block, ?>> blockBuilder) {
        this.blockBuilder = blockBuilder;
        return getThis();
    }

    public TYPE itemBuilder(@Nullable java.util.function.Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder) {
        this.itemBuilder = itemBuilder;
        return getThis();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TYPE onBlockEntityRegister(NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister) {
        this.onBlockEntityRegister = (NonNullConsumer) onBlockEntityRegister;
        return getThis();
    }

    public TYPE onMachineBlockEntityRegister(NonNullConsumer<BlockEntityType<MetaMachine>> onBlockEntityRegister) {
        this.onBlockEntityRegister = onBlockEntityRegister;
        return getThis();
    }

    public TYPE tier(int tier) {
        this.tier = tier;
        return getThis();
    }

    public TYPE recipeType(MachineRecipeType type) {
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        return getThis();
    }

    public TYPE recipeTypes(MachineRecipeType... types) {
        List<MachineRecipeType> typeList = new ArrayList<>();
        typeList.addAll(Arrays.asList(this.recipeTypes));
        typeList.addAll(Arrays.asList(types));
        this.recipeTypes = typeList.toArray(MachineRecipeType[]::new);
        return getThis();
    }

    public TYPE recipeOutputLimits(Reference2IntMap<RecipeCapability<?>> recipeOutputLimits) {
        this.recipeOutputLimits = recipeOutputLimits;
        return getThis();
    }

    public TYPE recipeOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return getThis();
    }

    public TYPE paintingColor(int paintingColor) {
        this.paintingColor = paintingColor;
        return getThis();
    }

    public TYPE itemColor(BiFunction<ItemStack, Integer, Integer> itemColor) {
        this.itemColor = itemColor;
        return getThis();
    }

    public TYPE tooltipBuilder(@Nullable BiConsumer<ItemStack, List<Component>> tooltipBuilder) {
        this.tooltipBuilder = tooltipBuilder;
        return getThis();
    }

    public TYPE allowCoverOnFront(boolean allowCoverOnFront) {
        this.allowCoverOnFront = allowCoverOnFront;
        return getThis();
    }

    public TYPE appearance(@Nullable Supplier<BlockState> appearance) {
        this.appearance = appearance;
        return getThis();
    }

    public TYPE appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return getThis();
    }

    public TYPE langValue(@Nullable String langValue) {
        this.langValue = langValue;
        return getThis();
    }

    public TYPE model(@Nullable MachineBuilder.ModelInitializer model) {
        this.model = model;
        return getThis();
    }

    public TYPE simpleModel(ResourceLocation modelName) {
        return model((context, provider) -> provider.simpleBlock(context.getEntry(),
                provider.models().getExistingFile(modelName)));
    }

    public TYPE defaultModel() {
        return simpleModel(registrate.makeResourceLocation("block/machine/template/" + name));
    }

    public TYPE tooltips(@Nullable Component... components) {
        return tooltips(Arrays.asList(components));
    }

    public TYPE tooltips(List<? extends @Nullable Component> components) {
        tooltips.addAll(components.stream().filter(Objects::nonNull).toList());
        return getThis();
    }

    public TYPE conditionalTooltip(Component component, BooleanSupplier condition) {
        return conditionalTooltip(component, condition.getAsBoolean());
    }

    public TYPE conditionalTooltip(Component component, boolean condition) {
        if (condition) {
            tooltips.add(component);
        }
        return getThis();
    }

    public TYPE abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return getThis();
    }

    public TYPE modelProperty(Property<?> property) {
        return modelProperty(property, null);
    }

    public <T extends Comparable<T>> TYPE modelProperty(Property<T> property, @Nullable T defaultValue) {
        modelProperties.put(property, defaultValue);
        return getThis();
    }

    public TYPE modelProperties(Property<?>... properties) {
        return modelProperties(List.of(properties));
    }

    public TYPE modelProperties(Collection<Property<?>> properties) {
        for (Property<?> property : properties) {
            modelProperties.put(property, null);
        }
        return getThis();
    }

    public TYPE modelProperties(Map<Property<?>, ? extends Comparable<?>> properties) {
        modelProperties.putAll(properties);
        return getThis();
    }

    public TYPE removeModelProperty(Property<?> property) {
        modelProperties.remove(property);
        return getThis();
    }

    public TYPE clearModelProperties() {
        modelProperties.clear();
        return getThis();
    }

    protected DEFINITION createDefinition() {
        return definition.apply(registrate.makeResourceLocation(name));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected void setupStateDefinition(MachineDefinition definition) {
        StateDefinition.Builder<MachineDefinition, MachineRenderState> builder = new StateDefinition.Builder<>(
                definition);
        modelProperties.keySet().forEach(builder::add);
        definition.setStateDefinition(builder.create(MachineDefinition::defaultRenderState, MachineRenderState::new));

        MachineRenderState defaultState = definition.getStateDefinition().any();
        for (var entry : modelProperties.entrySet()) {
            if (entry.getValue() == null) continue;
            defaultState = defaultState.setValue((Property) entry.getKey(), (Comparable) entry.getValue());
        }
        definition.registerDefaultState(defaultState);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public DEFINITION register() {
        registrate.object(name);
        DEFINITION definition = createDefinition();
        definition.setRotationState(rotationState);
        definition.setAllowExtendedFacing(allowExtendedFacing);
        setupStateDefinition(definition);
        if (model == null && blockModel == null) {
            defaultModel();
        }

        BlockBuilder<MetaMachineBlock, ?> blockBuilder = registrate
                .block(properties -> makeBlock(definition, properties))
                .initialProperties(() -> Blocks.DISPENSER)
                .properties(BlockBehaviour.Properties::noLootTable)
                .blockstate(blockModel != null ? (ctx, provider) -> blockModel.accept((DataGenContext) ctx, provider) :
                        (ctx, provider) -> model.configureModel((DataGenContext) ctx, provider))
                .properties(blockProp)
                .onRegister(block -> Arrays.stream(abilities).forEach(ability -> ability.register(tier, block)));
        if (langValue != null) {
            blockBuilder.lang(langValue);
            definition.setLangValue(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        BlockEntry<MetaMachineBlock> block = blockBuilder.register();

        ItemBuilder<MetaMachineItem, ?> itemBuilder = registrate
                .item(properties -> itemFactory.apply(block.get(), properties))
                .setData(ProviderType.LANG, NonNullBiConsumer.noop())
                .model((ctx, provider) -> provider.withExistingParent(ctx.getName(),
                        registrate.makeResourceLocation("block/machine/" + ctx.getName())))
                .color(() -> () -> itemColor::apply)
                .properties(itemProp);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntity = registrate
                .<MetaMachine>blockEntity((type, pos, state) -> blockEntityFactory.apply(new BlockEntityCreationInfo(type, pos, state)))
                .onRegister(onBlockEntityRegister)
                .validBlock(block)
                .register();

        definition.setBlock(block);
        definition.setItem(item);
        definition.setBlockEntity(blockEntity);
        definition.setRecipeTypes(recipeTypes);
        definition.setTier(tier);
        definition.setRecipeOutputLimits(recipeOutputLimits);
        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips);
            if (tooltipBuilder != null) tooltipBuilder.accept(itemStack, components);
        });
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        definition.setAppearance(appearance);
        definition.setAllowCoverOnFront(allowCoverOnFront);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        EFRegistries.register(EFRegistries.MACHINES, definition.getId(), definition);
        return definition;
    }

    private MetaMachineBlock makeBlock(DEFINITION definition, BlockBehaviour.Properties properties) {
        MachineDefinition.setBuilt(definition);
        try {
            return blockFactory.apply(properties, definition);
        } finally {
            MachineDefinition.clearBuilt();
        }
    }

    @FunctionalInterface
    public interface ModelInitializer {

        void configureModel(DataGenContext<Block, ? extends Block> context, RegistrateBlockstateProvider provider);

        default ModelInitializer andThen(ModelInitializer after) {
            Objects.requireNonNull(after);
            return (ctx, provider) -> {
                configureModel(ctx, provider);
                after.configureModel(ctx, provider);
            };
        }
    }
}
