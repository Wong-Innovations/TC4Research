package com.wonginnovations.oldresearch.client.gui;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchCategoryList;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.client.lib.Tessellator;
import com.wonginnovations.oldresearch.client.lib.UtilsFX;
import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketPlayerCompleteToServer;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import com.wonginnovations.oldresearch.config.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.ThaumcraftApi;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.io.IOException;
import java.util.*;

@SideOnly(Side.CLIENT)
public class GuiResearchBrowser extends GuiScreen {
    private static int guiMapTop;
    private static int guiMapLeft;
    private static int guiMapBottom;
    private static int guiMapRight;
    protected int paneWidth = 256;
    protected int paneHeight = 230;
    protected int mouseX = 0;
    protected int mouseY = 0;
    protected double field_74117_m;
    protected double field_74115_n;
    protected double guiMapX;
    protected double guiMapY;
    protected double field_74124_q;
    protected double field_74123_r;
    private int isMouseButtonDown = 0;
    public static int lastX = -5;
    public static int lastY = -6;
    private GuiButton button;
    private LinkedList<ResearchItem> research = new LinkedList<>();
    public static HashMap<String, ArrayList<String>> completedResearch = new HashMap<>();
    public static ArrayList<String> highlightedItem = new ArrayList<>();
    private static String selectedCategory = null;
    private FontRenderer galFontRenderer;
    private ResearchItem currentHighlight = null;
    private String player = "";
    long popuptime = 0L;
    String popupmessage = "";
    public boolean hasScribestuff = false;

    public GuiResearchBrowser() {
        short var2 = 141;
        short var3 = 141;
        this.field_74117_m = this.guiMapX = this.field_74124_q = lastX * 24 - var2 / 2 - 12;
        this.field_74115_n = this.guiMapY = this.field_74123_r = lastY * 24 - var3 / 2;
        this.updateResearch();
        this.galFontRenderer = FMLClientHandler.instance().getClient().standardGalacticFontRenderer;
        this.player = Minecraft.getMinecraft().player.getGameProfile().getName();
    }

    public GuiResearchBrowser(double x, double y) {
        this.field_74117_m = this.guiMapX = this.field_74124_q = x;
        this.field_74115_n = this.guiMapY = this.field_74123_r = y;
        this.updateResearch();
        this.galFontRenderer = FMLClientHandler.instance().getClient().standardGalacticFontRenderer;
        this.player = Minecraft.getMinecraft().player.getGameProfile().getName();
    }

    public void updateResearch() {
        if(this.mc == null) {
            this.mc = Minecraft.getMinecraft();
        }

        this.research.clear();
        this.hasScribestuff = false;
        if(selectedCategory == null) {
            Collection<String> cats = ResearchCategories.researchCategories.keySet();
            selectedCategory = cats.iterator().next();
        }

        this.research.addAll(ResearchCategories.getResearchList(selectedCategory).research.values());

        if(ResearchManager.consumeInkFromPlayer(this.mc.player, false) && InventoryUtils.isPlayerCarryingAmount(this.mc.player, new ItemStack(Items.PAPER), true)) {
            this.hasScribestuff = true;
        }

        guiMapTop = ResearchCategories.getResearchList(selectedCategory).minDisplayColumn * 24 - 85;
        guiMapLeft = ResearchCategories.getResearchList(selectedCategory).minDisplayRow * 24 - 112;
        guiMapBottom = ResearchCategories.getResearchList(selectedCategory).maxDisplayColumn * 24 - 112;
        guiMapRight = ResearchCategories.getResearchList(selectedCategory).maxDisplayRow * 24 - 61;
    }

    public void onGuiClosed() {
        short var2 = 141;
        short var3 = 141;
        lastX = (int)((this.guiMapX + (double)(var2 / 2) + 12.0D) / 24.0D);
        lastY = (int)((this.guiMapY + (double)(var3 / 2)) / 24.0D);
        super.onGuiClosed();
    }

    public void initGui() {
    }

    protected void actionPerformed(GuiButton par1GuiButton) {
        try {
            super.actionPerformed(par1GuiButton);
        } catch (IOException ignored) {}
    }

    protected void keyTyped(char par1, int par2) {
        if(par2 == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
            highlightedItem.clear();
            this.mc.displayGuiScreen(null);
            this.mc.setIngameFocus();
        } else {
            if(par2 == 1) {
                highlightedItem.clear();
            }

            try {
                super.keyTyped(par1, par2);
            } catch (IOException ignored) {}
        }

    }

