package com.wonginnovations.oldresearch.utils;

import net.minecraft.entity.player.EntityPlayer;

public interface ResearchComplexityGenerator {
    Integer get(EntityPlayer player, String key);
}
