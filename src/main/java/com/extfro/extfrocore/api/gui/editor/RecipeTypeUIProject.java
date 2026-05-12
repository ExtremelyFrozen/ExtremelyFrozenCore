package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.api.recipe.GTRecipeType;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import com.lowdragmc.lowdraglib2.editor.project.IProject;
import com.lowdragmc.lowdraglib2.editor.project.ProjectType;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.editor.view.UIEditorView;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.UITemplate;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class RecipeTypeUIProject implements IProject {

    public static final ProjectType TYPE = ProjectType.of(Icons.WIDGET_BASIC, "project.gtceu.recipe_type_ui", ".rtui",
            RecipeTypeUIProject::new);

    private UIElement root = createDefaultRoot(200, 200);
    @Nullable
    @Getter
    @Setter
    protected GTRecipeType recipeType;
    @Nullable
    private UIEditorView editorView;

    public static RecipeTypeUIProject fromRecipeType(GTRecipeType recipeType) {
        var project = new RecipeTypeUIProject();
        if (recipeType.getRecipeUI().hasCustomUI()) {
            var nbt = recipeType.getRecipeUI().getCustomUI();
            project.root = new UIElement();
            project.root.deserializeNBT(com.extfro.extfrocore.api.registry.GTRegistries.builtinRegistry(),
                    nbt.getCompound("root"));
        } else {
            project.root = recipeType.getRecipeUI().createEditableUITemplate(false, false).createDefault();
        }
        project.setRecipeType(recipeType);
        return project;
    }

    @Override
    public Resources getResources() {
        return Resources.EMPTY;
    }

    @Override
    public ProjectType getProjectType() {
        return TYPE;
    }

    @Override
    public void initNewProject() {
        root = createDefaultRoot(200, 200);
        recipeType = null;
    }

    @Override
    public Component getDisplayName() {
        return recipeType == null ? IProject.super.getDisplayName() : Component.translatable(recipeType.getTranslationKey());
    }

    @Override
    public CompoundTag serializeProject(@NotNull HolderLookup.Provider provider) {
        syncRootFromEditorView();
        var tag = new CompoundTag();
        tag.put("root", root.serializeNBT(provider));
        if (recipeType != null) {
            tag.putString("recipe_type", recipeType.registryName.toString());
        }
        return tag;
    }

    @Override
    public void deserializeProject(@NotNull HolderLookup.Provider provider, @NotNull CompoundTag tag) {
        root = new UIElement();
        root.deserializeNBT(provider, tag.getCompound("root"));
        if (tag.contains("recipe_type")) {
            recipeType = (GTRecipeType) BuiltInRegistries.RECIPE_TYPE.get(
                    ResourceLocation.parse(tag.getString("recipe_type")));
        } else {
            recipeType = null;
        }
    }

    @Override
    public void onLoad(@Nonnull Editor editor) {
        editorView = new UIEditorView().loadTemplate(createTemplate(), template -> root = createRoot(template));
        editorView.setIcon(Icons.WIDGET_BASIC);
        editorView.setDynamicName(this::getDisplayName);
        editor.centerWindow.getLeftTop().addView(editorView);
    }

    @Override
    public void onClosed(Editor editor) {
        if (editorView != null) {
            editorView.removeSelf();
            editorView = null;
        }
    }

    private UITemplate createTemplate() {
        return UITemplate.of(root, StylesheetManager.GDP);
    }

    private void syncRootFromEditorView() {
        if (editorView != null && editorView.getCurrentUI() != null) {
            root = createRoot(editorView.getCurrentUI().toTemplate());
        }
    }

    private static UIElement createRoot(UITemplate template) {
        var element = new UIElement();
        template.initUI(element);
        return element;
    }

    private static UIElement createDefaultRoot(int width, int height) {
        return new UIElement().layout(layout -> layout.left(30).top(30).width(width).height(height));
    }
}
