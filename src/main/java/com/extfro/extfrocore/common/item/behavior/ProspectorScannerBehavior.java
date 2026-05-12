package com.extfro.extfrocore.common.item.behavior;

import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IElectricItem;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.misc.ProspectorMode;
import com.extfro.extfrocore.api.gui.widget.ProspectingMapWidget;
import com.extfro.extfrocore.api.item.component.IAddInformation;
import com.extfro.extfrocore.api.item.component.IInteractionItem;
import com.extfro.extfrocore.api.item.component.IItemUIFactory;
import com.extfro.extfrocore.common.data.item.GTDataComponents;
import com.extfro.extfrocore.config.ConfigHolder;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Switch;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ProspectorScannerBehavior implements IItemUIFactory, IInteractionItem, IAddInformation {

    private final int radius;
    private final long cost;
    private final ProspectorMode<?>[] modes;

    public ProspectorScannerBehavior(int radius, long cost, ProspectorMode<?>... modes) {
        this.radius = radius + 1;
        this.modes = Arrays.stream(modes).filter(Objects::nonNull).toArray(ProspectorMode[]::new);
        this.cost = cost;
    }

    @NotNull
    public ProspectorMode<?> getMode(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return modes[0];
        }
        return modes[stack.getOrDefault(GTDataComponents.SCANNER_MODE, (byte) 0) % modes.length];
    }

    public void setNextMode(ItemStack stack) {
        stack.update(GTDataComponents.SCANNER_MODE, (byte) 0, mode -> (byte) ((mode + 1) % modes.length));
    }

    public boolean drainEnergy(@NotNull ItemStack stack, boolean simulate) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null) return false;

        int amount = Math.round(cost * (ConfigHolder.INSTANCE.machines.prospectorEnergyUseMultiplier / 100F));

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level level, Player player,
                                                  InteractionHand usedHand) {
        if (player.isShiftKeyDown() && modes.length > 1) {
            if (!level.isClientSide) {
                setNextMode(item);
                var mode = getMode(item);
                player.sendSystemMessage(Component.translatable(mode.unlocalizedName));
            }
            return InteractionResultHolder.success(item);
        }
        if (!player.isCreative() && !drainEnergy(item, true)) {
            player.sendSystemMessage(Component.translatable("behavior.prospector.not_enough_energy"));
            return InteractionResultHolder.success(item);
        }
        return IItemUIFactory.super.use(item, level, player, usedHand);
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        var mode = getMode(holder.itemStack);
        var map = new ProspectingMapWidget(4, 4, 332 - 8, 200 - 8, radius, mode, 1, holder.hand);
        UIElement root = new UIElement()
                .layout(layout -> layout.width(332).height(200))
                .style(style -> style.background(GuiTextures.BACKGROUND));
        root.addChild(map);

        Switch darkModeSwitch = new Switch();
        darkModeSwitch.layout(layout -> layout.left(-20).top(4).width(18).height(18));
        darkModeSwitch.setOn(map.isDarkMode());
        darkModeSwitch.setOnSwitchChanged(map::setDarkMode);
        darkModeSwitch.switchStyle(style -> style.baseTexture(new GuiTextureGroup(GuiTextures.BUTTON,
                GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().setSprite(0, 8, 20, 8).scale(0.8f)))
                .pressedTexture(new GuiTextureGroup(GuiTextures.BUTTON,
                        GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().setSprite(0, 0, 20, 8).scale(0.8f)))
                .markTexture(GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().setSprite(0, 0, 20, 8).scale(0.8f))
                .unmarkTexture(GuiTextures.PROGRESS_BAR_SOLAR_STEAM.get(true).copy().setSprite(0, 8, 20, 8).scale(0.8f)));
        root.addChild(darkModeSwitch);
        return ModularUI.of(UI.of(root), holder.player);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents,
                                TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.translatable("metaitem.prospector.tooltip.radius", radius));
        tooltipComponents.add(Component.translatable("metaitem.prospector.tooltip.modes"));
        for (ProspectorMode<?> mode : modes) {
            tooltipComponents.add(Component.literal(" -").append(Component.translatable(mode.unlocalizedName))
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
        }
    }
}
