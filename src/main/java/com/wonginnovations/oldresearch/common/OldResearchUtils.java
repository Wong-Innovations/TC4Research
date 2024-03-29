package com.wonginnovations.oldresearch.common;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thaumcraft.api.items.ItemsTC;

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

    public static boolean isPlayerCarrying(EntityPlayer player, Item item) {
        return isPlayerCarrying(player, new ItemStack(item));
    }

    public static boolean isPlayerCarrying(EntityPlayer player, ItemStack stack) {
        if (player.inventory.offHandInventory.get(0).isItemEqualIgnoreDurability(stack)) return true;
        for(int i = 0; i < player.inventory.mainInventory.size(); ++i) {
            if(player.inventory.mainInventory.get(i).isItemEqualIgnoreDurability(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isThaumometer(ItemStack stack) {
        return stack.getItem() == ItemsTC.thaumometer;
    }

}
