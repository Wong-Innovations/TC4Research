package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketResearchComplete;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.lib.SoundsTC;

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
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(!world.isRemote) {
            if(OldResearchManager.getData(stack) != null && OldResearchManager.getData(stack).isComplete() && !OldResearchManager.isResearchComplete(player.getGameProfile().getName(), OldResearchManager.getData(stack).key)) {
                if(OldResearchManager.doesPlayerHaveRequisites(player.getGameProfile().getName(), OldResearchManager.getData(stack).key)) {
                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(OldResearchManager.getData(stack).key), (EntityPlayerMP)player);
                    OldResearch.proxy.getOldResearchManager().completeResearch(player, OldResearchManager.getData(stack).key);
                    if(ResearchCategories.getResearch(OldResearchManager.getData(stack).key).siblings != null) {
                        for(String sibling : ResearchCategories.getResearch(OldResearchManager.getData(stack).key).siblings) {
                            if(!OldResearchManager.isResearchComplete(player.getGameProfile().getName(), sibling) && OldResearchManager.doesPlayerHaveRequisites(player.getGameProfile().getName(), sibling)) {
                                PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(sibling), (EntityPlayerMP)player);
                                OldResearch.proxy.getOldResearchManager().completeResearch(player, sibling);
                            }
                        }
                    }

                    stack.setCount(stack.getCount()-1);
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.MASTER, 0.75F, 1.0F, false);
                } else {
                    player.sendMessage(new TextComponentTranslation(I18n.format("tc.researcherror")));
                }
            } else if(stack.getItemDamage() == 42 || stack.getItemDamage() == 24) {
                String key = OldResearchManager.findHiddenResearch(player);
                if(key.equals("FAIL")) {
                    stack.setCount(stack.getCount()-1);
                    EntityItem entityItem = new EntityItem(world, player.posX, player.posY + (double)(player.getEyeHeight() / 2.0F), player.posZ, new ItemStack(ModItems.KNOWLEDGEFRAGMENT, 7 + world.rand.nextInt(3)));
                    world.spawnEntity(entityItem);
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.erase, SoundCategory.MASTER, 0.75F, 1.0F, false);
                } else {
                    stack.setItemDamage(0);
                    stack.setTagCompound(OldResearchManager.createNote(stack, key, player.world).getTagCompound());
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.write, SoundCategory.MASTER, 0.75F, 1.0F, false);
                }
            }
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
        if(rd != null && rd.key != null && ResearchCategories.getResearch(rd.key) != null) {
            tooltip.add(TextFormatting.GOLD + ResearchCategories.getResearch(rd.key).getName());
            tooltip.add(TextFormatting.ITALIC + ResearchCategories.getResearch(rd.key).getText());
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

