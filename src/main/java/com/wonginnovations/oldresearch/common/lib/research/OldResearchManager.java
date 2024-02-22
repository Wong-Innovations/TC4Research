package com.wonginnovations.oldresearch.common.lib.research;

import com.cleanroommc.groovyscript.sandbox.ClosureHelper;
import com.google.common.io.Files;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
//import cpw.mods.fml.common.ObfuscationReflectionHelper;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.core.mixin.ResearchManagerAccessor;
import com.wonginnovations.oldresearch.utils.ResearchComplexityGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import org.apache.commons.lang3.ArrayUtils;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.api.research.ResearchCategories;
import thaumcraft.api.research.ResearchCategory;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.api.research.ResearchStage;
import thaumcraft.common.lib.utils.HexUtils;

public abstract class OldResearchManager {
    private static final String NOTES_TAG = "THAUMCRAFT.NOTE.COUNT";
    private static final String ASPECT_TAG = "THAUMCRAFT.ASPECTS";
    private static final String SCANNED_OBJ_TAG = "THAUMCRAFT.SCAN.OBJECTS";
    private static final String SCANNED_ENT_TAG = "THAUMCRAFT.SCAN.ENTITIES";

    private static final Map<String, ItemStack> NOTES = new HashMap<>();
    public static final Map<Aspect, Integer> ASPECT_COMPLEXITY = new HashMap<>();

    public static ResearchComplexityGenerator RESEARCH_COMPLEXITY_FUNCTION = new DefaultResearchComplexity();
    public static Map<String, AspectList> RESEARCH_ASPECTS = new HashMap<>(); // to be populated by external libs like GrS or CT

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

    public static int getAspectComplexity(Aspect a) {
        return ASPECT_COMPLEXITY.get(a);
    }

//    public static Aspect getRandomAspect(Random rand, int complexity) {
//        List<Aspect> possible = ASPECT_COMPLEXITY.keySet().stream().filter(aspect -> ASPECT_COMPLEXITY.get(aspect) <= complexity).toList();
//        return possible.get(rand.nextInt(possible.size()));
//    }

