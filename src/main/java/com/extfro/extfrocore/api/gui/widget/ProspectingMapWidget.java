package com.extfro.extfrocore.api.gui.widget;

import com.extfro.extfrocore.api.gui.GuiTextures;
import com.extfro.extfrocore.api.gui.misc.PacketProspecting;
import com.extfro.extfrocore.api.gui.misc.ProspectorMode;
import com.extfro.extfrocore.api.gui.texture.ProspectingTexture;
import com.extfro.extfrocore.api.item.IComponentItem;
import com.extfro.extfrocore.common.item.behavior.ProspectorScannerBehavior;
import com.extfro.extfrocore.integration.map.WaypointManager;
import com.extfro.extfrocore.integration.map.cache.client.GTClientCache;
import com.extfro.extfrocore.integration.map.cache.server.ServerCache;
import com.extfro.extfrocore.integration.map.layer.builtin.OreRenderLayer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.TextTexture;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.SearchComponent;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.DrawerHelper;
import com.lowdragmc.lowdraglib2.utils.LocalizationUtils;
import com.lowdragmc.lowdraglib2.utils.search.IResultHandler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.LinkedBlockingQueue;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class ProspectingMapWidget extends UIElement implements SearchComponent.ISearchUI<Object> {

    private static final String INIT_MESSAGE = "prospecting_init";
    private static final String PACKET_MESSAGE = "prospecting_packet";

    private final int chunkRadius;
    private final ProspectorMode mode;
    private final int scanTick;
    private final InteractionHand hand;
    private final ScrollerView itemList;
    private final int imageWidth;
    private final int imageHeight;
    private boolean darkMode = false;
    @OnlyIn(Dist.CLIENT)
    private ProspectingTexture texture;
    private int playerChunkX;
    private int playerChunkZ;
    private int chunkIndex = 0;
    private final Queue<PacketProspecting> packetQueue = new LinkedBlockingQueue<>();
    private final Set<Object> items = new CopyOnWriteArraySet<>();
    private final Map<String, Button> selectedMap = new ConcurrentHashMap<>();

    public ProspectingMapWidget(int xPosition, int yPosition, int width, int height, int chunkRadius,
                                @NotNull ProspectorMode mode, int scanTick, InteractionHand hand) {
        this.chunkRadius = chunkRadius;
        this.mode = mode;
        this.scanTick = scanTick;
        this.hand = hand;
        this.imageWidth = (chunkRadius * 2 - 1) * 16;
        this.imageHeight = (chunkRadius * 2 - 1) * 16;

        layout(layout -> layout.left(xPosition).top(yPosition).width(width).height(height));
        style(style -> style.overflowVisible(false));

        UIElement mapBackground = new UIElement();
        mapBackground.layout(layout -> layout.left(0).top((height - imageHeight) / 2f - 4)
                .width(imageWidth + 8).height(imageHeight + 8));
        mapBackground.style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        mapBackground.setAllowHitTest(false);
        addChild(mapBackground);

        UIElement listPanel = new UIElement();
        listPanel.layout(layout -> layout.left(imageWidth + 10).top(0).width(width - (imageWidth + 10)).height(height));
        listPanel.style(style -> style.background(GuiTextures.BACKGROUND_INVERSE));
        addChild(listPanel);

        SearchComponent<Object> search = new SearchComponent<>(this);
        search.layout(layout -> layout.left(6).top(6).width(width - (imageWidth + 10) - 12).height(18));
        search.searchStyle(style -> style.maxItemCount(8).scrollerViewHeight(90).closeAfterSelect(true));
        search.setCandidateUIProvider(value -> {
            Label label = new Label();
            label.setValue(Component.literal(resultText(value)));
            label.layout(layout -> layout.width(width - (imageWidth + 10) - 16).height(12));
            label.textStyle(style -> style.textColor(0x404040).textShadow(false));
            return label;
        });
        listPanel.addChild(search);

        itemList = new ScrollerView();
        itemList.layout(layout -> layout.left(4).top(28).width(width - (imageWidth + 10) - 8).height(height - 32));
        listPanel.addChild(itemList);

        addNewItem(ProspectingTexture.SELECTED_ALL, Component.literal("all resources"), IGuiTexture.EMPTY, -1);
        onMessage(INIT_MESSAGE, this::readInitialData);
        onMessage(PACKET_MESSAGE, this::readProspectingPacket);
        addEventListener(UIEvents.HOVER_TOOLTIPS, this::appendHoverTooltips);
        addEventListener(UIEvents.CLICK, this::onClickMap);
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public void setDarkMode(boolean mode) {
        if (darkMode != mode) {
            darkMode = mode;
            if (texture != null) {
                texture.setDarkMode(darkMode);
            }
        }
    }

    @Override
    protected void onAdded() {
        super.onAdded();
        if (getModularUI() != null && getModularUI().player instanceof ServerPlayer player) {
            playerChunkX = player.chunkPosition().x;
            playerChunkZ = player.chunkPosition().z;
            CompoundTag tag = new CompoundTag();
            tag.putInt("chunkX", playerChunkX);
            tag.putInt("chunkZ", playerChunkZ);
            tag.putInt("posX", player.getBlockX());
            tag.putInt("posZ", player.getBlockZ());
            tag.putFloat("direction", player.getVisualRotationYInDegrees());
            sendMessage(INIT_MESSAGE, tag);
        }
    }

    @Override
    public void serverTick() {
        super.serverTick();
        Player player = getModularUI().player;
        var world = player.level();
        if (getModularUI().getTickCounter() % scanTick == 0 &&
                chunkIndex < (chunkRadius * 2 - 1) * (chunkRadius * 2 - 1)) {
            int row = chunkIndex / (chunkRadius * 2 - 1);
            int column = chunkIndex % (chunkRadius * 2 - 1);

            int ox = column - chunkRadius + 1;
            int oz = row - chunkRadius + 1;

            var chunk = world.getChunk(playerChunkX + ox, playerChunkZ + oz);
            if (mode == ProspectorMode.ORE && player instanceof ServerPlayer serverPlayer) {
                ServerCache.instance.prospectAllInChunk(world.dimension(), chunk.getPos(), serverPlayer);
            }
            PacketProspecting packet = new PacketProspecting(playerChunkX + ox, playerChunkZ + oz, this.mode);
            mode.scan(packet.data, chunk);
            sendPacket(packet);
            chunkIndex++;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof IComponentItem componentItem) {
            for (var component : componentItem.getComponents()) {
                if (component instanceof ProspectorScannerBehavior prospector) {
                    if (!player.isCreative() && !prospector.drainEnergy(held, false)) {
                        player.closeContainer();
                    }
                }
            }
        }
    }

    @Override
    public void screenTick() {
        super.screenTick();
        int max = 10;
        while (texture != null && max-- > 0 && !packetQueue.isEmpty()) {
            var packet = packetQueue.poll();
            texture.updateTexture(packet);
            addOresToList(packet.data);
        }
    }

    private void sendPacket(PacketProspecting packet) {
        RegistryFriendlyByteBuf byteBuf = RegistryFriendlyByteBuf.decorator(getModularUI().player.registryAccess())
                .apply(io.netty.buffer.Unpooled.buffer());
        packet.writePacketData(byteBuf);
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        byteBuf.release();
        CompoundTag tag = new CompoundTag();
        tag.putByteArray("data", bytes);
        sendMessage(PACKET_MESSAGE, tag);
    }

    @OnlyIn(Dist.CLIENT)
    private void readInitialData(CompoundTag tag) {
        texture = new ProspectingTexture(
                tag.getInt("chunkX"),
                tag.getInt("chunkZ"),
                tag.getInt("posX"),
                tag.getInt("posZ"),
                tag.getFloat("direction"), mode, chunkRadius, darkMode);
    }

    @OnlyIn(Dist.CLIENT)
    private void readProspectingPacket(CompoundTag tag) {
        RegistryFriendlyByteBuf byteBuf = RegistryFriendlyByteBuf.decorator(getModularUI().player.registryAccess())
                .apply(io.netty.buffer.Unpooled.wrappedBuffer(tag.getByteArray("data")));
        try {
            addPacketToQueue(PacketProspecting.readPacketData(mode, byteBuf));
        } finally {
            byteBuf.release();
        }
    }

    private void addOresToList(Object[][][] data) {
        var newItems = new HashSet<>();
        for (int x = 0; x < mode.cellSize; x++) {
            for (int z = 0; z < mode.cellSize; z++) {
                for (var item : data[x][z]) {
                    newItems.add(item);
                    addNewItem(mode.getUniqueID(item), mode.getDescription(item), mode.getItemIcon(item),
                            mode.getItemColor(item));
                }
            }
        }
        items.addAll(newItems);
    }

    private void addNewItem(String uniqueID, MutableComponent renderingName, IGuiTexture icon, int color) {
        if (!selectedMap.containsKey(uniqueID)) {
            int index = selectedMap.size();
            Button button = new Button();
            button.layout(layout -> layout.left(0).top(index * 15).width(itemList.getContentWidth() - 4).height(15));
            button.buttonStyle(style -> style.baseTexture(IGuiTexture.EMPTY).hoverTexture(
                    GuiTextures.BUTTON.copy().setColor(0x66FFFFFF)).pressedTexture(GuiTextures.BUTTON));
            button.noText();
            button.setOnClick(event -> selectUniqueId(uniqueID));
            button.addChild(fixed(0, 0, 15, 15).style(style -> style.background(icon)));
            button.addChild(fixed(15, 0, 96, 15).style(style -> style.background(new TextTexture(renderingName.getString())
                    .setWidth(96).setType(TextTexture.TextType.LEFT_HIDE))));
            itemList.addScrollViewChild(button);
            selectedMap.put(uniqueID, button);
            updateSelectedButtons();
        }
    }

    @Override
    public void drawBackgroundAdditional(GUIContext context) {
        super.drawBackgroundAdditional(context);
        if (texture == null) return;
        float x = getPositionX() + 3;
        float y = getPositionY() + (getSizeHeight() - texture.getImageHeight()) / 2f - 1;
        texture.draw(context.graphics, (int) x, (int) y);
        int cX = ((int) (context.mouseX - x)) / 16;
        int cZ = ((int) (context.mouseY - y)) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkRadius * 2 - 1 && cZ < chunkRadius * 2 - 1) {
            DrawerHelper.drawSolidRect(context.graphics, cX * 16 + (int) x, cZ * 16 + (int) y,
                    16, 16, 0x4B6C6C6C);
        }
    }

    private void appendHoverTooltips(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        if (texture == null) return;
        var position = mapPosition();
        int cX = ((int) (event.x - position.x)) / 16;
        int cZ = ((int) (event.y - position.y)) / 16;
        if (cX >= 0 && cZ >= 0 && cX < chunkRadius * 2 - 1 && cZ < chunkRadius * 2 - 1) {
            List<Component> tooltips = new ArrayList<>();
            tooltips.add(Component.translatable(mode.unlocalizedName));
            List<Object[]> hoverItems = new ArrayList<>();
            for (int i = 0; i < mode.cellSize; i++) {
                for (int j = 0; j < mode.cellSize; j++) {
                    if (texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j] != null) {
                        hoverItems.add(texture.data[cX * mode.cellSize + i][cZ * mode.cellSize + j]);
                    }
                }
            }
            mode.appendTooltips(hoverItems, tooltips, texture.getSelected());
            event.hoverTooltips = new HoverTooltips(tooltips, null, null, ItemStack.EMPTY);
        }
    }

    private void onClickMap(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent event) {
        if (texture == null) return;
        var clickedItem = getClickedVein(event.x, event.y);
        if (clickedItem == null) return;
        event.stopPropagation();
        if (!WaypointManager.isActive()) return;
        MutableComponent veinName = Component.literal(clickedItem.name());
        veinName.setStyle(veinName.getStyle().withColor(clickedItem.color));
        WaypointManager.setWaypoint(new ChunkPos(clickedItem.position).toString(),
                clickedItem.name,
                clickedItem.color,
                getModularUI().player.level().dimension(),
                clickedItem.position.getX(), clickedItem.position.getY(), clickedItem.position.getZ());
        getModularUI().player.displayClientMessage(
                Component.translatable("behavior.prospector.added_waypoint", veinName), false);
    }

    private WaypointItem getClickedVein(double mouseX, double mouseY) {
        if (texture == null) return null;
        var position = mapPosition();
        int cX = (int) (mouseX - position.x) / 16;
        int cZ = (int) (mouseY - position.y) / 16;
        int offsetX = Math.abs((int) (mouseX - position.x) % 16);
        int offsetZ = Math.abs((int) (mouseY - position.y) % 16);
        int xDiff = cX - (chunkRadius - 1);
        int zDiff = cZ - (chunkRadius - 1);
        Player player = getModularUI().player;

        int xPos = ((player.chunkPosition().x + xDiff) << 4) + offsetX;
        int zPos = ((player.chunkPosition().z + zDiff) << 4) + offsetZ;

        var blockPos = new BlockPos(xPos, player.level().getHeight(Heightmap.Types.WORLD_SURFACE, xPos, zPos), zPos);
        if (cX < 0 || cZ < 0 || cX >= chunkRadius * 2 - 1 || cZ >= chunkRadius * 2 - 1) {
            return null;
        }

        if (!texture.getSelected().equals(ProspectingTexture.SELECTED_ALL)) {
            for (var item : items) {
                if (!texture.getSelected().equals(mode.getUniqueID(item))) continue;
                var name = mode.getDescription(item).getString();
                var color = mode.getItemColor(item);
                return new WaypointItem(blockPos, name, color);
            }
        }

        var hoveredItem = texture.data[cX * mode.cellSize + (offsetX * mode.cellSize / 16)][cZ * mode.cellSize +
                (offsetZ * mode.cellSize / 16)];
        if (hoveredItem != null && hoveredItem.length != 0) {
            var name = mode.getDescription(hoveredItem[0]).getString();
            var color = mode.getItemColor(hoveredItem[0]);
            return new WaypointItem(blockPos, name, color);
        }

        var vein = GTClientCache.instance.getNearbyVeins(player.level().dimension(), blockPos, 32);
        if (!vein.isEmpty()) {
            vein.sort((o1, o2) -> (int) (o1.center().distToCenterSqr(xPos, o1.center().getY(), zPos) -
                    o2.center().distToCenterSqr(xPos, o2.center().getY(), zPos)));
            var name = OreRenderLayer.getName(vein.getFirst()).getString();
            var materials = vein.getFirst().definition().value().veinGenerator().getAllMaterials();
            var mostCommonItem = materials.getLast();
            var color = mostCommonItem.getMaterialRGB();
            return new WaypointItem(blockPos, name, color);
        }

        return new WaypointItem(blockPos, "Depleted Vein", 0x990000);
    }

    private Position mapPosition() {
        return new Position((int) (getPositionX() + 3), (int) (getPositionY() + (getSizeHeight() - imageHeight) / 2f - 1));
    }

    @OnlyIn(Dist.CLIENT)
    private void addPacketToQueue(PacketProspecting packet) {
        packetQueue.add(packet);
        if (mode == ProspectorMode.FLUID && packet.data[0][0].length > 0) {
            GTClientCache.instance.addFluid(getModularUI().player.level().dimension(), packet.chunkX, packet.chunkZ,
                    (ProspectorMode.FluidInfo) packet.data[0][0][0]);
        }
    }

    private void selectUniqueId(String uid) {
        if (texture != null) {
            texture.setSelected(uid);
        }
        updateSelectedButtons();
    }

    private void updateSelectedButtons() {
        selectedMap.forEach((uid, button) -> button.style(style -> style.overlay(
                texture != null && texture.getSelected().equals(uid) ? GuiTextures.BUTTON.copy() : IGuiTexture.EMPTY)));
    }

    @Override
    public String resultText(Object value) {
        return mode.getDescription(value).getString();
    }

    @Override
    public void onResultSelected(Object item) {
        var uid = mode.getUniqueID(item);
        selectUniqueId(uid);
    }

    @Override
    public void search(String s, IResultHandler<Object> consumer) {
        var added = new HashSet<String>();
        for (var item : this.items) {
            if (Thread.currentThread().isInterrupted()) return;
            var id = mode.getUniqueID(item);
            if (!added.contains(id)) {
                added.add(id);
                var localized = LocalizationUtils.format(resultText(item));
                if (item.toString().toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT)) ||
                        localized.toLowerCase(Locale.ROOT).contains(s.toLowerCase(Locale.ROOT))) {
                    consumer.acceptResult(item);
                }
            }
        }
    }

    private static UIElement fixed(int x, int y, int width, int height) {
        return new UIElement().layout(layout -> layout.left(x).top(y).width(width).height(height));
    }

    private record Position(float x, float y) {}

    private record WaypointItem(BlockPos position, String name, int color) {}
}
