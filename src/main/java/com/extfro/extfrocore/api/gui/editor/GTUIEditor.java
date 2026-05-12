package com.extfro.extfrocore.api.gui.editor;

import com.extfro.extfrocore.api.registry.GTRegistries;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;

import com.lowdragmc.lowdraglib2.gui.editor.UIEditor;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GTUIEditor extends UIEditor {

    public GTUIEditor() {
        super();
        fileMenu.addProjectProvider(MachineUIProject.TYPE);
        fileMenu.addProjectProvider(RecipeTypeUIProject.TYPE);
        fileMenu.registerNewMenuCreator((tab, menu) -> menu.branch(
                "ldlib.gui.editor.register.editor.gtceu.template_tab",
                templates -> {
                    addMachineTemplates(templates);
                    addRecipeTypeTemplates(templates);
                }));
    }

    private void addMachineTemplates(com.lowdragmc.lowdraglib2.gui.util.TreeBuilder.Menu templates) {
        Map<String, List<com.extfro.extfrocore.api.machine.MachineDefinition>> categories = new LinkedHashMap<>();
        for (var definition : GTRegistries.MACHINES) {
            var editableUI = definition.getEditableUI();
            if (editableUI != null) {
                categories.computeIfAbsent(editableUI.getGroupName(), ignored -> new ArrayList<>()).add(definition);
            }
        }
        templates.branch("ldlib.gui.editor.register.editor.gtceu.mui", machineMenu -> categories.forEach((groupName, definitions) -> machineMenu.branch(groupName, groupMenu -> {
            for (var definition : definitions) {
                groupMenu.leaf(new ItemStackTexture(definition.asStack()), definition.getDescriptionId(),
                        () -> loadProject(MachineUIProject.fromDefinition(definition), null));
            }
        })));
    }

    private void addRecipeTypeTemplates(com.lowdragmc.lowdraglib2.gui.util.TreeBuilder.Menu templates) {
        Map<String, List<com.extfro.extfrocore.api.recipe.GTRecipeType>> categories = new LinkedHashMap<>();
        for (RecipeType<?> recipeType : BuiltInRegistries.RECIPE_TYPE) {
            if (recipeType instanceof com.extfro.extfrocore.api.recipe.GTRecipeType gtType) {
                categories.computeIfAbsent(gtType.group, ignored -> new ArrayList<>()).add(gtType);
            }
        }
        templates.branch("ldlib.gui.editor.register.editor.gtceu.rtui", recipeMenu -> categories.forEach((groupName, recipeTypes) -> recipeMenu.branch(groupName, groupMenu -> {
            for (var recipeType : recipeTypes) {
                IGuiTexture icon = recipeType.getIconSupplier() == null ?
                        new ItemStackTexture(Items.BARRIER) :
                        new ItemStackTexture(recipeType.getIconSupplier().get());
                groupMenu.leaf(icon, recipeType.getTranslationKey(),
                        () -> loadProject(RecipeTypeUIProject.fromRecipeType(recipeType), null));
            }
        })));
    }
}
