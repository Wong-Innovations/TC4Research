package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ScanResult;
import com.wonginnovations.oldresearch.client.lib.PlayerNotifications;
import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectDiscovery;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectPool;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.config.ModConfig;
import com.wonginnovations.oldresearch.core.mixin.ThaumcraftCraftingManagerAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import net.minecraftforge.fml.common.registry.GameRegistry;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.golems.EntityThaumcraftGolem;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class ScanManager implements IScanEventHandler {
    public ScanResult scanPhenomena(ItemStack stack, World world, EntityPlayer player) {
        return null;
    }

    private static int generateEntityHash(Entity entity) {
        String hash = EntityList.getEntityString(entity);
        if(hash == null) {
            hash = "generic";
        }

        if(entity instanceof EntityPlayer) {
            hash = "player_" + ((EntityPlayer)entity).getGameProfile().getName();
        }

        label61:
        for(OldResearchApi.EntityTags et : OldResearchApi.scanEntities) {
            if(et.entityName.equals(hash) && et.nbts != null && et.nbts.length != 0) {
                NBTTagCompound tc = new NBTTagCompound();
                entity.writeToNBT(tc);

                for(OldResearchApi.EntityTagsNBT nbt : et.nbts) {
                    if(!tc.hasKey(nbt.name)) {
                        continue label61;
                    }

                    Object val = OldResearchUtils.getNBTDataFromId(tc, tc.getTagId(nbt.name), nbt.name);
                    Class<?> c = val.getClass();

                    try {
                        if(!c.cast(val).equals(c.cast(nbt.value))) {
                            continue label61;
                        }
                    } catch (Exception var13) {
                        continue label61;
                    }
                }

                for(OldResearchApi.EntityTagsNBT nbt : et.nbts) {
                    Object val = OldResearchUtils.getNBTDataFromId(tc, tc.getTagId(nbt.name), nbt.name);
                    Class<?> c = val.getClass();

                    try {
                        hash = hash + nbt.name + c.cast(nbt.value);
                    } catch (Exception ignored) {}
                }
            }
        }

        if(entity instanceof EntityLivingBase) {
            EntityLivingBase le = (EntityLivingBase)entity;
            if(le.isChild()) {
                hash = hash + "CHILD";
            }
        }

        if(entity instanceof EntityZombieVillager) {
            hash = hash + "VILLAGER";
        }

        if(entity instanceof EntityCreeper) {
            if(((EntityCreeper)entity).getCreeperState() == 1) {
                hash = hash + "FLASHING";
            }

            if(((EntityCreeper)entity).getPowered()) {
                hash = hash + "POWERED";
            }
        }

        if(entity instanceof EntityThaumcraftGolem) {
            hash = hash + "" + ((EntityThaumcraftGolem)entity).getGolemEntity().getName();
        }

        return hash.hashCode();
    }

    public static int generateItemHash(Item item, int meta) {
        ItemStack t = new ItemStack(item, 1, meta);

        try {
            if(t.isItemStackDamageable() || !t.getHasSubtypes()) {
                meta = -1;
            }
        } catch (Exception ignored) {}

        if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(item, meta))) {
            meta = ((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(item, meta)))[0];
        }

        String hash;
        try {
            ResourceLocation ui = GameRegistry.findRegistry(Item.class).getKey(item);
            if(ui != null) {
                hash = ui + ":" + meta;
            } else {
                hash = t.getItem().getRegistryName() + ":" + meta;
            }
        } catch (Exception var14) {
            hash = "oops:" + meta;
        }

        if(!OldResearchApi.objectTags.containsKey(Arrays.asList(item, meta))) {
            for(List<?> l : OldResearchApi.objectTags.keySet()) {
                String name = ((Item)l.get(0)).getRegistryName().toString();
                if((Item.REGISTRY.getObject(new ResourceLocation(name)) == item || Block.REGISTRY.getObject(new ResourceLocation(name)) == Block.getBlockFromItem(item)) && l.get(1) instanceof int[]) {
                    int[] range = (int[]) l.get(1);
                    Arrays.sort(range);
                    if(Arrays.binarySearch(range, meta) >= 0) {
                        ResourceLocation ui = GameRegistry.findRegistry(Item.class).getKey(item);
                        if(ui != null) {
                            hash = ui.toString();
                        } else {
                            hash = "" + t.getItem().getRegistryName();
                        }

                        for(int r : range) {
                            hash = hash + ":" + r;
                        }

                        return hash.hashCode();
                    }
                }
            }

            if(!OldResearchApi.objectTags.containsKey(Arrays.asList(item, -1)) && meta == -1) {
                int index = 0;
                boolean found = false;

                while(true) {
                    found = OldResearchApi.objectTags.containsKey(Arrays.asList(item, index));
                    ++index;
                    if(index >= 16 || found) {
                        break;
                    }
                }

                if(found) {
                    ResourceLocation ui = GameRegistry.findRegistry(Item.class).getKey(item);
                    if(ui != null) {
                        hash = ui + ":" + index;
                    } else {
                        hash = t.getItem().getRegistryName() + ":" + index;
                    }
                }
            }
        }

        return hash.hashCode();
    }

    public static AspectList generateEntityAspects(Entity entity) {
        AspectList tags = null;
        String s = null;

        try {
            if (entity instanceof EntityPlayer) {
                s = ((EntityPlayer)entity).getGameProfile().getName();
            } else {
                s = EntityList.getEntityString(entity);
            }
        } catch (Throwable ignored) {
        }

        if(s == null) {
            s = "generic";
        }

        if(entity instanceof EntityPlayer) {
            s = "player_" + ((EntityPlayer)entity).getGameProfile().getName();
            tags = new AspectList();
            tags.add(Aspect.MAN, 4);
            if(((EntityPlayer)entity).getGameProfile().getName().equalsIgnoreCase("azanor")) {
                tags.add(Aspect.ELDRITCH, 20);
            } else if(((EntityPlayer)entity).getGameProfile().getName().equalsIgnoreCase("direwolf20")) {
                tags.add(Aspect.BEAST, 20);
            } else if(((EntityPlayer)entity).getGameProfile().getName().equalsIgnoreCase("pahimar")) {
                tags.add(Aspect.EXCHANGE, 20);
            } else {
                Random rand = new Random(s.hashCode());
                Aspect[] posa = Aspect.aspects.values().toArray(new Aspect[0]);
                tags.add(posa[rand.nextInt(posa.length)], 4);
                tags.add(posa[rand.nextInt(posa.length)], 4);
                tags.add(posa[rand.nextInt(posa.length)], 4);
            }
        } else {
            label264:
            for(OldResearchApi.EntityTags et : OldResearchApi.scanEntities) {
                if(et.entityName.equals(s)) {
                    if(et.nbts != null && et.nbts.length != 0) {
                        NBTTagCompound tc = new NBTTagCompound();
                        entity.writeToNBT(tc);

                        for(OldResearchApi.EntityTagsNBT nbt : et.nbts) {
                            if(!tc.hasKey(nbt.name) || !OldResearchUtils.getNBTDataFromId(tc, tc.getTagId(nbt.name), nbt.name).equals(nbt.value)) {
                                continue label264;
                            }
                        }

                        tags = et.aspects;
                    } else {
                        tags = et.aspects;
                    }
                }
            }
        }

        return tags;
    }

