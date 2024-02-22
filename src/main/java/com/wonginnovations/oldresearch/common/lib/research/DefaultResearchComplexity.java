package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.utils.ResearchComplexityGenerator;
import net.minecraft.entity.player.EntityPlayer;

public class DefaultResearchComplexity implements ResearchComplexityGenerator {
    @Override
    public Integer get(EntityPlayer player, String key) {
        int researchCompleted = OldResearch.proxy.getPlayerKnowledge().getResearchCompleted(player.getGameProfile().getName());
        return Math.floorDiv(researchCompleted, 10) + 1;
    }
}
