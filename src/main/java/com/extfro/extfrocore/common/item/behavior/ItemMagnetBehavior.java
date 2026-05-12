package com.extfro.extfrocore.common.item.behavior;

import com.extfro.extfrocore.ExtForCore;
import com.extfro.extfrocore.api.EFValues;
import com.extfro.extfrocore.api.capability.GTCapabilityHelper;
import com.extfro.extfrocore.api.capability.IElectricItem;
import com.extfro.extfrocore.api.cover.filter.ItemFilter;
import com.extfro.extfrocore.api.cover.filter.SimpleItemFilter;
import com.extfro.extfrocore.api.cover.filter.TagItemFilter;
import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.item.ComponentItem;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.api.item.component.IAddInformation;
import com.extfro.extfrocore.api.item.component.IInteractionItem;
import com.extfro.extfrocore.api.item.component.IItemLifeCycle;
import com.extfro.extfrocore.api.item.component.IItemUIFactory;
import com.extfro.extfrocore.common.data.GTItems;
import com.extfro.extfrocore.common.data.item.GTDataComponents;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Selector;
import com.lowdragmc.lowdraglib2.gui.ui.elements.inventory.InventorySlots;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import io.netty.buffer.ByteBuf;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.jetbrains.annotations.NotNull;
import oshi.util.tuples.Triplet;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ItemMagnetBehavior implements IInteractionItem, IItemLifeCycle, IAddInformation, IItemUIFactory {

    private final int range;
    private final long energyDraw;

    public ItemMagnetBehavior(int range) {
        this.range = range;
        this.energyDraw = EFValues.V[range > 8 ? EFValues.HV : EFValues.LV];
    }

    static {
        NeoForge.EVENT_BUS.register(ItemMagnetBehavior.class);
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        final ItemStack held = holder.itemStack;
        MagnetComponent magnetData = held.getOrDefault(GTDataComponents.MAGNET, MagnetComponent.EMPTY);
        Filter selected = magnetData.filterType();

        HashSet<Triplet<Filter, UIElement, UIElement>> widgets = new HashSet<>();
        HashMap<Filter, ItemFilter> filters = new HashMap<>();
        UIElement root = new UIElement()
                .layout(layout -> layout.width(176).height(157))
                .style(style -> style.background(GuiTextures.BACKGROUND));

        Selector<Filter> selector = new Selector<>();
        selector.layout(layout -> layout.left(120).top(5).width(46).height(20));
        selector.setCandidates(Arrays.asList(Filter.values()));
        selector.setSelected(selected);
        selector.setOnValueChanged(val -> updateSelection(held, val, widgets));
        selector.setCandidateUIProvider(filter -> {
            Label label = new Label();
            label.setValue(Component.translatable(filter.getTooltip()));
            label.layout(layout -> layout.width(60).height(12));
            return label;
        });
        root.addChild(selector);

        InventorySlots inventory = new InventorySlots();
        inventory.layout(layout -> layout.left(7).top(75).width(162).height(76));
        inventory.apply(slot -> slot.style(style -> style.background(GuiTextures.SLOT)));
        root.addChild(inventory);

        for (Filter f : Filter.values()) {
            ItemStack stack = f.getFilter(held);
            ItemFilter filter = ItemFilter.loadFilter(stack);
            filters.put(f, filter);
            Label description = new Label();
            description.setValue(Component.translatable(stack.getDescriptionId()));
            description.layout(layout -> layout.left(5).top(5).width(110).height(10));
            description.textStyle(style -> style.textColor(0x404040).textShadow(false));
            UIElement config = filter.openConfigurator((176 - 80) / 2, (60 - 55) / 2 + 15);
            boolean visible = f == selected;
            description.setVisible(visible);
            config.setVisible(visible);
            widgets.add(new Triplet<>(f, description, config));
            root.addChild(description);
            root.addChild(config);
        }
        // TODO LDLib2 close hook migration: save on selection changes and filter updates already covers normal edits.
        return ModularUI.of(UI.of(root), holder.player);
    }

    private void updateSelection(ItemStack stack, Filter filter, Collection<Triplet<Filter, UIElement, UIElement>> widgets) {
        stack.update(GTDataComponents.MAGNET, MagnetComponent.EMPTY, c -> new MagnetComponent(c.active(), filter));
        widgets.forEach(tri -> {
            var visible = tri.getA() == filter;
            tri.getB().setVisible(visible);
            tri.getC().setVisible(visible);
        });
    }

    @Override
    public InteractionResultHolder<ItemStack> use(ItemStack item, Level world, @NotNull Player player,
                                                  InteractionHand hand) {
        if (!player.level().isClientSide && player.isShiftKeyDown()) {
            player.displayClientMessage(Component.translatable(toggleActive(player.getItemInHand(hand)) ?
                    "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"), true);
        } else {
            IItemUIFactory.super.use(item, world, player, hand);
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    private static boolean isActive(ItemStack stack) {
        if (stack == ItemStack.EMPTY) {
            return false;
        }
        return stack.getOrDefault(GTDataComponents.MAGNET, MagnetComponent.EMPTY).active();
    }

    private static boolean toggleActive(ItemStack stack) {
        MutableBoolean active = new MutableBoolean();
        stack.update(GTDataComponents.MAGNET, MagnetComponent.EMPTY,
                c -> {
                    active.setValue(!c.active);
                    return new MagnetComponent(!c.active, c.filterType);
                });
        return active.booleanValue();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        // Adapted logic from Draconic Evolution
        // https://github.com/Draconic-Inc/Draconic-Evolution/blob/1.12.2/src/main/java/com/brandon3055/draconicevolution/items/tools/Magnet.java
        if (!entity.isShiftKeyDown() && entity.tickCount % 10 == 0 && isActive(stack) &&
                entity instanceof Player player) {
            Level world = entity.level();
            if (!drainEnergy(true, stack, energyDraw)) {
                return;
            }

            List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class,
                    new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX(), entity.getY(), entity.getZ())
                            .inflate(range, range, range));

            ItemFilter filter = null;
            boolean didMoveEntity = false;
            for (ItemEntity itemEntity : items) {
                if (itemEntity.isRemoved()) {
                    continue;
                }

                CompoundTag itemTag = itemEntity.getPersistentData();
                if (itemTag.contains("PreventRemoteMovement")) {
                    continue;
                }

                if (itemEntity.getOwner() != null && itemEntity.getOwner().equals(entity) &&
                        itemEntity.hasPickUpDelay()) {
                    continue;
                }

                Player closest = world.getNearestPlayer(itemEntity, 4);
                if (closest != null && closest != entity) {
                    continue;
                }

                if (!world.isClientSide) {
                    if (filter == null) {
                        filter = stack.get(GTDataComponents.MAGNET).filterType().loadFilter(stack);
                    }

                    if (!filter.test(itemEntity.getItem())) {
                        continue;
                    }

                    if (itemEntity.hasPickUpDelay()) {
                        itemEntity.setNoPickUpDelay();
                    }
                    itemEntity.setDeltaMovement(0, 0, 0);
                    itemEntity.setPos(entity.getX() - 0.2 + (world.random.nextDouble() * 0.4), entity.getY() - 0.6,
                            entity.getZ() - 0.2 + (world.random.nextDouble() * 0.4));
                    didMoveEntity = true;
                }
            }

            if (didMoveEntity) {
                world.playSound(null, entity, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                        0.1F, 0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 2F));
            }

            List<ExperienceOrb> xp = world.getEntitiesOfClass(ExperienceOrb.class,
                    new AABB(entity.getX(), entity.getY(), entity.getZ(), entity.getX(), entity.getY(), entity.getZ())
                            .inflate(4, 4, 4));

            for (ExperienceOrb orb : xp) {
                if (!world.isClientSide && !orb.isRemoved()) {
                    if (player.takeXpDelay == 0) {
                        if (NeoForge.EVENT_BUS.post(new PlayerXpEvent.PickupXp(player, orb)).isCanceled()) {
                            continue;
                        }
                        world.playSound(null, entity, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS,
                                0.1F, 0.5F * ((world.random.nextFloat() - world.random.nextFloat()) * 0.7F + 1.8F));
                        player.take(orb, 1);
                        player.giveExperiencePoints(orb.value);
                        orb.discard();
                        didMoveEntity = true;
                    }
                }
            }

            if (didMoveEntity) {
                drainEnergy(false, stack, energyDraw);
            }
        }
    }

    @SubscribeEvent
    public static void onItemToss(@NotNull ItemTossEvent event) {
        if (event.getPlayer() == null) return;
        if (hasMagnet(event.getPlayer())) {
            event.getEntity().setPickUpDelay(60);
        }
    }

    private static boolean hasMagnet(@NotNull Player player) {
        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stackInSlot = inventory.getItem(i);
            if (isMagnet(stackInSlot) && isActive(stackInSlot)) {
                return true;
            }
        }

        if (!ExtForCore.Mods.isCuriosLoaded()) {
            return false;
        }
        return CuriosUtils.hasMagnetCurios(player);
    }

    private static boolean isMagnet(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof IComponentItem metaItem) {
            for (var behavior : metaItem.getComponents()) {
                if (behavior instanceof ItemMagnetBehavior) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean drainEnergy(boolean simulate, @NotNull ItemStack stack, long amount) {
        IElectricItem electricItem = GTCapabilityHelper.getElectricItem(stack);
        if (electricItem == null)
            return false;

        return electricItem.discharge(amount, Integer.MAX_VALUE, true, false, simulate) >= amount;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag isAdvanced) {
        lines.add(Component
                .translatable(isActive(itemStack) ? "behavior.item_magnet.enabled" : "behavior.item_magnet.disabled"));
    }

    private static class CuriosUtils {

        public static boolean hasMagnetCurios(Player player) {
            return CuriosApi.getCuriosInventory(player)
                    .map(curios -> curios.findFirstCurio(i -> isMagnet(i) && isActive(i)).isPresent())
                    .orElse(false);
        }
    }

    public enum Filter implements StringRepresentable {

        SIMPLE(GTItems.ITEM_FILTER, "item_filter"),
        TAG(GTItems.TAG_FILTER, "item_tag_filter");

        public static final Codec<Filter> CODEC = StringRepresentable.fromEnum(Filter::values);

        public final ItemEntry<ComponentItem> item;
        public final String name;

        Filter(ItemEntry<ComponentItem> item, String name) {
            this.item = item;
            this.name = name;
        }

        public ItemStack getFilter(ItemStack magnet) {
            var mockStack = new ItemStack(item.asItem());
            switch (this) {
                case SIMPLE -> mockStack.set(GTDataComponents.SIMPLE_ITEM_FILTER,
                        magnet.get(GTDataComponents.SIMPLE_ITEM_FILTER));
                case TAG -> mockStack.set(GTDataComponents.TAG_FILTER_EXPRESSION,
                        magnet.get(GTDataComponents.TAG_FILTER_EXPRESSION));
            }
            return mockStack;
        }

        public ItemFilter loadFilter(ItemStack magnet) {
            var stack = getFilter(magnet);
            return ItemFilter.loadFilter(stack);
        }

        public void saveFilter(ItemStack stack, ItemFilter filter) {
            switch (this) {
                case SIMPLE -> {
                    if (filter instanceof SimpleItemFilter simple) {
                        stack.set(GTDataComponents.SIMPLE_ITEM_FILTER, simple);
                    }
                }
                case TAG -> {
                    if (filter instanceof TagItemFilter tag) {
                        stack.set(GTDataComponents.TAG_FILTER_EXPRESSION, tag.getTagFilterExpression());
                    }
                }
            }
        }

        public static Filter get(int ordinal) {
            return Filter.values()[ordinal];
        }

        public @NotNull String getTooltip() {
            return item.asItem().getDescriptionId();
        }

        public @NotNull IGuiTexture getIcon() {
            return SpriteTexture.of("gtceu:textures/item/" + name + ".png");
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record MagnetComponent(boolean active, Filter filterType) {

        public static final Codec<MagnetComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.orElse(false).fieldOf("active").forGetter(MagnetComponent::active),
                Filter.CODEC.fieldOf("filter_type").forGetter(MagnetComponent::filterType))
                .apply(instance, MagnetComponent::new));
        public static final StreamCodec<ByteBuf, MagnetComponent> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, MagnetComponent::active,
                ByteBufCodecs.VAR_INT, c -> c.filterType().ordinal(),
                (active, ordinal) -> new MagnetComponent(active, Filter.get(ordinal)));

        public static final MagnetComponent EMPTY = new MagnetComponent(false, Filter.SIMPLE);
    }
}
