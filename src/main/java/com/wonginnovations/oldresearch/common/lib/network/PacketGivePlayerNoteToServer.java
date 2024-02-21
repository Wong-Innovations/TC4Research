package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.capabilities.ThaumcraftCapabilities;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.research.ResearchManager;

public class PacketGivePlayerNoteToServer implements IMessage, IMessageHandler<PacketGivePlayerNoteToServer, IMessage> {
    private String key;
    private int dim;
    private String username;

    public PacketGivePlayerNoteToServer() {
    }

    public PacketGivePlayerNoteToServer(String key, String username, int dim, byte type) {
        this.key = key;
        this.dim = dim;
        this.username = username;
    }

    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.key);
        buffer.writeInt(this.dim);
        ByteBufUtils.writeUTF8String(buffer, this.username);
    }

    public void fromBytes(ByteBuf buffer) {
        this.key = ByteBufUtils.readUTF8String(buffer);
        this.dim = buffer.readInt();
        this.username = ByteBufUtils.readUTF8String(buffer);
    }

    public IMessage onMessage(PacketGivePlayerNoteToServer message, MessageContext ctx) {
        IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
        mainThread.addScheduledTask(new Runnable() {
            public void run() {
                World world = ctx.getServerHandler().player.world;
                EntityPlayer player = ctx.getServerHandler().player;
                if(world != null && player != null && !ThaumcraftCapabilities.knowsResearchStrict(player, message.key)) {
                    if(ResearchManager.doesPlayerHaveRequisites(player, message.key)) {
                        OldResearchManager.givePlayerResearchNote(world, player, message.key);
                        world.playSound(player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.MASTER, 0.75F, 1.0F, false);
                    } else {
                        player.sendMessage(new TextComponentTranslation(I18n.format("tc.researcherror")));
                    }
                }
            }
        });
        return null;
    }
}
