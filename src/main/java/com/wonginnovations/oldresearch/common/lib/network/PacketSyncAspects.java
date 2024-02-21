package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

public class PacketSyncAspects implements IMessage, IMessageHandler<PacketSyncAspects, IMessage> {
    protected AspectList data = new AspectList();

    public PacketSyncAspects() {
    }

    public PacketSyncAspects(EntityPlayer player) {
        this.data = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(player.getGameProfile().getName());
    }

    public void toBytes(ByteBuf buffer) {
        if(this.data != null && this.data.size() > 0) {
            buffer.writeShort(this.data.size());

            for(Aspect a : this.data.getAspects()) {
                if(a != null) {
                    ByteBufUtils.writeUTF8String(buffer, a.getTag());
                    buffer.writeShort(this.data.getAmount(a));
                }
            }
        } else {
            buffer.writeShort(0);
        }

    }

    public void fromBytes(ByteBuf buffer) {
        short size = buffer.readShort();
        this.data = new AspectList();

        for(int a = 0; a < size; ++a) {
            String tag = ByteBufUtils.readUTF8String(buffer);
            short amount = buffer.readShort();
            this.data.add(Aspect.getAspect(tag), amount);
        }

    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketSyncAspects message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
                for (Aspect key : message.data.getAspects()) {
                    OldResearchManager.completeAspect(Minecraft.getMinecraft().player, key, (short) message.data.getAmount(key));
                }
            }
        });
        return null;
    }
}