    public void drawScreen(int mx, int my, float par3) {
        int var4 = (this.width - this.paneWidth) / 2;
        int var5 = (this.height - this.paneHeight) / 2;
        if(Mouse.isButtonDown(0)) {
            int var6 = var4 + 8;
            int var7 = var5 + 17;
            if((this.isMouseButtonDown == 0 || this.isMouseButtonDown == 1) && mx >= var6 && mx < var6 + 224 && my >= var7 && my < var7 + 196) {
                if(this.isMouseButtonDown == 0) {
                    this.isMouseButtonDown = 1;
                } else {
                    this.guiMapX -= mx - this.mouseX;
                    this.guiMapY -= my - this.mouseY;
                    this.field_74124_q = this.field_74117_m = this.guiMapX;
                    this.field_74123_r = this.field_74115_n = this.guiMapY;
                }

                this.mouseX = mx;
                this.mouseY = my;
            }

            if(this.field_74124_q < (double)guiMapTop) {
                this.field_74124_q = guiMapTop;
            }

            if(this.field_74123_r < (double)guiMapLeft) {
                this.field_74123_r = guiMapLeft;
            }

            if(this.field_74124_q >= (double)guiMapBottom) {
                this.field_74124_q = guiMapBottom - 1;
            }

            if(this.field_74123_r >= (double)guiMapRight) {
                this.field_74123_r = guiMapRight - 1;
            }
        } else {
            this.isMouseButtonDown = 0;
        }

        this.drawDefaultBackground();
        this.genResearchBackground(mx, my, par3);
        if(this.popuptime > System.currentTimeMillis()) {
            int xq = var4 + 128;
            int yq = var5 + 128;
            int var41 = this.fontRenderer.FONT_HEIGHT * this.fontRenderer.listFormattedStringToWidth(this.popupmessage, 150).size() / 2;
            this.drawGradientRect(xq - 78, yq - var41 - 3, xq + 78, yq + var41 + 3, -1073741824, -1073741824);
            this.fontRenderer.drawSplitString(this.popupmessage, xq - 75, yq - var41, 150, -7302913);
        }

        Collection<String> cats = ResearchCategories.researchCategories.keySet();
        int count = 0;
        boolean swop = false;

        for(String obj : cats) {
            if(count == 9) {
                count = 0;
                swop = true;
            }

            ResearchCategoryList rcl = ResearchCategories.getResearchList(obj);
            if(!obj.equals("ELDRITCH") || ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR")) {
                int mposx = mx - (var4 - 24 + (swop?280:0));
                int mposy = my - (var5 + count * 24);
                if(mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
                    this.fontRenderer.drawStringWithShadow(ResearchCategories.getCategoryName(obj), mx, my - 8, 16777215);
                }

                ++count;
            }
        }

    }

    public void updateScreen() {
        this.field_74117_m = this.guiMapX;
        this.field_74115_n = this.guiMapY;
        double var1 = this.field_74124_q - this.guiMapX;
        double var3 = this.field_74123_r - this.guiMapY;
        if(var1 * var1 + var3 * var3 < 4.0D) {
            this.guiMapX += var1;
            this.guiMapY += var3;
        } else {
            this.guiMapX += var1 * 0.85D;
            this.guiMapY += var3 * 0.85D;
        }

    }

