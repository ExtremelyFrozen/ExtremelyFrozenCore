package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.integration.xei.entry.item.ItemEntryList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemStackList;
import com.extfro.extfrocore.integration.xei.entry.item.ItemTagList;
import com.extfro.extfrocore.integration.xei.handlers.item.CycleItemEntryHandler;

import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

import com.lowdragmc.lowdraglib2.configurator.ConfiguratorParser;
import com.lowdragmc.lowdraglib2.configurator.annotation.ConfigSetter;
import com.lowdragmc.lowdraglib2.configurator.annotation.Configurable;
import com.lowdragmc.lowdraglib2.configurator.ui.ConfiguratorGroup;
import com.lowdragmc.lowdraglib2.gui.slot.ItemHandlerSlot;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.integration.xei.IngredientIO;
import com.lowdragmc.lowdraglib2.math.Position;
import com.lowdragmc.lowdraglib2.math.Size;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

@LDLRegister(name = "gtm_item_slot", group = "widget.gtm_container", priority = 50, registry = "ldlib2:ui_element")
public class SlotWidget extends ItemSlot {

    @Configurable(name = "ldlib.gui.editor.name.canTakeItems")
    protected boolean canTakeItems = true;
    @Configurable(name = "ldlib.gui.editor.name.canPutItems")
    protected boolean canPutItems = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverOverlay")
    protected boolean drawHoverOverlay = true;
    @Configurable(name = "ldlib.gui.editor.name.drawHoverTips")
    protected boolean drawHoverTips = true;

    @Getter
    protected IngredientIO ingredientIO = IngredientIO.NONE;
    protected float XEIChance = 1.0f;

    protected Runnable changeListener;
    protected Function<ItemStack, ItemStack> itemHook = Function.identity();
    protected BiConsumer<SlotWidget, List<Component>> onAddedTooltips;

    public SlotWidget() {
        super();
        initSlot(0, 0, 18, 18);
    }

    public SlotWidget(Container inventory, int slotIndex, int xPosition, int yPosition, boolean canTakeItems,
                      boolean canPutItems) {
        this();
        setCanTakeItems(canTakeItems);
        setCanPutItems(canPutItems);
        setContainerSlot(inventory, slotIndex);
        setSelfPosition(xPosition, yPosition);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition,
                      boolean canTakeItems, boolean canPutItems) {
        this();
        setCanTakeItems(canTakeItems);
        setCanPutItems(canPutItems);
        setHandlerSlot(itemHandler, slotIndex);
        setSelfPosition(xPosition, yPosition);
    }

    public SlotWidget(IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        this(itemHandler, slotIndex, xPosition, yPosition, true, true);
    }

    public SlotWidget(Container container, int slotIndex, int xPosition, int yPosition) {
        this(container, slotIndex, xPosition, yPosition, true, true);
    }

    protected void initSlot(int x, int y, int width, int height) {
        layout(layout -> layout.left(x).top(y).width(width).height(height));
        slotStyle(style -> style.showItemTooltips(drawHoverTips));
        addEventListener(UIEvents.HOVER_TOOLTIPS, this::addExtraTooltips);
    }

    protected void setSelfPosition(int xPosition, int yPosition) {
        layout(layout -> layout.left(xPosition).top(yPosition));
    }

    public Position getPosition() {
        return new Position(Math.round(getPositionX()), Math.round(getPositionY()));
    }

    public Size getSize() {
        return new Size(Math.round(getSizeWidth()), Math.round(getSizeHeight()));
    }

    public Rect2i toRectangleBox() {
        var pos = getPosition();
        var size = getSize();
        return new Rect2i(pos.x, pos.y, size.width, size.height);
    }

    protected Slot createSlot(IItemHandlerModifiable itemHandler, int index) {
        return new WidgetSlotItemHandler(itemHandler, index, 0, 0);
    }

