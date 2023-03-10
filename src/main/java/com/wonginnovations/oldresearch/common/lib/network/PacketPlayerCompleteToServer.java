package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.lib.SoundsTC;

public class PacketPlayerCompleteToServer implements IMessage, IMessageHandler<PacketPlayerCompleteToServer, IMessage> {
    private String key;
    private int dim;
    private String username;
    private byte type;

    public PacketPlayerCompleteToServer() {
    }

    public PacketPlayerCompleteToServer(String key, String username, int dim, byte type) {
        this.key = key;
        this.dim = dim;
        this.username = username;
        this.type = type;
    }

    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.key);
        buffer.writeInt(this.dim);
        ByteBufUtils.writeUTF8String(buffer, this.username);
        buffer.writeByte(this.type);
    }

    public void fromBytes(ByteBuf buffer) {
        this.key = ByteBufUtils.readUTF8String(buffer);
        this.dim = buffer.readInt();
        this.username = ByteBufUtils.readUTF8String(buffer);
        this.type = buffer.readByte();
    }

    public IMessage onMessage(PacketPlayerCompleteToServer message, MessageContext ctx) {
        World world = DimensionManager.getWorld(message.dim);
        if(world != null && (ctx.getServerHandler().player == null || ctx.getServerHandler().player.getGameProfile().getName().equals(message.username))) {
            EntityPlayer player = world.getPlayerEntityByName(message.username);
            if(player != null && !ResearchManager.isResearchComplete(message.username, message.key)) {
                if(ResearchManager.doesPlayerHaveRequisites(message.username, message.key)) {
                    if(message.type != 0) {
                        if(message.type == 1) {
                            ResearchManager.createResearchNoteForPlayer(world, player, message.key);
                        }
                    } else {
                        for(Aspect a : ResearchCategories.getResearch(message.key).tags.getAspects()) {
                            OldResearch.proxy.playerKnowledge.addAspectPool(message.username, a, (short)(-ResearchCategories.getResearch(message.key).tags.getAmount(a)));
                            ResearchManager.scheduleSave(player);
                            PacketHandler.INSTANCE.sendTo(new PacketAspectPool(a.getTag(), (short) (-ResearchCategories.getResearch(message.key).tags.getAmount(a)), OldResearch.proxy.playerKnowledge.getAspectPoolFor(message.username, a)), (EntityPlayerMP)player);
                        }

                        PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(message.key), (EntityPlayerMP)player);
                        OldResearch.proxy.getResearchManager().completeResearch(player, message.key);
                        if(ResearchCategories.getResearch(message.key).siblings != null) {
                            for(String sibling : ResearchCategories.getResearch(message.key).siblings) {
                                if(!ResearchManager.isResearchComplete(message.username, sibling) && ResearchManager.doesPlayerHaveRequisites(message.username, sibling)) {
                                    PacketHandler.INSTANCE.sendTo(new PacketResearchComplete(sibling), (EntityPlayerMP)player);
                                    OldResearch.proxy.getResearchManager().completeResearch(player, sibling);
                                }
                            }
                        }
                    }

                    world.playSound(player.posX, player.posY, player.posZ, SoundsTC.learn, SoundCategory.MASTER, 0.75F, 1.0F, false);
                } else {
                    player.sendMessage(new TextComponentTranslation(I18n.format("tc.researcherror")));
                }
            }

            return null;
        } else {
            return null;
        }
    }
}
