package com.wonginnovations.oldresearch.api.research.curio;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;

public class RitesCurio extends BaseCurio {

    public RitesCurio() {
        super("crimson");
        this.setCategory("ELDRITCH");
        this.setWarp(IPlayerWarp.EnumWarpType.NORMAL, 1);
        this.setWarp(IPlayerWarp.EnumWarpType.TEMPORARY, 5);
    }

    @Override
    public boolean onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        int aw = ThaumcraftApi.internalMethods.getActualWarp(player);
        if (aw <= 20) {
            player.sendMessage(new TextComponentTranslation("fail.crimsonrites").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE)));
            return false;
        }

        ThaumcraftApi.internalMethods.completeResearch(player, "CrimsonRites");

        super.onItemRightClick(worldIn, player, hand);
        if (player.getRNG().nextBoolean()) {
            ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
        }
        return true;
    }

}
