package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.capabilities.PlayerAspects;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.research.PlayerKnowledge;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import com.wonginnovations.oldresearch.core.mixin.DustTriggerOreAccessor;
import com.wonginnovations.oldresearch.core.mixin.DustTriggerSimpleAccessor;
import com.wonginnovations.oldresearch.core.mixin.IDustTriggerAccessor;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.crafting.IDustTrigger;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.api.research.*;
import thaumcraft.common.items.curios.ItemThaumonomicon;
import thaumcraft.common.lib.crafting.DustTriggerOre;
import thaumcraft.common.lib.crafting.DustTriggerSimple;

import java.util.ArrayList;
import java.util.Map;

public class Proxy implements IGuiHandler {
    ProxyGUI proxyGUI = new ProxyGUI();

    public PlayerKnowledge playerKnowledge = new PlayerKnowledge();
    public OldResearchManager oldResearchManager = new OldResearchManager();

    public PlayerKnowledge getPlayerKnowledge() {
        return this.playerKnowledge;
    }

    public OldResearchManager getOldResearchManager() {
        return this.oldResearchManager;
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

    public void preInit(FMLPreInitializationEvent event) {
        PacketHandler.preInit();
        PlayerAspects.preInit();
        GameRegistry.registerTileEntity(TileResearchTable.class, new ResourceLocation("oldresearch:TileResearchTable"));

        MinecraftForge.EVENT_BUS.register(OldResearch.instance);
    }

    public void init(FMLInitializationEvent event) {
        NetworkRegistry.INSTANCE.registerGuiHandler(OldResearch.instance, this);
        this.registerDisplayInformation();
    }

    public void postInit(FMLPostInitializationEvent event) {
        ResearchManager.patchResearch();
        ResearchManager.computeAspectComplexity();
        this.syncAspects();
        this.patchSalisTriggers();
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

    private void syncAspects() {
        CommonInternals.scanEntities.forEach(tag -> {
            OldResearchApi.EntityTagsNBT[] nbts = new OldResearchApi.EntityTagsNBT[tag.nbts.length];
            for (int i = 0; i < tag.nbts.length; i++) {
                nbts[i] = new OldResearchApi.EntityTagsNBT(tag.nbts[i].name, tag.nbts[i].value);
            }
            OldResearchApi.registerEntityTag(tag.entityName, tag.aspects, nbts);
        });
    }

//    private void patchResearch() {
    //        ResearchStage[] researchStages = new ResearchStage[12];
//        for (int i = 1; i <= 12; i++) {
//            researchStages[i-1] = new ResearchStage();
//            researchStages[i-1].setText("research.KNOWLEDGETYPES.stage." + i);
//        }
//        ResearchCategories.getResearch("KNOWLEDGETYPES").setStages(researchStages);
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

    private void patchSalisTriggers() {
        ArrayList<IDustTrigger> iDustTriggers = IDustTriggerAccessor.getTriggers();
        // remove default thaumonomicion trigger with this mod's thaumonomicon
        if (iDustTriggers != null) {
            for (IDustTrigger trigger : iDustTriggers) {
                if (trigger instanceof DustTriggerSimple && ((DustTriggerSimpleAccessor) trigger).getResult().getItem() instanceof ItemThaumonomicon) {
                    ((DustTriggerSimpleAccessor) trigger).setResult(new ItemStack(ModItems.THAUMONOMICON));
                } else if (trigger instanceof DustTriggerOre && ((DustTriggerOreAccessor) trigger).getResult().getItem() instanceof ItemThaumonomicon) {
                    ((DustTriggerOreAccessor) trigger).setResult(new ItemStack(ModItems.THAUMONOMICON));
                }
            }
        }
    }

}
