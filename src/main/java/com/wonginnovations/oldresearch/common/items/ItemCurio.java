package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.SoundsTC;

public class ItemCurio extends Item {

    public ItemCurio() {
        this.setRegistryName(Tags.MODID + ":curio");
        this.setTranslationKey("curio");
        this.setCreativeTab(ConfigItems.TABTC);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);
        this.setNoRepair();
    }

    public @NotNull String getTranslationKey(ItemStack stack) {
        return "curio." + OldResearchManager.CURIOS.get(stack.getMetadata()).getName();
    }

    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (tab == ConfigItems.TABTC || tab == CreativeTabs.SEARCH) {
            for (int meta = 0; meta < OldResearchManager.CURIOS.size(); meta++) {
                items.add(new ItemStack(this, 1, meta));
            }
        }
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, @NotNull EnumHand hand) {
        worldIn.playSound(null, player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.NEUTRAL, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));
        if (!worldIn.isRemote) {

            OldResearchManager.CURIOS.get(player.getHeldItem(hand).getMetadata()).onItemRightClick(worldIn, player, hand);

            if (!player.capabilities.isCreativeMode) {
                player.getHeldItem(hand).shrink(1);
            }

            player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("tc.knowledge.gained")));
        }
        player.addStat(StatList.getObjectUseStats(this));
        return super.onItemRightClick(worldIn, player, hand);
    }

}
