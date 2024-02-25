package com.wonginnovations.oldresearch.common.lib.network;


import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import com.wonginnovations.oldresearch.config.ModConfig;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ScanningManager;

public class PacketScanSelfToServer implements IMessage, IMessageHandler<PacketScanSelfToServer, IMessage> {

    public void fromBytes(ByteBuf buf) {
    }

    public void toBytes(ByteBuf buf) {
    }

    public IMessage onMessage(PacketScanSelfToServer message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                if (!ModConfig.inventoryScanning) return;
                EntityPlayer entityPlayer = ctx.getServerHandler().player;
                if (ScanManager.canScanThing(entityPlayer, entityPlayer, true))
                    ScanningManager.scanTheThing(entityPlayer, entityPlayer);
            }
        });
        return null;
    }
}
