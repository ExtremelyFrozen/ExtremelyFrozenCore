package com.extfro.extfrocore.data.pack;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.IoSupplier;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class EFDynamicPackContents {

    private static class Node {

        Object contents = new Object2ObjectOpenHashMap<String, Node>();

        void collectResources(String namespace, String[] pathComponents, int curIndex,
                              PackResources.ResourceOutput output) {
            if (curIndex < pathComponents.length) {
                Node child = getChild(pathComponents[curIndex]);
                if (child != null) {
                    child.collectResources(namespace, pathComponents, curIndex + 1, output);
                }
            } else {
                outputResources(namespace, String.join("/", pathComponents), output);
            }
        }

        private boolean isTerminalNode() {
            return contents instanceof IoSupplier<?>;
        }

        @SuppressWarnings("unchecked")
        private Map<String, Node> getChildren() {
            if (!(contents instanceof Map<?, ?>)) {
                throw new IllegalStateException("Cannot read children from a terminal resource node");
            }
            return (Map<String, Node>) contents;
        }

        void outputResources(String namespace, String path, PackResources.ResourceOutput output) {
            if (isTerminalNode()) {
                output.accept(ResourceLocation.fromNamespaceAndPath(namespace, path), createIoSupplier());
                return;
            }
            for (var entry : getChildren().entrySet()) {
                entry.getValue().outputResources(namespace, path + "/" + entry.getKey(), output);
            }
        }

        @SuppressWarnings("unchecked")
        @Nullable
        IoSupplier<InputStream> createIoSupplier() {
            if (!isTerminalNode()) {
                return null;
            }
            return (IoSupplier<InputStream>) contents;
        }

        @Nullable
        Node getChild(String name) {
            return isTerminalNode() ? null : getChildren().get(name);
        }
    }

    private final Node root = new Node();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public void addToData(ResourceLocation location, byte[] bytes) {
        addToData(location, () -> new ByteArrayInputStream(bytes));
    }

    public void addToData(ResourceLocation location, IoSupplier<InputStream> supplier) {
        String[] pathComponents = location.getPath().split("/");
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            Node node = root.getChildren().computeIfAbsent(location.getNamespace(), ignored -> new Node());
            for (String component : pathComponents) {
                node = node.getChildren().computeIfAbsent(component, ignored -> new Node());
            }
            node.contents = supplier;
        } finally {
            writeLock.unlock();
        }
    }

    public void clearData() {
        var writeLock = lock.writeLock();
        writeLock.lock();
        try {
            root.getChildren().clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Nullable
    public IoSupplier<InputStream> getResource(ResourceLocation location) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            Node node = root.getChild(location.getNamespace());
            for (String path : location.getPath().split("/")) {
                if (node == null) {
                    return null;
                }
                node = node.getChild(path);
            }
            return node == null ? null : node.createIoSupplier();
        } finally {
            readLock.unlock();
        }
    }

    public void listResources(String namespace, String path, PackResources.ResourceOutput output) {
        var readLock = lock.readLock();
        readLock.lock();
        try {
            Node base = root.getChild(namespace);
            if (base != null) {
                base.collectResources(namespace, path.split("/"), 0, output);
            }
        } finally {
            readLock.unlock();
        }
    }
}
