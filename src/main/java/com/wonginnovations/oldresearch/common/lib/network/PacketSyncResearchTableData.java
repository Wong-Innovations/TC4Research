package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.api.research.ResearchTableData;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import thaumcraft.Thaumcraft;
import thaumcraft.common.lib.utils.Utils;

public class PacketSyncResearchTableData implements IMessage, IMessageHandler<PacketSyncResearchTableData, IMessage> {

    private long pos;
    ResearchTableData data = new ResearchTableData();

    public PacketSyncResearchTableData() {
    }

    public PacketSyncResearchTableData(BlockPos pos, ResearchTableData data) {
        this.pos = pos.toLong();
        this.data = data;
    }

    public void toBytes(ByteBuf buffer) {
        buffer.writeLong(this.pos);
        Utils.writeNBTTagCompoundToBuffer(buffer, this.data.serialize());
    }

    public void fromBytes(ByteBuf buffer) {
        this.pos = buffer.readLong();
        this.data.deserialize(Utils.readNBTTagCompoundFromBuffer(buffer));
    }

    public IMessage onMessage(final PacketSyncResearchTableData message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
                World world = Thaumcraft.proxy.getClientWorld();
                BlockPos bp = BlockPos.fromLong(message.pos);
                if (world != null && bp != null) {
                    TileEntity te = world.getTileEntity(bp);
                    if (te != null && te instanceof TileResearchTable) {
                        ((TileResearchTable) te).setTableData(message.data);
                    }
                }

            }
        });
        return null;
    }

}
