package com.wonginnovations.oldresearch.api.research;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.FMLLog;
import org.apache.logging.log4j.Level;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;

public class ResearchCategories {
    public static LinkedHashMap<String, ResearchCategoryList> researchCategories = new LinkedHashMap();

    public static ResearchCategoryList getResearchList(String key) {
        return researchCategories.get(key);
    }

    public static String getCategoryName(String key) {
        return I18n.format("tc.research_category." + key);
    }

    public static ResearchItem getResearch(String key) {
        label23:
        for(Object cat : researchCategories.values()) {
            Collection rl = ((ResearchCategoryList)cat).research.values();
            Iterator i$ = rl.iterator();

            Object ri;
            while(true) {
                if(!i$.hasNext()) {
                    continue label23;
                }

                ri = i$.next();
                if(((ResearchItem)ri).key.equals(key)) {
                    break;
                }
            }

            return (ResearchItem)ri;
        }

        return null;
    }

    public static void registerCategory(String key, ResourceLocation icon, ResourceLocation background) {
        if(getResearchList(key) == null) {
            ResearchCategoryList rl = new ResearchCategoryList(icon, background);
            researchCategories.put(key, rl);
        }

    }

    public static void addResearch(ResearchItem ri) {
        ResearchCategoryList rl = getResearchList(ri.category);
        if(rl != null && !rl.research.containsKey(ri.key)) {
            if(!ri.isVirtual()) {
                for(ResearchItem rr : rl.research.values()) {
                    if(rr.displayColumn == ri.displayColumn && rr.displayRow == ri.displayRow) {
                        FMLLog.log(Level.FATAL, "[Thaumcraft] Research [" + ri.getName() + "] not added as it overlaps with existing research [" + rr.getName() + "]", new Object[0]);
                        return;
                    }
                }
            }

            rl.research.put(ri.key, ri);
            if(ri.displayColumn < rl.minDisplayColumn) {
                rl.minDisplayColumn = ri.displayColumn;
            }

            if(ri.displayRow < rl.minDisplayRow) {
                rl.minDisplayRow = ri.displayRow;
            }

            if(ri.displayColumn > rl.maxDisplayColumn) {
                rl.maxDisplayColumn = ri.displayColumn;
            }

            if(ri.displayRow > rl.maxDisplayRow) {
                rl.maxDisplayRow = ri.displayRow;
            }
        }

    }
}
