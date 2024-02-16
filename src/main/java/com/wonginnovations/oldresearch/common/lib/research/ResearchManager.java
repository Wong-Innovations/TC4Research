package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.utils.HexUtils;

import java.util.*;
import java.util.stream.Collectors;

public abstract class ResearchManager {

    private static final int COMPLEXITY = 1;

    private static final Map<String, ItemStack> NOTES = new HashMap<>();
    private static final Map<Aspect, Integer> ASPECT_COMPLEXITY = new HashMap<>();

    private static final Random RANDOM = new Random(69420);

    public static void computeAspectComplexity() {
        for (Aspect aspect : Aspect.aspects.values()) {
            ASPECT_COMPLEXITY.put(aspect, computeAspectComplexity(aspect, 0));
        }
    }

    private static int computeAspectComplexity(Aspect aspect, int depth) {
        if (aspect.isPrimal()) return depth;
        ArrayList<Integer> childDepths = new ArrayList<>();
        for (Aspect asp : aspect.getComponents()) {
            childDepths.add(computeAspectComplexity(asp, depth + 1));
        }
        return Collections.max(childDepths);
    }

//    public static Aspect getRandomAspect(Random rand, int complexity) {
//        List<Aspect> possible = ASPECT_COMPLEXITY.keySet().stream().filter(aspect -> ASPECT_COMPLEXITY.get(aspect) <= complexity).toList();
//        return possible.get(rand.nextInt(possible.size()));
//    }

    public static AspectList getRandomAspects(Random rand, int complexity, int quantity) {
        List<Aspect> possible = ASPECT_COMPLEXITY.keySet().stream().filter(aspect -> ASPECT_COMPLEXITY.get(aspect) <= complexity).collect(Collectors.toList());
        AspectList selected = new AspectList();
        int upto = Math.min(quantity, possible.size());
        for (int i = 0; i < upto; i++) {
            int toadd = rand.nextInt(possible.size());
            selected.add(possible.get(toadd), 1);
            possible.remove(toadd);
        }

        return selected;
    }

    public static void patchResearch() {
        for (ResearchCategory category : ResearchCategories.researchCategories.values()) {
            for (ResearchEntry entry : category.research.values()) {
                int i = 0;
                for (ResearchStage stage : entry.getStages()) {
                    if (stage == null || stage.getKnow() == null || stage.getKnow().length == 0) continue;
                    for (ResearchStage.Knowledge knowledge : stage.getKnow()) {
                        if (knowledge.type == IPlayerKnowledge.EnumKnowledgeType.THEORY) {
                            String key = "rn_" + entry.getKey() + (++i);
                            stage.setResearch(ArrayUtils.add(stage.getResearch(), key));
                            NOTES.put(key, createNote(key));
                            if (stage.getResearchIcon() == null) stage.setResearchIcon(new String[]{null});
                            else stage.setResearchIcon(ArrayUtils.add(stage.getResearchIcon(), null));
                        }
                    }
                    stage.setKnow(null);
                }
            }
        }
    }

    private static ItemStack createNote(String key) {
        ItemStack note = new ItemStack(ModItems.RESEARCHNOTE);
        ResearchNoteData data = new ResearchNoteData();
        data.key = key;
        Aspect[] asps = Aspect.aspects.values().toArray(new Aspect[0]);
        data.color =  asps[RANDOM.nextInt(asps.length)].getColor();
        updateData(note, data);
        return note;
    }

    public static ItemStack getNote(String key) {
        return NOTES.get(key);
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

    public static void givePlayerResearchNote(World world, EntityPlayer player, String key) {
        if(!hasResearchNote(player, key)
            && consumeInkFromPlayer(player, false)
            && OldResearchUtils.consumeInventoryItem(player, Items.PAPER)
        ) {
            consumeInkFromPlayer(player, true);
            ItemStack note = NOTES.get(key).copy();
            int numberOfAspects = (COMPLEXITY - 1) * world.rand.nextInt() * 3 + 3;
            ResearchNoteData data = getData(note);
            data.generateHexes(world, getRandomAspects(world.rand, COMPLEXITY, numberOfAspects), COMPLEXITY);
            updateData(note, data);
            if(!player.inventory.addItemStackToInventory(note)) {
                ForgeHooks.onPlayerTossEvent(player, note, false);
            }

            player.inventoryContainer.detectAndSendChanges();
        }
    }

    public static boolean hasResearchNote(EntityPlayer player, String key) {
        ItemStack[] inv = player.inventory.mainInventory.toArray(new ItemStack[0]);
        for (ItemStack itemStack : inv) {
            if (itemStack != null && itemStack.getItem() == ModItems.RESEARCHNOTE && getData(itemStack) != null && getData(itemStack).key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static boolean consumeInkFromPlayer(EntityPlayer player, boolean doit) {
        ItemStack[] inv = player.inventory.mainInventory.toArray(new ItemStack[0]);

        for (ItemStack itemStack : inv) {
            if (itemStack != null && itemStack.getItem() instanceof IScribeTools && itemStack.getItemDamage() < itemStack.getMaxDamage()) {
                if (doit) {
                    itemStack.damageItem(1, player);
                }

                return true;
            }
        }

        return false;
    }

}
