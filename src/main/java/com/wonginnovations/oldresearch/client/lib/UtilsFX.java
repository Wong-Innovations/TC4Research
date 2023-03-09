package com.wonginnovations.oldresearch.client.lib;

import com.wonginnovations.oldresearch.core.mixin.GuiAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class UtilsFX extends thaumcraft.client.lib.UtilsFX {

    public static void bindTexture(String texture) {
        ResourceLocation rl = new ResourceLocation("oldresearch", texture);

        Minecraft.getMinecraft().renderEngine.bindTexture(rl);
    }

    public static void bindTexture(ResourceLocation rl) {
        Minecraft.getMinecraft().renderEngine.bindTexture(rl);
    }

    public static int getGuiXSize(GuiContainer gui) {
        return gui.getXSize();
    }

    public static int getGuiYSize(GuiContainer gui) {
        return gui.getYSize();
    }

    public static float getGuiZLevel(Gui gui) {
        return ((GuiAccessor) gui).getZLevel();
    }

    public static void drawCustomTooltip(GuiScreen gui, RenderItem itemRenderer, FontRenderer fr, List<String> list, int par2, int par3, int subTipColor) {
        GL11.glDisable(32836);
        GL11.glDisable(2929);
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
        GL11.glEnable(2929);
    }

    public static void renderAnimatedQuadStrip(float scale, float alpha, int frames, int strip, int cframe, float partialTicks, int color) {
        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            Tessellator tessellator = Tessellator.instance;
            tessellator.startDrawingQuads();
            tessellator.setBrightness(220);
            tessellator.setColorRGBA_I(color, (int)(alpha * 255.0F));
            float f2 = (float)cframe / (float)frames;
            float f3 = (float)(cframe + 1) / (float)frames;
            float f4 = (float)strip / (float)frames;
            float f5 = (float)(strip + 1) / (float)frames;
            tessellator.setNormal(0.0F, 0.0F, -1.0F);
            tessellator.addVertexWithUV(-0.5D * (double)scale, 0.5D * (double)scale, 0.0D, (double)f2, (double)f5);
            tessellator.addVertexWithUV(0.5D * (double)scale, 0.5D * (double)scale, 0.0D, (double)f3, (double)f5);
            tessellator.addVertexWithUV(0.5D * (double)scale, -0.5D * (double)scale, 0.0D, (double)f3, (double)f4);
            tessellator.addVertexWithUV(-0.5D * (double)scale, -0.5D * (double)scale, 0.0D, (double)f2, (double)f4);
            tessellator.draw();
        }
    }

}
