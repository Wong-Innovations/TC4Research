package com.wonginnovations.oldresearch.client.gui;

import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.client.lib.UtilsFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.util.ArrayList;

@SideOnly(Side.CLIENT)
public class GuiResearchPopup extends Gui {
    private Minecraft theGame;
    private int windowWidth;
    private int windowHeight;
    private ArrayList<ResearchItem> theResearch = new ArrayList<>();
    private long researchTime;
    private RenderItem itemRender;
    private static final ResourceLocation texture = new ResourceLocation("textures/gui/achievement/achievement_background.png");

    public GuiResearchPopup(Minecraft par1Minecraft) {
        this.theGame = par1Minecraft;
        this.itemRender = par1Minecraft.getRenderItem();
    }

    public void queueResearchInformation(ResearchItem research) {
        if(this.researchTime == 0L) {
            this.researchTime = Minecraft.getSystemTime();
        }

        this.theResearch.add(research);
        OldGuiResearchBrowser.lastX = research.displayColumn;
        OldGuiResearchBrowser.lastY = research.displayRow;
    }

    private void updateResearchWindowScale() {
        GL11.glViewport(0, 0, this.theGame.displayWidth, this.theGame.displayHeight);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        this.windowWidth = this.theGame.displayWidth;
        this.windowHeight = this.theGame.displayHeight;
        ScaledResolution var1 = new ScaledResolution(Minecraft.getMinecraft());
        this.windowWidth = var1.getScaledWidth();
        this.windowHeight = var1.getScaledHeight();
        GL11.glClear(256);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0D, this.windowWidth, this.windowHeight, 0.0D, 1000.0D, 3000.0D);
        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        GL11.glTranslatef(0.0F, 0.0F, -2000.0F);
    }

    public void updateResearchWindow() {
        if(this.theResearch.size() > 0 && this.researchTime != 0L) {
            double var1 = (double)(Minecraft.getSystemTime() - this.researchTime) / 3000.0D;
            if(var1 >= 0.0D && var1 <= 1.0D) {
                this.updateResearchWindowScale();
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                double var3 = var1 * 2.0D;
                if(var3 > 1.0D) {
                    var3 = 2.0D - var3;
                }

                var3 = var3 * 4.0D;
                var3 = 1.0D - var3;
                if(var3 < 0.0D) {
                    var3 = 0.0D;
                }

                var3 = var3 * var3;
                var3 = var3 * var3;
                int var5 = 0;
                int var6 = -(int) (var3 * 36.0D);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glEnable(3553);
                this.theGame.getTextureManager().bindTexture(texture);
                GL11.glDisable(2896);
                this.drawTexturedModalRect(var5, var6, 96, 202, 160, 32);
                this.theGame.fontRenderer.drawString("Research Completed!", var5 + 30, var6 + 7, -256);
                int offset = this.theGame.fontRenderer.getStringWidth(this.theResearch.get(0).getName());
                if(offset <= 125) {
                    this.theGame.fontRenderer.drawString(this.theResearch.get(0).getName(), var5 + 30, var6 + 18, -1);
                } else {
                    float vv = 125.0F / (float)offset;
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)(var5 + 30), (float)(var6 + 16) + 2.0F / vv, 0.0F);
                    GL11.glScalef(vv, vv, vv);
                    this.theGame.fontRenderer.drawString(this.theResearch.get(0).getName(), 0, 0, -1);
                    GL11.glPopMatrix();
                }

                GL11.glDepthMask(true);
                GL11.glEnable(2929);
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glDisable(2896);
                GL11.glEnable(32836);
                GL11.glEnable(2903);
                GL11.glEnable(2896);
                if(this.theResearch.get(0).icon_item != null) {
//                    this.itemRender.renderItemIntoGUI(Minecraft.getMinecraft().fontRenderer, Minecraft.getMinecraft().renderEngine, InventoryUtils.cycleItemStack(((ResearchItem)this.theResearch.get(0)).icon_item), var5 + 8, var6 + 8);
                    this.itemRender.renderItemIntoGUI(InventoryUtils.cycleItemStack(this.theResearch.get(0).icon_item), var5 + 8, var6 + 8);
                } else if(this.theResearch.get(0).icon_resource != null) {
                    Minecraft.getMinecraft().renderEngine.bindTexture(this.theResearch.get(0).icon_resource);
                    UtilsFX.drawTexturedQuadFull(var5 + 8, var6 + 8, this.zLevel);
                }

                GL11.glDisable(2896);
            } else {
                this.theResearch.remove(0);
                if(this.theResearch.size() > 0) {
                    this.researchTime = Minecraft.getSystemTime();
                } else {
                    this.researchTime = 0L;
                }
            }
        }

    }
}
