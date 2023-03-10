package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketResearchComplete;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
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
        this.setRegistryName(OldResearch.ID + ":researchnote");
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setMaxStackSize(1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(!world.isRemote) {
            if(ResearchManager.getData(stack) != null && ResearchManager.getData(stack).isComplete() && !ResearchManager.isResearchComplete(player.getGameProfile().getName(), ResearchManager.getData(stack).key)) {
                if(ResearchManager.doesPlayerHaveRequisites(player.getGameProfile().getName(), ResearchManager.getData(stack).key)) {
                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(ResearchManager.getData(stack).key), (EntityPlayerMP)player);
                    OldResearch.proxy.getResearchManager().completeResearch(player, ResearchManager.getData(stack).key);
                    if(ResearchCategories.getResearch(ResearchManager.getData(stack).key).siblings != null) {
                        for(String sibling : ResearchCategories.getResearch(ResearchManager.getData(stack).key).siblings) {
                            if(!ResearchManager.isResearchComplete(player.getGameProfile().getName(), sibling) && ResearchManager.doesPlayerHaveRequisites(player.getGameProfile().getName(), sibling)) {
                                PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(sibling), (EntityPlayerMP)player);
                                OldResearch.proxy.getResearchManager().completeResearch(player, sibling);
                            }
                        }
                    }

                    stack.setCount(stack.getCount()-1);
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.MASTER, 0.75F, 1.0F, false);
                } else {
                    player.sendMessage(new TextComponentTranslation(I18n.format("tc.researcherror")));
                }
            } else if(stack.getItemDamage() == 42 || stack.getItemDamage() == 24) {
                String key = ResearchManager.findHiddenResearch(player);
                if(key.equals("FAIL")) {
                    stack.setCount(stack.getCount()-1);
                    EntityItem entityItem = new EntityItem(world, player.posX, player.posY + (double)(player.getEyeHeight() / 2.0F), player.posZ, new ItemStack(ModItems.KNOWLEDGEFRAGMENT, 7 + world.rand.nextInt(3)));
                    world.spawnEntity(entityItem);
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.erase, SoundCategory.MASTER, 0.75F, 1.0F, false);
                } else {
                    stack.setItemDamage(0);
                    stack.setTagCompound(ResearchManager.createNote(stack, key, player.world).getTagCompound());
                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.write, SoundCategory.MASTER, 0.75F, 1.0F, false);
                }
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @SideOnly(Side.CLIENT)
    public int getColorFromItemStack(ItemStack stack, int par2) {
        if(par2 == 1) {
            int c = 10066329;
            ResearchNoteData rd = ResearchManager.getData(stack);
            if(rd != null) {
                c = rd.color;
            }

            return c;
        } else {
            return 10066329;// super.getColorFromItemStack(stack, par2);
        }
    }

    public boolean getShareTag() {
        return true;
    }

    @Override
    public String getItemStackDisplayName(ItemStack itemstack) {
        return itemstack.getItemDamage() < 64 ? I18n.format("item.researchnotes.name") : I18n.format("item.discovery.name");
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(stack.getItemDamage() == 24 || stack.getItemDamage() == 42) {
            tooltip.add(TextFormatting.GOLD + I18n.format("item.researchnotes.unknown.1"));
            tooltip.add(TextFormatting.BLUE + I18n.format("item.researchnotes.unknown.2"));
        }

        ResearchNoteData rd = ResearchManager.getData(stack);
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
        ModelResourceLocation location0 = new ModelResourceLocation(OldResearch.ID + ":researchnotes", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 0, location0);

        ModelResourceLocation location2 = new ModelResourceLocation(OldResearch.ID + ":discovery", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 64, location2);
    }

}

