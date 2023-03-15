package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.api.research.ScanResult;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketScannedToServer implements IMessage, IMessageHandler<PacketScannedToServer, IMessage> {
    private int playerid;
    private int dim;
    private byte type;
    private int id;
    private int md;
    private int entityid;
    private String phenomena;
    private String prefix;

    public PacketScannedToServer() {
    }

    public PacketScannedToServer(ScanResult scan, EntityPlayer player, String prefix) {
        this.playerid = player.getEntityId();
        this.dim = player.world.provider.getDimension();
        this.type = scan.type;
        this.id = scan.id;
        this.md = scan.meta;
        this.entityid = scan.entity == null? 0 : scan.entity.getEntityId();
        this.phenomena = scan.phenomena;
        this.prefix = prefix;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.playerid);
        buffer.writeInt(this.dim);
        buffer.writeByte(this.type);
        buffer.writeInt(this.id);
        buffer.writeInt(this.md);
        buffer.writeInt(this.entityid);
        ByteBufUtils.writeUTF8String(buffer, this.phenomena);
        ByteBufUtils.writeUTF8String(buffer, this.prefix);
    }

    public void fromBytes(ByteBuf buffer) {
        this.playerid = buffer.readInt();
        this.dim = buffer.readInt();
        this.type = buffer.readByte();
        this.id = buffer.readInt();
        this.md = buffer.readInt();
        this.entityid = buffer.readInt();
        this.phenomena = ByteBufUtils.readUTF8String(buffer);
        this.prefix = ByteBufUtils.readUTF8String(buffer);
    }

    public IMessage onMessage(PacketScannedToServer message, MessageContext ctx) {
        World world = DimensionManager.getWorld(message.dim);
        if (world == null) {
            return null;
        } else {
            Entity player = world.getEntityByID(message.playerid);
            Entity e = null;
            if(message.entityid != 0) {
                e = world.getEntityByID(message.entityid);
            }

            if(player instanceof EntityPlayer) {
                ScanManager.completeScan((EntityPlayer)player, new ScanResult(message.type, message.id, message.md, e, message.phenomena), message.prefix);
            }

            return null;
        }
    }
}