    public SlotWidget setContainerSlot(Container inventory, int slotIndex) {
        return updateSlot(new Slot(inventory, slotIndex, 0, 0) {

            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return SlotWidget.this.canPutStack(stack) && super.mayPlace(stack);
            }

            @Override
            public boolean mayPickup(@Nullable Player player) {
                return SlotWidget.this.canTakeStack(player) && super.mayPickup(player);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                SlotWidget.this.onSlotChanged();
            }
        });
    }

    public SlotWidget updateSlot(Slot slot) {
        bind(slot);
        return this;
    }

    public SlotWidget setHandlerSlot(IItemHandlerModifiable itemHandler, int slotIndex) {
        updateSlot(createSlot(itemHandler, slotIndex));
        return this;
    }

    public SlotWidget setBackgroundTexture(IGuiTexture backgroundTexture) {
        style(style -> style.background(backgroundTexture));
        return this;
    }

    public SlotWidget setBackground(IGuiTexture backgroundTexture) {
        return setBackgroundTexture(backgroundTexture);
    }

    public SlotWidget setBackground(IGuiTexture backgroundTexture, IGuiTexture overlayTexture) {
        style(style -> style.background(backgroundTexture).overlay(overlayTexture));
        return this;
    }

    public SlotWidget setOverlay(IGuiTexture overlayTexture) {
        style(style -> style.overlay(overlayTexture));
        return this;
    }

    public SlotWidget setLocationInfo(boolean isPlayerContainer, boolean isPlayerHotBar) {
        slotStyle(style -> style.isPlayerSlot(isPlayerContainer).quickMovePriority(isPlayerHotBar ? 100 : 90));
        return this;
    }

    @ConfigSetter(field = "canTakeItems")
    public SlotWidget setCanTakeItems(boolean canTakeItems) {
        this.canTakeItems = canTakeItems;
        return this;
    }

    @ConfigSetter(field = "canPutItems")
    public SlotWidget setCanPutItems(boolean canPutItems) {
        this.canPutItems = canPutItems;
        return this;
    }

    public SlotWidget setDrawHoverOverlay(boolean drawHoverOverlay) {
        this.drawHoverOverlay = drawHoverOverlay;
        slotStyle(style -> style.hoverOverlay(drawHoverOverlay ? DRAGGING_BG : IGuiTexture.EMPTY));
        return this;
    }

    public SlotWidget setDrawHoverTips(boolean drawHoverTips) {
        this.drawHoverTips = drawHoverTips;
        slotStyle(style -> style.showItemTooltips(drawHoverTips));
        return this;
    }

    public SlotWidget setIngredientIO(IngredientIO ingredientIO) {
        this.ingredientIO = ingredientIO;
        return this;
    }

    public SlotWidget setChangeListener(Runnable changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public SlotWidget setXEIChance(float XEIChance) {
        this.XEIChance = XEIChance;
        return this;
    }

    public float getXEIChance() {
        return XEIChance;
    }

    public SlotWidget setItemHook(Function<ItemStack, ItemStack> itemHook) {
        this.itemHook = itemHook == null ? Function.identity() : itemHook;
        return this;
    }

    public SlotWidget setOnAddedTooltips(BiConsumer<SlotWidget, List<Component>> onAddedTooltips) {
        this.onAddedTooltips = onAddedTooltips;
        return this;
    }

    public SlotWidget setHoverTooltips(String... tooltips) {
        style(style -> style.tooltips(tooltips));
        return this;
    }

    public SlotWidget setHoverTooltips(Component... tooltips) {
        style(style -> style.tooltips(tooltips));
        return this;
    }

    public boolean canTakeStack(@Nullable Player player) {
        return canTakeItems;
    }

    public boolean canPutStack(ItemStack stack) {
        return canPutItems;
    }

    public boolean canMergeSlot(ItemStack stack) {
        return true;
    }

    protected void onSlotChanged() {
        if (changeListener != null) {
            changeListener.run();
        }
    }

    public ItemStack getRealStack(ItemStack itemStack) {
        return itemHook.apply(itemStack);
    }

    private void addExtraTooltips(UIEvent event) {
        if (onAddedTooltips == null || event.hoverTooltips == null) {
            return;
        }
        var tooltips = new java.util.ArrayList<>(event.hoverTooltips.tooltipTexts());
        onAddedTooltips.accept(this, tooltips);
        event.hoverTooltips = new HoverTooltips(tooltips, event.hoverTooltips.tooltipComponent(),
                event.hoverTooltips.tooltipFont(), event.hoverTooltips.tooltipStack());
    }

    @Override
    public ItemStack getValue() {
        return getRealStack(super.getValue());
    }

    @Override
    public void buildConfigurator(ConfiguratorGroup father) {
        var handler = new ItemStackHandler();
        handler.setStackInSlot(0, Blocks.STONE.asItem().getDefaultInstance());
        ConfiguratorParser.createConfigurators(father, new HashMap<>(), getClass(), this);
    }

    public List<Object> getXEIIngredients() {
        var stack = getValue();
        if (stack.isEmpty()) return Collections.emptyList();
        if (getSlot() instanceof WidgetSlotItemHandler slotHandler &&
                slotHandler.itemHandler instanceof CycleItemEntryHandler entryHandler) {
            return getXEIIngredientsClickable(entryHandler, slotHandler.index);
        }
        return List.of(convertIngredient(stack));
    }

    @Nullable
    public Object getXEIIngredientOverMouse(double mouseX, double mouseY) {
        if (isMouseOverElement((float) mouseX, (float) mouseY)) {
            var ingredients = getXEIIngredients();
            return ingredients.isEmpty() ? null : ingredients.getFirst();
        }
        return null;
    }

    private Object convertIngredient(ItemStack itemStack) {
        if (ExtForCore.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredient(itemStack, getXEIChance());
        }
        return itemStack;
    }

    private List<Object> getXEIIngredientsClickable(CycleItemEntryHandler handler, int index) {
        ItemEntryList entryList = handler.getEntry(index);
        if (ExtForCore.Mods.isEMILoaded()) {
            return EMICallWrapper.getEMIIngredients(entryList, getXEIChance(), this::getRealStack);
        }
        return Collections.emptyList();
    }

    public class WidgetSlotItemHandler extends ItemHandlerSlot {

        @Getter
        private final IItemHandlerModifiable itemHandler;
        private final int index;

        public WidgetSlotItemHandler(IItemHandlerModifiable itemHandler, int index, int xPosition, int yPosition) {
            super(itemHandler, index, xPosition, yPosition);
            this.itemHandler = itemHandler;
            this.index = index;
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return SlotWidget.this.canPutStack(stack) && super.mayPlace(stack);
        }

        @Override
        public boolean mayPickup(@Nullable Player playerIn) {
            return SlotWidget.this.canTakeStack(playerIn) && super.mayPickup(playerIn);
        }

        @Override
        public void setChanged() {
            super.setChanged();
            SlotWidget.this.onSlotChanged();
        }

        @Override
        public boolean isActive() {
            return SlotWidget.this.isActive();
        }
    }

    public static final class EMICallWrapper {

        private static EmiIngredient toEMIIngredient(Stream<ItemStack> stream, UnaryOperator<ItemStack> realStack) {
            return EmiIngredient.of(stream.map(realStack).map(EmiStack::of).toList());
        }

        public static List<Object> getEMIIngredients(ItemStackList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            return List.of(toEMIIngredient(list.stream(), realStack).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(ItemTagList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            return List.of(toEMIIngredient(list.getStacks().stream(), realStack).setChance(xeiChance));
        }

        public static List<Object> getEMIIngredients(ItemEntryList list, float xeiChance,
                                                     UnaryOperator<ItemStack> realStack) {
            if (list instanceof ItemTagList tagList) return getEMIIngredients(tagList, xeiChance, realStack);
            if (list instanceof ItemStackList stackList) return getEMIIngredients(stackList, xeiChance, realStack);
            return Collections.emptyList();
        }

        public static Object getEMIIngredient(ItemStack stack, float xeiChance) {
            return EmiStack.of(stack).setChance(xeiChance);
        }
    }
}