//    Not yet ( ._.)

//    private static AspectList generateNodeAspects(World world, String node) {
//        AspectList tags = new AspectList();
//        ArrayList<Integer> loc = (ArrayList)TileNode.locations.get(node);
//        if(loc != null && loc.size() > 0) {
//            int dim = ((Integer)loc.get(0)).intValue();
//            int x = ((Integer)loc.get(1)).intValue();
//            int y = ((Integer)loc.get(2)).intValue();
//            int z = ((Integer)loc.get(3)).intValue();
//            if(dim == world.provider.dimensionId) {
//                TileEntity tnb = world.getTileEntity(x, y, z);
//                if(tnb != null && tnb instanceof INode) {
//                    AspectList ta = ((INode)tnb).getAspects();
//
//                    for(Aspect a : ta.getAspectsSorted()) {
//                        tags.merge(a, Math.max(4, ta.getAmount(a) / 10));
//                    }
//
//                    switch(((INode)tnb).getNodeType()) {
//                        case UNSTABLE:
//                            tags.merge(Aspect.ENTROPY, 4);
//                            break;
//                        case HUNGRY:
//                            tags.merge(Aspect.HUNGER, 4);
//                            break;
//                        case TAINTED:
//                            tags.merge(Aspect.TAINT, 4);
//                            break;
//                        case PURE:
//                            tags.merge(Aspect.HEAL, 2);
//                            tags.add(Aspect.ORDER, 2);
//                            break;
//                        case DARK:
//                            tags.merge(Aspect.DEATH, 2);
//                            tags.add(Aspect.DARKNESS, 2);
//                    }
//                }
//            }
//        }
//
//        return tags.size() > 0?tags:null;
//    }

    public static boolean isValidScanTarget(EntityPlayer player, ScanResult scan, String prefix) {
        if(scan == null) {
            return false;
        } else if(prefix.equals("@") && !isValidScanTarget(player, scan, "#")) {
            return false;
        } else {
            if(scan.type == 1) {
                if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(Item.getItemById(scan.id), scan.meta))) {
                    scan.meta = ((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(Item.getItemById(scan.id), scan.meta)))[0];
                }

                List<String> list = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
                return list == null || !list.contains(prefix + generateItemHash(Item.getItemById(scan.id), scan.meta));
            } else if(scan.type == 2) {
                if(scan.entity instanceof EntityItem item) {
                    ItemStack t = item.getItem().copy();
                    t.setCount(1);
                    if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(t.getItem(), t.getItemDamage()))) {
                        t.setItemDamage(((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(t.getItem(), t.getItemDamage())))[0]);
                    }

                    List<String> list = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
                    return list == null || !list.contains(prefix + generateItemHash(t.getItem(), t.getItemDamage()));
                } else {
                    List<String> list = OldResearch.proxy.getScannedEntities().get(player.getGameProfile().getName());
                    return list == null || !list.contains(prefix + generateEntityHash(scan.entity));
                }
            } else if(scan.type == 3) {
                List<String> list = OldResearch.proxy.getScannedPhenomena().get(player.getGameProfile().getName());
                return list == null || !list.contains(prefix + scan.phenomena);
            }

            return true;
        }
    }

    public static boolean hasBeenScanned(EntityPlayer player, ScanResult scan) {
        if(scan.type == 1) {
            if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(Item.getItemById(scan.id), scan.meta))) {
                scan.meta = ((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(Item.getItemById(scan.id), scan.meta)))[0];
            }

            List<String> list = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
            return list != null && (list.contains("@" + generateItemHash(Item.getItemById(scan.id), scan.meta)) || list.contains("#" + generateItemHash(Item.getItemById(scan.id), scan.meta)));
        } else if(scan.type == 2) {
            if(scan.entity instanceof EntityItem) {
                EntityItem item = (EntityItem)scan.entity;
                ItemStack t = item.getItem().copy();
                t.setCount(1);
                if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(t.getItem(), t.getItemDamage()))) {
                    t.setItemDamage(((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(t.getItem(), t.getItemDamage())))[0]);
                }

                List<String> list = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
                return list != null && (list.contains("@" + generateItemHash(t.getItem(), t.getItemDamage())) || list.contains("#" + generateItemHash(t.getItem(), t.getItemDamage())));
            } else {
                List<String> list = OldResearch.proxy.getScannedEntities().get(player.getGameProfile().getName());
                return list != null && (list.contains("@" + generateEntityHash(scan.entity)) || list.contains("#" + generateEntityHash(scan.entity)));
            }
        } else if(scan.type == 3) {
            List<String> list = OldResearch.proxy.getScannedPhenomena().get(player.getGameProfile().getName());
            return list != null && (list.contains("@" + scan.phenomena) || list.contains("#" + scan.phenomena));
        }

        return false;
    }

    public static boolean completeScan(EntityPlayer player, ScanResult scan, String prefix) {
        AspectList aspects = null;
        PlayerKnowledge rp = OldResearch.proxy.getPlayerKnowledge();
        boolean ret = false;
        boolean scannedByThaumometer = prefix.equals("#") && !isValidScanTarget(player, scan, "@");
        Object clue = null;
        if(scan.type == 1) {
            if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(Item.getItemById(scan.id), scan.meta))) {
                scan.meta = ((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(Item.getItemById(scan.id), scan.meta)))[0];
            }

            aspects = ThaumcraftCraftingManager.getObjectTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta));
            aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta), aspects);
            if((aspects == null || aspects.size() == 0) && scan.id > 0) {
                aspects = ThaumcraftCraftingManager.getObjectTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta));
                aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta), aspects);
            }

            if(validScan(aspects, player)) {
                clue = new ItemStack(Item.getItemById(scan.id), 1, scan.meta);
                OldResearch.proxy.getOldResearchManager().completeScannedObject(player, prefix + generateItemHash(Item.getItemById(scan.id), scan.meta));
                ret = true;
            }
        } else if(scan.type == 2) {
            if(scan.entity instanceof EntityItem) {
                EntityItem item = (EntityItem)scan.entity;
                ItemStack t = item.getItem().copy();
                t.setCount(1);
                if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(t.getItem(), t.getItemDamage()))) {
                    t.setItemDamage(((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(t.getItem(), t.getItemDamage())))[0]);
                }

                aspects = ThaumcraftCraftingManager.getObjectTags(t);
                aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(t, aspects);
                if(validScan(aspects, player)) {
                    clue = item.getItem();
                    OldResearch.proxy.getOldResearchManager().completeScannedObject(player, prefix + generateItemHash(item.getItem().getItem(), item.getItem().getItemDamage()));
                    ret = true;
                }
            } else {
                aspects = generateEntityAspects(scan.entity);
                if(validScan(aspects, player)) {
                    clue = EntityList.getEntityString(scan.entity);
                    OldResearch.proxy.getOldResearchManager().completeScannedEntity(player, prefix + generateEntityHash(scan.entity));
                    ret = true;
                }
            }
        }
