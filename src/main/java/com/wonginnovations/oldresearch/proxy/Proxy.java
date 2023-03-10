package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.PlayerKnowledge;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ResearchAddendum;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchStage;

import java.util.ArrayList;
import java.util.Map;

public class Proxy implements IGuiHandler {
    ProxyGUI proxyGUI = new ProxyGUI();

    public PlayerKnowledge playerKnowledge = new PlayerKnowledge();
    public ResearchManager researchManager = new ResearchManager();

    public PlayerKnowledge getPlayerKnowledge() {
        return this.playerKnowledge;
    }

    public ResearchManager getResearchManager() {
        return this.researchManager;
    }

    public Map<String, ArrayList<String>> getCompletedResearch() {
        return this.playerKnowledge.researchCompleted;
    }

    public Map<String, ArrayList<String>> getScannedObjects() {
        return this.playerKnowledge.objectsScanned;
    }

    public Map<String, ArrayList<String>> getScannedEntities() {
        return this.playerKnowledge.entitiesScanned;
    }

    public Map<String, ArrayList<String>> getScannedPhenomena() {
        return this.playerKnowledge.phenomenaScanned;
    }

    public Map<String, AspectList> getKnownAspects() {
        return this.playerKnowledge.aspectsDiscovered;
    }

    public void registerModel(ItemBlock itemBlock) {
    }

    public void onConstruction(FMLConstructionEvent event) {
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(OldResearch.instance, this);
        this.registerDisplayInformation();
    }

    public void postInit(FMLPostInitializationEvent event) {
        this.patchResearch();
    }

    public void registerDisplayInformation() {
    }

    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return this.proxyGUI.getClientGuiElement(ID, player, world, x, y, z);
    }

    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return this.proxyGUI.getServerGuiElement(ID, player, world, x, y, z);
    }

    public boolean isClient() {
        return false;
    }

    public boolean isServer() {
        return true;
    }

    public World getClientWorld() {
        return null;
    }

    private void patchResearch() {
//        ResearchStage[] researchStages = new ResearchStage[12];
//        for (int i = 1; i <= 12; i++) {
//            researchStages[i-1] = new ResearchStage();
//            researchStages[i-1].setText("research.KNOWLEDGETYPES.stage." + i);
//        }
//        ResearchCategories.getResearch("KNOWLEDGETYPES").setStages(researchStages);
    }

//    private void patchResearch() {
//        ResearchStage[] researchStages = new ResearchStage[]{new ResearchStage()};
//        researchStages[0].setText("research.KNOWLEDGETYPES.stage.1");
//        ResearchAddendum[] researchAddenda = new ResearchAddendum[11];
//        for (int i = 1; i < 12; i++) {
//            researchAddenda[i-1] = new ResearchAddendum();
//            researchAddenda[i-1].setText("research.KNOWLEDGETYPES.addenda." + i);
//        }
//        ResearchCategories.getResearch("KNOWLEDGETYPES").setStages(researchStages);
//        ResearchCategories.getResearch("KNOWLEDGETYPES").setAddenda(researchAddenda);
//    }

}
