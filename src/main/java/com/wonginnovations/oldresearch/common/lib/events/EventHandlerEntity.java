package com.wonginnovations.oldresearch.common.lib.events;

import com.google.common.io.Files;
import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchCategoryList;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;

public class EventHandlerEntity {
    public HashMap<Integer, Float> prevStep = new HashMap();
    public static HashMap<String, ArrayList<WeakReference<Entity>>> linkedEntities = new HashMap();

    @SubscribeEvent
    public void playerLoad(PlayerEvent.LoadFromFile event) {
        EntityPlayer p = event.getEntityPlayer();
        OldResearch.proxy.getPlayerKnowledge().wipePlayerKnowledge(p.getGameProfile().getName());
        File file1 = this.getPlayerFile("thaum", event.getPlayerDirectory(), p.getGameProfile().getName());
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
                File filet = this.getLegacyPlayerFile(p);
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

        ResearchManager.loadPlayerData(p, file1, this.getPlayerFile("thaumback", event.getPlayerDirectory(), p.getGameProfile().getName()), legacy);

        for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
            for(ResearchItem ri : cat.research.values()) {
                if(ri.isAutoUnlock()) {
                    OldResearch.proxy.getResearchManager().completeResearch(p, ri.key);
                }
            }
        }

    }

    public File getLegacyPlayerFile(EntityPlayer player) {
        try {
            File playersDirectory = new File(player.world.getSaveHandler().getWorldDirectory(), "players");
            return new File(playersDirectory, player.getGameProfile().getName() + ".thaum");
        } catch (Exception var3) {
            var3.printStackTrace();
            return null;
        }
    }

    public File getPlayerFile(String suffix, File playerDirectory, String playername) {
        if("dat".equals(suffix)) {
            throw new IllegalArgumentException("The suffix \'dat\' is reserved");
        } else {
            return new File(playerDirectory, playername + "." + suffix);
        }
    }

    @SubscribeEvent
    public void playerSave(PlayerEvent.SaveToFile event) {
        EntityPlayer p = event.getEntityPlayer();
        ResearchManager.savePlayerData(p, this.getPlayerFile("thaum", event.getPlayerDirectory(), p.getGameProfile().getName()), this.getPlayerFile("thaumback", event.getPlayerDirectory(), p.getGameProfile().getName()));
    }

}