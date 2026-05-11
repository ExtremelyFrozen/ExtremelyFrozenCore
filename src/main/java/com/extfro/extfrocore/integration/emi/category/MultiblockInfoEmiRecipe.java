package com.extfro.extfrocore.integration.emi.category;

import com.extfro.extfrocore.api.pattern.BlockInfo;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoDisplay;
import com.extfro.extfrocore.integration.xei.multipage.MultiblockInfoPage;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MultiblockInfoEmiRecipe implements EmiRecipe {

    private static final int MAX_COLUMNS = 7;
    private static final int MAX_ROWS = 5;

    private final EmiRecipeCategory category;
    private final MultiblockInfoDisplay display;
    private final MultiblockInfoPage page;
    private final ResourceLocation id;
    private final List<EmiStack> outputs;

    public MultiblockInfoEmiRecipe(EmiRecipeCategory category, MultiblockInfoDisplay display, MultiblockInfoPage page) {
        this.category = category;
        this.display = display;
        this.page = page;
        this.id = EmiXEIHelper.syntheticId("multiblock_info", display.id(), page.index());
        this.outputs = List.of(EmiXEIHelper.item(display.icon()));
    }

    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of();
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return 150;
    }

    @Override
    public int getDisplayHeight() {
        return 34 + MAX_ROWS * 18;
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addText(display.title(), 0, 0, 0x404040, false);
        widgets.addText(Component.literal((page.index() + 1) + " / " + display.pages().size()), 0, 11, 0x707070, false);
        int shown = 0;
        for (BlockInfo[][] aisle : page.shape().getBlocks()) {
            for (BlockInfo[] row : aisle) {
                for (BlockInfo blockInfo : row) {
                    if (shown >= MAX_COLUMNS * MAX_ROWS) {
                        return;
                    }
                    ItemStack stack = blockInfo.blockState().getBlock().asItem().getDefaultInstance();
                    if (!stack.isEmpty()) {
                        widgets.addSlot(EmiXEIHelper.item(stack), shown % MAX_COLUMNS * 18, 34 + shown / MAX_COLUMNS * 18);
                        shown++;
                    }
                }
            }
        }
    }
}
