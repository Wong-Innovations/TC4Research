package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.api.aspects.Aspect;

public class PacketAspectCombinationToServer implements IMessage, IMessageHandler<PacketAspectCombinationToServer, IMessage> {
    private int dim;
    private int playerid;
    private int x;
    private int y;
    private int z;
    Aspect aspect1;
    Aspect aspect2;
    boolean ab1;
    boolean ab2;

    public PacketAspectCombinationToServer() {
    }

    public PacketAspectCombinationToServer(EntityPlayer player, int x, int y, int z, Aspect aspect1, Aspect aspect2, boolean ab1, boolean ab2, boolean ret) {
        this.dim = player.world.provider.getDimension();
        this.playerid = player.getEntityId();
        this.x = x;
        this.y = y;
        this.z = z;
        this.aspect1 = aspect1;
        this.aspect2 = aspect2;
        this.ab1 = ab1;
        this.ab2 = ab2;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeInt(this.dim);
        buffer.writeInt(this.playerid);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
        ByteBufUtils.writeUTF8String(buffer, this.aspect1.getTag());
        ByteBufUtils.writeUTF8String(buffer, this.aspect2.getTag());
        buffer.writeBoolean(this.ab1);
        buffer.writeBoolean(this.ab2);
    }

    public void fromBytes(ByteBuf buffer) {
        this.dim = buffer.readInt();
        this.playerid = buffer.readInt();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
        this.aspect1 = Aspect.getAspect(ByteBufUtils.readUTF8String(buffer));
        this.aspect2 = Aspect.getAspect(ByteBufUtils.readUTF8String(buffer));
        this.ab1 = buffer.readBoolean();
        this.ab2 = buffer.readBoolean();
    }

    public IMessage onMessage(PacketAspectCombinationToServer message, MessageContext ctx) {
        World world = DimensionManager.getWorld(message.dim);
        if(world != null && (ctx.getServerHandler().player == null || ctx.getServerHandler().player.getEntityId() == message.playerid)) {
            Entity player = world.getEntityByID(message.playerid);
            if(player instanceof EntityPlayer && message.aspect1 != null) {
                Aspect combo = ResearchManager.getCombinationResult(message.aspect1, message.aspect2);
                if((OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer)player).getGameProfile().getName(), message.aspect1) > 0 || message.ab1) && (OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer)player).getGameProfile().getName(), message.aspect2) > 0 || message.ab2)) {
                    TileEntity rt = player.world.getTileEntity(new BlockPos(message.x, message.y, message.z));
                    if(OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer)player).getGameProfile().getName(), message.aspect1) <= 0 && message.ab1) {
                        if(rt instanceof TileResearchTable) {
                            ((TileResearchTable)rt).data.bonusAspects.remove(message.aspect1, 1);
                            BlockPos pos = new BlockPos(message.x, message.y, message.z);
                            player.world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                            rt.markDirty();
                        }
                    } else {
                        OldResearch.proxy.playerKnowledge.addAspectPool(((EntityPlayer)player).getGameProfile().getName(), message.aspect1, (short)-1);
                        PacketHandler.INSTANCE.sendTo(new PacketAspectPool(message.aspect1.getTag(), (short) 0, OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer) player).getGameProfile().getName(), message.aspect1)), (EntityPlayerMP)player);
                    }

                    if(OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer)player).getGameProfile().getName(), message.aspect2) <= 0 && message.ab2) {
                        if(rt instanceof TileResearchTable) {
                            ((TileResearchTable)rt).data.bonusAspects.remove(message.aspect2, 1);
                            BlockPos pos = new BlockPos(message.x, message.y, message.z);
                            player.world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                            rt.markDirty();
                        }
                    } else {
                        OldResearch.proxy.playerKnowledge.addAspectPool(((EntityPlayer)player).getGameProfile().getName(), message.aspect2, (short)-1);
                        PacketHandler.INSTANCE.sendTo(new PacketAspectPool(message.aspect2.getTag(), (short) 0, OldResearch.proxy.playerKnowledge.getAspectPoolFor(((EntityPlayer) player).getGameProfile().getName(), message.aspect2)), (EntityPlayerMP)player);
                    }

                    if(combo != null) {
                        ScanManager.checkAndSyncAspectKnowledge((EntityPlayer)player, combo, 1);
                    }

                    ResearchManager.scheduleSave((EntityPlayer)player);
                }
            }

            return null;
        } else {
            return null;
        }
    }
}
