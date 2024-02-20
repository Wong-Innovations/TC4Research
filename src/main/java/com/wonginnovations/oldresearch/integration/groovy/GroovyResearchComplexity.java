package com.wonginnovations.oldresearch.integration.groovy;

import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.wonginnovations.oldresearch.common.lib.research.DefaultResearchComplexity;
import groovy.lang.Closure;
import net.minecraft.entity.player.EntityPlayer;

public class GroovyResearchComplexity extends DefaultResearchComplexity {

    Closure<Integer> function;

    public GroovyResearchComplexity(Closure<Integer> func) {
        function = func;
    }

    @Override
    public Integer get(EntityPlayer player, String key) {
        if (function == null) return super.get(player, key);
        return ClosureHelper.call(super.get(player, key), function, player, key);
    }

}
