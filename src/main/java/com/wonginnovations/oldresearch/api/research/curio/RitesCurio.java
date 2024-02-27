package com.wonginnovations.oldresearch.api.research.curio;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.capabilities.IPlayerWarp;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.api.research.ResearchCategories;

public class RitesCurio extends BaseCurio {

    public RitesCurio() {
        super("rites");
        this.setAspects(ResearchCategories.getResearchCategory("ELDRITCH").formula);
        this.setWarp(IPlayerWarp.EnumWarpType.NORMAL, 1);
        this.setWarp(IPlayerWarp.EnumWarpType.TEMPORARY, 5);
    }

    @Override
    public void onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        int aw = ThaumcraftApi.internalMethods.getActualWarp(player);
        if (aw <= 20) {
            player.sendMessage(new TextComponentString(TextFormatting.DARK_PURPLE + I18n.translateToLocal("fail.crimsonrites")));
            return;
        }

        IPlayerKnowledge knowledge = ThaumcraftCapabilities.getKnowledge(player);
        if (!knowledge.isResearchKnown("CrimsonRites")) {
            ThaumcraftApi.internalMethods.completeResearch(player, "CrimsonRites");
        }

        super.onItemRightClick(worldIn, player, hand);
        if (player.getRNG().nextBoolean()) {
            ThaumcraftApi.internalMethods.addWarpToPlayer(player, 1, IPlayerWarp.EnumWarpType.PERMANENT);
        }
    }

}
