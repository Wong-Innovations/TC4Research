package com.wonginnovations.oldresearch.api;

import com.wonginnovations.oldresearch.common.lib.research.IScanEventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.oredict.OreDictionary;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.internal.DummyInternalMethodHandler;
import thaumcraft.api.internal.IInternalMethodHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class OldResearchApi {

    public static IInternalMethodHandler internalMethods = new DummyInternalMethodHandler();
    public static ArrayList<IScanEventHandler> scanEventhandlers = new ArrayList<>();
    public static ArrayList<OldResearchApi.EntityTags> scanEntities = new ArrayList<>();
    public static ConcurrentHashMap<List<?>, AspectList> objectTags = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<List<?>, int[]> groupedObjectTags = new ConcurrentHashMap<>();
    private static HashMap<Object, Integer> warpMap = new HashMap<>();

    public static void registerScanEventhandler(IScanEventHandler scanEventHandler) {
        scanEventhandlers.add(scanEventHandler);
    }

    public static void registerEntityTag(String entityName, AspectList aspects, OldResearchApi.EntityTagsNBT... nbt) {
        scanEntities.add(new OldResearchApi.EntityTags(entityName, aspects, nbt));
    }

    public static boolean exists(Item item, int meta) {
        AspectList tmp = objectTags.get(Arrays.asList(item, meta));
        if(tmp == null) {
            tmp = objectTags.get(Arrays.asList(item, 32767));
            if(meta == 32767 && tmp == null) {
                int index = 0;

                while(true) {
                    tmp = objectTags.get(Arrays.asList(item, index));
                    ++index;
                    if(index >= 16 || tmp != null) {
                        break;
                    }
                }
            }

            if(tmp == null) {
                return false;
            }
        }

        return true;
    }

    public static void registerObjectTag(ItemStack item, AspectList aspects) {
        if(aspects == null) {
            aspects = new AspectList();
        }

        try {
            objectTags.put(Arrays.asList(item.getItem(), item.getItemDamage()), aspects);
        } catch (Exception ignored) {}

    }

    public static void registerObjectTag(ItemStack item, int[] meta, AspectList aspects) {
        if(aspects == null) {
            aspects = new AspectList();
        }

        try {
            objectTags.put(Arrays.asList(item.getItem(), meta[0]), aspects);

            for(int m : meta) {
                groupedObjectTags.put(Arrays.asList(item.getItem(), m), meta);
            }
        } catch (Exception ignored) {}

    }

    public static void registerObjectTag(String oreDict, AspectList aspects) {
        if(aspects == null) {
            aspects = new AspectList();
        }

        NonNullList<ItemStack> ores = OreDictionary.getOres(oreDict);
        if(ores != null && ores.size() > 0) {
            for(ItemStack ore : ores) {
                try {
                    objectTags.put(Arrays.asList(ore.getItem(), ore.getItemDamage()), aspects);
                } catch (Exception ignore) {}
            }
        }

    }

    public static void registerComplexObjectTag(ItemStack item, AspectList aspects) {
        if(!exists(item.getItem(), item.getItemDamage())) {
            AspectList tmp = ThaumcraftApi.internalMethods.generateTags(item);
            if(tmp != null && tmp.size() > 0) {
                for(Aspect tag : tmp.getAspects()) {
                    aspects.add(tag, tmp.getAmount(tag));
                }
            }

            registerObjectTag(item, aspects);
        } else {
            AspectList tmp = ThaumcraftApi.internalMethods.getObjectAspects(item);

            for(Aspect tag : aspects.getAspects()) {
                tmp.merge(tag, tmp.getAmount(tag));
            }

            registerObjectTag(item, tmp);
        }

    }

    public static void addWarpToItem(ItemStack craftresult, int amount) {
        warpMap.put(Arrays.asList(craftresult.getItem(), craftresult.getItemDamage()), amount);
    }

    public static void addWarpToResearch(String research, int amount) {
        warpMap.put(research, amount);
    }

    public static int getWarp(Object in) {
        return in == null? 0 : (in instanceof ItemStack && warpMap.containsKey(Arrays.asList(((ItemStack)in).getItem(), ((ItemStack) in).getItemDamage()))? warpMap.get(Arrays.asList(((ItemStack) in).getItem(), ((ItemStack) in).getItemDamage())) :(in instanceof String && warpMap.containsKey(in)? warpMap.get(in) :0));
    }

    public static class EntityTags {
        public String entityName;
        public OldResearchApi.EntityTagsNBT[] nbts;
        public AspectList aspects;

        public EntityTags(String entityName, AspectList aspects, OldResearchApi.EntityTagsNBT... nbts) {
            this.entityName = entityName;
            this.nbts = nbts;
            this.aspects = aspects;
        }
    }

    public static class EntityTagsNBT {
        public String name;
        public Object value;

        public EntityTagsNBT(String name, Object value) {
            this.name = name;
            this.value = value;
        }
    }

}
