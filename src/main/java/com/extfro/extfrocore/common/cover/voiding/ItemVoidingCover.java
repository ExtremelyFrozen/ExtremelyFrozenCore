package com.extfro.extfrocore.common.cover.voiding;

import com.extfro.extfrocore.api.capability.ICoverable;
import com.extfro.extfrocore.api.cover.CoverDefinition;
import com.extfro.extfrocore.api.cover.IUICover;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.tool.GTToolType;
import com.extfro.extfrocore.common.cover.ConveyorCover;
import com.extfro.extfrocore.common.data.item.GTItemAbilities;
import com.extfro.extfrocore.utils.ExtendedUseOnContext;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class ItemVoidingCover extends ConveyorCover implements IUICover {

    public ItemVoidingCover(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        super(definition, coverHolder, attachedSide, 0);
        setWorkingEnabled(false);
    }

    @Override
    protected boolean isSubscriptionActive() {
        return isWorkingEnabled();
    }

    //////////////////////////////////////////////
    // *********** COVER LOGIC ***********//
    //////////////////////////////////////////////

    @Override
    protected void update() {
        if (coverHolder.getOffsetTimer() % 5 != 0)
            return;

        doVoidItems();
        subscriptionHandler.updateSubscription();
    }

    protected void doVoidItems() {
        IItemHandler handler = getOwnItemHandler();
        if (handler == null) {
            return;
        }
        voidAny(handler);
    }

    void voidAny(IItemHandler handler) {
        ItemFilter filter = filterHandler.getFilter();

        for (int slot = 0; slot < handler.getSlots(); slot++) {
            ItemStack sourceStack = handler.extractItem(slot, Integer.MAX_VALUE, true);
            if (sourceStack.isEmpty() || !filter.test(sourceStack)) {
                continue;
            }
            handler.extractItem(slot, Integer.MAX_VALUE, false);
        }
    }

    //////////////////////////////////////
    // *********** GUI ***********//
    //////////////////////////////////////

    @Override
    public UIElement createUIElement() {
        final var group = new UIElement().layout(layout -> layout.width(176).height(120));
        group.addChild(label(10, 5, getUITitle(), 130));

        group.addChild(toggleButton(10, 20, GuiTextures.BUTTON_POWER, this::isWorkingEnabled, this::setWorkingEnabled));

        group.addChild(filterHandler.createFilterSlotUI(148, 91));
        group.addChild(filterHandler.createFilterConfigUI(10, 50, 126, 60));

        buildVoidingAdditionalUI(group);

        return group;
    }

    protected void buildVoidingAdditionalUI(UIElement group) {
        // Hook for advanced voiding covers.
    }

    protected Label label(int x, int y, String translationKey, int width) {
        Label label = new Label();
        label.setValue(Component.translatable(translationKey));
        label.layout(layout -> layout.left(x).top(y).width(width).height(10));
        label.textStyle(style -> style.textColor(0x404040).textShadow(false));
        return label;
    }

    protected Button toggleButton(int x, int y, IGuiTexture icon, BooleanSupplier getter, Consumer<Boolean> setter) {
        Button button = new Button().noText();
        button.layout(layout -> layout.left(x).top(y).width(20).height(20));
        Consumer<Button> update = b -> {
            IGuiTexture texture = new GuiTextureGroup(GuiTextures.VANILLA_BUTTON.copy()
                    .setColor(getter.getAsBoolean() ? 0xffa0ffa0 : -1), icon);
            b.buttonStyle(style -> style.baseTexture(texture).hoverTexture(texture).pressedTexture(texture));
        };
        update.accept(button);
        button.setOnServerClick(event -> {
            setter.accept(!getter.getAsBoolean());
            update.accept(button);
        });
        return button;
    }

    @NotNull
    protected String getUITitle() {
        return "cover.item.voiding.title";
    }

    @Override
    public InteractionResult onSoftMalletClick(ExtendedUseOnContext context) {
        if (!context.getItemInHand().canPerformAction(GTItemAbilities.MALLET_PAUSE)) {
            return InteractionResult.PASS;
        }
        if (!isRemote()) {
            setWorkingEnabled(!isWorkingEnabled);
            context.getPlayer().sendSystemMessage(Component.translatable(isWorkingEnabled() ?
                    "cover.voiding.message.enabled" : "cover.voiding.message.disabled"));
        }
        return InteractionResult.sidedSuccess(isRemote());
    }

    @Override
    public @Nullable IGuiTexture sideTips(Player player, BlockPos pos, BlockState state, Set<GTToolType> toolTypes,
                                          ItemStack held, Direction side) {
        var superTips = super.sideTips(player, pos, state, toolTypes, held, side);
        if (superTips != null) return superTips;
        if (toolTypes.contains(GTToolType.SOFT_MALLET)) {
            return isWorkingEnabled() ? GuiTextures.TOOL_START : GuiTextures.TOOL_PAUSE;
        }
        return null;
    }
}
