package com.wonginnovations.oldresearch.common.container;

import java.util.HashMap;

import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.container.slot.SlotLimitedByClass;
import thaumcraft.common.container.slot.SlotLimitedByItemstack;

public class ContainerResearchTable extends Container {
    public TileResearchTable tileEntity;
    String[] aspects;
    EntityPlayer player;

    public ContainerResearchTable(InventoryPlayer iinventory, TileResearchTable iinventory1) {
        this.player = iinventory.player;
        this.tileEntity = iinventory1;
        this.aspects = Aspect.aspects.keySet().toArray(new String[0]);
        this.addSlotToContainer(new SlotLimitedByClass(IScribeTools.class, iinventory1, 0, 16, 15));
        this.addSlotToContainer(new SlotLimitedByItemstack(new ItemStack(Items.PAPER), iinventory1, 1, 224, 16));
        this.bindPlayerInventory(iinventory);
    }

    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        int i;
        int j;
        for(i = 0; i < 3; ++i) {
            for(j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 77 + j * 18, 190 + i * 18));
            }
        }

        for(i = 0; i < 3; ++i) {
            for(j = 0; j < 3; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, i + j * 3, 20 + i * 18, 190 + j * 18));
            }
        }

    }

    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int slot) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slotObject = this.inventorySlots.get(slot);
        if (slotObject != null && slotObject.getHasStack()) {
            ItemStack stackInSlot = slotObject.getStack();
            stack = stackInSlot.copy();
            if (slot < 2) {
                if (!this.tileEntity.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 2, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.tileEntity.isItemValidForSlot(slot, stackInSlot) || !this.mergeItemStack(stackInSlot, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.getCount() == 0) {
                slotObject.putStack(ItemStack.EMPTY);
            } else {
                slotObject.onSlotChanged();
            }
        }

        return stack;
    }

    public boolean canInteractWith(EntityPlayer player) {
        return this.tileEntity.isUsableByPlayer(player);
    }
}
