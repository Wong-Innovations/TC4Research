package com.wonginnovations.oldresearch.common.lib.network;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.client.gui.OldGuiResearchBrowser;
import com.wonginnovations.oldresearch.client.lib.ClientTickEventsFML;
import com.wonginnovations.oldresearch.client.lib.PlayerNotifications;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.common.lib.SoundsTC;

import java.util.ArrayList;

public class PacketResearchComplete implements IMessage, IMessageHandler<PacketResearchComplete, IMessage> {
    private String key;

    public PacketResearchComplete() {
    }

    public PacketResearchComplete(String key) {
        this.key = key;
    }

    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeUTF8String(buffer, this.key);
    }

    public void fromBytes(ByteBuf buffer) {
        this.key = ByteBufUtils.readUTF8String(buffer);
    }

    @SideOnly(Side.CLIENT)
    public IMessage onMessage(PacketResearchComplete message, MessageContext ctx) {
        if(message.key != null && message.key.length() > 0) {
            OldResearch.proxy.getResearchManager().completeResearch(Minecraft.getMinecraft().player, message.key);
            if(message.key.startsWith("@")) {
                String text = I18n.format("tc.addclue");
                PlayerNotifications.addNotification("§a" + text);
                Minecraft.getMinecraft().player.playSound(SoundsTC.learn, 0.2F, 1.0F + Minecraft.getMinecraft().player.world.rand.nextFloat() * 0.1F);
            } else if(!ResearchCategories.getResearch(message.key).isVirtual()) {
                ClientTickEventsFML.researchPopup.queueResearchInformation(ResearchCategories.getResearch(message.key));
                OldGuiResearchBrowser.highlightedItem.add(message.key);
                OldGuiResearchBrowser.highlightedItem.add(ResearchCategories.getResearch(message.key).category);
            }

            if(Minecraft.getMinecraft().currentScreen instanceof OldGuiResearchBrowser) {
                ArrayList<String> al = OldGuiResearchBrowser.completedResearch.get(Minecraft.getMinecraft().player.getGameProfile().getName());
                if(al == null) {
                    al = new ArrayList<>();
                }

                al.add(message.key);
                OldGuiResearchBrowser.completedResearch.put(Minecraft.getMinecraft().player.getGameProfile().getName(), al);
                ((OldGuiResearchBrowser)Minecraft.getMinecraft().currentScreen).updateResearch();
            }
        }

        return null;
    }
}
