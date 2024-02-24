package com.wonginnovations.oldresearch.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.compat.mods.thaumcraft.aspect.AspectStack;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.DefaultResearchComplexity;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import groovy.lang.Closure;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

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
        OldResearch.proxy.postInit(null);
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
