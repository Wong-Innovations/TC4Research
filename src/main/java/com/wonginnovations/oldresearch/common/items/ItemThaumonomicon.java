package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchCategoryList;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncAspects;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncResearch;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.lib.SoundsTC;

import java.util.List;

public class ItemThaumonomicon extends ItemTCBase implements IModelRegister {
    public ItemThaumonomicon() {
        super("thaumonomicon", "normal", "cheat");
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
    }

    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab == ConfigItems.TABTC || tab == CreativeTabs.SEARCH) {
            items.add(new ItemStack(this, 1, 0));
            if (ModConfig.CONFIG_MISC.allowCheatSheet) {
                items.add(new ItemStack(this, 1, 42));
            }
        }

    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        if (stack.getItemDamage() == 42) {
            tooltip.add(TextFormatting.DARK_PURPLE + "Creative only");
        }

    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if(!world.isRemote) {
            ItemStack stack = player.getHeldItem(hand);
            if(ModConfig.CONFIG_MISC.allowCheatSheet && stack.getItemDamage() == 42) {
                for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
                    for(ResearchItem ri : cat.research.values()) {
                        if(!ResearchManager.isResearchComplete(player.getGameProfile().getName(), ri.key)) {
                            OldResearch.proxy.getResearchManager().completeResearch(player, ri.key);
                        }
                    }
                }

                for(Aspect aspect : Aspect.aspects.values()) {
                    if(!OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(player.getGameProfile().getName(), aspect)) {
                        OldResearch.proxy.researchManager.completeAspect(player, aspect, (short)50);
                    }
                }
            } else {
                for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
                    for(ResearchItem ri : cat.research.values()) {
                        if(ResearchManager.isResearchComplete(player.getGameProfile().getName(), ri.key) && ri.siblings != null) {
                            for(String sib : ri.siblings) {
                                if(!ResearchManager.isResearchComplete(player.getGameProfile().getName(), sib)) {
                                    OldResearch.proxy.getResearchManager().completeResearch(player, sib);
                                }
                            }
                        }
                    }
                }
            }

            PacketHandler.INSTANCE.sendTo(new PacketSyncResearch(player), (EntityPlayerMP)player);
            PacketHandler.INSTANCE.sendTo(new PacketSyncAspects(player), (EntityPlayerMP)player);
        } else {
            world.playSound(player.posX, player.posY, player.posZ, SoundsTC.page, SoundCategory.MASTER, 1.0F, 1.0F, false);
        }

        player.openGui(Thaumcraft.instance, 12, world, 0, 0, 0);
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
//        if (!world.isRemote) {
//            Collection rc;
//            Iterator var5;
//            ResearchCategory cat;
//            Collection rl;
//            Iterator var8;
//            ResearchEntry ri;
//            if (ModConfig.CONFIG_MISC.allowCheatSheet && player.getHeldItem(hand).getItemDamage() == 1) {
//                rc = ResearchCategories.researchCategories.values();
//                var5 = rc.iterator();
//
//                while(var5.hasNext()) {
//                    cat = (ResearchCategory)var5.next();
//                    rl = cat.research.values();
//                    var8 = rl.iterator();
//
//                    while(var8.hasNext()) {
//                        ri = (ResearchEntry)var8.next();
//                        CommandThaumcraft.giveRecursiveResearch(player, ri.getKey());
//                    }
//                }
//            } else {
//                rc = ResearchCategories.researchCategories.values();
//                var5 = rc.iterator();
//
//                label65:
//                while(var5.hasNext()) {
//                    cat = (ResearchCategory)var5.next();
//                    rl = cat.research.values();
//                    var8 = rl.iterator();
//
//                    while(true) {
//                        do {
//                            do {
//                                if (!var8.hasNext()) {
//                                    continue label65;
//                                }
//
//                                ri = (ResearchEntry)var8.next();
//                            } while(!ThaumcraftCapabilities.knowsResearch(player, ri.getKey()));
//                        } while(ri.getSiblings() == null);
//
//                        String[] var10 = ri.getSiblings();
//                        int var11 = var10.length;
//
//                        for(int var12 = 0; var12 < var11; ++var12) {
//                            String sib = var10[var12];
//                            if (!ThaumcraftCapabilities.knowsResearch(player, sib)) {
//                                ResearchManager.completeResearch(player, sib);
//                            }
//                        }
//                    }
//                }
//            }
//
//            ThaumcraftCapabilities.getKnowledge(player).sync((EntityPlayerMP)player);
//        } else {
//            world.playSound(player.posX, player.posY, player.posZ, SoundsTC.page, SoundCategory.MASTER, 1.0F, 1.0F, false);
//        }
//
//        player.openGui(Thaumcraft.instance, 12, world, 0, 0, 0);
//        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    public EnumRarity getRarity(ItemStack itemstack) {
        return itemstack.getItemDamage() != 42 ? EnumRarity.UNCOMMON : EnumRarity.EPIC;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelResourceLocation location0 = new ModelResourceLocation(OldResearch.ID + ":thaumonomicon", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 0, location0);

        ModelResourceLocation location2 = new ModelResourceLocation(OldResearch.ID + ":thaumonomicon_cheat", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 42, location2);
    }

}
