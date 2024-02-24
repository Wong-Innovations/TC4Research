package com.wonginnovations.oldresearch.client.lib;

import com.wonginnovations.oldresearch.core.mixin.UtilsFXAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.common.config.ModConfig;

import java.awt.*;
import java.util.List;

public class UtilsFX extends thaumcraft.client.lib.UtilsFX {

    public static void bindTexture(String texture) {
        ResourceLocation rl = new ResourceLocation("oldresearch", texture);

        Minecraft.getMinecraft().renderEngine.bindTexture(rl);
    }

    public static void bindTexture(ResourceLocation rl) {
        Minecraft.getMinecraft().renderEngine.bindTexture(rl);
    }

    public static void drawCustomTooltip(GuiScreen gui, RenderItem itemRenderer, FontRenderer fr, List<String> list, int par2, int par3, int subTipColor) {
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableDepth();
        if(!list.isEmpty()) {
            int var5 = 0;

            for(String var7 : list) {
                int var8 = fr.getStringWidth(var7);
                if(var8 > var5) {
                    var5 = var8;
                }
            }

            int x = par2 + 12;
            int y = par3 - 12;
            int var9 = 8;
            if(list.size() > 1) {
                var9 += 2 + (list.size() - 1) * 10;
            }

            itemRenderer.zLevel = 300.0F;
            int var10 = -267386864;
            drawGradientRect(x - 3, y - 4, x + var5 + 3, y - 3, var10, var10);
            drawGradientRect(x - 3, y + var9 + 3, x + var5 + 3, y + var9 + 4, var10, var10);
            drawGradientRect(x - 3, y - 3, x + var5 + 3, y + var9 + 3, var10, var10);
            drawGradientRect(x - 4, y - 3, x - 3, y + var9 + 3, var10, var10);
            drawGradientRect(x + var5 + 3, y - 3, x + var5 + 4, y + var9 + 3, var10, var10);
            int var11 = 1347420415;
            int var12 = (var11 & 16711422) >> 1 | var11 & -16777216;
            drawGradientRect(x - 3, y - 3 + 1, x - 3 + 1, y + var9 + 3 - 1, var11, var12);
            drawGradientRect(x + var5 + 2, y - 3 + 1, x + var5 + 3, y + var9 + 3 - 1, var11, var12);
            drawGradientRect(x - 3, y - 3, x + var5 + 3, y - 3 + 1, var11, var11);
            drawGradientRect(x - 3, y + var9 + 2, x + var5 + 3, y + var9 + 3, var12, var12);

            for(int i = 0; i < list.size(); ++i) {
                String string = list.get(i);
                if(i == 0) {
                    string = TextFormatting.fromColorIndex(subTipColor) + string;
                } else {
                    string = TextFormatting.GRAY + string;
                }

                fr.drawStringWithShadow(string, x, y, -1);
                if(i == 0) {
                    y += 2;
                }

                y += 10;
            }
        }

        itemRenderer.zLevel = 0.0F;
        GlStateManager.enableDepth();
    }

    public static void renderQuadCenteredFromTexture(String texture, float scale, float red, float green, float blue, int brightness, int blend, float opacity) {
        bindTexture(texture);
        renderQuadCenteredFromTexture(scale, red, green, blue, brightness, blend, opacity);
    }

    public static void renderQuadCenteredFromTexture(ResourceLocation texture, float scale, float red, float green, float blue, int brightness, int blend, float opacity) {
        Minecraft.getMinecraft().renderEngine.bindTexture(texture);
        renderQuadCenteredFromTexture(scale, red, green, blue, brightness, blend, opacity);
    }

    public static void renderQuadCenteredFromTexture(float scale, float red, float green, float blue, int brightness, int blend, float opacity) {
        Tessellator tessellator = Tessellator.instance;
        GlStateManager.scale(scale, scale, scale);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, blend);
        GlStateManager.color(1.0F, 1.0F, 1.0F, opacity);
        tessellator.startDrawingQuads();
        if(brightness > 0) {
            tessellator.setBrightness(brightness);
        }

