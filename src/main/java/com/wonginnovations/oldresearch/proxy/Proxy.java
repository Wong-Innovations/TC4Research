package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.capabilities.PlayerAspects;
import com.wonginnovations.oldresearch.common.blocks.ModBlocks;
import com.wonginnovations.oldresearch.common.items.ItemCurio;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.research.PlayerKnowledge;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
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
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.research.ResearchCategories;

import java.util.ArrayList;
import java.util.Map;

public class Proxy implements IGuiHandler {
    ProxyGUI proxyGUI = new ProxyGUI();

    public PlayerKnowledge playerKnowledge = new PlayerKnowledge();

    public PlayerKnowledge getPlayerKnowledge() {
        return this.playerKnowledge;
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
        ResearchCategories.getResearchCategory("BASICS").research.remove("KNOWLEDGETYPES");
        ResearchCategories.getResearchCategory("BASICS").research.remove("THEORYRESEARCH");
        ResearchCategories.getResearchCategory("BASICS").research.remove("CELESTIALSCANNING");
        OldResearchManager.parseJsonResearch(new ResourceLocation("oldresearch", "research/basics.json"));
        OldResearchManager.patchResearch();
        ThaumcraftApi.registerObjectTag(new ItemStack(ModBlocks.RESEARCHTABLE, 1, 32767), new AspectList(new ItemStack(BlocksTC.researchTable)));
        OldResearchManager.computeAspectComplexity();
        OldResearchManager.initCurioMeta();
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

}
