package com.extfro.extfrocore.api.registry.registrate;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.machine.MetaMachine;
import com.extfro.extfrocore.api.machine.MultiblockMachineDefinition;
import com.extfro.extfrocore.api.machine.feature.multiblock.IMultiPart;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockControllerMachine;
import com.extfro.extfrocore.api.pattern.BlockPattern;
import com.extfro.extfrocore.api.pattern.MultiblockShapeInfo;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class MultiblockMachineBuilder<DEFINITION extends MultiblockMachineDefinition,
        TYPE extends MultiblockMachineBuilder<DEFINITION, TYPE>> extends MachineBuilder<DEFINITION, TYPE> {

    private boolean generator;
    @Nullable
    private Function<MultiblockMachineDefinition, BlockPattern> pattern;
    private final List<Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>>> shapeInfos = new ArrayList<>();
    private boolean allowFlip = true;
    private final List<Supplier<ItemStack[]>> recoveryItems = new ArrayList<>();
    private Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter = controller -> (a, b) -> 0;
    @Nullable
    private TriFunction<MultiblockControllerMachine, IMultiPart, Direction, BlockState> partAppearance;
    private BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay = (machine, tooltip) -> {};

    @SuppressWarnings("unchecked")
    public MultiblockMachineBuilder(
                                    EFRegistrate registrate,
                                    String name,
                                    BiFunction<BlockBehaviour.Properties, DEFINITION, MetaMachineBlock> blockFactory,
                                    BiFunction<MetaMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                    Function<BlockEntityCreationInfo, MetaMachine> blockEntityFactory) {
        super(registrate, name, location -> (DEFINITION) new MultiblockMachineDefinition(location),
                blockFactory, itemFactory, blockEntityFactory);
        allowExtendedFacing(true);
        allowCoverOnFront(true);
        modelProperty(BlockStateProperties.POWERED, false);
    }

    public TYPE generator(boolean generator) {
        this.generator = generator;
        return getThis();
    }

    public TYPE pattern(Function<MultiblockMachineDefinition, BlockPattern> pattern) {
        this.pattern = pattern;
        return getThis();
    }

    public TYPE allowFlip(boolean allowFlip) {
        this.allowFlip = allowFlip;
        return getThis();
    }

    public TYPE partSorter(Function<MultiblockControllerMachine, Comparator<IMultiPart>> partSorter) {
        this.partSorter = partSorter;
        return getThis();
    }

    public TYPE partSorter(Comparator<IMultiPart> sorter) {
        this.partSorter = controller -> sorter;
        return getThis();
    }

    public TYPE partAppearance(
                               TriFunction<MultiblockControllerMachine, IMultiPart, Direction, BlockState> partAppearance) {
        this.partAppearance = partAppearance;
        return getThis();
    }

    public TYPE additionalDisplay(BiConsumer<MultiblockControllerMachine, List<Component>> additionalDisplay) {
        this.additionalDisplay = additionalDisplay;
        return getThis();
    }

    public TYPE shapeInfo(Function<MultiblockMachineDefinition, MultiblockShapeInfo> shape) {
        shapeInfos.add(definition -> List.of(shape.apply(definition)));
        return getThis();
    }

    public TYPE shapeInfos(Function<MultiblockMachineDefinition, List<MultiblockShapeInfo>> shapes) {
        shapeInfos.add(shapes);
        return getThis();
    }

    public TYPE recoveryItems(Supplier<ItemLike[]> items) {
        recoveryItems.add(() -> Arrays.stream(items.get()).map(ItemLike::asItem).map(Item::getDefaultInstance)
                .toArray(ItemStack[]::new));
        return getThis();
    }

    public TYPE recoveryStacks(Supplier<ItemStack[]> stacks) {
        recoveryItems.add(stacks);
        return getThis();
    }

    @Override
    public DEFINITION register() {
        DEFINITION definition = super.register();
        definition.setGenerator(generator);
        definition.setPatternFactory(() -> pattern == null ? BlockPattern.EMPTY : pattern.apply(definition));
        definition.setShapes(() -> shapeInfos.stream()
                .map(factory -> factory.apply(definition))
                .flatMap(Collection::stream)
                .toList());
        definition.setAllowFlip(allowFlip);
        if (!recoveryItems.isEmpty()) {
            definition.setRecoveryItems(() -> recoveryItems.stream()
                    .map(Supplier::get)
                    .flatMap(Arrays::stream)
                    .toArray(ItemStack[]::new));
        }
        definition.setPartSorter(partSorter);
        if (partAppearance != null) {
            definition.setPartAppearance(partAppearance);
        }
        definition.setAdditionalDisplay(additionalDisplay);
        return definition;
    }
}
