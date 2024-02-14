package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.client.gui.OldGuiResearchBrowser;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;

public class PacketSyncResearch implements IMessage, IMessageHandler<PacketSyncResearch, IMessage> {
    protected ArrayList<String> data = new ArrayList<>();

    public PacketSyncResearch() {
    }

    public PacketSyncResearch(EntityPlayer player) {
        this.data = ResearchManager.getResearchForPlayer(player.getGameProfile().getName());
    }

    public void toBytes(ByteBuf buffer) {
        if(this.data != null && this.data.size() > 0) {
            buffer.writeShort(this.data.size());

            for(String s : this.data) {
                if(s != null) {
                    ByteBufUtils.writeUTF8String(buffer, s);
                }
            }
        } else {
            buffer.writeShort(0);
        }

    }

    public void fromBytes(ByteBuf buffer) {
        short size = buffer.readShort();
        this.data = new ArrayList<>();

        for(int a = 0; a < size; ++a) {
            this.data.add(ByteBufUtils.readUTF8String(buffer));
        }

    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketSyncResearch message, MessageContext ctx) {
        for(String key : message.data) {
            OldResearch.proxy.getResearchManager().completeResearch(Minecraft.getMinecraft().player, key);
        }

        OldGuiResearchBrowser.completedResearch.put(Minecraft.getMinecraft().player.getGameProfile().getName(), message.data);

        return null;
    }
}
