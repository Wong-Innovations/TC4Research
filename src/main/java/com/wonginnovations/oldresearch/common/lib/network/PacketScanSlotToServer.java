package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import com.wonginnovations.oldresearch.config.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ScanningManager;

public class PacketScanSlotToServer implements IMessage, IMessageHandler<PacketScanSlotToServer, IMessage> {

    int slotNumber;

    public PacketScanSlotToServer() {
    }

    public PacketScanSlotToServer(int slotNumber) {
        this.slotNumber = slotNumber;
    }

    public void fromBytes(ByteBuf buf) {
        this.slotNumber = buf.readInt();
    }

    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.slotNumber);
    }

    public IMessage onMessage(PacketScanSlotToServer message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                if (!ModConfig.inventoryScanning) return;
                EntityPlayer entityPlayer = ctx.getServerHandler().player;
                Container container = entityPlayer.openContainer;
                if (container != null && message.slotNumber >= 0 && message.slotNumber < container.inventorySlots.size()) {
                    Slot slot = container.inventorySlots.get(message.slotNumber);
                    if (!slot.getStack().isEmpty() && slot.canTakeStack(entityPlayer) && !(slot instanceof SlotCrafting)) {
                        ItemStack itemStack = slot.getStack();
                        if (ScanManager.canScanThing(entityPlayer, itemStack, true))
                            ScanningManager.scanTheThing(entityPlayer, itemStack);
                    }
                }
            }
        });
        return null;
    }
}
