package com.wonginnovations.oldresearch.common.container;

import com.wonginnovations.oldresearch.common.items.ItemResearchNote;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.common.container.slot.SlotLimitedByClass;

public class ContainerResearchTable extends Container {
    public TileResearchTable tileEntity;
    String[] aspects;
    EntityPlayer player;

    public ContainerResearchTable(InventoryPlayer playerInventory, TileResearchTable tableInventory) {
        this.player = playerInventory.player;
        this.tileEntity = tableInventory;
        this.aspects = Aspect.aspects.keySet().toArray(new String[0]);
        this.addSlotToContainer(new SlotLimitedByClass(IScribeTools.class, tableInventory, 0, 14, 10));
        this.addSlotToContainer(new SlotLimitedByClass(ItemResearchNote.class, tableInventory, 1, 70, 10));
        this.bindPlayerInventory(playerInventory);
    }

    protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
        for(int i = 0; i < 3; ++i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 48 + j * 18, 175 + i * 18));
            }
        }

        for(int i = 0; i < 9; ++i) {
            this.addSlotToContainer(new Slot(inventoryPlayer, i, 48 + i * 18, 233));
        }

    }

    @Override
    public @NotNull ItemStack transferStackInSlot(@NotNull EntityPlayer player, int slotIndex) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack stackInSlot = slot.getStack();
            stack = stackInSlot.copy();
            if (slotIndex < 2) {
                if (!this.tileEntity.isItemValidForSlot(slotIndex, stackInSlot) || !this.mergeItemStack(stackInSlot, 2, this.inventorySlots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            }
            else if (!this.mergeItemStack(stackInSlot, 0, 2, false)) {
                return ItemStack.EMPTY;
            }

            if (stackInSlot.getCount() == 0) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onSlotChanged();
            }
        }

        return stack;
    }

    public boolean canInteractWith(@NotNull EntityPlayer player) {
        return this.tileEntity.isUsableByPlayer(player);
    }

}