    protected void genResearchBackground(int par1, int par2, float par3) {
        long t = System.nanoTime() / 50000000L;
        int var4 = OldResearchUtils.floor_double(this.field_74117_m + (this.guiMapX - this.field_74117_m) * (double)par3);
        int var5 = OldResearchUtils.floor_double(this.field_74115_n + (this.guiMapY - this.field_74115_n) * (double)par3);
        if(var4 < guiMapTop) {
            var4 = guiMapTop;
        }

        if(var5 < guiMapLeft) {
            var5 = guiMapLeft;
        }

        if(var4 >= guiMapBottom) {
            var4 = guiMapBottom - 1;
        }

        if(var5 >= guiMapRight) {
            var5 = guiMapRight - 1;
        }

        int var8 = (this.width - this.paneWidth) / 2;
        int var9 = (this.height - this.paneHeight) / 2;
        int var10 = var8 + 16;
        int var11 = var9 + 17;
        this.zLevel = 0.0F;
        GL11.glDepthFunc(518);
        GL11.glPushMatrix();
        GL11.glTranslatef(0.0F, 0.0F, -200.0F);
        GL11.glEnable(3553);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glDisable(2896);
        GL11.glEnable(32836);
        GL11.glEnable(2903);
        GL11.glPushMatrix();
        GL11.glScalef(2.0F, 2.0F, 1.0F);
        int vx = (int)((float)(var4 - guiMapTop) / (float)Math.abs(guiMapTop - guiMapBottom) * 288.0F);
        int vy = (int)((float)(var5 - guiMapLeft) / (float)Math.abs(guiMapLeft - guiMapRight) * 316.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getMinecraft().renderEngine.bindTexture(ResearchCategories.getResearchList(selectedCategory).background);
        this.drawTexturedModalRect(var10 / 2, var11 / 2, vx / 2, vy / 2, 112, 98);
        GL11.glScalef(0.5F, 0.5F, 1.0F);
        GL11.glPopMatrix();
        GL11.glEnable(2929);
        GL11.glDepthFunc(515);
        if(completedResearch.get(this.player) != null) {
            for(int var22 = 0; var22 < this.research.size(); ++var22) {
                ResearchItem var33 = this.research.get(var22);
                if(var33.parents != null && var33.parents.length > 0) {
                    for(int a = 0; a < var33.parents.length; ++a) {
                        if(var33.parents[a] != null && ResearchCategories.getResearch(var33.parents[a]).category.equals(selectedCategory)) {
                            ResearchItem parent = ResearchCategories.getResearch(var33.parents[a]);
                            if(!parent.isVirtual()) {
                                int var24 = var33.displayColumn * 24 - var4 + 11 + var10;
                                int var25 = var33.displayRow * 24 - var5 + 11 + var11;
                                int var26 = parent.displayColumn * 24 - var4 + 11 + var10;
                                int var27 = parent.displayRow * 24 - var5 + 11 + var11;
                                boolean var28 = completedResearch.get(this.player).contains(var33.key);
                                boolean var29 = completedResearch.get(this.player).contains(parent.key);
                                int var30 = Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) > 0.6D?255:130;
                                if(var28) {
                                    this.drawLine(var24, var25, var26, var27, 0.1F, 0.1F, 0.1F, par3, false);
                                } else if(!var33.isLost() && (!var33.isHidden() && !var33.isLost() || completedResearch.get(this.player).contains("@" + var33.key)) && (!var33.isConcealed() || this.canUnlockResearch(var33))) {
                                    if(var29) {
                                        this.drawLine(var24, var25, var26, var27, 0.0F, 1.0F, 0.0F, par3, true);
                                    } else if((!parent.isHidden() && !var33.isLost() || completedResearch.get(this.player).contains("@" + parent.key)) && (!parent.isConcealed() || this.canUnlockResearch(parent))) {
                                        this.drawLine(var24, var25, var26, var27, 0.0F, 0.0F, 1.0F, par3, true);
                                    }
                                }
                            }
                        }
                    }
                }

                if(var33.siblings != null && var33.siblings.length > 0) {
                    for(int a = 0; a < var33.siblings.length; ++a) {
                        if(var33.siblings[a] != null && ResearchCategories.getResearch(var33.siblings[a]).category.equals(selectedCategory)) {
                            ResearchItem sibling = ResearchCategories.getResearch(var33.siblings[a]);
                            if(!sibling.isVirtual() && (sibling.parents == null || !Arrays.asList(sibling.parents).contains(var33.key))) {
                                int var24 = var33.displayColumn * 24 - var4 + 11 + var10;
                                int var25 = var33.displayRow * 24 - var5 + 11 + var11;
                                int var26 = sibling.displayColumn * 24 - var4 + 11 + var10;
                                int var27 = sibling.displayRow * 24 - var5 + 11 + var11;
                                boolean var28 = completedResearch.get(this.player).contains(var33.key);
                                boolean var29 = completedResearch.get(this.player).contains(sibling.key);
                                if(var28) {
                                    this.drawLine(var24, var25, var26, var27, 0.1F, 0.1F, 0.2F, par3, false);
                                } else if(!var33.isLost() && (!var33.isHidden() || completedResearch.get(this.player).contains("@" + var33.key)) && (!var33.isConcealed() || this.canUnlockResearch(var33))) {
                                    if(var29) {
                                        this.drawLine(var24, var25, var26, var27, 0.0F, 1.0F, 0.0F, par3, true);
                                    } else if((!sibling.isHidden() || completedResearch.get(this.player).contains("@" + sibling.key)) && (!sibling.isConcealed() || this.canUnlockResearch(sibling))) {
                                        this.drawLine(var24, var25, var26, var27, 0.0F, 0.0F, 1.0F, par3, true);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        this.currentHighlight = null;
        RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
        boolean renderWithColor = true;
        GL11.glEnable(32836);
        GL11.glEnable(2903);
        if(completedResearch.get(this.player) != null) {
            for(int var24 = 0; var24 < this.research.size(); ++var24) {
                ResearchItem var35 = this.research.get(var24);
                int var26 = var35.displayColumn * 24 - var4;
                int var27 = var35.displayRow * 24 - var5;
                if(!var35.isVirtual() && var26 >= -24 && var27 >= -24 && var26 <= 224 && var27 <= 196) {
                    int var42 = var10 + var26;
                    int var41 = var11 + var27;
                    if(completedResearch.get(this.player).contains(var35.key)) {
                        if(OldResearchApi.getWarp(var35.key) > 0) {
                            this.drawForbidden(var42 + 11, var41 + 11);
                        }

                        float var38 = 1.0F;
                        GL11.glColor4f(var38, var38, var38, 1.0F);
                    } else {
                        if(!completedResearch.get(this.player).contains("@" + var35.key) && (var35.isLost() || var35.isHidden() && !completedResearch.get(this.player).contains("@" + var35.key) || var35.isConcealed() && !this.canUnlockResearch(var35))) {
                            continue;
                        }

                        if(OldResearchApi.getWarp(var35.key) > 0) {
                            this.drawForbidden(var42 + 11, var41 + 11);
                        }

                        if(this.canUnlockResearch(var35)) {
                            float var38 = (float)Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) * 0.25F + 0.75F;
                            GL11.glColor4f(var38, var38, var38, 1.0F);
                        } else {
                            float var38 = 0.3F;
                            GL11.glColor4f(var38, var38, var38, 1.0F);
                        }
                    }

                    UtilsFX.bindTexture("textures/gui/gui_research.png");
                    GL11.glEnable(2884);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    if(var35.isRound()) {
                        this.drawTexturedModalRect(var42 - 2, var41 - 2, 54, 230, 26, 26);
                    } else if(var35.isHidden()) {
                        if(ModConfig.researchDifficulty != -1 && (ModConfig.researchDifficulty != 0 || !var35.isSecondary())) {
                            this.drawTexturedModalRect(var42 - 2, var41 - 2, 86, 230, 26, 26);
                        } else {
                            this.drawTexturedModalRect(var42 - 2, var41 - 2, 230, 230, 26, 26);
                        }
                    } else if(ModConfig.researchDifficulty != -1 && (ModConfig.researchDifficulty != 0 || !var35.isSecondary())) {
                        this.drawTexturedModalRect(var42 - 2, var41 - 2, 0, 230, 26, 26);
                    } else {
                        this.drawTexturedModalRect(var42 - 2, var41 - 2, 110, 230, 26, 26);
                    }

                    if(var35.isSpecial()) {
                        this.drawTexturedModalRect(var42 - 2, var41 - 2, 26, 230, 26, 26);
                    }

                    if(!this.canUnlockResearch(var35)) {
                        float var40 = 0.1F;
                        GL11.glColor4f(var40, var40, var40, 1.0F);
                        renderWithColor = false;
                    }

                    GL11.glDisable(3042);
                    if(highlightedItem.contains(var35.key)) {
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                        this.mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
                        int px = (int)(t % 16L) * 16;
                        GL11.glTranslatef((float)(var42 - 5), (float)(var41 - 5), 0.0F);
                        UtilsFX.drawTexturedQuad(0, 0, px, 80, 16, 16, 0.0D);
                        GL11.glDisable(3042);
                        GL11.glPopMatrix();
                    }

                    if(var35.icon_item != null) {
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        RenderHelper.enableGUIStandardItemLighting();
                        GL11.glDisable(2896);
                        GL11.glEnable(32836);
                        GL11.glEnable(2903);
                        GL11.glEnable(2896);
                        itemRenderer.renderItemAndEffectIntoGUI(InventoryUtils.cycleItemStack(var35.icon_item), var42 + 3, var41 + 3);
                        GL11.glDisable(2896);
                        GL11.glDepthMask(true);
                        GL11.glEnable(2929);
                        GL11.glDisable(3042);
                        GL11.glPopMatrix();
                    } else if(var35.icon_resource != null) {
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 771);
                        this.mc.renderEngine.bindTexture(var35.icon_resource);
                        if(!renderWithColor) {
                            GL11.glColor4f(0.2F, 0.2F, 0.2F, 1.0F);
                        }

                        UtilsFX.drawTexturedQuadFull(var42 + 3, var41 + 3, this.zLevel);
                        GL11.glPopMatrix();
                    }

                    if(!this.canUnlockResearch(var35)) {
                        renderWithColor = true;
                    }

                    if(par1 >= var10 && par2 >= var11 && par1 < var10 + 224 && par2 < var11 + 196 && par1 >= var42 && par1 <= var42 + 22 && par2 >= var41 && par2 <= var41 + 22) {
                        this.currentHighlight = var35;
                    }

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                }
            }
        }

        GL11.glDisable(2929);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        Collection<String> cats = ResearchCategories.researchCategories.keySet();
        int count = 0;
        boolean swop = false;

        for(String obj : cats) {
            ResearchCategoryList rcl = ResearchCategories.getResearchList(obj);
            if(!(obj).equals("ELDRITCH") || ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR")) {
                GL11.glPushMatrix();
                if(count == 9) {
                    count = 0;
                    swop = true;
                }

                int s0 = !swop?0:264;
                int s1 = 0;
                int s2 = swop?14:0;
                if(!selectedCategory.equals(obj)) {
                    s1 = 24;
                    s2 = swop?6:8;
                }

                UtilsFX.bindTexture("textures/gui/gui_research.png");
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                if(swop) {
                    this.drawTexturedModalRectReversed(var8 + s0 - 8, var9 + count * 24, 176 + s1, 232, 24, 24);
                } else {
                    this.drawTexturedModalRect(var8 - 24 + s0, var9 + count * 24, 152 + s1, 232, 24, 24);
                }

                if(highlightedItem.contains(obj)) {
                    GL11.glPushMatrix();
                    this.mc.renderEngine.bindTexture(ParticleEngine.particleTexture);
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    int px = (int)(16L * (t % 16L));
                    UtilsFX.drawTexturedQuad(var8 - 27 + s2 + s0, var9 - 4 + count * 24, px, 80, 16, 16, -90.0D);
                    GL11.glPopMatrix();
                }

                GL11.glPushMatrix();
                this.mc.renderEngine.bindTexture(rcl.icon);
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                UtilsFX.drawTexturedQuadFull(var8 - 19 + s2 + s0, var9 + 4 + count * 24, -80.0D);
                GL11.glPopMatrix();
                if(!selectedCategory.equals(obj)) {
                    UtilsFX.bindTexture("textures/gui/gui_research.png");
                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                    if(swop) {
                        this.drawTexturedModalRectReversed(var8 + s0 - 8, var9 + count * 24, 224, 232, 24, 24);
                    } else {
                        this.drawTexturedModalRect(var8 - 24 + s0, var9 + count * 24, 200, 232, 24, 24);
                    }
                }

                GL11.glPopMatrix();
                ++count;
            }
        }

        UtilsFX.bindTexture("textures/gui/gui_research.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.drawTexturedModalRect(var8, var9, 0, 0, this.paneWidth, this.paneHeight);
        GL11.glPopMatrix();
        this.zLevel = 0.0F;
        GL11.glDepthFunc(515);
        GL11.glDisable(2929);
        GL11.glEnable(3553);
        super.drawScreen(par1, par2, par3);
        if(completedResearch.get(this.player) != null && this.currentHighlight != null) {
            String var34 = this.currentHighlight.getName();
            int var26 = par1 + 6;
            int var27 = par2 - 4;
            int var99 = 0;
            FontRenderer fr = this.fontRenderer;
            if(!completedResearch.get(this.player).contains(this.currentHighlight.key) && !this.canUnlockResearch(this.currentHighlight)) {
                fr = this.galFontRenderer;
            }

            if(!this.canUnlockResearch(this.currentHighlight)) {
                GL11.glPushMatrix();
                int var42 = (int)Math.max((float)fr.getStringWidth(var34), (float)fr.getStringWidth(I18n.format("tc.researchmissing")) / 1.5F);
                String var39 = I18n.format("tc.researchmissing");
                int var30 = fr.FONT_HEIGHT * fr.listFormattedStringToWidth(var39, var42 * 2).size();
                this.drawGradientRect(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + var30 + 10, -1073741824, -1073741824);
                GL11.glTranslatef((float)var26, (float)(var27 + 12), 0.0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                this.fontRenderer.drawSplitString(var39, 0, 0, var42 * 2, -9416624);
                GL11.glPopMatrix();
            } else {
                boolean secondary = !completedResearch.get(this.player).contains(this.currentHighlight.key) && this.currentHighlight.tags != null && this.currentHighlight.tags.size() > 0 && (ModConfig.researchDifficulty == -1 || ModConfig.researchDifficulty == 0 && this.currentHighlight.isSecondary());
                boolean primary = !secondary && !completedResearch.get(this.player).contains(this.currentHighlight.key);
                int var42 = (int)Math.max((float)fr.getStringWidth(var34), (float)fr.getStringWidth(this.currentHighlight.getText()) / 1.9F);
                int var41 = fr.FONT_HEIGHT * fr.listFormattedStringToWidth(var34, var42).size() + 5;
                if(primary) {
                    var99 += 9;
                    var42 = (int)Math.max((float)var42, (float)fr.getStringWidth(I18n.format("tc.research.shortprim")) / 1.9F);
                }

                if(secondary) {
                    var99 += 29;
                    var42 = (int)Math.max((float)var42, (float)fr.getStringWidth(I18n.format("tc.research.short")) / 1.9F);
                }

                int warp = OldResearchApi.getWarp(this.currentHighlight.key);
                if(warp > 5) {
                    warp = 5;
                }

                String ws = I18n.format("tc.forbidden");
                String wr = I18n.format("tc.forbidden.level." + warp);
                String wte = ws.replaceAll("%n", wr);
                if(OldResearchApi.getWarp(this.currentHighlight.key) > 0) {
                    var99 += 9;
                    var42 = (int)Math.max((float)var42, (float)fr.getStringWidth(wte) / 1.9F);
                }

                this.drawGradientRect(var26 - 3, var27 - 3, var26 + var42 + 3, var27 + var41 + 6 + var99, -1073741824, -1073741824);
                GL11.glPushMatrix();
                GL11.glTranslatef((float)var26, (float)(var27 + var41 - 1), 0.0F);
                GL11.glScalef(0.5F, 0.5F, 0.5F);
                this.fontRenderer.drawStringWithShadow(this.currentHighlight.getText(), 0, 0, -7302913);
                GL11.glPopMatrix();
                if(warp > 0) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)var26, (float)(var27 + var41 + 8), 0.0F);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    this.fontRenderer.drawStringWithShadow(wte, 0, 0, 16777215);
                    GL11.glPopMatrix();
                    var41 += 9;
                }

                GL11.glPushMatrix();
                if(primary) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)var26, (float)(var27 + var41 + 8), 0.0F);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    if(ResearchManager.getResearchSlot(this.mc.player, this.currentHighlight.key) >= 0) {
                        this.fontRenderer.drawStringWithShadow(I18n.format("tc.research.hasnote"), 0, 0, 16753920);
                    } else if(this.hasScribestuff) {
                        this.fontRenderer.drawStringWithShadow(I18n.format("tc.research.getprim"), 0, 0, 8900331);
                    } else {
                        this.fontRenderer.drawStringWithShadow(I18n.format("tc.research.shortprim"), 0, 0, 14423100);
                    }

                    GL11.glPopMatrix();
                } else if(secondary) {
                    boolean enough = true;
                    int cc = 0;

                    for(Aspect a : this.currentHighlight.tags.getAspectsSortedByAmount()) {
                        if(OldResearch.proxy.playerKnowledge.hasDiscoveredAspect(this.player, a)) {
                            float alpha = 1.0F;
                            if(OldResearch.proxy.playerKnowledge.getAspectPoolFor(this.player, a) < this.currentHighlight.tags.getAmount(a)) {
                                alpha = (float)Math.sin((double)(Minecraft.getSystemTime() % 600L) / 600.0D * 3.141592653589793D * 2.0D) * 0.25F + 0.75F;
                                enough = false;
                            }

                            GL11.glPushMatrix();
                            GL11.glPushAttrib(1048575);
                            UtilsFX.drawTag(var26 + cc * 16, var27 + var41 + 8, a, (float)this.currentHighlight.tags.getAmount(a), 0, 0.0D, 771, alpha, false);
                            GL11.glPopAttrib();
                            GL11.glPopMatrix();
                        } else {
                            enough = false;
                            GL11.glPushMatrix();
                            UtilsFX.bindTexture("textures/aspects/_unknown.png");
                            GL11.glColor4f(0.5F, 0.5F, 0.5F, 0.5F);
                            GL11.glTranslated(var26 + cc * 16, var27 + var41 + 8, 0.0D);
                            UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);
                            GL11.glPopMatrix();
                        }

                        ++cc;
                    }

                    GL11.glPushMatrix();
                    GL11.glTranslatef((float)var26, (float)(var27 + var41 + 27), 0.0F);
                    GL11.glScalef(0.5F, 0.5F, 0.5F);
                    if(enough) {
                        this.fontRenderer.drawStringWithShadow(I18n.format("tc.research.purchase"), 0, 0, 8900331);
                    } else {
                        this.fontRenderer.drawStringWithShadow(I18n.format("tc.research.short"), 0, 0, 14423100);
                    }

                    GL11.glPopMatrix();
                }

                GL11.glPopMatrix();
            }

            fr.drawStringWithShadow(var34, var26, var27, this.canUnlockResearch(this.currentHighlight)?(this.currentHighlight.isSpecial()?-128:-1):(this.currentHighlight.isSpecial()?-8355776:-8355712));
        }

        GL11.glEnable(2929);
        GL11.glEnable(2896);
        RenderHelper.disableStandardItemLighting();
    }

    protected void mouseClicked(int par1, int par2, int par3) {
        this.popuptime = System.currentTimeMillis() - 1L;
        if(this.currentHighlight != null && !completedResearch.get(this.player).contains(this.currentHighlight.key) && this.canUnlockResearch(this.currentHighlight)) {
            this.updateResearch();
            boolean secondary = this.currentHighlight.tags != null && this.currentHighlight.tags.size() > 0 && (ModConfig.researchDifficulty == -1 || ModConfig.researchDifficulty == 0 && this.currentHighlight.isSecondary());
            if(secondary) {
                boolean enough = true;

                for(Aspect a : this.currentHighlight.tags.getAspects()) {
                    if(OldResearch.proxy.playerKnowledge.getAspectPoolFor(this.player, a) < this.currentHighlight.tags.getAmount(a)) {
                        enough = false;
                        break;
                    }
                }

                if(enough) {
                    PacketHandler.INSTANCE.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, this.mc.player.getGameProfile().getName(), this.mc.player.world.provider.getDimension(), (byte)0));
                }
            } else if(this.hasScribestuff && ResearchManager.getResearchSlot(this.mc.player, this.currentHighlight.key) == -1) {
                PacketHandler.INSTANCE.sendToServer(new PacketPlayerCompleteToServer(this.currentHighlight.key, this.mc.player.getGameProfile().getName(), this.mc.player.world.provider.getDimension(), (byte)1));
                this.popuptime = System.currentTimeMillis() + 3000L;
                this.popupmessage = (new TextComponentTranslation(I18n.format("tc.research.popup"), "" + this.currentHighlight.getName())).getUnformattedText();
            }
        } else if(this.currentHighlight != null && completedResearch.get(this.player).contains(this.currentHighlight.key)) {
//            TODO: Implement GuiResearchRecipe
//            this.mc.displayGuiScreen(new GuiResearchRecipe(this.currentHighlight, 0, this.guiMapX, this.guiMapY));
        } else {
            int var4 = (this.width - this.paneWidth) / 2;
            int var5 = (this.height - this.paneHeight) / 2;
            Collection cats = ResearchCategories.researchCategories.keySet();
            int count = 0;
            boolean swop = false;

            for(Object obj : cats) {
                ResearchCategoryList rcl = ResearchCategories.getResearchList((String)obj);
                if(!obj.equals("ELDRITCH") || ResearchManager.isResearchComplete(this.player, "ELDRITCHMINOR")) {
                    if(count == 9) {
                        count = 0;
                        swop = true;
                    }

                    int mposx = par1 - (var4 - 24 + (swop?280:0));
                    int mposy = par2 - (var5 + count * 24);
                    if(mposx >= 0 && mposx < 24 && mposy >= 0 && mposy < 24) {
                        selectedCategory = (String)obj;
                        this.updateResearch();
                        this.playButtonClick();
                        break;
                    }

                    ++count;
                }
            }
        }

