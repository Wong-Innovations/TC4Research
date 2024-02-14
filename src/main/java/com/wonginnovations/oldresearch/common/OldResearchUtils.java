package com.wonginnovations.oldresearch.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class OldResearchUtils {

    public static boolean consumeInventoryItem(EntityPlayer player, Item item) {
        return consumeInventoryItem(player, item, 0);
    }

    private static boolean consumeOffhand(EntityPlayer player, Item item) {
        return consumeOffhand(player, item, 0);
    }

    public static boolean consumeInventoryItem(EntityPlayer player, Item item, int md) {
        if (consumeOffhand(player, item)) return true;
        int i = -1;
        for (int ii = 0;ii<player.inventory.mainInventory.size(); ii++) {
            if (player.inventory.mainInventory.get(ii).isItemEqual(new ItemStack(item, 1, md))) {
                i = ii;
                break;
            }
        }
        if (i < 0) {
            return false;
        } else {
            int count = player.inventory.mainInventory.get(i).getCount();
            if (count-1 <= 0) {
                player.inventory.mainInventory.set(i, ItemStack.EMPTY);
            } else {
                player.inventory.mainInventory.get(i).setCount(count-1);
            }
            return true;
        }
    }

    private static boolean consumeOffhand(EntityPlayer player, Item item, int md) {
        if (player.inventory.offHandInventory.get(0).isItemEqual(new ItemStack(item, md))) {
            int count = player.inventory.offHandInventory.get(0).getCount();
            if (count-1 <= 0) {
                player.inventory.mainInventory.set(0, ItemStack.EMPTY);
            } else {
                player.inventory.offHandInventory.get(0).setCount(count-1);
            }
            return true;
        } else {
            return false;
        }
    }

    public static int isPlayerCarrying(EntityPlayer player, ItemStack stack) {
        for(int var2 = 0; var2 < player.inventory.mainInventory.size(); ++var2) {
            if(player.inventory.mainInventory.get(var2).isItemEqual(stack)) {
                return var2;
            }
        }

        return -1;
    }

    public static float sqrt_double(double val) {
        return (float)Math.sqrt(val);
    }

    public static int floor_double(double val) {
        int i = (int)val;
        return val < (double)i ? i - 1 : i;
    }

    public static Object getNBTDataFromId(NBTTagCompound nbt, byte id, String key) {
        switch(id) {
            case 1:
                return nbt.getByte(key);
            case 2:
                return nbt.getShort(key);
            case 3:
                return nbt.getInteger(key);
            case 4:
                return nbt.getLong(key);
            case 5:
                return nbt.getFloat(key);
            case 6:
                return nbt.getDouble(key);
            case 7:
                return nbt.getByteArray(key);
            case 8:
                return nbt.getString(key);
            case 9:
                return nbt.getTagList(key, 10);
            case 10:
                return nbt.getTag(key);
            case 11:
                return nbt.getIntArray(key);
            default:
                return null;
        }
    }

}
