package com.extfro.extfrocore.integration.kjs.builders;

import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.recipe.category.GTRecipeCategory;
import com.extfro.extfrocore.common.data.GTRecipeCategories;
import com.extfro.extfrocore.common.data.GTRecipeTypes;
import com.extfro.extfrocore.integration.kjs.helpers.GTResourceLocation;
import com.extfro.extfrocore.utils.FormattingUtil;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import dev.latvian.mods.kubejs.client.LangKubeEvent;
import dev.latvian.mods.kubejs.registry.BuilderBase;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@Accessors(chain = true, fluent = true)
public class GTRecipeCategoryBuilder extends BuilderBase<GTRecipeCategory> {

    @Setter
    private transient GTRecipeType recipeType;
    @Setter
    @Nullable
    private transient IGuiTexture icon;
    @Setter
    private transient boolean isXEIVisible;
    @Setter
    @Nullable
    private transient String langValue;

    public GTRecipeCategoryBuilder(ResourceLocation id) {
        super(GTResourceLocation.implicitAsGtceu(id));
        recipeType = GTRecipeTypes.DUMMY_RECIPES;
        icon = null;
        isXEIVisible = true;
        langValue = null;
    }

    @Override
    public void generateLang(LangKubeEvent lang) {
        super.generateLang(lang);
        if (langValue != null) lang.add(get().getLanguageKey(), langValue);
        else lang.add(id.getNamespace(), get().getLanguageKey(), FormattingUtil.toEnglishName(get().name));
    }

    @Override
    public GTRecipeCategory createObject() {
        return GTRecipeCategories.register(id, recipeType)
                .setIcon(icon)
                .setXEIVisible(isXEIVisible);
    }

    public GTRecipeCategoryBuilder setCustomIcon(ResourceLocation location) {
        this.icon = SpriteTexture.of(location.withPrefix("textures/").withSuffix(".png"));
        return this;
    }

    public GTRecipeCategoryBuilder setItemIcon(ItemStack... stacks) {
        this.icon = new ItemStackTexture(stacks);
        return this;
    }
}
