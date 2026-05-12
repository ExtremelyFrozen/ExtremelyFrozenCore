package com.extfro.extfrocore.common.machine.multiblock.electric;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.blockentity.BlockEntityCreationInfo;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IMonitorComponent;
import com.extfro.extfrocore.api.capability.recipe.IO;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.component.IItemComponent;
import com.extfro.extfrocore.api.item.component.IMonitorModuleItem;
import com.extfro.extfrocore.api.machine.feature.IDataInfoProvider;
import com.extfro.extfrocore.api.machine.multiblock.MultiblockDisplayText;
import com.extfro.extfrocore.api.machine.multiblock.PartAbility;
import com.extfro.extfrocore.api.machine.multiblock.WorkableElectricMultiblockMachine;
import com.extfro.extfrocore.api.misc.EnergyContainerList;
import com.extfro.extfrocore.api.pattern.*;
import com.extfro.extfrocore.api.pattern.util.RelativeDirection;
import com.extfro.extfrocore.api.sync_system.annotations.RerenderOnChanged;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.common.data.GTBlocks;
import com.extfro.extfrocore.common.data.GTMachines;
import com.extfro.extfrocore.common.item.behavior.PortableScannerBehavior;
import com.extfro.extfrocore.common.machine.multiblock.electric.monitor.MonitorGroup;
import com.extfro.extfrocore.common.machine.trait.CentralMonitorLogic;
import com.extfro.extfrocore.common.network.packets.SCPacketMonitorGroupNBTChange;
import com.extfro.extfrocore.data.lang.LangHandler;

