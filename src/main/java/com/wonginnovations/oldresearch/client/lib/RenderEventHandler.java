package com.wonginnovations.oldresearch.client.lib;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import truetyper.FontLoader;
import truetyper.TrueTypeFont;

public class RenderEventHandler {
    TrueTypeFont font = null;
    @SideOnly(Side.CLIENT)
    public REHNotifyHandler notifyHandler;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderOverlay(RenderGameOverlayEvent event) {
        if(this.font == null) {
            this.font = FontLoader.loadSystemFont("Arial", 12.0F, true);
        }

        Minecraft mc = Minecraft.getMinecraft();
        long time = System.nanoTime() / 1000000L;

        if(this.notifyHandler == null) {
            this.notifyHandler = new REHNotifyHandler();
        }

        if(event.getType() == RenderGameOverlayEvent.ElementType.TEXT) {
            this.notifyHandler.handleNotifications(mc, time, event);
        }

    }
}