        tessellator.setColorRGBA_F(red, green, blue, opacity);
        tessellator.addVertexWithUV(-0.5D, 0.5D, 0.0D, 0.0D, 1.0D);
        tessellator.addVertexWithUV(0.5D, 0.5D, 0.0D, 1.0D, 1.0D);
        tessellator.addVertexWithUV(0.5D, -0.5D, 0.0D, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-0.5D, -0.5D, 0.0D, 0.0D, 0.0D);
        tessellator.draw();
        GlStateManager.disableBlend();
    }

    public static void renderFacingStrip(double px, double py, double pz, float angle, float scale, float alpha, int frames, int strip, int frame, float partialTicks, int color) {
        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            Tessellator tessellator = Tessellator.instance;
            float arX = ActiveRenderInfo.getRotationX();
            float arZ = ActiveRenderInfo.getRotationZ();
            float arYZ = ActiveRenderInfo.getRotationYZ();
            float arXY = ActiveRenderInfo.getRotationXY();
            float arXZ = ActiveRenderInfo.getRotationXZ();
            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
            double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            GlStateManager.translate(-iPX, -iPY, -iPZ);
            tessellator.startDrawingQuads();
            tessellator.setBrightness(220);
            tessellator.setColorRGBA_I(color, (int)(alpha * 255.0F));
            Vec3d v1 = new Vec3d(-arX * scale - arYZ * scale, -arXZ * scale, -arZ * scale - arXY * scale);
            Vec3d v2 = new Vec3d(-arX * scale + arYZ * scale, arXZ * scale, -arZ * scale + arXY * scale);
            Vec3d v3 = new Vec3d(arX * scale + arYZ * scale, arXZ * scale, arZ * scale + arXY * scale);
            Vec3d v4 = new Vec3d(arX * scale - arYZ * scale, -arXZ * scale, arZ * scale - arXY * scale);
            if(angle != 0.0F) {
                Vec3d pvec = new Vec3d(iPX, iPY, iPZ);
                Vec3d tvec = new Vec3d(px, py, pz);
                Vec3d qvec = pvec.subtract(tvec).normalize();
                QuadHelper.setAxis(qvec, angle).rotate(v1);
                QuadHelper.setAxis(qvec, angle).rotate(v2);
                QuadHelper.setAxis(qvec, angle).rotate(v3);
                QuadHelper.setAxis(qvec, angle).rotate(v4);
            }

            float f2 = (float)frame / (float)frames;
            float f3 = (float)(frame + 1) / (float)frames;
            float f4 = (float)strip / (float)frames;
            float f5 = ((float)strip + 1.0F) / (float)frames;
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            tessellator.addVertexWithUV(px + v1.x, py + v1.y, pz + v1.z, f3, f5);
            tessellator.addVertexWithUV(px + v2.x, py + v2.y, pz + v2.z, f3, f4);
            tessellator.addVertexWithUV(px + v3.x, py + v3.y, pz + v3.z, f2, f4);
            tessellator.addVertexWithUV(px + v4.x, py + v4.y, pz + v4.z, f2, f5);
            tessellator.draw();
        }

    }

    public static void drawTag(int x, int y, Aspect aspect, float amount, int bonus, double z, int blend, float alpha) {
        drawTag(x, y, aspect, amount, bonus, z, blend, alpha, false);
    }

    public static void drawTag(int x, int y, Aspect aspect, float amt, int bonus, double z) {
        drawTag(x, y, aspect, amt, bonus, z, 771, 1.0F, false);
    }

    public static void drawTag(int x, int y, Aspect aspect, float amount, int bonus, double z, int blend, float alpha, boolean bw) {
        drawTag(x, (double)y, aspect, amount, bonus, z, blend, alpha, bw);
    }

    public static void drawTag(double x, double y, Aspect aspect, float amount, int bonus, double z, int blend, float alpha, boolean bw) {
        if (aspect != null) {
            boolean blendon = GL11.glIsEnabled(3042);
            Minecraft mc = Minecraft.getMinecraft();
            boolean isLightingEnabled = GL11.glIsEnabled(2896);
            Color color = new Color(aspect.getColor());
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.alphaFunc(516, 0.003921569F);
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, blend);
            GlStateManager.pushMatrix();
            mc.renderEngine.bindTexture(aspect.getImage());
            if (!bw) {
                GlStateManager.color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha);
            } else {
                GlStateManager.color(0.1F, 0.1F, 0.1F, alpha * 0.8F);
            }

            net.minecraft.client.renderer.Tessellator var9 = net.minecraft.client.renderer.Tessellator.getInstance();
            var9.getBuffer().begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
            if (!bw) {
                var9.getBuffer().pos(x + 0.0, y + 16.0, z).tex(0.0, 1.0).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha).endVertex();
                var9.getBuffer().pos(x + 16.0, y + 16.0, z).tex(1.0, 1.0).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha).endVertex();
                var9.getBuffer().pos(x + 16.0, y + 0.0, z).tex(1.0, 0.0).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha).endVertex();
                var9.getBuffer().pos(x + 0.0, y + 0.0, z).tex(0.0, 0.0).color((float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, alpha).endVertex();
            } else {
                var9.getBuffer().pos(x + 0.0, y + 16.0, z).tex(0.0, 1.0).color(0.1F, 0.1F, 0.1F, alpha * 0.8F).endVertex();
                var9.getBuffer().pos(x + 16.0, y + 16.0, z).tex(1.0, 1.0).color(0.1F, 0.1F, 0.1F, alpha * 0.8F).endVertex();
                var9.getBuffer().pos(x + 16.0, y + 0.0, z).tex(1.0, 0.0).color(0.1F, 0.1F, 0.1F, alpha * 0.8F).endVertex();
                var9.getBuffer().pos(x + 0.0, y + 0.0, z).tex(0.0, 0.0).color(0.1F, 0.1F, 0.1F, alpha * 0.8F).endVertex();
            }

            var9.draw();
            GlStateManager.popMatrix();
            if (amount > 0.0F) {
                GlStateManager.pushMatrix();
                float q = 0.5F;
                if (!ModConfig.CONFIG_GRAPHICS.largeTagText) {
                    GlStateManager.scale(0.5F, 0.5F, 0.5F);
                    q = 1.0F;
                }

                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                String am = UtilsFXAccessor.getMyFormatter().format(amount);
                int sw = mc.fontRenderer.getStringWidth(am);
                EnumFacing[] var20 = EnumFacing.HORIZONTALS;
                int var21 = var20.length;

                for(int var22 = 0; var22 < var21; ++var22) {
                    EnumFacing e = var20[var22];
                    mc.fontRenderer.drawString(am, (float)(32 - sw + (int)x * 2) * q + (float)e.getXOffset(), (float)(32 - mc.fontRenderer.FONT_HEIGHT + (int)y * 2) * q + (float)e.getZOffset(), 0, false);
                }

                mc.fontRenderer.drawString(am, (float)(32 - sw + (int)x * 2) * q, (float)(32 - mc.fontRenderer.FONT_HEIGHT + (int)y * 2) * q, 16777215, false);
                GlStateManager.popMatrix();
            }

            if (bonus > 0) {
                GlStateManager.pushMatrix();
                mc.renderEngine.bindTexture(new ResourceLocation("oldresearch", "textures/misc/particles.png"));
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int px = 16 * (mc.player.ticksExisted % 16);
                drawTexturedQuad((float)((int)x - 4), (float)((int)y - 4), (float)px, 80.0F, 16.0F, 16.0F, z);
                if (bonus > 1) {
                    float q = 0.5F;
                    if (!ModConfig.CONFIG_GRAPHICS.largeTagText) {
                        GlStateManager.scale(0.5F, 0.5F, 0.5F);
                        q = 1.0F;
                    }

                    String am = "" + bonus;
                    int sw = mc.fontRenderer.getStringWidth(am) / 2;
                    GlStateManager.translate(0.0, 0.0, -1.0);
                    mc.fontRenderer.drawStringWithShadow(am, (float)(8 - sw + (int)x * 2) * q, (float)(15 - mc.fontRenderer.FONT_HEIGHT + (int)y * 2) * q, 16777215);
                }

                GlStateManager.popMatrix();
            }

            GlStateManager.blendFunc(770, 771);
            if (!blendon) {
                GlStateManager.disableBlend();
            }

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.alphaFunc(516, 0.1F);
            if (isLightingEnabled) {
                GlStateManager.enableLighting();
            }

            GlStateManager.popMatrix();
        }
    }

}
