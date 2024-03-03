package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.client.lib.PlayerNotifications;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.api.aspects.Aspect;

public class PacketAspectPool implements IMessage, IMessageHandler<PacketAspectPool, IMessage> {
    private String key;
    private int amount;
    private int total;
    private static long lastSound = 0L;

    public PacketAspectPool() {
    }

    public PacketAspectPool(String key, int amount, int total) {
        this.key = key;
        this.amount = amount;
        this.total = total;
    }

    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.key);
        buffer.writeInt(this.amount);
        buffer.writeInt(this.total);
    }

    public void fromBytes(ByteBuf buffer) {
        this.key = ByteBufUtils.readUTF8String(buffer);
        this.amount = buffer.readInt();
        this.total = buffer.readInt();
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketAspectPool message, MessageContext ctx) {
        Minecraft.getMinecraft().addScheduledTask(new Runnable() {
            public void run() {
                if (Aspect.getAspect(message.key) != null) {
                    boolean success = OldResearch.proxy.getPlayerKnowledge().setAspectPool(Minecraft.getMinecraft().player.getGameProfile().getName(), Aspect.getAspect(message.key), message.total);
                    if (success && message.amount > 0) {
                        String text = I18n.format("tc.addaspectpool", message.amount + "", Aspect.getAspect(message.key).getName());
                        PlayerNotifications.addNotification(text, Aspect.getAspect(message.key));

                        for (int a = 0; a < message.amount; ++a) {
                            PlayerNotifications.addAspectNotification(Aspect.getAspect(message.key));
                        }

                        if (System.currentTimeMillis() > lastSound) {
                            Minecraft.getMinecraft().player.playSound(new SoundEvent(new ResourceLocation("entity.experience_orb.pickup")), 0.1F, 0.9F + Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.2F);
                            lastSound = System.currentTimeMillis() + 100L;
                        }
                    }
                }
            }
        });
        return null;
    }
}

