package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.client.lib.PlayerNotifications;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.lib.SoundsTC;

public class PacketWarpMessage implements IMessage, IMessageHandler<PacketWarpMessage, IMessage> {
    protected int data = 0;
    protected byte type = 0;

    public PacketWarpMessage() {
    }

    public PacketWarpMessage(EntityPlayer player, byte type, int change) {
        this.data = change;
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
    public IMessage onMessage(PacketWarpMessage message, MessageContext ctx) {
        if(message.data != 0) {
            if(message.type == 0 && message.data > 0) {
                String text = I18n.format("tc.addwarp");
                if(message.data < 0) {
                    text = I18n.format("tc.removewarp");
                } else {
                    Minecraft.getMinecraft().player.playSound(SoundsTC.whispers, 0.5F, 1.0F);
                }

                PlayerNotifications.addNotification(text);
            } else if(message.type == 1) {
                String text = I18n.format("tc.addwarpsticky");
                if(message.data < 0) {
                    text = I18n.format("tc.removewarpsticky");
                } else {
                    Minecraft.getMinecraft().player.playSound(SoundsTC.whispers, 0.5F, 1.0F);
                }

                PlayerNotifications.addNotification(text);
            } else if(message.data > 0) {
                String text = I18n.format("tc.addwarptemp");
                if(message.data < 0) {
                    text = I18n.format("tc.removewarptemp");
                }

                PlayerNotifications.addNotification(text);
            }
        }

        return null;
    }
}
