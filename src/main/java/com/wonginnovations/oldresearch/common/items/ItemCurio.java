package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import thaumcraft.Thaumcraft;
import thaumcraft.common.config.ConfigItems;

import java.util.List;

public class ItemCurio extends Item {

    public ItemCurio() {
        this.setRegistryName(Thaumcraft.MODID + ":curio");
        this.setTranslationKey("curio");
        this.setCreativeTab(ConfigItems.TABTC);
        this.setHasSubtypes(true);
        this.setMaxStackSize(64);
        this.setMaxDamage(0);
        this.setNoRepair();
    }

    @Override
    public @NotNull EnumRarity getRarity(@NotNull ItemStack itemstack) {
        return EnumRarity.UNCOMMON;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, World worldIn, List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        tooltip.add(I18n.format("item.curio.text"));
    }

    public @NotNull String getTranslationKey(ItemStack itemStack) {
        return super.getTranslationKey() + "." + OldResearchManager.CURIOS.get(itemStack.getMetadata()).getName().toLowerCase();
    }

    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (tab == ConfigItems.TABTC || tab == CreativeTabs.SEARCH) {
            for(int meta = 0; meta < OldResearchManager.CURIOS.size(); ++meta) {
                items.add(new ItemStack(this, 1, meta));
            }
        }
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World worldIn, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!worldIn.isRemote && OldResearchManager.CURIOS.get(stack.getMetadata()).onItemRightClick(worldIn, player, hand)) {
            player.sendStatusMessage(new TextComponentTranslation("tc.knowledge.gained").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE).setItalic(true)), true);
            if(!player.capabilities.isCreativeMode) {
                stack.setCount(stack.getCount()-1);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

}
