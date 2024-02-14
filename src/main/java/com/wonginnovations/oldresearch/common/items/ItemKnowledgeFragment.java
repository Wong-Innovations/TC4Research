package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.api.registration.IModelRegister;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectPool;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ConfigItems;

public class ItemKnowledgeFragment extends Item implements IModelRegister {

    public ItemKnowledgeFragment() {
        this.setRegistryName(Tags.MODID + ":knowledgefragment");
        this.setMaxStackSize(64);
        this.setHasSubtypes(false);
        this.setMaxDamage(0);
        this.setTranslationKey("knowledgefragment");
        this.setCreativeTab(ConfigItems.TABTC);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if(!player.capabilities.isCreativeMode) {
            stack.setCount(stack.getCount()-1);
        }

        if(!world.isRemote) {
            for(Aspect a : Aspect.getPrimalAspects()) {
                short q = (short)(world.rand.nextInt(2) + 1);
                OldResearch.proxy.playerKnowledge.addAspectPool(player.getGameProfile().getName(), a, q);
                OldResearchManager.scheduleSave(player);
                PacketHandler.INSTANCE.sendTo(new PacketAspectPool(a.getTag(), q, OldResearch.proxy.playerKnowledge.getAspectPoolFor(player.getGameProfile().getName(), a)), (EntityPlayerMP)player);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModels() {
        ModelResourceLocation location0 = new ModelResourceLocation(Tags.MODID + ":knowledgefragment", "inventory");
        ModelLoader.setCustomModelResourceLocation(this, 0, location0);
    }

}
