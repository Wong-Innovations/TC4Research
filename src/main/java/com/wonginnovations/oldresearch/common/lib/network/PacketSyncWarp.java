package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSyncWarp implements IMessage, IMessageHandler<PacketSyncWarp, IMessage> {
    protected int data = 0;
    protected byte type = 0;

    public PacketSyncWarp() {
    }

    public PacketSyncWarp(EntityPlayer player, byte type) {
        if(type == 0) {
            this.data = OldResearch.proxy.getPlayerKnowledge().getWarpPerm(player.getGameProfile().getName());
        }

        if(type == 1) {
            this.data = OldResearch.proxy.getPlayerKnowledge().getWarpSticky(player.getGameProfile().getName());
        }

        if(type == 2) {
            this.data = OldResearch.proxy.getPlayerKnowledge().getWarpTemp(player.getGameProfile().getName());
        }

        this.type = type;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.data);
        buffer.writeByte(this.type);
    }

    public void fromBytes(ByteBuf buffer) {
        this.data = buffer.readInt();
        this.type = buffer.readByte();
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketSyncWarp message, MessageContext ctx) {
        if(message.type == 0) {
            OldResearch.proxy.getPlayerKnowledge().setWarpPerm(Minecraft.getMinecraft().player.getGameProfile().getName(), message.data);
        } else if(message.type == 1) {
            OldResearch.proxy.getPlayerKnowledge().setWarpSticky(Minecraft.getMinecraft().player.getGameProfile().getName(), message.data);
        } else {
            OldResearch.proxy.getPlayerKnowledge().setWarpTemp(Minecraft.getMinecraft().player.getGameProfile().getName(), message.data);
        }

        return null;
    }
}
