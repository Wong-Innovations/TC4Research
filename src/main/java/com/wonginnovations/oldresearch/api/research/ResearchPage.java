package com.wonginnovations.oldresearch.api.research;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.crafting.CrucibleRecipe;
import thaumcraft.api.crafting.IArcaneRecipe;
import thaumcraft.api.crafting.InfusionRecipe;
import thaumcraft.common.lib.crafting.InfusionEnchantmentRecipe;

import java.util.List;

public class ResearchPage {
    public ResearchPage.PageType type = ResearchPage.PageType.TEXT;
    public String text = null;
    public String research = null;
    public ResourceLocation image = null;
    public AspectList aspects = null;
    public Object recipe = null;
    public ItemStack recipeOutput = null;

    public ResearchPage(String text) {
        this.type = ResearchPage.PageType.TEXT;
        this.text = text;
    }

    public ResearchPage(String research, String text) {
        this.type = ResearchPage.PageType.TEXT_CONCEALED;
        this.research = research;
        this.text = text;
    }

    public ResearchPage(IRecipe recipe) {
        this.type = ResearchPage.PageType.NORMAL_CRAFTING;
        this.recipe = recipe;
        this.recipeOutput = recipe.getRecipeOutput();
    }

    public ResearchPage(IRecipe[] recipe) {
        this.type = ResearchPage.PageType.NORMAL_CRAFTING;
        this.recipe = recipe;
    }

    public ResearchPage(IArcaneRecipe[] recipe) {
        this.type = ResearchPage.PageType.ARCANE_CRAFTING;
        this.recipe = recipe;
    }

    public ResearchPage(CrucibleRecipe[] recipe) {
        this.type = ResearchPage.PageType.CRUCIBLE_CRAFTING;
        this.recipe = recipe;
    }

    public ResearchPage(InfusionRecipe[] recipe) {
        this.type = ResearchPage.PageType.INFUSION_CRAFTING;
        this.recipe = recipe;
    }

    public ResearchPage(List recipe) {
        this.type = ResearchPage.PageType.COMPOUND_CRAFTING;
        this.recipe = recipe;
    }

    public ResearchPage(IArcaneRecipe recipe) {
        this.type = ResearchPage.PageType.ARCANE_CRAFTING;
        this.recipe = recipe;
        this.recipeOutput = recipe.getRecipeOutput();
    }

    public ResearchPage(CrucibleRecipe recipe) {
        this.type = ResearchPage.PageType.CRUCIBLE_CRAFTING;
        this.recipe = recipe;
        this.recipeOutput = recipe.getRecipeOutput();
    }

    public ResearchPage(ItemStack input) {
        this.type = ResearchPage.PageType.SMELTING;
        this.recipe = input;
        this.recipeOutput = FurnaceRecipes.instance().getSmeltingResult(input);
    }

    public ResearchPage(InfusionRecipe recipe) {
        this.type = ResearchPage.PageType.INFUSION_CRAFTING;
        this.recipe = recipe;
        if(recipe.getRecipeOutput() instanceof ItemStack) {
            this.recipeOutput = (ItemStack)recipe.getRecipeOutput();
        } else {
            this.recipeOutput = recipe.getRecipeInput().getMatchingStacks()[0];
        }

    }

    public ResearchPage(InfusionEnchantmentRecipe recipe) {
        this.type = ResearchPage.PageType.INFUSION_ENCHANTMENT;
        this.recipe = recipe;
    }

    public ResearchPage(ResourceLocation image, String caption) {
        this.type = ResearchPage.PageType.IMAGE;
        this.image = image;
        this.text = caption;
    }

    public ResearchPage(AspectList as) {
        this.type = ResearchPage.PageType.ASPECTS;
        this.aspects = as;
    }

    public String getTranslatedText() {
        String ret = "";
        if(this.text != null) {
            ret = I18n.format(this.text);
            if(ret.isEmpty()) {
                ret = this.text;
            }
        }

        return ret;
    }

    public static enum PageType {
        TEXT,
        TEXT_CONCEALED,
        IMAGE,
        CRUCIBLE_CRAFTING,
        ARCANE_CRAFTING,
        ASPECTS,
        NORMAL_CRAFTING,
        INFUSION_CRAFTING,
        COMPOUND_CRAFTING,
        INFUSION_ENCHANTMENT,
        SMELTING;
    }
}