        try {
            super.mouseClicked(par1, par2, par3);
        } catch (IOException ignored) {}
    }

    public void drawTexturedModalRectReversed(int par1, int par2, int par3, int par4, int par5, int par6) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(par1 + 0, par2 + par6, this.zLevel, (float)(par3 + 0) * f, (float)(par4 + par6) * f1);
        tessellator.addVertexWithUV(par1 + par5, par2 + par6, this.zLevel, (float)(par3 - par5) * f, (float)(par4 + par6) * f1);
        tessellator.addVertexWithUV(par1 + par5, par2 + 0, this.zLevel, (float)(par3 - par5) * f, (float)(par4 + 0) * f1);
        tessellator.addVertexWithUV(par1 + 0, par2 + 0, this.zLevel, (float)(par3 + 0) * f, (float)(par4 + 0) * f1);
        tessellator.draw();
    }

    private void playButtonClick() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.clack, SoundCategory.MASTER, 0.4F, 1.0F, false);
    }

    private boolean canUnlockResearch(ResearchItem res) {
        if(res.parents != null && res.parents.length > 0) {
            for(String pt : res.parents) {
                ResearchItem parent = ResearchCategories.getResearch(pt);
                if(parent != null && !completedResearch.get(this.player).contains(parent.key)) {
                    return false;
                }
            }
        }

        if(res.parentsHidden != null && res.parentsHidden.length > 0) {
            for(String pt : res.parentsHidden) {
                ResearchItem parent = ResearchCategories.getResearch(pt);
                if(parent != null && !completedResearch.get(this.player).contains(parent.key)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    private void drawLine(int x, int y, int x2, int y2, float r, float g, float b, float te, boolean wiggle) {
        float count = (float) FMLClientHandler.instance().getClient().player.ticksExisted + te;
        Tessellator var12 = Tessellator.instance;
        GL11.glPushMatrix();
        GL11.glAlphaFunc(516, 0.003921569F);
        GL11.glDisable(3553);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
        double d3 = x - x2;
        double d4 = y - y2;
        float dist = OldResearchUtils.sqrt_double(d3 * d3 + d4 * d4);
        int inc = (int)(dist / 2.0F);
        float dx = (float)(d3 / (double)inc);
        float dy = (float)(d4 / (double)inc);
        if(Math.abs(d3) > Math.abs(d4)) {
            dx *= 2.0F;
        } else {
            dy *= 2.0F;
        }

        GL11.glLineWidth(3.0F);
        GL11.glEnable(2848);
        GL11.glHint(3154, 4354);
        var12.startDrawing(3);

        for(int a = 0; a <= inc; ++a) {
            float r2 = r;
            float g2 = g;
            float b2 = b;
            float mx = 0.0F;
            float my = 0.0F;
            float op = 0.6F;
            if(wiggle) {
                float phase = (float)a / (float)inc;
                mx = MathHelper.sin((count + (float)a) / 7.0F) * 5.0F * (1.0F - phase);
                my = MathHelper.sin((count + (float)a) / 5.0F) * 5.0F * (1.0F - phase);
                r2 = r * (1.0F - phase);
                g2 = g * (1.0F - phase);
                b2 = b * (1.0F - phase);
                op *= phase;
            }

            var12.setColorRGBA_F(r2, g2, b2, op);
            var12.addVertex((float)x - dx * (float)a + mx, (float)y - dy * (float)a + my, 0.0D);
            if(Math.abs(d3) > Math.abs(d4)) {
                dx *= 1.0F - 1.0F / ((float)inc * 3.0F / 2.0F);
            } else {
                dy *= 1.0F - 1.0F / ((float)inc * 3.0F / 2.0F);
            }
        }

        var12.draw();
        GL11.glBlendFunc(770, 771);
        GL11.glDisable(2848);
        GL11.glDisable(3042);
        GL11.glDisable(32836);
        GL11.glEnable(3553);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glPopMatrix();
    }

    private void drawForbidden(double x, double y) {
        int count = FMLClientHandler.instance().getClient().player.ticksExisted;
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 771);
//        UtilsFX.bindTexture(TileNodeRenderer.nodetex); TODO: maybe bring in node tex for GUIs not tile
        int frames = 32;
        int part = count % frames;
        GL11.glTranslated(x, y, 0.0D);
        UtilsFX.renderAnimatedQuadStrip(80.0F, 0.66F, frames, 5, frames - 1 - part, 0.0F, 4456533);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glDisable(3042);
        GL11.glPopMatrix();
    }
}