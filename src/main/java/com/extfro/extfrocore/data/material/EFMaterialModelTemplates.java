package com.extfro.extfrocore.data.material;

import com.extfro.extfrocore.ExtForCore;

import net.minecraft.resources.ResourceLocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class EFMaterialModelTemplates {

    private static final ResourceLocation TINTED_CUBE_PARENT = ExtForCore.id("block/cube/tinted/all_0");
    private static final ResourceLocation TINTED_CUBE_OVERLAY_PARENT = ExtForCore.id("block/cube/tinted/all");

    private EFMaterialModelTemplates() {}

    public static JsonObject generatedItem(ResourceLocation layer0) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "item/generated");
        JsonObject textures = new JsonObject();
        textures.addProperty("layer0", layer0.toString());
        model.add("textures", textures);
        return model;
    }

    public static JsonObject tintedCubeAll(ResourceLocation texture) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", TINTED_CUBE_PARENT.toString());
        JsonObject textures = new JsonObject();
        textures.addProperty("all", texture.toString());
        textures.addProperty("particle", texture.toString());
        model.add("textures", textures);
        return model;
    }

    public static JsonObject tintedCubeAllWithSecondary(ResourceLocation baseTexture, ResourceLocation secondaryTexture) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", "block/block");
        model.addProperty("loader", "neoforge:composite");

        JsonObject textures = new JsonObject();
        textures.addProperty("particle", baseTexture.toString());
        model.add("textures", textures);

        JsonObject children = new JsonObject();
        children.add("base", childModel(TINTED_CUBE_PARENT, "solid", baseTexture));
        children.add("secondary", childModel(TINTED_CUBE_OVERLAY_PARENT, "translucent", secondaryTexture));
        model.add("children", children);

        JsonArray order = new JsonArray();
        order.add("base");
        order.add("secondary");
        model.add("item_render_order", order);
        return model;
    }

    private static JsonObject childModel(ResourceLocation parent, String renderType, ResourceLocation texture) {
        JsonObject model = new JsonObject();
        model.addProperty("parent", parent.toString());
        model.addProperty("render_type", renderType);

        JsonObject textures = new JsonObject();
        textures.addProperty("all", texture.toString());
        model.add("textures", textures);
        return model;
    }
}
