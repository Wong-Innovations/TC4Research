package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.client.gui.ResearchNoteToast;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;

import javax.annotation.Nullable;
import java.util.List;

public class ItemResearchNote extends Item implements IModelRegister {

    public ItemResearchNote() {
        this.setRegistryName(Tags.MODID + ":researchnote");
        this.setTranslationKey("researchnote");
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if(OldResearchManager.getData(stack) != null && OldResearchManager.getData(stack).isComplete() && !ThaumcraftCapabilities.getKnowledge(player).isResearchComplete(OldResearchManager.getData(stack).key)) {
            if (!world.isRemote) {
                ResearchManager.progressResearch(player, OldResearchManager.getData(stack).key);
            } else {
                Minecraft.getMinecraft().getToastGui().add(new ResearchNoteToast(ResearchCategories.getResearch(OldResearchManager.getStrippedKey(stack))));
                world.playSound(player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.MASTER, 0.75F, 1.0F, false);
            }
            stack = ItemStack.EMPTY;
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    public static int getColorFromItemStack(ItemStack stack) {
        int c = 2337949;
        ResearchNoteData rd = OldResearchManager.getData(stack);
        if(rd != null) {
            c = rd.color;
        }

        return c;
    }

    public boolean getShareTag() {
        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        return itemstack.getItemDamage() < 64 ? I18n.format("item.researchnote.name") : I18n.format("item.discovery.name");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(stack.getItemDamage() == 24 || stack.getItemDamage() == 42) {
            tooltip.add(TextFormatting.GOLD + I18n.format("item.researchnote.unknown.1"));
            tooltip.add(TextFormatting.BLUE + I18n.format("item.researchnote.unknown.2"));
        }

        ResearchNoteData rd = OldResearchManager.getData(stack);
        if(rd != null && rd.key != null && ResearchCategories.getResearch(OldResearchManager.getStrippedKey(stack)) != null) {
            tooltip.add(TextFormatting.GOLD + ResearchCategories.getResearch(OldResearchManager.getStrippedKey(stack)).getLocalizedName());
//            tooltip.add(TextFormatting.ITALIC + OldResearchCategories.getResearch(OldResearchManager.getStrippedKey(stack)).getText());
            int warp = OldResearchApi.getWarp(rd.key);
            if(warp > 0) {
                if(warp > 5) {
                    warp = 5;
                }

                String ws = I18n.format("tc.forbidden");
                String wr = I18n.format("tc.forbidden.level." + warp);
                String wte = ws.replaceAll("%n", wr);
                tooltip.add(TextFormatting.DARK_PURPLE + wte);
            }
        }

    }

    public EnumRarity getRarity(ItemStack itemstack) {
        return itemstack.getItemDamage() < 64 ? EnumRarity.RARE : EnumRarity.EPIC;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelResourceLocation location0 = new ModelResourceLocation(Tags.MODID + ":researchnote", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 0, location0);

        ModelResourceLocation location2 = new ModelResourceLocation(Tags.MODID + ":discovery", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 64, location2);
    }

}

