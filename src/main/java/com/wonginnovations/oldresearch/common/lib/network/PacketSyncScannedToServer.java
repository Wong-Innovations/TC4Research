package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.api.research.ScanResult;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.utils.Utils;

public class PacketSyncScannedToServer implements IMessage, IMessageHandler<PacketSyncScannedToServer, IMessage> {
    private int playerid;
    private int dim;
    private int entityid;
    private BlockPos bp;

    public PacketSyncScannedToServer() {
    }

    public PacketSyncScannedToServer(EntityPlayer player, Entity scannedEntity, BlockPos scannedBlock) {
        this.playerid = player.getEntityId();
        this.dim = player.world.provider.getDimension();
        this.entityid = scannedEntity == null? 0 : scannedEntity.getEntityId();
        this.bp = scannedBlock;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.playerid);
        buffer.writeInt(this.dim);
        buffer.writeInt(this.entityid);
        buffer.writeLong(this.bp == null? 0L : this.bp.toLong());
    }

    public void fromBytes(ByteBuf buffer) {
        this.playerid = buffer.readInt();
        this.dim = buffer.readInt();
        this.entityid = buffer.readInt();
        this.bp = BlockPos.fromLong(buffer.readLong());
    }

    public IMessage onMessage(PacketSyncScannedToServer message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                World world = ctx.getServerHandler().player.world;
                EntityPlayer player = ctx.getServerHandler().player;
                if (world != null && player != null) {
                    Entity e = null;
                    if(message.entityid != 0) {
                        e = world.getEntityByID(message.entityid);
                    }
                    if (e != null) ScanningManager.scanTheThing(player, e);
                    else ScanningManager.scanTheThing(player, message.bp);
                }
            }
        });
        return null;
    }
}