import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import com.lowdragmc.lowdraglib2.gui.texture.*;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CentralMonitorMachine extends WorkableElectricMultiblockMachine
                                   implements IMonitorComponent, IDataInfoProvider {

    @SaveField
    @SyncToClient
    @Getter
    private int leftDist = 0, rightDist = 0, upDist = 0, downDist = 0;
    @SaveField
    @SyncToClient
    @Getter
    @RerenderOnChanged
    private List<MonitorGroup> monitorGroups = new ArrayList<>();
    private final Set<IMonitorComponent> selectedComponents = new HashSet<>();
    private final List<IMonitorComponent> selectedTargets = new ArrayList<>();

    private @Nullable MultiblockState patternFindingState;

    private static @Nullable TraceabilityPredicate MULTI_PREDICATE = null;

    public CentralMonitorMachine(BlockEntityCreationInfo info) {
        super(info, new CentralMonitorLogic());
    }

    public static TraceabilityPredicate getMultiPredicate() {
        if (MULTI_PREDICATE == null) {
            MULTI_PREDICATE = Predicates.abilities(PartAbility.INPUT_ENERGY)
                    .setMinGlobalLimited(1).setMaxGlobalLimited(2).setPreviewCount(1)
                    .or(Predicates.abilities(PartAbility.DATA_ACCESS).setPreviewCount(1)
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_4).setPreviewCount(0))
                            .or(Predicates.machines(GTMachines.BATTERY_BUFFER_16).setPreviewCount(0))
                            .setMaxGlobalLimited(4))
                    .or(Predicates.machines(GTMachines.HULL))
                    .or(Predicates.machines(GTMachines.MONITOR))
                    .or(Predicates.machines(GTMachines.ADVANCED_MONITOR))
                    .or(Predicates.blocks(GTBlocks.CASING_ALUMINIUM_FROSTPROOF.get()));
        }
        return MULTI_PREDICATE;
    }

    @Override
    public void onStructureInvalid() {
        super.onStructureInvalid();
        this.clearPatternFindingState();
    }

    @Override
    public CentralMonitorLogic getRecipeLogic() {
        return (CentralMonitorLogic) super.getRecipeLogic();
    }

    public @Nullable EnergyContainerList getFormedEnergyContainer() {
        return this.energyContainer;
    }

    public void tick() {
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (MonitorGroup group : monitorGroups) {
            ItemStack stack = group.getItemStackHandler().getStackInSlot(0);
            if (stack.isEmpty() || !(stack.getItem() instanceof IComponentItem componentItem)) {
                continue;
            }

            for (IItemComponent component : componentItem.getComponents()) {
                if (!(component instanceof IMonitorModuleItem module)) {
                    continue;
                }
                module.tick(stack, this, group);
                PacketDistributor.sendToPlayersTrackingChunk(serverLevel, new ChunkPos(getBlockPos()),
                        new SCPacketMonitorGroupNBTChange(stack, group, this));
            }
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        this.clearPatternFindingState();
    }

    protected void clearPatternFindingState() {
        if (this.patternFindingState != null)
            this.patternFindingState.clean();
        this.patternFindingState = null;
    }

    protected MultiblockState getPatternFindingState() {
        if (this.patternFindingState == null) {
            this.patternFindingState = new MultiblockState(getLevel(), getBlockPos());
            this.patternFindingState.clean();
        }
        return this.patternFindingState;
    }

    public boolean isValidMonitorBlock(Level level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos)) return false;

        MultiblockState state = getPatternFindingState();
        if (!state.update(pos, getMultiPredicate())) {
            return false;
        }
        state.io = IO.BOTH;

        return Stream.concat(state.predicate.common.stream(), state.predicate.limited.stream())
                .anyMatch(predicate -> predicate.test(state));
    }

    public void updateStructureDimensions() {
        Level level = getLevel();
        if (level == null) return;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();

        Direction left = RelativeDirection.LEFT.getRelative(front, spin, false);
        Direction right = RelativeDirection.RIGHT.getRelative(front, spin, false);
        Direction up = RelativeDirection.UP.getRelative(front, spin, false);
        Direction down = RelativeDirection.DOWN.getRelative(front, spin, false);
        BlockPos.MutableBlockPos posLeft = getBlockPos().mutable().move(left);
        BlockPos.MutableBlockPos posRight = getBlockPos().mutable().move(right);
        BlockPos.MutableBlockPos posUp = getBlockPos().mutable().move(up);
        BlockPos.MutableBlockPos posDown = getBlockPos().mutable().move(down);
        this.leftDist = 0;
        this.rightDist = 0;
        this.upDist = 0;
        this.downDist = 0;

        while (isValidMonitorBlock(level, posLeft)) {
            posLeft.move(left);
            leftDist++;
        }
        while (isValidMonitorBlock(level, posRight)) {
            posRight.move(right);
            rightDist++;
        }
        while (isValidMonitorBlockRow(level, posUp, leftDist, rightDist, left, right)) {
            posUp.move(up);
            upDist++;
        }
        while (isValidMonitorBlockRow(level, posDown, leftDist, rightDist, left, right)) {
            posDown.move(down);
            downDist++;
        }
    }

    private boolean isValidMonitorBlockRow(Level level, BlockPos pos, int leftDist, int rightDist, Direction left,
                                           Direction right) {
        BlockPos.MutableBlockPos mutable = pos.mutable();
        mutable.move(left, leftDist);
        for (int i = 0; i < leftDist + rightDist; i++) {
            if (!isValidMonitorBlock(level, mutable)) return false;
            mutable.move(right);
        }
        return isValidMonitorBlock(level, mutable);
    }

    @Override
    public BlockPattern getPattern() {
        updateStructureDimensions();
        if (leftDist + rightDist < 1 || upDist + downDist < 1) {
            leftDist = 3;
            rightDist = 0;
            upDist = 1;
            downDist = 1;
        }

        StringBuilder[] pattern = new StringBuilder[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            pattern[i] = new StringBuilder(leftDist + rightDist + 1);
            for (int j = 0; j < leftDist + rightDist + 1; j++) {
                if (i == downDist && j == rightDist)
                    pattern[i].append('C'); // controller
                else
                    pattern[i].append('B'); // any valid block
            }
        }

        String[] aisle = new String[upDist + downDist + 1];
        for (int i = 0; i < upDist + downDist + 1; i++) {
            aisle[i] = pattern[i].toString();
        }

        return FactoryBlockPattern.start()
                .aisle(aisle)
                .where('B', getMultiPredicate())
                .where('C', Predicates.controller(Predicates.blocks(this.getDefinition().get())))
                .build();
    }

    public BlockPos toRelative(BlockPos pos) {
        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        boolean flipped = isFlipped();
        Direction right = RelativeDirection.RIGHT.getRelative(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelative(front, spin, flipped);

        BlockPos tmp = getBlockPos().mutable().move(right, rightDist).move(up, upDist);

        return new BlockPos(Math.abs(tmp.get(right.getAxis()) - pos.get(right.getAxis())),
                Math.abs(tmp.get(up.getAxis()) - pos.get(up.getAxis())),
                0);
    }

    @Nullable
    public IMonitorComponent getComponent(int row, int col) {
        Level level = getLevel();
        if (level == null) return null;

        Direction front = getFrontFacing();
        Direction spin = getUpwardsFacing();
        boolean flipped = isFlipped();

        Direction left = RelativeDirection.LEFT.getRelative(front, spin, flipped);
        Direction up = RelativeDirection.UP.getRelative(front, spin, flipped);

        col = leftDist + rightDist - col;
        BlockPos pos = getBlockPos().relative(left, leftDist - col).relative(up, upDist - row);

        return GTCapabilityHelper.getMonitorComponent(level, pos, null);
    }

    public boolean isMonitor(int row, int col) {
        IMonitorComponent component = this.getComponent(row, col);
        if (component == null) return false;
        return component.isMonitor();
    }

    private IGuiTexture getComponentTexture(int row, int col) {
        if (row < 0 || col < 0 || row > downDist + upDist + 1 || col > leftDist + rightDist + 1)
            return GuiTextures.BLANK_TRANSPARENT;
        IMonitorComponent component = getComponent(row, col);
        if (component == null) return GuiTextures.BLANK_TRANSPARENT;
        return component.getComponentIcon();
    }

    private boolean isInAnyGroup(IMonitorComponent component) {
        return monitorGroups.stream().anyMatch(group -> group.contains(component.getBlockPos()));
    }

    public int getLeftDist() {
        return leftDist;
    }

    public int getRightDist() {
        return rightDist;
    }

    public int getUpDist() {
        return upDist;
    }

    public int getDownDist() {
        return downDist;
    }

    public List<MonitorGroup> getMonitorGroups() {
        return monitorGroups;
    }

    @Override
    public void addDisplayText(List<Component> textList) {
        MultiblockDisplayText.builder(textList, isFormed())
                .addWorkingStatusLine();
        getDefinition().getAdditionalDisplay().accept(this, textList);
    }

    @Override
    public UIElement createUIWidget() {
        updateStructureDimensions();
        selectedComponents.clear();
        UIElement builder = super.createUIWidget();

        UIElement main = new UIElement().layout(layout -> layout.left(0).top(0).width(240).height(140));
        ScrollerView componentSelection = new ScrollerView();
        componentSelection.layout(layout -> layout.left(0).top(10).width(200).height(110));
        componentSelection.style(style -> style.background(GuiTextures.DISPLAY));
        main.addChild(componentSelection);
        UIElement options = new UIElement().layout(layout -> layout.left(-100).top(20).width(70).height(60));
        UIElement groupConfig = new UIElement().layout(layout -> layout.left(10).top(30).width(230).height(150));
        groupConfig.setVisible(false);

        Button infoWidget = iconButton(200, 10, 20, 20, GuiTextures.INFO_ICON);
        infoWidget.style(style -> style.tooltips(
                LangHandler.getSingleOrMultiLang("gtceu.central_monitor.info_tooltip").toArray(Component[]::new)));
        builder.addChild(infoWidget);
        List<@Nullable MonitorGroup> configGroup = new ArrayList<>();
        configGroup.add(null);
        List<List<Runnable>> imageButtons = new ArrayList<>();
        Map<BlockPos, Runnable> rightClickCallbacks = new HashMap<>();
        int[] dataSlot = new int[] { 1, 9 };
        TextField dataSlotInput = intInput(120, 0, 60, 14, dataSlot[0], 1, dataSlot[1], n -> dataSlot[0] = n);
        dataSlotInput.setVisible(false);
        builder.addChild(dataSlotInput);

        ScrollerView groupList = new ScrollerView();
        groupList.layout(layout -> layout.left(-100).top(50).width(85).height(80));
        groupList.style(style -> style.background(GuiTextures.DISPLAY));
        builder.addChild(groupList);

        Consumer<@Nullable MonitorGroup> openGroupConfig = (group) -> {
            configGroup.set(0, group);
            if (group == null) {
                main.setVisible(true);
                groupConfig.setVisible(false);
                return;
            }
            groupConfig.clearAllChildren();
            groupConfig.addChild(label(0, 5, 150, 12,
                    Component.translatable("gtceu.central_monitor.gui.currently_editing", group.getName())));
            for (int i = 0; i < 8; i++) {
                ItemSlot slot = new ItemSlot().bind(group.getPlaceholderSlotsHandler(), i);
                slot.layout(layout -> layout.left(-38).top(16 * i + 46).width(18).height(18));
                slot.style(style -> style.background(GuiTextures.SLOT).tooltips(LangHandler
                        .getMultiLang("gtceu.gui.computer_monitor_cover.slot_tooltip", i + 1)
                        .toArray(Component[]::new)));
                groupConfig.addChild(slot);
            }
            ItemSlot moduleSlot = new ItemSlot().bind(group.getItemStackHandler(), 0);
            moduleSlot.layout(layout -> layout.left(0).top(20).width(18).height(18));
            moduleSlot.style(style -> style.background(GuiTextures.SLOT));
            UIElement itemUI = new UIElement().layout(layout -> layout.left(40).top(20).width(190).height(130));
            Runnable refreshModuleUI = () -> {
                itemUI.clearAllChildren();
                ItemStack stack = group.getItemStackHandler().getStackInSlot(0);
                if (stack.getItem() instanceof IComponentItem item) {
                    for (IItemComponent component : item.getComponents()) {
                        if (component instanceof IMonitorModuleItem module) {
                            itemUI.addChild(module.createUIWidget(stack, this, group));
                        }
                    }
                }
            };
            moduleSlot.registerValueListener(stack -> refreshModuleUI.run());
            refreshModuleUI.run();
            groupConfig.addChild(itemUI);
            groupConfig.addChild(moduleSlot);
            main.setVisible(false);
            groupConfig.setVisible(true);
        };
        builder.addChild(groupConfig);

        int[] groupListY = { 5 };
        Consumer<MonitorGroup> addGroupToList = group -> {
            int y = groupListY[0];
            Button labelButton = textButton(20, y, 60, 12, Component.literal(group.getName()));
            labelButton.setOnClick(click -> {
                group.getMonitorPositions().forEach(pos -> {
                    BlockPos rel = toRelative(pos);
                    if (imageButtons.size() - 1 < rel.getY()) return;
                    if (imageButtons.get(rel.getY()).size() - 1 < rel.getX()) return;
                    imageButtons.get(rel.getY()).get(rel.getX()).run();
                });
                if (group.getTargetRaw() != null) {
                    rightClickCallbacks.getOrDefault(group.getTargetRaw(), () -> {}).run();
                }
            });
            groupList.addScrollViewChild(labelButton);

            Button configButton = iconButton(0, y - 3, 16, 16, GuiTextures.IO_CONFIG_COVER_SETTINGS);
            configButton.setOnClick(click -> {
                if (configGroup.get(0) == null) {
                    openGroupConfig.accept(group);
                } else {
                    openGroupConfig.accept(null);
                }
            });
            groupList.addScrollViewChild(configButton);
            groupListY[0] += 15;
        };

        monitorGroups.forEach(addGroupToList);
        main.addChild(options);
        Button removeFromGroupButton = textButton(0, 0, 70, 16,
                Component.translatable("gtceu.central_monitor.gui.remove_from_group"));
        removeFromGroupButton.setVisible(false);
        Button setTargetButton = textButton(0, 18, 70, 16,
                Component.translatable("gtceu.central_monitor.gui.set_target"));
        setTargetButton.setVisible(false);
        Button createGroupButton = textButton(0, 0, 70, 16,
                Component.translatable("gtceu.central_monitor.gui.create_group"));
        createGroupButton.setOnServerClick(click -> {
            MonitorGroup group = new MonitorGroup(
                    Component.translatable("gtceu.gui.central_monitor.group_default_name", monitorGroups.size() + 1)
                            .getString());
            for (IMonitorComponent component : selectedComponents) {
                if (isInAnyGroup(component)) return;
                group.add(component.getBlockPos());
            }
            monitorGroups.add(group);
            addGroupToList.accept(group);

            createGroupButton.setVisible(false);
            removeFromGroupButton.setVisible(true);
            Iterator<IMonitorComponent> it = selectedComponents.iterator();
            while (it.hasNext()) {
                IMonitorComponent c = it.next();
                BlockPos rel = toRelative(c.getBlockPos());
                imageButtons.get(rel.getY()).get(rel.getX()).run();
            }
            if (!selectedTargets.isEmpty()) {
                rightClickCallbacks.getOrDefault(selectedTargets.get(0).getBlockPos(), () -> {}).run();
            }
        });
        setTargetButton.setOnServerClick(click -> {
            MonitorGroup group = null;
            for (MonitorGroup group2 : monitorGroups) {
                for (IMonitorComponent component : selectedComponents) {
                    if (group2.contains(component.getBlockPos())) {
                        group = group2;
                        break;
                    }
                }
                if (group != null) break;
            }
            if (group == null) return;
            if (selectedTargets.isEmpty()) group.setTarget(null);
            else {
                group.setTarget(selectedTargets.get(0).getBlockPos());
                group.setDataSlot(dataSlot[0] - 1);
            }
        });
        removeFromGroupButton.setOnServerClick(click -> {
            for (MonitorGroup group : monitorGroups) {
                for (IMonitorComponent component : selectedComponents) group.remove(component.getBlockPos());
            }
            Iterator<MonitorGroup> itg = monitorGroups.iterator();
            while (itg.hasNext()) {
                MonitorGroup group = itg.next();
                if (group.isEmpty()) {
                    group.getItemStackHandler().dropInventoryInWorld(getLevel(), getBlockPos());
                    group.getPlaceholderSlotsHandler().dropInventoryInWorld(getLevel(), getBlockPos());
                    itg.remove();
                }
            }
            groupList.clearAllScrollViewChildren();
            groupListY[0] = 5;
            monitorGroups.forEach(addGroupToList);

            removeFromGroupButton.setVisible(false);
            createGroupButton.setVisible(true);
            for (IMonitorComponent c : selectedComponents) {
                BlockPos rel = toRelative(c.getBlockPos());
                if (imageButtons.size() - 1 < rel.getY()) continue;
                if (imageButtons.get(rel.getY()).size() - 1 < rel.getX()) continue;
                imageButtons.get(rel.getY()).get(rel.getX()).run();
            }
            if (!selectedTargets.isEmpty()) {
                rightClickCallbacks.getOrDefault(selectedTargets.get(0).getBlockPos(), () -> {}).run();
            }
        });
        createGroupButton.setVisible(false);
        options.addChild(removeFromGroupButton);
        options.addChild(createGroupButton);
        options.addChild(setTargetButton);
        int startX = 20;
        int startY = 30;
        for (int row = 0; row <= downDist + upDist; row++) {
            imageButtons.add(new ArrayList<>());
            for (int col = 0; col <= leftDist + rightDist; col++) {
                IGuiTexture texture = getComponentTexture(row, col);
                GuiTextureGroup textures = new GuiTextureGroup(texture, new ColorBorderTexture(2, 0xFFFFFF));
                IMonitorComponent component = getComponent(row, col);
                if (component == null) {
                    imageButtons.getLast().add(() -> {});
                    continue;
                }
                Button img = iconButton(startX + (16 * col), startY + (16 * row), 16, 16, textures);
                Runnable callback = () -> {
                    if (!component.isMonitor()) return;
                    if (selectedComponents.contains(component)) {
                        selectedComponents.remove(component);

                        if (!selectedTargets.isEmpty() && selectedTargets.get(0) == component) {
                            ColorRectTexture rect = new ColorRectTexture(0x800000ff);
                            textures.setTextures(rect, texture);
                        } else {
                            textures.setTextures(texture);
                        }

                        createGroupButton.setVisible(selectedComponents.stream().noneMatch(this::isInAnyGroup));
                        removeFromGroupButton.setVisible(selectedComponents.stream().allMatch(this::isInAnyGroup));
                        setTargetButton.setVisible(removeFromGroupButton.isVisible());

                        if (selectedComponents.isEmpty()) {
                            createGroupButton.setVisible(false);
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                    } else {
                        boolean inAnyGroup = isInAnyGroup(component);
                        // yes I know this is terrible but if it works don't touch it :)
                        if (selectedComponents.isEmpty() && !inAnyGroup) createGroupButton.setVisible(true);
                        if (inAnyGroup) createGroupButton.setVisible(false);
                        if (selectedComponents.isEmpty() && inAnyGroup) {
                            removeFromGroupButton.setVisible(true);
                            setTargetButton.setVisible(true);
                        }
                        if (!inAnyGroup) {
                            removeFromGroupButton.setVisible(false);
                            setTargetButton.setVisible(false);
                        }
                        selectedComponents.add(component);
                        ColorRectTexture rect = new ColorRectTexture(
                                (selectedTargets.isEmpty() || selectedTargets.get(0) != component) ? 0x80ff0000 :
                                        0x80ff80c0);
                        textures.setTextures(rect, texture);
                    }
                    if (isInAnyGroup(component)) {
                        monitorGroups.forEach(group -> {
                            if (group.contains(component.getBlockPos())) {
                                img.style(style -> style.tooltips(
                                        Component.translatable("gtceu.gui.central_monitor.group", group.getName())));
                            }
                        });
                    } else {
                        img.style(style -> style.tooltips(Component.translatable("gtceu.gui.central_monitor.group",
                                Component.translatable("gtceu.gui.central_monitor.none"))));
                    }
                };
                Runnable rightClickCallback = () -> {
                    if (!selectedTargets.isEmpty()) {
                        if (selectedTargets.get(0).getBlockPos() == component.getBlockPos()) {
                            selectedTargets.clear();
                            if (selectedComponents.contains(component)) {
                                ColorRectTexture rect = new ColorRectTexture(0x80ff0000);
                                textures.setTextures(rect, texture);
                            } else {
                                textures.setTextures(texture);
                            }
                            dataSlotInput.setVisible(false);
                            return;
                        } else {
                            try {
                                rightClickCallbacks.get(selectedTargets.get(0).getBlockPos()).run();
                            } catch (StackOverflowError e) {
                                ExtForCore.LOGGER.error(
                                        "Stack overflow when right-clicking monitor component {} at {} (selectedTarget is {} at {})",
                                        component, component.getBlockPos(), selectedTargets.get(0),
                                        selectedTargets.get(0).getBlockPos());
                            }
                        }
                    }
                    selectedTargets.add(component);
                    ColorRectTexture rect;
                    if (selectedComponents.contains(component)) {
                        rect = new ColorRectTexture(0x80ff80c0);
                    } else {
                        rect = new ColorRectTexture(0x800000ff);
                    }
                    textures.setTextures(rect, texture);
                    if (component.getDataItems() != null) {
                        IItemHandler dataItems = component.getDataItems();
                        MonitorGroup selectedGroup = null;
                        for (MonitorGroup group : monitorGroups) {
                            for (IMonitorComponent c : selectedComponents) {
                                if (group.contains(c.getBlockPos())) {
                                    if (selectedGroup == null || selectedGroup == group) {
                                        selectedGroup = group;
                                    } else {
                                        selectedGroup = null;
                                        break;
                                    }
                                }
                            }
                        }
                        if (selectedGroup != null) {
                            dataSlot[0] = selectedGroup.getDataSlot() + 1;
                        }
                        dataSlot[1] = dataItems.getSlots();
                        dataSlotInput.setVisible(true);
                    }
                };
                if (isInAnyGroup(component)) {
                    monitorGroups.forEach(group -> {
                        if (group.contains(component.getBlockPos())) img.style(style -> style.tooltips(
                                Component.translatable("gtceu.gui.central_monitor.group", group.getName())));
                    });
                } else {
                    img.style(style -> style.tooltips(Component.translatable("gtceu.gui.central_monitor.group",
                            Component.translatable("gtceu.gui.central_monitor.none"))));
                }
                img.setOnClick(click -> {
                    if (click.button == 0) callback.run();
                    else if (click.button == 1) rightClickCallback.run();
                });
                componentSelection.addScrollViewChild(img);
                imageButtons.getLast().add(callback);
                rightClickCallbacks.put(component.getBlockPos(), rightClickCallback);
            }
        }
        builder.addChild(main);
        return builder;
    }

    @Override
    public IGuiTexture getComponentIcon() {
        return SpriteTexture.of("extfrocore:textures/block/multiblock/network_switch/overlay_front_active.png");
    }

    private Button iconButton(int x, int y, int width, int height, IGuiTexture texture) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        return button;
    }

    private Button textButton(int x, int y, int width, int height, Component text) {
        IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON,
                new TextTexture(text.getString()).setType(TextTexture.TextType.LEFT).setWidth(width - 4));
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        button.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        button.style(style -> style.tooltips(text));
        return button;
    }

    private TextField intInput(int x, int y, int width, int height, int value, int min, int max,
                               Consumer<Integer> setter) {
        TextField field = new TextField();
        field.layout(layout -> layout.left(x).top(y).width(width).height(height));
        field.style(style -> style.background(GuiTextures.DISPLAY));
        field.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        field.setNumbersOnlyInt(min, max);
        field.setText(String.valueOf(value));
        field.setTextResponder(text -> {
            if (!text.isBlank()) {
                setter.accept(Mth.clamp(Integer.parseInt(text), min, max));
            }
        });
        return field;
    }

    @Override
    public @NotNull List<Component> getDebugInfo(Player player, int logLevel,
                                                 PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public @NotNull List<Component> getDataInfo(PortableScannerBehavior.DisplayMode mode) {
        return List.of(Component.translatable("gtceu.central_monitor.size", leftDist, rightDist, upDist, downDist));
    }

    @Override
    public void onMachineDestroyed() {
        super.onMachineDestroyed();
        for (MonitorGroup group : monitorGroups) {
            group.getItemStackHandler().dropInventoryInWorld(getLevel(), getBlockPos());;
            group.getPlaceholderSlotsHandler().dropInventoryInWorld(getLevel(), getBlockPos());
        }
    }
}
