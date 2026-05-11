package com.extfro.extfrocore.api.machine;

import com.extfro.extfrocore.api.block.MetaMachineBlock;
import com.extfro.extfrocore.api.capability.recipe.RecipeCapability;
import com.extfro.extfrocore.api.data.RotationState;
import com.extfro.extfrocore.api.item.MetaMachineItem;
import com.extfro.extfrocore.api.recipe.MachineRecipeType;
import com.extfro.extfrocore.api.registry.registrate.EFDefinitionHolder;

import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MachineDefinition implements Supplier<MetaMachineBlock>,
                               EFDefinitionHolder<MetaMachineBlock, MetaMachineItem, MetaMachine> {

    public static final IdMapper<MachineRenderState> RENDER_STATE_REGISTRY = new IdMapper<>(512);

    @Getter
    private final ResourceLocation id;
    @Getter
    @Setter
    @Nullable
    private String langValue;
    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;
    @Getter
    @Setter
    private @NotNull MachineRecipeType @NotNull [] recipeTypes = new MachineRecipeType[0];
    @Getter
    @Setter
    private int tier;
    @Getter
    @Setter
    private int defaultPaintingColor = -1;
    @Getter
    @Setter
    private boolean allowExtendedFacing;
    @Getter
    @Setter
    private RotationState rotationState = RotationState.NON_Y_AXIS;
    @Setter
    private VoxelShape shape = Shapes.block();
    @Getter
    @Setter
    private boolean renderWorldPreview = true;
    @Getter
    @Setter
    private boolean renderXEIPreview = true;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder = (stack, tooltip) -> {};
    @Getter
    @Setter
    private Supplier<BlockState> appearance;
    @Getter
    @Setter
    private boolean allowCoverOnFront;
    @Getter
    @Setter
    private Reference2IntMap<RecipeCapability<?>> recipeOutputLimits = new Reference2IntOpenHashMap<>();

    @Getter
    @Setter(onMethod_ = @ApiStatus.Internal)
    private StateDefinition<MachineDefinition, MachineRenderState> stateDefinition;
    @Accessors(fluent = true)
    @Getter
    private MachineRenderState defaultRenderState;

    public MachineDefinition(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public ResourceLocation id() {
        return id;
    }

    public final void registerDefaultState(MachineRenderState state) {
        defaultRenderState = state;
    }

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) {
            return shape;
        }
        return cache.computeIfAbsent(direction, dir -> rotateShape(shape, dir));
    }

    private static VoxelShape rotateShape(VoxelShape source, Direction direction) {
        VoxelShape[] result = { source };
        int times = switch (direction) {
            case SOUTH -> 2;
            case WEST -> 1;
            case EAST -> 3;
            default -> 0;
        };
        for (int i = 0; i < times; i++) {
            VoxelShape[] rotated = { Shapes.empty() };
            result[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> rotated[0] = Shapes.or(rotated[0],
                    Shapes.box(1.0D - maxZ, minY, minX, 1.0D - minZ, maxY, maxX)));
            result[0] = rotated[0];
        }
        return result[0];
    }

    @Override
    public MetaMachineBlock get() {
        return (MetaMachineBlock) blockSupplier.get();
    }

    public String getName() {
        return id.getPath();
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public void setBlock(BlockEntry<MetaMachineBlock> block) {
        setBlockSupplier(block);
    }

    @Override
    public void setItem(ItemEntry<MetaMachineItem> item) {
        setItemSupplier(item);
    }

    @Override
    public void setBlockEntity(RegistryEntry<BlockEntityType<?>, BlockEntityType<MetaMachine>> blockEntity) {
        setBlockEntityTypeSupplier(blockEntity::get);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MachineDefinition that = (MachineDefinition) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    private static final ThreadLocal<MachineDefinition> STATE = new ThreadLocal<>();

    public static MachineDefinition getBuilt() {
        return STATE.get();
    }

    public static void setBuilt(MachineDefinition state) {
        STATE.set(state);
    }

    public static void clearBuilt() {
        STATE.remove();
    }
}
