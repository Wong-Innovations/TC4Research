package com.wonginnovations.oldresearch.integration.groovy;

import com.cleanroommc.groovyscript.api.GroovyBlacklist;
import com.cleanroommc.groovyscript.registry.VirtualizedRegistry;
import com.wonginnovations.oldresearch.common.lib.research.DefaultResearchComplexity;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import groovy.lang.Closure;
import thaumcraft.api.aspects.AspectList;

public class GroovyRegistry extends VirtualizedRegistry<Boolean> {

    @Override
    @GroovyBlacklist
    public void onReload() {
        OldResearchManager.RESEARCH_COMPLEXITY_FUNCTION = new DefaultResearchComplexity();
        OldResearchManager.RESEARCH_ASPECTS.clear();
    }

    public void complexity(Closure<Integer> func) {
        OldResearchManager.RESEARCH_COMPLEXITY_FUNCTION = new GroovyResearchComplexity(func);
    }

    public void setResearchAspects(String key, AspectList aspects) {
        OldResearchManager.RESEARCH_ASPECTS.put(key, aspects);
    }

}
