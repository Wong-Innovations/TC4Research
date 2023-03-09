package com.wonginnovations.oldresearch.client.lib;

import com.wonginnovations.oldresearch.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.client.fx.ParticleEngine;

import java.awt.*;
import java.util.ArrayList;

public class REHNotifyHandler {
    public void handleNotifications(Minecraft mc, long time, RenderGameOverlayEvent event) {
        if(PlayerNotifications.getListAndUpdate(time).size() > 0) {
            this.renderNotifyHUD(event.getResolution().getScaledWidth_double(), event.getResolution().getScaledHeight_double(), time);
        }

        if(PlayerNotifications.getAspectListAndUpdate(time).size() > 0) {
            this.renderAspectHUD(event.getResolution().getScaledWidth_double(), event.getResolution().getScaledHeight_double(), time);
        }

    }

    @SideOnly(Side.CLIENT)
    public void renderNotifyHUD(double sw, double sh, long time) {
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glPushMatrix();
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, sw, sh, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glDisable(3008);
        GL11.glEnable(2881);
        GL11.glHint(3155, 4354);
        int k = (int)sw;
        int l = (int)sh;
        ArrayList<PlayerNotifications.Notification> notifications = PlayerNotifications.getListAndUpdate(time);
        int entry = 0;

        for(float shift = -8.0F; entry < notifications.size() && entry < ModConfig.notificationMax; ++entry) {
            PlayerNotifications.Notification li = notifications.get(entry);
            String text = li.text;
            int size = mc.fontRenderer.getStringWidth(text) / 2;
            int alpha = 255;
            if(entry == notifications.size() - 1 && li.created > time) {
                alpha = 255 - (int)((float)(li.created - time) / (float)(ModConfig.notificationDelay / 4) * 240.0F);
            }

            if(li.expire < time + (long)ModConfig.notificationDelay) {
                alpha = (int)(255.0F - (float)(time + (long) ModConfig.notificationDelay - li.expire) / (float)ModConfig.notificationDelay * 240.0F);
                shift = -8.0F * ((float)alpha / 255.0F);
            }

            int color = (alpha / 2 << 24) + 16711680 + '\uff00' + 255;
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glTranslatef((float)(k - size - 10), (float)(l - entry * 8) + shift, 0.0F);
            GL11.glScalef(0.5F, 0.5F, 0.5F);
            mc.ingameGUI.drawString(mc.fontRenderer, text, -4, -8, color);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            if(li.image != null) {
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(k - 9), (float)(l - entry * 8) + shift - 6.0F, 0.0F);
                GL11.glScalef(0.03125F, 0.03125F, 0.03125F);
                mc.renderEngine.bindTexture(li.image);
                Tessellator tessellator = Tessellator.instance;
                Color c = new Color(li.color);
                GL11.glColor4f((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, (float)alpha / 511.0F);
                UtilsFX.drawTexturedQuad(0, 0, 0, 0, 256, 256, -90.0D);
                GL11.glPopMatrix();
            }

            if(entry == notifications.size() - 1 && li.created > time) {
                float scale = (float)(li.created - time) / (float)(ModConfig.notificationDelay / 4);
                alpha = 255 - (int)(scale * 240.0F);
                GL11.glPushMatrix();
                GL11.glTranslatef((float)(k - 5) - 8.0F * scale - (1.0F - scale) * (1.0F - scale) * (1.0F - scale) * (float)size * 3.0F, (float)(l - entry * 8) + shift - 2.0F - 8.0F * scale, 0.0F);
                GL11.glScalef(scale, scale, scale);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F - (float)alpha / 511.0F);
                UtilsFX.bindTexture(ParticleEngine.particleTexture);
                int px = 16 * ((mc.player.ticksExisted + entry * 3) % 16);
                UtilsFX.drawTexturedQuad(0, 0, px, 80, 16, 16, (double)(-90 - notifications.size()));
                GL11.glPopMatrix();
            }
        }

        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glDisable(2881);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }

    @SideOnly(Side.CLIENT)
    public void renderAspectHUD(double sw, double sh, long time) {
        Minecraft mc = Minecraft.getMinecraft();
        GL11.glPushMatrix();
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, sw, sh, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
        GL11.glDisable(2929);
        GL11.glDepthMask(false);
        GL11.glDisable(3008);
        GL11.glEnable(2881);
        GL11.glHint(3155, 4354);
        int k = (int)sw;
        int l = (int)sh;
        float mainAlpha = 0.0F;
        ArrayList<PlayerNotifications.AspectNotification> notifications = PlayerNotifications.getAspectListAndUpdate(time);
        int entry = 0;

        for(float shift = -8.0F; entry < notifications.size(); ++entry) {
            PlayerNotifications.AspectNotification li = notifications.get(entry);
            if(li.created <= time) {
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                if(li.aspect.getImage() != null) {
                    GL11.glPushMatrix();
                    int startX = (int)(sw * (double)li.startX);
                    int startY = (int)(sh * (double)li.startY);
                    int endY = -8;
                    int bezierX = (int)((float)k * (0.25F + li.startX));
                    int bezierY = (int)((float)l * li.startY);
                    double t = (double)(time - li.created) / (double)(li.expire - li.created);
                    double x = (1.0D - t) * (1.0D - t) * (double)startX + 2.0D * (1.0D - t) * t * (double)bezierX + t * t * (double)k;
                    double y = (1.0D - t) * (1.0D - t) * (double)startY + 2.0D * (1.0D - t) * t * (double)bezierY + t * t * (double)endY;
                    float alpha = 1.0F;
                    if(t < 0.30000001192092896D) {
                        alpha = (float)(t / 0.30000001192092896D);
                    } else if(t > 0.6600000262260437D) {
                        alpha = (float)(1.0D - (t - 0.6600000262260437D) / 0.3400000035762787D);
                    }

                    if(alpha > mainAlpha) {
                        mainAlpha = alpha;
                    }

                    GL11.glTranslated(x, y, 0.0D);
                    GL11.glScaled(0.075F * alpha, 0.075D * (double)alpha, 0.075D * (double)alpha);
                    mc.renderEngine.bindTexture(li.aspect.getImage());
                    Color c = new Color(li.aspect.getColor());
                    GL11.glColor4f((float)c.getRed() / 255.0F, (float)c.getGreen() / 255.0F, (float)c.getBlue() / 255.0F, alpha * 0.66F);
                    UtilsFX.drawTexturedQuad(0, 0, 0, 0, 256, 256, -90.0D);
                    GL11.glPopMatrix();
                }
            }
        }

        if(mainAlpha > 0.0F) {
            try {
                GL11.glPushMatrix();
                UtilsFX.bindTexture("textures/items/thaumonomicon.png");
                GL11.glColor4f(1.0F, 1.0F, 1.0F, mainAlpha);
                GL11.glTranslated((double)(k - 16), 0.0D, 0.0D);
                GL11.glScaled(0.0625D, 0.0625D, 0.0625D);
                UtilsFX.drawTexturedQuad(0, 0, 0, 0, 256, 256, -90.0D);
                GL11.glPopMatrix();
            } catch (Exception ignored) {}
        }

        GL11.glDepthMask(true);
        GL11.glEnable(2929);
        GL11.glDisable(3042);
        GL11.glDisable(2881);
        GL11.glEnable(3008);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPopMatrix();
    }
}

