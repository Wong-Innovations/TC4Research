package com.wonginnovations.oldresearch.common.lib.events;

import com.google.common.io.Files;
import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchCategoryList;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;

@Mod.EventBusSubscriber
public class EntityEvents {

    @SubscribeEvent
    public static void playerLoad(PlayerEvent.LoadFromFile event) {
        EntityPlayer p = event.getEntityPlayer();
        OldResearch.proxy.getPlayerKnowledge().wipePlayerKnowledge(p.getGameProfile().getName());
        File file1 = getPlayerFile("thaum", event.getPlayerDirectory(), p.getGameProfile().getName());
        boolean legacy = false;
        if(!file1.exists()) {
            File filep = event.getPlayerFile("thaum");
            if(filep.exists()) {
                try {
                    Files.copy(filep, file1);
                    OldResearch.LOGGER.info("Using and converting UUID Thaumcraft savefile for " + p.getGameProfile().getName());
                    legacy = true;
                    filep.delete();
                    File fb = event.getPlayerFile("thaumback");
                    if(fb.exists()) {
                        fb.delete();
                    }
                } catch (IOException var12) {
                    ;
                }
            } else {
                File filet = getLegacyPlayerFile(p);
                if(filet.exists()) {
                    try {
                        Files.copy(filet, file1);
                        OldResearch.LOGGER.info("Using pre MC 1.7.10 Thaumcraft savefile for " + p.getGameProfile().getName());
                        legacy = true;
                    } catch (IOException var11) {
                        ;
                    }
                }
            }
        }

        OldResearchManager.loadPlayerData(p, file1, getPlayerFile("thaumback", event.getPlayerDirectory(), p.getGameProfile().getName()), legacy);

        for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
            for(ResearchItem ri : cat.research.values()) {
                if(ri.isAutoUnlock()) {
                    OldResearch.proxy.getOldResearchManager().completeResearch(p, ri.key);
                }
            }
        }

    }

    public static File getLegacyPlayerFile(EntityPlayer player) {
        try {
            File playersDirectory = new File(player.world.getSaveHandler().getWorldDirectory(), "players");
            return new File(playersDirectory, player.getGameProfile().getName() + ".thaum");
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public static File getPlayerFile(String suffix, File playerDirectory, String playername) {
        if("dat".equals(suffix)) {
            throw new IllegalArgumentException("The suffix \'dat\' is reserved");
        } else {
            return new File(playerDirectory, playername + "." + suffix);
        }
    }

    @SubscribeEvent
    public static void playerSave(PlayerEvent.SaveToFile event) {
        EntityPlayer p = event.getEntityPlayer();
        OldResearchManager.savePlayerData(p, getPlayerFile("thaum", event.getPlayerDirectory(), p.getGameProfile().getName()), getPlayerFile("thaumback", event.getPlayerDirectory(), p.getGameProfile().getName()));
    }

}