//        else if(scan.type == 3 && scan.phenomena.startsWith("NODE")) {
//            aspects = generateNodeAspects(player.world, scan.phenomena.replace("NODE", ""));
//            if(validScan(aspects, player)) {
//                OldResearch.proxy.getResearchManager().completeScannedPhenomena(player, prefix + scan.phenomena);
//                ret = true;
//            }
//        }

        if(!player.world.isRemote && ret && aspects != null) {
            AspectList aspectsFinal = new AspectList();

            for(Aspect aspect : aspects.getAspects()) {
                if(rp.hasDiscoveredParentAspects(player.getGameProfile().getName(), aspect)) {
                    int amt = aspects.getAmount(aspect);
                    if(scannedByThaumometer) {
                        amt = 0;
                    }

                    if(prefix.equals("#")) {
                        ++amt;
                    }

                    int a = checkAndSyncAspectKnowledge(player, aspect, amt);
                    if(a > 0) {
                        aspectsFinal.merge(aspect, a);
                    }
                }
            }

            if(clue != null) {
                OldResearchManager.createClue(player.world, player, clue, aspectsFinal);
            }
        }

        String msg = player.getGameProfile().getName() + " aspects: [";

        AspectList alist = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(player.getGameProfile().getName());
        for (Aspect a : alist.getAspects()) {
            msg = msg + a.getName() + " " + alist.getAmount(a) + ", ";
        }

        msg += "]";

        player.sendMessage(new TextComponentString(msg));

        return ret;
    }

    public static int checkAndSyncAspectKnowledge(EntityPlayer player, Aspect aspect, int amount) {
        PlayerKnowledge rp = OldResearch.proxy.getPlayerKnowledge();
        int save = 0;
        if(!rp.hasDiscoveredAspect(player.getGameProfile().getName(), aspect)) {
            PacketHandler.INSTANCE.sendTo(new PacketAspectDiscovery(aspect.getTag()), (EntityPlayerMP)player);
            amount += 2;
            save = amount;
        }

        if(rp.getAspectPoolFor(player.getGameProfile().getName(), aspect) >= ModConfig.aspectTotalCap) {
            amount = (int)Math.sqrt(amount);
        }

        if(amount > 1 && (float)rp.getAspectPoolFor(player.getGameProfile().getName(), aspect) >= (float)ModConfig.aspectTotalCap * 1.25F) {
            amount = 1;
        }

        if(rp.addAspectPool(player.getGameProfile().getName(), aspect, (short)amount)) {
            PacketHandler.INSTANCE.sendTo(new PacketAspectPool(aspect.getTag(), (short) amount, rp.getAspectPoolFor(player.getGameProfile().getName(), aspect)), (EntityPlayerMP)player);
            save = amount;
        }

        if(save > 0) {
            OldResearch.proxy.getOldResearchManager().completeAspect(player, aspect, rp.getAspectPoolFor(player.getGameProfile().getName(), aspect));
        }

        return save;
    }

    public static boolean validScan(AspectList aspects, EntityPlayer player) {
        PlayerKnowledge rp = OldResearch.proxy.getPlayerKnowledge();
        if(aspects != null && aspects.size() > 0) {
            for(Aspect aspect : aspects.getAspects()) {
                if(aspect != null && !aspect.isPrimal() && !rp.hasDiscoveredParentAspects(player.getGameProfile().getName(), aspect)) {
                    if(player.world.isRemote) {
                        for(Aspect parent : aspect.getComponents()) {
                            if(!rp.hasDiscoveredAspect(player.getGameProfile().getName(), parent)) {
                                PlayerNotifications.addNotification((new TextComponentTranslation(I18n.format("tc.discoveryerror"), I18n.format("tc.aspect.help." + parent.getTag()))).getUnformattedText());
                                break;
                            }
                        }
                    }

                    return false;
                }
            }

            return true;
        } else {
            if(player.world.isRemote) {
                PlayerNotifications.addNotification(I18n.format("tc.unknownobject"));
            }

            return false;
        }
    }

    public static AspectList getScanAspects(ScanResult scan, World world) {
        AspectList aspects = new AspectList();
        boolean ret = false;
        if(scan.type == 1) {
            if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(Item.getItemById(scan.id), scan.meta))) {
                scan.meta = ((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(Item.getItemById(scan.id), scan.meta)))[0];
            }

            aspects = ThaumcraftCraftingManager.getObjectTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta));
            aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta), aspects);
            if((aspects == null || aspects.size() == 0) && scan.id > 0) {
                aspects = ThaumcraftCraftingManager.getObjectTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta));
                aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(new ItemStack(Item.getItemById(scan.id), 1, scan.meta), aspects);
            }
        } else if(scan.type == 2) {
            if(scan.entity instanceof EntityItem) {
                EntityItem item = (EntityItem)scan.entity;
                ItemStack t = item.getItem().copy();
                t.setCount(1);
                if(OldResearchApi.groupedObjectTags.containsKey(Arrays.asList(t.getItem(), t.getItemDamage()))) {
                    t.setItemDamage(((int[])OldResearchApi.groupedObjectTags.get(Arrays.asList(t.getItem(), t.getItemDamage())))[0]);
                }

                aspects = ThaumcraftCraftingManager.getObjectTags(t);
                aspects = ThaumcraftCraftingManagerAccessor.getBonusTags(t, aspects);
            } else {
                aspects = generateEntityAspects(scan.entity);
            }
        }
//        else if(scan.type == 3 && scan.phenomena.startsWith("NODE")) {
//            aspects = generateNodeAspects(world, scan.phenomena.replace("NODE", ""));
//        }

        return aspects;
    }
}
