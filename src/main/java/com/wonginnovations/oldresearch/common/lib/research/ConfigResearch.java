package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.api.research.ResearchPage;
import com.wonginnovations.oldresearch.common.items.ModItems;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ConfigResearch {

    public static HashMap<String, Object> recipes = new HashMap<>();

    public static void init() {
        initCategories();
        initBasicResearch();
    }

    private static void initCategories() {
        ResearchCategories.registerCategory("BASICS", new ResourceLocation("oldresearch", "textures/items/thaumonomicon_cheat.png"), new ResourceLocation("oldresearch", "textures/gui/gui_researchback.png"));
    }

    private static void initBasicResearch() {
        (new ResearchItem("ASPECTS", "BASICS", new AspectList(), 0, 0, 0, new ResourceLocation("thaumcraft", "textures/misc/r_aspects.png"))).setPages(new ResearchPage("tc.research_page.ASPECTS.1"), new ResearchPage("tc.research_page.ASPECTS.2"), new ResearchPage("tc.research_page.ASPECTS.3")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("PECH", "BASICS", new AspectList(), -4, -4, 0, new ResourceLocation("thaumcraft", "textures/misc/r_pech.png"))).setPages(new ResearchPage("tc.research_page.PECH.1"), new ResearchPage("tc.research_page.PECH.2")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("NODES", "BASICS", new AspectList(), -2, 0, 0, new ResourceLocation("thaumcraft", "textures/misc/r_nodes.png"))).setPages(new ResearchPage("tc.research_page.NODES.1"), new ResearchPage("tc.research_page.NODES.2"), new ResearchPage("tc.research_page.NODES.3")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("WARP", "BASICS", new AspectList(), 0, 2, 0, new ResourceLocation("thaumcraft", "textures/misc/r_warp.png"))).setPages(new ResearchPage("tc.research_page.WARP.1"), new ResearchPage("tc.research_page.WARP.2"), new ResearchPage("tc.research_page.WARP.3")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("RESEARCH", "BASICS", new AspectList(), 2, 0, 0, new ItemStack(Items.PAPER, 1))).setPages(new ResearchPage("tc.research_page.RESEARCH.1"), new ResearchPage("tc.research_page.RESEARCH.2"), new ResearchPage((IRecipe)recipes.get("Thaumometer")), new ResearchPage("tc.research_page.RESEARCH.3"), new ResearchPage("tc.research_page.RESEARCH.4"), new ResearchPage((IRecipe)recipes.get("Scribe1")), new ResearchPage((IRecipe)recipes.get("Scribe2")), new ResearchPage((IRecipe)recipes.get("Scribe3")), new ResearchPage("tc.research_page.RESEARCH.5"), new ResearchPage("tc.research_page.RESEARCH.6"), new ResearchPage("tc.research_page.RESEARCH.7"), new ResearchPage("tc.research_page.RESEARCH.8"), new ResearchPage("tc.research_page.RESEARCH.9"), new ResearchPage("tc.research_page.RESEARCH.10"), new ResearchPage("tc.research_page.RESEARCH.11"), new ResearchPage("tc.research_page.RESEARCH.12")).setAutoUnlock().setStub().setRound().registerResearchItem();
        (new ResearchItem("KNOWFRAG", "BASICS", new AspectList(), 3, -2, 0, new ItemStack(ModItems.KNOWLEDGEFRAGMENT))).setPages(new ResearchPage("tc.research_page.KNOWFRAG.1"), new ResearchPage((IRecipe)recipes.get("KnowFrag"))).setStub().setRound().setAutoUnlock().setParents("RESEARCH").registerResearchItem();
        (new ResearchItem("THAUMONOMICON", "BASICS", new AspectList(), 1, -2, 0, new ItemStack(ModItems.THAUMONOMICON, 1))).setPages(new ResearchPage("tc.research_page.THAUMONOMICON.1"), new ResearchPage((List)recipes.get("Thaumonomicon"))).setAutoUnlock().setStub().setRound().setParents("RESEARCH").registerResearchItem();
        ArrayList<IRecipe> rc = new ArrayList<>();

        for(int a = 0; a <= 6; ++a) {
            rc.add((IRecipe)recipes.get("Clusters" + a));
        }

        (new ResearchItem("ORE", "BASICS", new AspectList(), -2, -2, 0, new ItemStack(Items.PAPER, 1))).setPages(new ResearchPage("tc.research_page.ORE.1"), new ResearchPage("tc.research_page.ORE.2"), new ResearchPage((IRecipe[])rc.toArray(new IRecipe[0])), new ResearchPage("tc.research_page.ORE.3"), new ResearchPage("tc.research_page.ORE.4")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("PLANTS", "BASICS", new AspectList(), -2, -4, 0, new ItemStack(Items.PAPER, 1))).setPages(new ResearchPage("tc.research_page.PLANTS.1"), new ResearchPage((IRecipe)recipes.get("PlankGreatwood")), new ResearchPage("tc.research_page.PLANTS.2"), new ResearchPage((IRecipe)recipes.get("PlankSilverwood")), new ResearchPage("tc.research_page.PLANTS.3"), new ResearchPage("tc.research_page.PLANTS.4"), new ResearchPage("tc.research_page.PLANTS.5"), new ResearchPage("tc.research_page.PLANTS.6")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("ENCHANT", "BASICS", new AspectList(), -4, -2, 0, new ResourceLocation("thaumcraft", "textures/misc/r_enchant.png"))).setPages(new ResearchPage("tc.research_page.ENCHANT.1"), new ResearchPage("tc.research_page.ENCHANT.2")).setStub().setRound().setAutoUnlock().registerResearchItem();
        (new ResearchItem("NODETAPPER1", "BASICS", (new AspectList()).add(Aspect.AURA, 3).add(Aspect.MAGIC, 3).add(Aspect.MOTION, 3).add(Aspect.EXCHANGE, 3), -4, 1, 2, new ResourceLocation("thaumcraft", "textures/misc/r_nodetap1.png"))).setPages(new ResearchPage("tc.research_page.NODETAPPER1.1")).setParents("NODES").setRound().registerResearchItem();
        (new ResearchItem("NODEPRESERVE", "BASICS", (new AspectList()).add(Aspect.AURA, 3).add(Aspect.UNDEAD, 3).add(Aspect.SENSES, 3), -6, 2, 2, new ResourceLocation("thaumcraft", "textures/misc/r_nodepreserve.png"))).setPages(new ResearchPage("tc.research_page.NODEPRESERVE")).setParents("NODETAPPER1").setRound().registerResearchItem();
//        ItemStack jarIs = new ItemStack(ConfigItems.itemJarNode);
//        ((ItemJarNode)jarIs.getItem()).setAspects(jarIs, (new AspectList()).add(Aspect.AIR, 40).add(Aspect.FIRE, 40).add(Aspect.WATER, 40).add(Aspect.EARTH, 40));
//        ((ItemJarNode)jarIs.getItem()).setNodeAttributes(jarIs, NodeType.NORMAL, (NodeModifier)null, "");
        (new ResearchItem("NODEJAR", "BASICS", (new AspectList()).add(Aspect.AURA, 6).add(Aspect.UNDEAD, 3).add(Aspect.EXCHANGE, 3).add(Aspect.MOTION, 3), -7, 4, 3, new ItemStack(Items.PAPER, 1))).setPages(new ResearchPage("tc.research_page.NODEJAR.1"), new ResearchPage((List)recipes.get("NodeJar")), new ResearchPage("tc.research_page.NODEJAR.2")).setParents("NODEPRESERVE").setConcealed().registerResearchItem();
        (new ResearchItem("NODETAPPER2", "BASICS", (new AspectList()).add(Aspect.AURA, 6).add(Aspect.MAGIC, 3).add(Aspect.MOTION, 3).add(Aspect.EXCHANGE, 3), -3, 3, 2, new ResourceLocation("thaumcraft", "textures/misc/r_nodetap2.png"))).setPages(new ResearchPage("tc.research_page.NODETAPPER2.1")).setParents("NODETAPPER1").setSpecial().setRound().registerResearchItem();
        (new ResearchItem("RESEARCHER1", "BASICS", (new AspectList()).add(Aspect.MIND, 3).add(Aspect.SENSES, 3).add(Aspect.ORDER, 3), 4, 1, 1, new ResourceLocation("thaumcraft", "textures/misc/r_researcher1.png"))).setPages(new ResearchPage("tc.research_page.RESEARCHER1.1")).setRound().setParents("RESEARCH").registerResearchItem();
//        (new ResearchItem("DECONSTRUCTOR", "BASICS", (new AspectList()).add(Aspect.MIND, 3).add(Aspect.CRAFT, 3).add(Aspect.ENTROPY, 3), 6, 2, 1, new ItemStack(ConfigBlocks.blockTable, 1, 14))).setPages(new ResearchPage[]{new ResearchPage("tc.research_page.DECONSTRUCTOR.1"), new ResearchPage((IArcaneRecipe)recipes.get("Deconstructor")), new ResearchPage("tc.research_page.DECONSTRUCTOR.2")}).setRound().setParents(new String[]{"RESEARCHER1"}).registerResearchItem();
//        (new ResearchItem("RESEARCHER2", "BASICS", (new AspectList()).add(Aspect.MIND, 6).add(Aspect.ORDER, 3).add(Aspect.SENSES, 3).add(Aspect.MAGIC, 3), 3, 3, 2, new ResourceLocation("thaumcraft", "textures/misc/r_researcher2.png"))).setPages(new ResearchPage[]{new ResearchPage("tc.research_page.RESEARCHER2.1")}).setRound().setSpecial().setParents(new String[]{"RESEARCHER1"}).registerResearchItem();
//        ThaumcraftApi.addWarpToResearch("RESEARCHER2", 1);
//        (new ResearchItem("RESEARCHDUPE", "BASICS", (new AspectList()).add(Aspect.MIND, 6).add(Aspect.EXCHANGE, 3).add(Aspect.SENSES, 3).add(Aspect.GREED, 3).add(Aspect.CRAFT, 3), 4, 5, 3, new ResourceLocation("thaumcraft", "textures/misc/r_resdupe.png"))).setPages(new ResearchPage[]{new ResearchPage("tc.research_page.RESEARCHDUPE.1")}).setRound().setParents(new String[]{"RESEARCHER2"}).registerResearchItem();
//        (new ResearchItem("CRIMSON", "BASICS", new AspectList(), 0, 4, 0, new ItemStack(ConfigItems.itemEldritchObject, 1, 1))).setPages(new ResearchPage[]{new ResearchPage("tc.research_page.CRIMSON.1")}).setStub().setHidden().setRound().setSpecial().registerResearchItem();
//        ThaumcraftApi.addWarpToResearch("CRIMSON", 3);
    }

}
