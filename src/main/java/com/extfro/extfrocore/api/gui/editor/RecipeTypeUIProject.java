package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.api.recipe.GTRecipeType;
import com.extfro.extfrocore.api.registry.GTRegistries;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import com.lowdragmc.lowdraglib2.LDLib;
import com.lowdragmc.lowdraglib2.editor.resource.Resources;
import com.lowdragmc.lowdraglib2.editor.ui.Editor;
import com.lowdragmc.lowdraglib2.gui.ColorPattern;
import com.lowdragmc.lowdraglib2.gui.editor.data.UIProject;
import com.lowdragmc.lowdraglib2.gui.editor.ui.tool.WidgetToolBox;
import com.lowdragmc.lowdraglib2.gui.texture.*;
import com.lowdragmc.lowdraglib2.gui.texture.Icons;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.util.TreeBuilder;
import com.lowdragmc.lowdraglib2.gui.widget.TabButton;
import com.lowdragmc.lowdraglib2.registry.annotation.LDLRegister;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@LDLRegister(name = "rtui", group = "editor.gtceu")
public class RecipeTypeUIProject extends UIProject {

    @Nullable
    @Getter
    @Setter
    protected GTRecipeType recipeType;

    private RecipeTypeUIProject() {
        this(null, null);
    }

    public RecipeTypeUIProject(Resources resources, UIElement root) {
        super(resources, root);
    }

    public RecipeTypeUIProject(CompoundTag tag) {
        super(tag);
    }

    @Override
    public RecipeTypeUIProject newEmptyProject() {
        return new RecipeTypeUIProject(Resources.defaultResource(),
                new UIElement().layout(layout -> layout.left(30).top(30).width(200).height(200)));
    }

    @Override
    public UIProject loadProject(Path file) {
        try {
            var tag = NbtIo.read(file);
            if (tag != null) {
                return new RecipeTypeUIProject(tag);
            }
        } catch (IOException ignored) {}
        return null;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        var tag = super.serializeNBT(provider);
        if (recipeType != null) {
            tag.putString("recipe_type", recipeType.registryName.toString());
        }
        return tag;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag tag) {
        super.deserializeNBT(provider, tag);
        if (tag.contains("recipe_type")) {
            var type = BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.parse(tag.getString("recipe_type")));
            recipeType = (GTRecipeType) type;
        }
    }

    @Override
    public void onLoad(Editor editor) {
        editor.getResourcePanel().loadResource(getResources(), false);
        editor.getTabPages().addTab(new TabButton(50, 16, 60, 14).setTexture(
                new GuiTextureGroup(ColorPattern.T_GREEN.rectTexture().setBottomRadius(10).transform(0, 0.4f),
                        new TextTexture("Main")),
                new GuiTextureGroup(ColorPattern.T_RED.rectTexture().setBottomRadius(10).transform(0, 0.4f),
                        new TextTexture("Main"))),
                new UIMainPanel(editor, root, recipeType == null ? null : recipeType.getTranslationKey()));
        for (WidgetToolBox.Default tab : WidgetToolBox.Default.TABS) {
            if (tab == WidgetToolBox.Default.CONTAINER) {
                continue;
            }
            editor.getToolPanel().addNewToolBox("ldlib.gui.editor.group." + tab.groupName, tab.icon,
                    tab::createToolBox);
        }
    }

    @Override
    public void attachMenu(Editor editor, String name, TreeBuilder.Menu menu) {
        if (name.equals("file")) {
            if (recipeType == null) {
                menu.remove("ldlib.gui.editor.menu.save");
            } else {
                menu.remove("ldlib.gui.editor.menu.save");
                menu.leaf(Icons.SAVE, "ldlib.gui.editor.menu.save", () -> {
                    var path = new File(LDLib.getLDLibDir(),
                            "assets/%s/ui/recipe_type".formatted(recipeType.registryName.getNamespace()));
                    path.mkdirs();
                    saveProject(Path.of(recipeType.registryName.getPath(), ".", this.getRegisterUI().name()));
                    recipeType.getRecipeUI().reloadCustomUI();
                });
            }
        } else if (name.equals("template_tab")) {
            Map<String, List<GTRecipeType>> categories = new LinkedHashMap<>();
            for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
                if (!(recipeType instanceof GTRecipeType gtType)) {
                    continue;
                }
                categories.computeIfAbsent(gtType.group, group -> new ArrayList<>()).add(gtType);
            }
            categories.forEach((groupName, recipeTypes) -> menu.branch(groupName, m -> {
                for (GTRecipeType recipeType : recipeTypes) {
                    IGuiTexture icon;
                    if (recipeType.getIconSupplier() != null) {
                        icon = new ItemStackTexture(recipeType.getIconSupplier().get());
                    } else {
                        icon = new ItemStackTexture(Items.BARRIER);
                    }
                    m.leaf(icon, recipeType.getTranslationKey(), () -> {
                        root.clearAllExternalChildren();
                        if (recipeType.getRecipeUI().hasCustomUI()) {
                            var nbt = recipeType.getRecipeUI().getCustomUI();
                            root.deserializeNBT(GTRegistries.builtinRegistry(), nbt.getCompound("root"));
                        } else {
                            var widget = recipeType.getRecipeUI().createEditableUITemplate(false, false)
                                    .createDefault();
                            root.layout(layout -> layout.width(widget.getSizeWidth()).height(widget.getSizeHeight()));
                            for (UIElement child : List.copyOf(widget.getChildren())) {
                                root.addChild(child);
                            }
                        }
                        setRecipeType(recipeType);
                    });
                }
            }));
        }
    }
}
