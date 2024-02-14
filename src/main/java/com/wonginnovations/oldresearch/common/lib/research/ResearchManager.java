package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.common.items.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.utils.HexUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ResearchManager {

    private final Map<String, ItemStack> notes = new HashMap<>();

    private final Random random = new Random(69420);

    public void patchResearch() {
        for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry entry : category.research.values()) {
                int i = 0;
                for (ResearchStage stage : entry.getStages()) {
                    if (stage == null || stage.getKnow() == null || stage.getKnow().length == 0) continue;
                    for (ResearchStage.Knowledge knowledge : stage.getKnow()) {
                        if (knowledge.type == IPlayerKnowledge.EnumKnowledgeType.THEORY) {
                            String key = "rn_" + entry.getKey() + (++i);
                            stage.setResearch(ArrayUtils.add(stage.getResearch(), key));
                            notes.put(key, createNote(key));
                            if (stage.getResearchIcon() == null) stage.setResearchIcon(new String[]{null});
                            else stage.setResearchIcon(ArrayUtils.add(stage.getResearchIcon(), null));
                        }
                    }
                    stage.setKnow(null);
                }
            }
        }
    }

    private ItemStack createNote(String key) {
        ItemStack note = new ItemStack(ModItems.RESEARCHNOTE);
        ResearchNoteData data = new ResearchNoteData();
        data.key = key;
        Aspect[] asps = Aspect.aspects.values().toArray(new Aspect[0]);
        data.color =  asps[random.nextInt(asps.length)].getColor();
        updateData(note, data);
        return note;
    }

    public ItemStack getNote(String key) {
        return this.notes.get(key);
    }

    public static ResearchNoteData getData(ItemStack stack) {
        if(stack == null) {
            return null;
        } else {
            ResearchNoteData data = new ResearchNoteData();
            if(stack.getTagCompound() == null) {
                return null;
            } else {
                data.key = stack.getTagCompound().getString("key");
                data.color = stack.getTagCompound().getInteger("color");
                data.complete = stack.getTagCompound().getBoolean("complete");
                data.copies = stack.getTagCompound().getInteger("copies");
                NBTTagList grid = stack.getTagCompound().getTagList("hexgrid", 10);
                data.hexEntries = new HashMap<>();

                for(int x = 0; x < grid.tagCount(); ++x) {
                    NBTTagCompound nbt = grid.getCompoundTagAt(x);
                    int q = nbt.getByte("hexq");
                    int r = nbt.getByte("hexr");
                    int type = nbt.getByte("type");
                    String tag = nbt.getString("aspect");
                    Aspect aspect = Aspect.getAspect(tag);
                    HexUtils.Hex hex = new HexUtils.Hex(q, r);
                    data.hexEntries.put(hex.toString(), new OldResearchManager.HexEntry(aspect, type));
                    data.hexes.put(hex.toString(), hex);
                }

                return data;
            }
        }
    }

    public static void updateData(ItemStack stack, ResearchNoteData data) {
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setString("key", data.key);
        stack.getTagCompound().setInteger("color", data.color);
        stack.getTagCompound().setBoolean("complete", data.complete);
        stack.getTagCompound().setInteger("copies", data.copies);
        NBTTagList gridtag = new NBTTagList();

        for(HexUtils.Hex hex : data.hexes.values()) {
            NBTTagCompound gt = new NBTTagCompound();
            gt.setByte("hexq", (byte)hex.q);
            gt.setByte("hexr", (byte)hex.r);
            gt.setByte("type", (byte) data.hexEntries.get(hex.toString()).type);
            if(data.hexEntries.get(hex.toString()).aspect != null) {
                gt.setString("aspect", data.hexEntries.get(hex.toString()).aspect.getTag());
            }

            gridtag.appendTag(gt);
        }

        stack.getTagCompound().setTag("hexgrid", gridtag);
    }

}
