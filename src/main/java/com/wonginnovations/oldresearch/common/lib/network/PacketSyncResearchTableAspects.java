package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.Thaumcraft;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class PacketSyncResearchTableAspects implements IMessage, IMessageHandler<PacketSyncResearchTableAspects, IMessage> {

    private long pos;
    AspectList aspects;

    public PacketSyncResearchTableAspects() {
    }

    public PacketSyncResearchTableAspects(BlockPos pos, AspectList aspects) {
        this.pos = pos.toLong();
        this.aspects = aspects;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(this.pos);
        if(this.aspects != null && this.aspects.size() > 0) {
            buffer.writeShort(this.aspects.size());

            for(Aspect a : this.aspects.getAspects()) {
                if(a != null) {
                    ByteBufUtils.writeUTF8String(buffer, a.getTag());
                    buffer.writeInt(this.aspects.getAmount(a));
                }
            }
        } else {
            buffer.writeShort(0);
        }
    }

    public void fromBytes(ByteBuf buffer) {
        this.pos = buffer.readLong();
        short size = buffer.readShort();
        this.aspects = new AspectList();

        for(int a = 0; a < size; ++a) {
            String tag = ByteBufUtils.readUTF8String(buffer);
            int amount = buffer.readInt();
            this.aspects.add(Aspect.getAspect(tag), amount);
        }
    }

    public IMessage onMessage(final PacketSyncResearchTableAspects message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
                World world = Thaumcraft.proxy.getClientWorld();
                BlockPos bp = BlockPos.fromLong(message.pos);
                if (world != null) {
                    TileEntity te = world.getTileEntity(bp);
                    if (te instanceof TileResearchTable) {
                        ((TileResearchTable) te).bonusAspects = message.aspects;
                    }
                }
            }
        });
        return null;
    }

}
