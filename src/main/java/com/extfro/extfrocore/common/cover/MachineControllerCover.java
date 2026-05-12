package com.extfro.extfrocore.common.cover;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IControllable;
import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverBehavior;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.machine.MachineCoverContainer;
import com.extfro.extfrocore.api.sync_system.annotations.SaveField;
import com.extfro.extfrocore.api.sync_system.annotations.SyncToClient;
import com.extfro.extfrocore.api.transfer.item.CustomItemStackHandler;
import com.extfro.extfrocore.common.cover.data.ControllerMode;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MachineControllerCover extends CoverBehavior implements IUICover {

    private CustomItemStackHandler sideCoverSlot;
    private Button modeButton;

    @SaveField
    @Getter
    private boolean isInverted = false;

    @SaveField
    @Getter
    private int minRedstoneStrength = 1;

    @SaveField
    @SyncToClient
    @Getter
    @Nullable
    private ControllerMode controllerMode = ControllerMode.MACHINE;

    @Getter
    @Accessors(fluent = true)
    @SaveField
    private boolean preventPowerFail = false;

    public boolean preventPowerFail() {
        return preventPowerFail;
    }

    public MachineControllerCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide);
    }

    @Override
    public boolean canAttach() {
        return super.canAttach() && !getAllowedModes().isEmpty();
    }

    @Override
    public void onAttached(ItemStack itemStack, @Nullable ServerPlayer player) {
        super.onAttached(itemStack, player);

        var allowedModes = getAllowedModes();
        setControllerMode(allowedModes.isEmpty() ? null : allowedModes.get(0));
    }

    @Override
    public void onRemoved() {
        super.onRemoved();

        resetCurrentControllable();
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }

    @Override
    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        super.onNeighborChanged(block, fromPos, isMoving);

        updateInput();
    }

    public void setControllerMode(@Nullable ControllerMode controllerMode) {
        resetCurrentControllable();

        this.controllerMode = controllerMode;
        syncDataHolder.markClientSyncFieldDirty("filterMode");

        updateAll();
    }

    public void setMinRedstoneStrength(int minRedstoneStrength) {
        this.minRedstoneStrength = minRedstoneStrength;
        updateAll();
    }

    public void setInverted(boolean inverted) {
        isInverted = inverted;
        updateAll();
    }

    private void updateAll() {
        updateInput();
        updateUI();
    }

    ///////////////////////////////////////////////////
    // *********** CONTROLLER LOGIC ***********//
    ///////////////////////////////////////////////////

    @Nullable
    private IControllable getControllable(@Nullable Direction side) {
        if (side == null) {
            return GTCapabilityHelper.getControllable(coverHolder.getLevel(), coverHolder.getBlockPos(), null);
        }

        if (coverHolder.getCoverAtSide(side) instanceof IControllable cover) {
            return cover;
        } else {
            return null;
        }
    }

    private void updateInput() {
        if (controllerMode == null)
            return;

        IControllable controllable = getControllable(controllerMode.side);
        if (controllable != null) {
            controllable.setWorkingEnabled(shouldAllowWorking() && doOthersAllowWorking());
        }
    }

    private void resetCurrentControllable() {
        if (controllerMode == null)
            return;

        IControllable controllable = getControllable(controllerMode.side);
        if (controllable != null) {
            controllable.setWorkingEnabled(doOthersAllowWorking());
        }
    }

    private boolean shouldAllowWorking() {
        boolean shouldAllowWorking = getInputSignal() < minRedstoneStrength;

        return isInverted != shouldAllowWorking;
    }

    private boolean doOthersAllowWorking() {
        return coverHolder.getCovers().stream()
                .filter(cover -> this.attachedSide != cover.attachedSide)
                .filter(cover -> cover instanceof MachineControllerCover)
                .filter(cover -> ((MachineControllerCover) cover).controllerMode == this.controllerMode)
                .allMatch(cover -> ((MachineControllerCover) cover).shouldAllowWorking());
    }

    public List<ControllerMode> getAllowedModes() {
        return Arrays.stream(ControllerMode.values())
                .filter(mode -> mode.side != this.attachedSide)
                .filter(mode -> getControllable(mode.side) != null)
                .collect(Collectors.toList());
    }

    private int getInputSignal() {
        Level level = coverHolder.getLevel();
        BlockPos sourcePos = coverHolder.getBlockPos().relative(attachedSide);

        return level.getSignal(sourcePos, attachedSide);
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIElement() {
        if (controllerMode != null && getControllable(controllerMode.side) == null) {
            setControllerMode(null);
        }
        UIElement group = new UIElement().layout(layout -> layout.width(176).height(95));

        group.addChild(label(10, 5, "cover.machine_controller.title", 156));
        TextField redstoneInput = new TextField();
        redstoneInput.layout(layout -> layout.left(10).top(20).width(131).height(20));
        redstoneInput.style(style -> style.background(GuiTextures.DISPLAY));
        redstoneInput.textFieldStyle(style -> style.textColor(0x404040).textShadow(false));
        redstoneInput.setNumbersOnlyInt(1, 15);
        redstoneInput.setText(String.valueOf(minRedstoneStrength));
        redstoneInput.setTextResponder(value -> {
            if (!value.isBlank()) {
                setMinRedstoneStrength(Integer.parseInt(value));
            }
        });
        group.addChild(redstoneInput);

        modeButton = new Button().noText();
        modeButton.layout(layout -> layout.left(10).top(45).width(131).height(20));
        modeButton.setOnServerClick(event -> selectNextMode());
        group.addChild(modeButton);

        group.addChild(toggleButton(146, 20, 20, 20,
                GuiTextures.INVERT_REDSTONE_BUTTON, () -> isInverted, this::setInverted,
                "cover.machine_controller.invert"));

        group.addChild(label(10, 72, "cover.machine_controller.suspend_powerfail", 130));
        group.addChild(toggleButton(147, 68, 18, 18, GuiTextures.BUTTON_POWER,
                () -> preventPowerFail, data -> {
                    preventPowerFail = data;
                    updateAll();
                }, null));

        sideCoverSlot = new CustomItemStackHandler(1);
        ItemSlot sideSlot = new ItemSlot().bind(sideCoverSlot, 0);
        sideSlot.layout(layout -> layout.left(147).top(46).width(18).height(18));
        sideSlot.style(style -> style.background(GuiTextures.SLOT));
        sideSlot.slotStyle(style -> style.acceptQuickMove(false));
        sideSlot.xeiPhantom();
        sideSlot.setActive(false);
        group.addChild(sideSlot);

        updateUI();

        return group;
    }

    private Label label(int x, int y, String translationKey, int width) {
        Label label = new Label();
        label.setValue(Component.translatable(translationKey));
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    private Button toggleButton(int x, int y, int width, int height, IGuiTexture icon, BooleanSupplier getter,
                                Consumer<Boolean> setter, @Nullable String tooltipPrefix) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(width).height(height));
        Consumer<Button> update = b -> {
            IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON.copy()
                    .setColor(getter.getAsBoolean() ? 0xffa0ffa0 : -1), icon);
            b.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
            if (tooltipPrefix != null) {
                b.style(style -> style.tooltips(Component.translatable(tooltipPrefix + "." +
                        (getter.getAsBoolean() ? "enabled" : "disabled"))));
            }
        };
        update.accept(button);
        button.setOnServerClick(event -> {
            setter.accept(!getter.getAsBoolean());
            update.accept(button);
        });
        return button;
    }

    private void selectNextMode() {
        var allowedModes = getAllowedModes();

        setControllerMode(allowedModes.stream()
                .dropWhile(mode -> this.controllerMode != null && mode != this.controllerMode)
                .skip(1)
                .findFirst()
                .orElse(allowedModes.isEmpty() ? null : allowedModes.get(0)));

        updateAll();
    }

    private void updateUI() {
        updateModeButton();
        updateCoverSlot();
    }

    private void updateModeButton() {
        if (modeButton == null) {
            return;
        }

        IGuiTexture texture = new GuiTextureGroup(
                GuiTextures.VANILLA_BUTTON,
                new TextTexture(controllerMode != null ? controllerMode.localeName : ControllerMode.nullLocaleName)
                        .setWidth(131));
        modeButton.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
    }

    private void updateCoverSlot() {
        if (sideCoverSlot == null) {
            return;
        }

        if (controllerMode == null) {
            sideCoverSlot.setStackInSlot(0, ItemStack.EMPTY);
            sideCoverSlot.onContentsChanged(0);
        } else {
            var side = controllerMode.side;
            if (side == null && coverHolder instanceof MachineCoverContainer coverContainer) {
                sideCoverSlot.setStackInSlot(0, coverContainer.getMachine().getDefinition().asStack());
            } else {
                var cover = coverHolder.getCoverAtSide(side);
                if (cover != null) {
                    sideCoverSlot.setStackInSlot(0, cover.getAttachItem().copy());
                } else {
                    sideCoverSlot.setStackInSlot(0, ItemStack.EMPTY);
                }
            }
            sideCoverSlot.onContentsChanged(0);
        }
    }

    @Override
    public CompoundTag copyConfig(CompoundTag tag) {
        tag.putBoolean("inverted", isInverted);
        tag.putInt("redstoneLvl", minRedstoneStrength);
        tag.putBoolean("preventPowerfail", preventPowerFail);
        return super.copyConfig(tag);
    }

    @Override
    public void pasteConfig(ServerPlayer player, CompoundTag tag) {
        setInverted(tag.getBoolean("inverted"));
        setMinRedstoneStrength(tag.getInt("redstoneLvl"));
        preventPowerFail = tag.getBoolean("preventPowerfail");
        super.pasteConfig(player, tag);
    }
}
