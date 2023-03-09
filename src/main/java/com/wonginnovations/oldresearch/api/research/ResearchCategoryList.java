package com.wonginnovations.oldresearch.api.research;

import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ResearchCategoryList {
    public int minDisplayColumn;
    public int minDisplayRow;
    public int maxDisplayColumn;
    public int maxDisplayRow;
    public ResourceLocation icon;
    public ResourceLocation background;
    public Map<String, ResearchItem> research = new HashMap();

    public ResearchCategoryList(ResourceLocation icon, ResourceLocation background) {
        this.icon = icon;
        this.background = background;
    }
}