    public static AspectList getRandomAspects(Random rand, int maxComplexity, int quantity) {
        List<Aspect> possible = ASPECT_COMPLEXITY.keySet().stream().filter(aspect -> ASPECT_COMPLEXITY.get(aspect) <= maxComplexity).collect(Collectors.toList());
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
                            String key = "rn_" + entry.getKey() + "_" + (++i);
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

    public static int getResearchComplexity(EntityPlayer player, String key) {
        return RESEARCH_COMPLEXITY_FUNCTION.get(player, key);
    }

    public static void givePlayerResearchNote(World world, EntityPlayer player, String key) {
        if(!hasResearchNote(player, key)
                && consumeInkFromPlayer(player, false)
                && OldResearchUtils.consumeInventoryItem(player, Items.PAPER)
        ) {
            consumeInkFromPlayer(player, true);
            ItemStack note = NOTES.get(key).copy();
            int complexity = getResearchComplexity(player, key);
            ResearchNoteData data = getData(note);
            AspectList aspects = (RESEARCH_ASPECTS.containsKey(key))
                                    ? RESEARCH_ASPECTS.get(key)
                                    : getRandomAspects(world.rand, complexity, complexity + 2);
            data.generateHexes(world, player, aspects, complexity);
            updateData(note, data);
            if(!player.inventory.addItemStackToInventory(note)) {
                ForgeHooks.onPlayerTossEvent(player, note, false);
            }

            player.inventoryContainer.detectAndSendChanges();
        }
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

    public static boolean hasResearchNote(EntityPlayer player, String key) {
        ItemStack[] inv = player.inventory.mainInventory.toArray(new ItemStack[0]);
        for (ItemStack itemStack : inv) {
            if (itemStack != null && itemStack.getItem() == ModItems.RESEARCHNOTE && getData(itemStack) != null && getData(itemStack).key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static String getStrippedKey(ItemStack stack) {
        return getStrippedKey(getData(stack).key);
    }

    public static String getStrippedKey(String key) {
        return key.substring(key.indexOf('_') + 1, key.lastIndexOf('_'));
    }

    public static boolean checkResearchCompletion(ItemStack contents, ResearchNoteData note, String username) {
        ArrayList<String> checked = new ArrayList<>();
        ArrayList<String> main = new ArrayList<>();
        ArrayList<String> remains = new ArrayList<>();

        for(HexUtils.Hex hex : note.hexes.values()) {
            if(note.hexEntries.get(hex.toString()).type == 1) {
                main.add(hex.toString());
            }
        }

        for(HexUtils.Hex hex : note.hexes.values()) {
            if(note.hexEntries.get(hex.toString()).type == 1) {
                main.remove(hex.toString());
                checkConnections(note, hex, checked, main, remains, username);
                break;
            }
        }

        if(main.size() != 0) {
            return false;
        } else {
            ArrayList<String> remove = new ArrayList<>();

            for(HexUtils.Hex hex : note.hexes.values()) {
                if(note.hexEntries.get(hex.toString()).type != 1 && !remains.contains(hex.toString())) {
                    remove.add(hex.toString());
                }
            }

            for(String s : remove) {
                note.hexEntries.remove(s);
                note.hexes.remove(s);
            }

            note.complete = true;
            updateData(contents, note);
            return true;
        }
    }

    private static void checkConnections(ResearchNoteData note, HexUtils.Hex hex, ArrayList<String> checked, ArrayList<String> main, ArrayList<String> remains, String username) {
        checked.add(hex.toString());

        for(int a = 0; a < 6; ++a) {
            HexUtils.Hex target = hex.getNeighbour(a);
            if(!checked.contains(target.toString()) && note.hexEntries.containsKey(target.toString()) && note.hexEntries.get(target.toString()).type >= 1) {
                Aspect aspect1 = note.hexEntries.get(hex.toString()).aspect;
                Aspect aspect2 = note.hexEntries.get(target.toString()).aspect;
                if(OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(username, aspect1) && OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(username, aspect2) && (!aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2) || !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1))) {
                    remains.add(target.toString());
                    if(note.hexEntries.get(target.toString()).type == 1) {
                        main.remove(target.toString());
                    }

                    checkConnections(note, target, checked, main, remains, username);
                }
            }
        }

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

    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        for(Aspect aspect : Aspect.aspects.values()) {
            if(aspect.getComponents() != null && (aspect.getComponents()[0] == aspect1 && aspect.getComponents()[1] == aspect2 || aspect.getComponents()[0] == aspect2 && aspect.getComponents()[1] == aspect1)) {
                return aspect;
            }
        }

        return null;
    }

    public static boolean completeAspectUnsaved(String username, Aspect aspect, short amount) {
        if(aspect == null) {
            return false;
        } else {
            OldResearch.proxy.getPlayerKnowledge().addDiscoveredAspect(username, aspect);
            OldResearch.proxy.getPlayerKnowledge().setAspectPool(username, aspect, amount);
            return true;
        }
    }

    public static void completeAspect(EntityPlayer player, Aspect aspect, short amount) {
        completeAspectUnsaved(player.getGameProfile().getName(), aspect, amount);
    }

    public static boolean completeScannedObjectUnsaved(String username, String object) {
        ArrayList<String> completed = OldResearch.proxy.getScannedObjects().get(username);
        if(completed == null) {
            completed = new ArrayList<>();
        }

        if(!completed.contains(object)) {
            completed.add(object);
            String t = object.replaceFirst("#", "@");
            if(object.startsWith("#") && completed.contains(t) && completed.remove(t)) {
                ;
            }

            OldResearch.proxy.getScannedObjects().put(username, completed);
        }

        return true;
    }

    public static boolean completeScannedEntityUnsaved(String username, String key) {
        ArrayList<String> completed = OldResearch.proxy.getScannedEntities().get(username);
        if(completed == null) {
            completed = new ArrayList<>();
        }

        if(!completed.contains(key)) {
            completed.add(key);
            String t = key.replaceFirst("#", "@");
            if(key.startsWith("#") && completed.contains(t) && completed.remove(t)) {
                ;
            }

            OldResearch.proxy.getScannedEntities().put(username, completed);
        }

        return true;
    }

    public static void completeScannedObject(EntityPlayer player, String object) {
        completeScannedObjectUnsaved(player.getGameProfile().getName(), object);
    }

    public static void completeScannedEntity(EntityPlayer player, String key) {
        completeScannedEntityUnsaved(player.getGameProfile().getName(), key);
    }

    public static void loadPlayerData(EntityPlayer player, File file1, File file2, boolean legacy) {
        try {
            NBTTagCompound data = null;
            if(file1 != null && file1.exists()) {
                try {
                    FileInputStream fileinputstream = new FileInputStream(file1);
                    data = CompressedStreamTools.readCompressed(fileinputstream);
                    fileinputstream.close();
                } catch (Exception var9) {
                    var9.printStackTrace();
                }
            }

            if(file1 == null || !file1.exists() || data == null || data.isEmpty()) {
                Thaumcraft.log.warn("Thaumcraft data not found for " + player.getGameProfile().getName() + ". Trying to load backup Thaumcraft data.");
                if(file2 != null && file2.exists()) {
                    try {
                        FileInputStream fileinputstream = new FileInputStream(file2);
                        data = CompressedStreamTools.readCompressed(fileinputstream);
                        fileinputstream.close();
                    } catch (Exception var8) {
                        var8.printStackTrace();
                    }
                }
            }

            if(data != null) {
                loadResearchCountNBT(data, player);
                loadAspectNBT(data, player);
                loadScannedNBT(data, player);
//                if(data.hasKey("Thaumcraft.shielding")) {
//                    Thaumcraft.instance.runicEventHandler.runicCharge.put(player.getEntityId(), data.getInteger("Thaumcraft.shielding"));
//                    Thaumcraft.instance.runicEventHandler.isDirty = true;
//                }

                if(data.hasKey("Thaumcraft.eldritch")) {
                    int warp = data.getInteger("Thaumcraft.eldritch");
                    if(legacy && !data.hasKey("Thaumcraft.eldritch.sticky")) {
                        warp /= 2;
                        OldResearch.proxy.getPlayerKnowledge().setWarpSticky(player.getGameProfile().getName(), warp);
                    }

                    OldResearch.proxy.getPlayerKnowledge().setWarpPerm(player.getGameProfile().getName(), warp);
                }

                if(data.hasKey("Thaumcraft.eldritch.temp")) {
                    OldResearch.proxy.getPlayerKnowledge().setWarpTemp(player.getGameProfile().getName(), data.getInteger("Thaumcraft.eldritch.temp"));
                }

                if(data.hasKey("Thaumcraft.eldritch.sticky")) {
                    OldResearch.proxy.getPlayerKnowledge().setWarpSticky(player.getGameProfile().getName(), data.getInteger("Thaumcraft.eldritch.sticky"));
                }

                if(data.hasKey("Thaumcraft.eldritch.counter")) {
                    OldResearch.proxy.getPlayerKnowledge().setWarpCounter(player.getGameProfile().getName(), data.getInteger("Thaumcraft.eldritch.counter"));
                } else {
                    OldResearch.proxy.getPlayerKnowledge().setWarpCounter(player.getGameProfile().getName(), 0);
                }
            } else {
                for(Aspect aspect : Aspect.aspects.values()) {
                    if(aspect.getComponents() == null) {
                        completeAspectUnsaved(player.getGameProfile().getName(), aspect, (short)(15 + player.world.rand.nextInt(5)));
                    }
                }

                Thaumcraft.log.info("Assigning initial aspects to " + player.getGameProfile().getName());
            }
        } catch (Exception var10) {
            var10.printStackTrace();
            Thaumcraft.log.fatal("Error loading Thaumcraft data");
        }

    }

    public static void loadResearchCountNBT(NBTTagCompound entityData, EntityPlayer player) {
        if(entityData.hasKey(NOTES_TAG)) {
            OldResearch.proxy.getPlayerKnowledge().setResearchCompleted(player.getGameProfile().getName(), entityData.getInteger(NOTES_TAG));
        }
    }

    public static void loadAspectNBT(NBTTagCompound entityData, EntityPlayer player) {
        if(entityData.hasKey(ASPECT_TAG)) {
            NBTTagList tagList = entityData.getTagList(ASPECT_TAG, 10);

            for(int j = 0; j < tagList.tagCount(); ++j) {
                NBTTagCompound rs = tagList.getCompoundTagAt(j);
                if(rs.hasKey("key")) {
                    Aspect aspect = Aspect.getAspect(rs.getString("key"));
                    short amount = rs.getShort("amount");
                    if(aspect != null) {
                        completeAspectUnsaved(player.getGameProfile().getName(), aspect, amount);
                    }
                }
            }
        }

    }

    public static void loadScannedNBT(NBTTagCompound entityData, EntityPlayer player) {
        NBTTagList tagList = entityData.getTagList(SCANNED_OBJ_TAG, 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeScannedObjectUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

        tagList = entityData.getTagList(SCANNED_ENT_TAG, 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeScannedEntityUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

    }

    public static boolean savePlayerData(EntityPlayer player, File file1, File file2) {
        boolean success = true;

        try {
            NBTTagCompound data = new NBTTagCompound();
            saveResearchCountNBT(data, player);
            saveAspectNBT(data, player);
            saveScannedNBT(data, player);
//            if(Thaumcraft.instance.runicEventHandler.runicCharge.containsKey(player.getEntityId())) {
//                data.setTag("Thaumcraft.shielding", new NBTTagInt((Integer) Thaumcraft.instance.runicEventHandler.runicCharge.get(player.getEntityId())));
//            }

            data.setTag("Thaumcraft.eldritch", new NBTTagInt(OldResearch.proxy.getPlayerKnowledge().getWarpPerm(player.getGameProfile().getName())));
            data.setTag("Thaumcraft.eldritch.temp", new NBTTagInt(OldResearch.proxy.getPlayerKnowledge().getWarpTemp(player.getGameProfile().getName())));
            data.setTag("Thaumcraft.eldritch.sticky", new NBTTagInt(OldResearch.proxy.getPlayerKnowledge().getWarpSticky(player.getGameProfile().getName())));
            data.setTag("Thaumcraft.eldritch.counter", new NBTTagInt(OldResearch.proxy.getPlayerKnowledge().getWarpCounter(player.getGameProfile().getName())));
            if(file1 != null && file1.exists()) {
                try {
                    Files.copy(file1, file2);
                } catch (Exception var8) {
                    Thaumcraft.log.error("Could not backup old research file for player " + player.getGameProfile().getName());
                }
            }

            try {
                if(file1 != null) {
                    FileOutputStream fileoutputstream = new FileOutputStream(file1);
                    CompressedStreamTools.writeCompressed(data, fileoutputstream);
                    fileoutputstream.close();
                }
            } catch (Exception var9) {
                Thaumcraft.log.error("Could not save research file for player " + player.getGameProfile().getName());
                if(file1.exists()) {
                    try {
                        file1.delete();
                    } catch (Exception ignored) {}
                }

                success = false;
            }
        } catch (Exception var10) {
            var10.printStackTrace();
            Thaumcraft.log.fatal("Error saving Thaumcraft data");
            success = false;
        }

        return success;
    }

    public static void saveResearchCountNBT(NBTTagCompound entityData, EntityPlayer player) {
        entityData.setInteger(NOTES_TAG, OldResearch.proxy.getPlayerKnowledge().getResearchCompleted(player.getGameProfile().getName()));
    }

    public static void saveAspectNBT(NBTTagCompound entityData, EntityPlayer player) {
        NBTTagList tagList = new NBTTagList();
        AspectList res = OldResearch.proxy.getKnownAspects().get(player.getGameProfile().getName());
        if(res != null && res.size() > 0) {
            for(Aspect aspect : res.getAspects()) {
                if(aspect != null) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", aspect.getTag());
                    f.setShort("amount", (short)res.getAmount(aspect));
                    tagList.appendTag(f);
                }
            }
        }

        entityData.setTag(ASPECT_TAG, tagList);
    }

    public static void saveScannedNBT(NBTTagCompound entityData, EntityPlayer player) {
        NBTTagList tagList = new NBTTagList();
        List<String> obj = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
        if(obj != null && obj.size() > 0) {
            for(String object : obj) {
                if(object != null) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", object);
                    tagList.appendTag(f);
                }
            }
        }

        entityData.setTag(SCANNED_OBJ_TAG, tagList);
        tagList = new NBTTagList();
        List<String> ent = OldResearch.proxy.getScannedEntities().get(player.getGameProfile().getName());
        if(ent != null && ent.size() > 0) {
            for(String key : ent) {
                if(key != null) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", key);
                    tagList.appendTag(f);
                }
            }
        }

        entityData.setTag(SCANNED_ENT_TAG, tagList);
    }

    public static void parseJsonResearch(ResourceLocation loc) {
        JsonParser parser = new JsonParser();
        String s = "/assets/" + loc.getNamespace() + "/" + loc.getPath();
        InputStream stream = OldResearchManager.class.getResourceAsStream(s);
        if (stream != null) {
            try {
                InputStreamReader reader = new InputStreamReader(stream);
                JsonObject obj = parser.parse(reader).getAsJsonObject();
                JsonArray entries = obj.get("entries").getAsJsonArray();
                int a = 0;

                for (JsonElement element : entries) {
                    ++a;

                    try {
                        JsonObject entry = element.getAsJsonObject();
                        ResearchEntry researchEntry = ResearchManagerAccessor.parseResearchJson(entry);
                        if (researchEntry != null && ResearchCategories.getResearchCategory(researchEntry.getCategory()) != null) {
                            ResearchManagerAccessor.addResearchToCategory(researchEntry);
                        }
                    } catch (Exception var13) {
                        var13.printStackTrace();
                        Thaumcraft.log.warn("Invalid research entry [" + a + "] found in " + loc);
                        --a;
                    }
                }

                Thaumcraft.log.info("Loaded " + a + " research entries from " + loc);
            } catch (Exception var14) {
                Thaumcraft.log.warn("Invalid research file: " + loc);
            }
        } else {
            Thaumcraft.log.warn("Research file not found: " + loc);
        }
    }

    public static class HexEntry {
        public Aspect aspect;
        public int type;

        public HexEntry(Aspect aspect, int type) {
            this.aspect = aspect;
            this.type = type;
        }
    }
}