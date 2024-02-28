package com.wonginnovations.oldresearch.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.compat.mods.thaumcraft.aspect.AspectStack;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.blocks.ModBlocks;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.research.DefaultResearchComplexity;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import groovy.lang.Closure;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.blocks.BlocksTC;
import thaumcraft.api.research.ResearchCategories;

public class GroovyRegistry extends VirtualizedRegistry<Boolean> {

    @Override
    @GroovyBlacklist
    public void onReload() {
        OldResearchManager.RESEARCH_COMPLEXITY_FUNCTION = new DefaultResearchComplexity();
        OldResearchManager.RESEARCH_ASPECTS.clear();
    }

    @Override
    @GroovyBlacklist
    public void afterScriptLoad() {
        OldResearchManager.ASPECT_COMPLEXITY.clear();
        ResearchCategories.getResearchCategory("BASICS").research.remove("KNOWLEDGETYPES");
        ResearchCategories.getResearchCategory("BASICS").research.remove("THEORYRESEARCH");
        ResearchCategories.getResearchCategory("BASICS").research.remove("CELESTIALSCANNING");
        ResearchCategories.getResearch("CrimsonRites").getStages()[0].setObtain(new Object[]{new ItemStack(ModItems.CURIO, 1, 7)});
        OldResearchManager.parseJsonResearch(new ResourceLocation("oldresearch", "research.json"));
        OldResearchManager.patchResearch();
        ThaumcraftApi.registerObjectTag(new ItemStack(ModBlocks.RESEARCHTABLE, 1, 32767), new AspectList(new ItemStack(BlocksTC.researchTable)));
        OldResearchManager.computeAspectComplexity();
    }

    public void complexity(Closure<Integer> func) {
        OldResearchManager.RESEARCH_COMPLEXITY_FUNCTION = new GroovyResearchComplexity(func);
    }

    public void setResearchAspects(String key, AspectList aspects) {
        OldResearchManager.RESEARCH_ASPECTS.put(key, aspects);
    }

    public void setResearchAspects(String key, Aspect... aspects) {
        AspectList list = new AspectList();
        for (Aspect a : aspects) list.add(a, 1);
        OldResearchManager.RESEARCH_ASPECTS.put(key, list);
    }

    public void setResearchAspects(String key, AspectStack... aspects) {
        AspectList list = new AspectList();
        for (AspectStack a : aspects) list.add(a.getAspect(), 1);
        OldResearchManager.RESEARCH_ASPECTS.put(key, list);
    }

}
