package com.wonginnovations.oldresearch.common.lib.research;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mojang.authlib.GameProfile;
//import cpw.mods.fml.common.ObfuscationReflectionHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchCategoryList;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.items.ModItems;
import com.wonginnovations.oldresearch.common.lib.network.PacketResearchComplete;
import com.wonginnovations.oldresearch.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerInteractionManager;
import net.minecraft.world.World;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.SaveHandler;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import thaumcraft.Thaumcraft;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.ThaumcraftInvHelper;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.lib.utils.InventoryUtils;

public class ResearchManager {
    static ArrayList<ResearchItem> allHiddenResearch = null;
    static ArrayList<ResearchItem> allValidResearch = null;
    private static final String RESEARCH_TAG = "THAUMCRAFT.RESEARCH";
    private static final String ASPECT_TAG = "THAUMCRAFT.ASPECTS";
    private static final String SCANNED_OBJ_TAG = "THAUMCRAFT.SCAN.OBJECTS";
    private static final String SCANNED_ENT_TAG = "THAUMCRAFT.SCAN.ENTITIES";
    private static final String SCANNED_PHE_TAG = "THAUMCRAFT.SCAN.PHENOMENA";

    public static boolean createClue(World world, EntityPlayer player, Object clue, AspectList aspects) {
        ArrayList<String> keys = new ArrayList<>();

        for(ResearchCategoryList rcl : ResearchCategories.researchCategories.values()) {
            label171:
            for(ResearchItem ri : rcl.research.values()) {
                boolean valid = ri.tags != null && ri.tags.size() > 0 && (ri.isLost() || ri.isHidden()) && !isResearchComplete(player.getGameProfile().getName(), ri.key) && !isResearchComplete(player.getGameProfile().getName(), "@" + ri.key);
                if(valid) {
                    if(clue instanceof ItemStack && ri.getItemTriggers() != null && ri.getItemTriggers().length > 0) {
                        for(ItemStack stack : ri.getItemTriggers()) {
                            if(InventoryUtils.areItemStacksEqual(stack, (ItemStack)clue, new ThaumcraftInvHelper.InvFilter(true, false, true, false))) {
                                keys.add(ri.key);
                                continue label171;
                            }
                        }
                    } else if(clue instanceof String && ri.getEntityTriggers() != null && ri.getEntityTriggers().length > 0) {
                        for(String entity : ri.getEntityTriggers()) {
                            if(clue.equals(entity)) {
                                keys.add(ri.key);
                                continue label171;
                            }
                        }
                    }

                    if(aspects != null && aspects.size() > 0 && ri.getAspectTriggers() != null && ri.getAspectTriggers().length > 0) {
                        for(Aspect aspect : ri.getAspectTriggers()) {
                            if(aspects.getAmount(aspect) > 0) {
                                keys.add(ri.key);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if(keys.size() > 0) {
            String key = keys.get(world.rand.nextInt(keys.size()));
            PacketHandler.INSTANCE.sendTo(new PacketResearchComplete("@" + key), (EntityPlayerMP)player);
            OldResearch.proxy.getResearchManager().completeResearch(player, "@" + key);
            return true;
        } else {
            return false;
        }
    }

    public static ItemStack createResearchNoteForPlayer(World world, EntityPlayer player, String key) {
        ItemStack note = null;
        boolean addslot = false;
        int slot = getResearchSlot(player, key);
        if(slot >= 0) {
            note = player.inventory.getStackInSlot(slot);
        } else if(consumeInkFromPlayer(player, false) && OldResearchUtils.consumeInventoryItem(player, Items.PAPER)) {
            consumeInkFromPlayer(player, true);
            note = createNote(new ItemStack(ModItems.RESEARCHNOTE), key, world);
            if(!player.inventory.addItemStackToInventory(note)) {
                ForgeHooks.onPlayerTossEvent(player, note, false);
            }

            player.inventoryContainer.detectAndSendChanges();
        }

        return note;
    }

    public static String findHiddenResearch(EntityPlayer player) {
        if(allHiddenResearch == null) {
            allHiddenResearch = new ArrayList<>();

            for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
                for(ResearchItem ri : cat.research.values()) {
                    if(ri.isHidden() && ri.tags != null && ri.tags.size() > 0) {
                        allHiddenResearch.add(ri);
                    }
                }
            }
        }

        ArrayList<String> keys = new ArrayList<>();

        for(ResearchItem research : allHiddenResearch) {
            if(!isResearchComplete(player.getGameProfile().getName(), research.key) && doesPlayerHaveRequisites(player.getGameProfile().getName(), research.key) && (research.getItemTriggers() != null || research.getEntityTriggers() != null || research.getAspectTriggers() != null)) {
                keys.add(research.key);
            }
        }

        Random rand = new Random(player.world.getWorldTime() / 10L / 5L);
        if(keys.size() > 0) {
            int r = rand.nextInt(keys.size());
            return keys.get(r);
        } else {
            return "FAIL";
        }
    }

    public static String findMatchingResearch(EntityPlayer player, Aspect aspect) {
        String randomMatch = null;
        if(allValidResearch == null) {
            allValidResearch = new ArrayList<>();

            for(ResearchCategoryList cat : ResearchCategories.researchCategories.values()) {
                for(ResearchItem ri : cat.research.values()) {
                    boolean secondary = ri.isSecondary() && ModConfig.researchDifficulty == 0 || ModConfig.researchDifficulty == -1;
                    if(!secondary && !ri.isHidden() && !ri.isLost() && !ri.isAutoUnlock() && !ri.isVirtual() && !ri.isStub()) {
                        allValidResearch.add(ri);
                    }
                }
            }
        }

        ArrayList<String> keys = new ArrayList<>();

        for(ResearchItem research : allValidResearch) {
            if(!isResearchComplete(player.getGameProfile().getName(), research.key) && doesPlayerHaveRequisites(player.getGameProfile().getName(), research.key) && research.tags.getAmount(aspect) > 0) {
                keys.add(research.key);
            }
        }

        if(keys.size() > 0) {
            randomMatch = keys.get(player.world.rand.nextInt(keys.size()));
        }

        return randomMatch;
    }

    public static int getResearchSlot(EntityPlayer player, String key) {
        ItemStack[] inv = player.inventory.mainInventory.toArray(new ItemStack[0]);
        if(inv.length != 0) {
            for(int a = 0; a < inv.length; ++a) {
                if(inv[a] != null && inv[a].getItem() == ModItems.RESEARCHNOTE && getData(inv[a]) != null && getData(inv[a]).key.equals(key)) {
                    return a;
                }
            }

            return -1;
        } else {
            return -1;
        }
    }

    public static boolean consumeInkFromPlayer(EntityPlayer player, boolean doit) {
        ItemStack[] inv = player.inventory.mainInventory.toArray(new ItemStack[0]);

        for(int a = 0; a < inv.length; ++a) {
            if(inv[a] != null && inv[a].getItem() instanceof IScribeTools && inv[a].getItemDamage() < inv[a].getMaxDamage()) {
                if(doit) {
                    inv[a].damageItem(1, player);
                }

                return true;
            }
        }

        return false;
    }

    public static boolean consumeInkFromTable(ItemStack stack, boolean doit) {
        if(stack != null && stack.getItem() instanceof IScribeTools && stack.getItemDamage() < stack.getMaxDamage()) {
            if(doit) {
                stack.setItemDamage(stack.getItemDamage() + 1);
            }

            return true;
        } else {
            return false;
        }
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

    public static ItemStack createNote(ItemStack stack, String key, World world) {
        ResearchItem rr = ResearchCategories.getResearch(key);
        Aspect primaryaspect = rr.getResearchPrimaryTag();
        if(primaryaspect == null) {
            return null;
        } else {
            if(stack.getTagCompound() == null) {
                stack.setTagCompound(new NBTTagCompound());
            }

            stack.getTagCompound().setString("key", key);
            stack.getTagCompound().setInteger("color", primaryaspect.getColor());
            stack.getTagCompound().setBoolean("complete", false);
            stack.getTagCompound().setInteger("copies", 0);
            int radius = 1 + Math.min(3, rr.getComplexity());
            HashMap<String, HexUtils.Hex> hexLocs = HexUtils.generateHexes(radius);
            ArrayList<HexUtils.Hex> outerRing = HexUtils.distributeRingRandomly(radius, rr.tags.size(), world.rand);
            HashMap<String, ResearchManager.HexEntry> hexEntries = new HashMap<>();
            HashMap<String, HexUtils.Hex> hexes = new HashMap<>();

            for(HexUtils.Hex hex : hexLocs.values()) {
                hexes.put(hex.toString(), hex);
                hexEntries.put(hex.toString(), new ResearchManager.HexEntry(null, 0));
            }

            int count = 0;

            for(HexUtils.Hex hex : outerRing) {
                hexes.put(hex.toString(), hex);
                hexEntries.put(hex.toString(), new ResearchManager.HexEntry(rr.tags.getAspects()[count], 1));
                ++count;
            }

            if(rr.getComplexity() > 1) {
                int blanks = rr.getComplexity() * 2;
                HexUtils.Hex[] temp = hexes.values().toArray(new HexUtils.Hex[0]);

                while(blanks > 0) {
                    int indx = world.rand.nextInt(temp.length);
                    if(hexEntries.get(temp[indx].toString()) != null && hexEntries.get(temp[indx].toString()).type == 0) {
                        boolean gtg = true;

                        for(int n = 0; n < 6; ++n) {
                            HexUtils.Hex neighbour = temp[indx].getNeighbour(n);
                            if(hexes.containsKey(neighbour.toString()) && hexEntries.get(neighbour.toString()).type == 1) {
                                int cc = 0;

                                for(int q = 0; q < 6; ++q) {
                                    if(hexes.containsKey(hexes.get(neighbour.toString()).getNeighbour(q).toString())) {
                                        ++cc;
                                    }

                                    if(cc >= 2) {
                                        break;
                                    }
                                }

                                if(cc < 2) {
                                    gtg = false;
                                    break;
                                }
                            }
                        }

                        if(gtg) {
                            hexes.remove(temp[indx].toString());
                            hexEntries.remove(temp[indx].toString());
                            temp = hexes.values().toArray(new HexUtils.Hex[0]);
                            --blanks;
                        }
                    }
                }
            }

            NBTTagList gridtag = new NBTTagList();

            for(HexUtils.Hex hex : hexes.values()) {
                NBTTagCompound gt = new NBTTagCompound();
                gt.setByte("hexq", (byte)hex.q);
                gt.setByte("hexr", (byte)hex.r);
                gt.setByte("type", (byte) hexEntries.get(hex.toString()).type);
                if(hexEntries.get(hex.toString()).aspect != null) {
                    gt.setString("aspect", hexEntries.get(hex.toString()).aspect.getTag());
                }

                gridtag.appendTag(gt);
            }

            stack.getTagCompound().setTag("hexgrid", gridtag);
            return stack;
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
                    data.hexEntries.put(hex.toString(), new ResearchManager.HexEntry(aspect, type));
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

    public static boolean isResearchComplete(String playername, String key) {
        if(!key.startsWith("@") && ResearchCategories.getResearch(key) == null) {
            return false;
        } else {
            List completed = getResearchForPlayer(playername);
            return completed != null && completed.size() > 0 && completed.contains(key);
        }
    }

    public static ArrayList<String> getResearchForPlayer(String playername) {
        ArrayList<String> out = OldResearch.proxy.getCompletedResearch().get(playername);

        try {
            if(out == null && OldResearch.proxy.getClientWorld() == null && OldResearch.proxy.getClientWorld().getMinecraftServer() != null) {
                OldResearch.proxy.getCompletedResearch().put(playername, new ArrayList<>());
                UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playername).getBytes(Charsets.UTF_8));
                EntityPlayerMP entityplayermp = new EntityPlayerMP(OldResearch.proxy.getClientWorld().getMinecraftServer(), OldResearch.proxy.getClientWorld().getMinecraftServer().getWorld(0), new GameProfile(id, playername), new PlayerInteractionManager(OldResearch.proxy.getClientWorld().getMinecraftServer().getWorld(0)));
                IPlayerFileData playerNBTManagerObj = OldResearch.proxy.getClientWorld().getMinecraftServer().getWorld(0).getSaveHandler().getPlayerNBTManager();
                SaveHandler sh = (SaveHandler)playerNBTManagerObj;
                File dir = ObfuscationReflectionHelper.getPrivateValue(SaveHandler.class, sh, new String[]{"playersDirectory", "field_75771_c"});
                File file1 = new File(dir, id + ".thaum");
                File file2 = new File(dir, id + ".thaumbak");
                loadPlayerData(entityplayermp, file1, file2, false);

                out = OldResearch.proxy.getCompletedResearch().get(playername);
            }
        } catch (Exception ignored) {}

        return out;
    }

    public static ArrayList<String> getResearchForPlayerSafe(String playername) {
        return OldResearch.proxy.getCompletedResearch().get(playername);
    }

    public static boolean doesPlayerHaveRequisites(String playername, String key) {
        boolean out = true;
        String[] parents = ResearchCategories.getResearch(key).parents;
        if(parents != null && parents.length > 0) {
            out = false;
            List<String> completed = getResearchForPlayer(playername);
            if(completed != null && completed.size() > 0) {
                out = true;

                for(String item : parents) {
                    if(!completed.contains(item)) {
                        return false;
                    }
                }
            }
        }

        parents = ResearchCategories.getResearch(key).parentsHidden;
        if(parents != null && parents.length > 0) {
            out = false;
            List<String> completed = getResearchForPlayer(playername);
            if(completed != null && completed.size() > 0) {
                out = true;

                for(String item : parents) {
                    if(!completed.contains(item)) {
                        return false;
                    }
                }
            }
        }

        return out;
    }

    public static Aspect getCombinationResult(Aspect aspect1, Aspect aspect2) {
        for(Aspect aspect : Aspect.aspects.values()) {
            if(aspect.getComponents() != null && (aspect.getComponents()[0] == aspect1 && aspect.getComponents()[1] == aspect2 || aspect.getComponents()[0] == aspect2 && aspect.getComponents()[1] == aspect1)) {
                return aspect;
            }
        }

        return null;
    }

    public static AspectList reduceToPrimals(AspectList al) {
        return reduceToPrimals(al, false);
    }

    public static AspectList reduceToPrimals(AspectList al, boolean merge) {
        AspectList out = new AspectList();

        for(Aspect aspect : al.getAspects()) {
            if(aspect != null) {
                if(aspect.isPrimal()) {
                    if(merge) {
                        out.merge(aspect, al.getAmount(aspect));
                    } else {
                        out.add(aspect, al.getAmount(aspect));
                    }
                } else {
                    AspectList send = new AspectList();
                    send.add(aspect.getComponents()[0], al.getAmount(aspect));
                    send.add(aspect.getComponents()[1], al.getAmount(aspect));
                    send = reduceToPrimals(send, merge);

                    for(Aspect a : send.getAspects()) {
                        if(merge) {
                            out.merge(a, send.getAmount(a));
                        } else {
                            out.add(a, send.getAmount(a));
                        }
                    }
                }
            }
        }

        return out;
    }

    public static boolean completeResearchUnsaved(String username, String key) {
        ArrayList<String> completed = getResearchForPlayerSafe(username);
        if(completed != null && completed.contains(key)) {
            return false;
        } else {
            if(completed == null) {
                completed = new ArrayList<>();
            }

            completed.add(key);
            OldResearch.proxy.getCompletedResearch().put(username, completed);
            return true;
        }
    }

    public void completeResearch(EntityPlayer player, String key) {
        if(completeResearchUnsaved(player.getGameProfile().getName(), key)) {
            int warp = OldResearchApi.getWarp(key);
            if(warp > 0 /* && !Config.wuss :tr: */ && !player.world.isRemote) {
                if(warp > 1) {
                    int w2 = warp / 2;
                    if(warp - w2 > 0) {
                        OldResearch.addWarpToPlayer(player, warp - w2, false);
                    }

                    if(w2 > 0) {
                        OldResearch.addStickyWarpToPlayer(player, w2);
                    }
                } else {
                    OldResearch.addWarpToPlayer(player, warp, false);
                }
            }

            scheduleSave(player);
        }

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

    public void completeAspect(EntityPlayer player, Aspect aspect, short amount) {
        if(completeAspectUnsaved(player.getGameProfile().getName(), aspect, amount)) {
            scheduleSave(player);
        }

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

    public static boolean completeScannedPhenomenaUnsaved(String username, String key) {
        ArrayList<String> completed = OldResearch.proxy.getScannedPhenomena().get(username);
        if(completed == null) {
            completed = new ArrayList<>();
        }

        if(!completed.contains(key)) {
            completed.add(key);
            String t = key.replaceFirst("#", "@");
            if(key.startsWith("#") && completed.contains(t) && completed.remove(t)) {
                ;
            }

            OldResearch.proxy.getScannedPhenomena().put(username, completed);
        }

        return true;
    }

    public void completeScannedObject(EntityPlayer player, String object) {
        if(completeScannedObjectUnsaved(player.getGameProfile().getName(), object)) {
            scheduleSave(player);
        }

    }

    public void completeScannedEntity(EntityPlayer player, String key) {
        if(completeScannedEntityUnsaved(player.getGameProfile().getName(), key)) {
            scheduleSave(player);
        }

    }

    public void completeScannedPhenomena(EntityPlayer player, String key) {
        if(completeScannedPhenomenaUnsaved(player.getGameProfile().getName(), key)) {
            scheduleSave(player);
        }

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
                loadResearchNBT(data, player);
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

                scheduleSave(player);
                Thaumcraft.log.info("Assigning initial aspects to " + player.getGameProfile().getName());
            }
        } catch (Exception var10) {
            var10.printStackTrace();
            Thaumcraft.log.fatal("Error loading Thaumcraft data");
        }

    }

    public static void loadResearchNBT(NBTTagCompound entityData, EntityPlayer player) {
        NBTTagList tagList = entityData.getTagList("THAUMCRAFT.RESEARCH", 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeResearchUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

    }

    public static void loadAspectNBT(NBTTagCompound entityData, EntityPlayer player) {
        if(entityData.hasKey("THAUMCRAFT.ASPECTS")) {
            NBTTagList tagList = entityData.getTagList("THAUMCRAFT.ASPECTS", 10);

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
        NBTTagList tagList = entityData.getTagList("THAUMCRAFT.SCAN.OBJECTS", 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeScannedObjectUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

        tagList = entityData.getTagList("THAUMCRAFT.SCAN.ENTITIES", 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeScannedEntityUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

        tagList = entityData.getTagList("THAUMCRAFT.SCAN.PHENOMENA", 10);

        for(int j = 0; j < tagList.tagCount(); ++j) {
            NBTTagCompound rs = tagList.getCompoundTagAt(j);
            if(rs.hasKey("key")) {
                completeScannedPhenomenaUnsaved(player.getGameProfile().getName(), rs.getString("key"));
            }
        }

    }

    public static void scheduleSave(EntityPlayer player) {
        if(!player.world.isRemote) {
            ;
        }
    }

    public static boolean savePlayerData(EntityPlayer player, File file1, File file2) {
        boolean success = true;

        try {
            NBTTagCompound data = new NBTTagCompound();
            saveResearchNBT(data, player);
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

    public static void saveResearchNBT(NBTTagCompound entityData, EntityPlayer player) {
        NBTTagList tagList = new NBTTagList();
        List<String> res = getResearchForPlayer(player.getGameProfile().getName());
        if(res != null && res.size() > 0) {
            Iterator<String> i$ = res.iterator();

            label37:
            while(true) {
                String key;
                while(true) {
                    if(!i$.hasNext()) {
                        break label37;
                    }

                    key = i$.next();
                    if(key != null && (key.startsWith("@") || ResearchCategories.getResearch(key) != null)) {
                        if(!key.startsWith("@")) {
                            break;
                        }

                        String k = key.substring(1);
                        if(!isResearchComplete(player.getGameProfile().getName(), k)) {
                            break;
                        }
                    }
                }

                if(ResearchCategories.getResearch(key) == null || !ResearchCategories.getResearch(key).isAutoUnlock()) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", key);
                    tagList.appendTag(f);
                }
            }
        }

        entityData.setTag("THAUMCRAFT.RESEARCH", tagList);
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

        entityData.setTag("THAUMCRAFT.ASPECTS", tagList);
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

        entityData.setTag("THAUMCRAFT.SCAN.OBJECTS", tagList);
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

        entityData.setTag("THAUMCRAFT.SCAN.ENTITIES", tagList);
        tagList = new NBTTagList();
        List<String> phe = OldResearch.proxy.getScannedPhenomena().get(player.getGameProfile().getName());
        if(phe != null && phe.size() > 0) {
            for(String key : phe) {
                if(key != null) {
                    NBTTagCompound f = new NBTTagCompound();
                    f.setString("key", key);
                    tagList.appendTag(f);
                }
            }
        }

        entityData.setTag("THAUMCRAFT.SCAN.PHENOMENA", tagList);
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