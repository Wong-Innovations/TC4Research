package com.wonginnovations.oldresearch.client.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.api.research.ResearchCategories;
import com.wonginnovations.oldresearch.api.research.ResearchItem;
import com.wonginnovations.oldresearch.common.container.ContainerResearchTable;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectCombinationToServer;
import com.wonginnovations.oldresearch.common.lib.network.PacketAspectPlaceToServer;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import com.wonginnovations.oldresearch.common.lib.research.ResearchNoteData;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import com.wonginnovations.oldresearch.client.lib.PlayerNotifications;
import com.wonginnovations.oldresearch.client.lib.Tessellator;
import com.wonginnovations.oldresearch.client.lib.UtilsFX;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.HexUtils;

@SideOnly(Side.CLIENT)
public class GuiResearchTable extends GuiContainer {
    private static boolean RESEARCHER_1;
    private static boolean RESEARCHER_2;
    private static boolean RESEARCHDUPE;
    private final int HEX_SIZE = 9;
    private float xSize_lo;
    private float ySize_lo;
    private long butcount1 = 0L;
    private long butcount2 = 0L;
    private int page = 0;
    private int lastPage = 0;
    private int isMouseButtonDown = 0;
    private TileResearchTable tileEntity;
    private final FontRenderer galFontRenderer;
    private final String username;
    EntityPlayer player;
    public Aspect select1 = null;
    public Aspect select2 = null;
    private final AspectList aspectlist = new AspectList();
    private final HashMap<String, GuiResearchTable.Rune> runes = new HashMap<>();
    private final float popupScale = 0.05F;
    private Aspect draggedAspect;
    public ResearchNoteData note = null;
    long lastRuneCheck = 0L;
    private final HashMap<String, HexUtils.Hex[]> lines = new HashMap<>();
    private final ArrayList<String> checked = new ArrayList<>();
    private final ArrayList<String> highlight = new ArrayList<>();

    public GuiResearchTable(EntityPlayer player, TileResearchTable e) {
        super(new ContainerResearchTable(player.inventory, e));
        this.tileEntity = e;
        this.xSize = 256;
        this.ySize = 256;
        this.galFontRenderer = FMLClientHandler.instance().getClient().standardGalacticFontRenderer;
        this.username = player.getGameProfile().getName();
        this.player = player;
        RESEARCHER_1 = OldResearchManager.isResearchComplete(player.getGameProfile().getName(), "RESEARCHER1"); // possible alternative: ThaumcraftCapabilities.getKnowledge(player).isResearchComplete("RESEARCHER1");
        RESEARCHER_2 = OldResearchManager.isResearchComplete(player.getGameProfile().getName(), "RESEARCHER2");
        RESEARCHDUPE = OldResearchManager.isResearchComplete(player.getGameProfile().getName(), "RESEARCHDUPE");
        int count = 0;

        for(Aspect aspect : Aspect.aspects.values()) {
            this.aspectlist.add(aspect, count);
            ++count;
        }

    }

    protected void drawGuiContainerForegroundLayer(int mx, int my) {
        Minecraft mc = Minecraft.getMinecraft();
        long time = System.nanoTime() / 1000000L;
        if(PlayerNotifications.getListAndUpdate(time).size() > 0) {
            GL11.glPushMatrix();
            OldResearch.renderEventHandler.notifyHandler.renderNotifyHUD((double)this.width, (double)this.height, time);
            GL11.glPopMatrix();
        }

    }

    public void drawScreen(int mx, int my, float par3) {
        super.drawScreen(mx, my, par3);
        this.xSize_lo = (float)mx;
        this.ySize_lo = (float)my;
        int gx = (this.width - this.xSize) / 2;
        int gy = (this.height - this.ySize) / 2;
        if(this.note != null && RESEARCHDUPE && this.note.isComplete()) {
            int var7 = mx - (gx + 37);
            int var8 = my - (gy + 5);
            if(var7 >= 0 && var8 >= 0 && var7 < 24 && var8 < 24) {
                RenderHelper.enableGUIStandardItemLighting();
                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                ResearchItem rr = ResearchCategories.getResearch(this.note.key);
                String ss = I18n.format("tc.research.copy");
                GL11.glEnable(3042);
                UtilsFX.bindTexture("textures/gui/guiresearchtable2.png");
                this.drawTexturedModalRect(gx + 100, gy + 21, 184, 224, 48, 16);
                AspectList al = rr.tags.copy();

                for(Aspect aspect : al.getAspects()) {
                    al.add(aspect, this.note.copies);
                }

                int count = 0;

                for(Aspect aspect : al.getAspects()) {
                    UtilsFX.drawTag(gx + 100 + 48 + count * 16, gy + 21, aspect, (float)al.getAmount(aspect), 0, this.zLevel);
                    ++count;
                }

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                this.fontRenderer.drawStringWithShadow(ss, gx + 100, gy + 12, -1);
            }
        }

        RenderHelper.disableStandardItemLighting();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        if(Mouse.isButtonDown(0)) {
            int sx = gx + 10;
            int sy = gy + 40;
            if(this.isMouseButtonDown == 0 && mx >= sx && mx < sx + 80 && my >= sy && my < sy + 80) {
                Aspect aspect = this.getClickedAspect(mx, my, gx, gy, false);
                if(aspect != null) {
                    this.playButtonAspect();
                    this.isMouseButtonDown = 1;
                    this.draggedAspect = aspect;
                }
            } else if(this.isMouseButtonDown == 1 && this.draggedAspect != null) {
                GL11.glEnable(3042);
                this.drawOrb(mx - 8, my - 8, this.draggedAspect.getColor());
                GL11.glDisable(3042);
            }
        } else {
            if(this.isMouseButtonDown == 1 && this.draggedAspect != null) {
                if(this.note != null) {
                    int mouseX = mx - (gx + 169);
                    int mouseY = my - (gy + 83);
                    HexUtils.Hex hp = (new HexUtils.Pixel(mouseX, mouseY)).toHex(HEX_SIZE);
                    if(this.note.hexEntries.containsKey(hp.toString()) && (this.note.hexEntries.get(hp.toString())).type == 0) {
                        this.playButtonCombine();
                        this.playButtonWrite();
                        PacketHandler.INSTANCE.sendToServer(new PacketAspectPlaceToServer(this.player, (byte)hp.q, (byte)hp.r, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ(), this.draggedAspect));
                        this.draggedAspect = null;
                    }
                }

                if(this.draggedAspect != null) {
                    boolean skip = false;
                    int mouseX = mx - (gx + 20);
                    int mouseY = my - (gy + 146);
                    if(mouseX >= -16 && mouseY >= -16 && mouseX < 16 && mouseY < 16) {
                        this.playButtonAspect();
                        this.select1 = this.draggedAspect;
                        skip = true;
                    }

                    mouseX = mx - (gx + 79);
                    mouseY = my - (gy + 146);
                    if(!skip && mouseX >= -16 && mouseY >= -16 && mouseX < 16 && mouseY < 16) {
                        this.playButtonAspect();
                        this.select2 = this.draggedAspect;
                        skip = true;
                    }

                    if(!skip) {
                        Aspect aspect = this.getClickedAspect(mx, my, gx, gy, false);
                        if(aspect == this.draggedAspect) {
                            if(this.select1 == null) {
                                this.select1 = this.draggedAspect;
                            } else if(this.select2 == null) {
                                this.select2 = this.draggedAspect;
                            }
                        }
                    }
                }
            }

            this.isMouseButtonDown = 0;
            this.draggedAspect = null;
        }

        this.drawAspectText(this.guiLeft + 10, this.guiTop + 40, mx, my);
        if(this.note != null && (this.tileEntity.getStackInSlot(0) == null || this.tileEntity.getStackInSlot(0).getItemDamage() == this.tileEntity.getStackInSlot(0).getMaxDamage())) {
            int sx = Math.max(this.fontRenderer.getStringWidth(I18n.format("tile.researchtable.noink.0")), this.fontRenderer.getStringWidth(I18n.format("tile.researchtable.noink.1"))) / 2;
            UtilsFX.drawCustomTooltip(this, itemRender, this.fontRenderer, Arrays.asList(I18n.format("tile.researchtable.noink.0"), I18n.format("tile.researchtable.noink.1")), gx + 157 - sx, gy + 84, 11);
        }

        this.renderHoveredToolTip(mx, my);
    }

    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(3042);
        UtilsFX.bindTexture("textures/gui/guiresearchtable2.png");
        this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 255, 167);
        this.drawTexturedModalRect(this.guiLeft + 40, this.guiTop + 167, 0, 166, 184, 88);
        if(this.page < this.lastPage) {
            this.drawTexturedModalRect(this.guiLeft + 51, this.guiTop + 121, 208, 208, 24, 8);
        }

        if(this.page > 0) {
            this.drawTexturedModalRect(this.guiLeft + 27, this.guiTop + 121, 184, 208, 24, 8);
        }

        if(this.butcount2 < System.nanoTime() && this.select1 != null && this.select2 != null) {
            this.drawTexturedModalRect(this.guiLeft + 35, this.guiTop + 139, 184, 184, 32, 16);
            this.drawOrb(this.guiLeft + 43, this.guiTop + 139);
        } else if(this.butcount2 >= System.nanoTime() && this.select1 != null && this.select2 != null) {
            this.drawTexturedModalRect(this.guiLeft + 35, this.guiTop + 139, 184, 184, 32, 16);
            this.drawTexturedModalRect(this.guiLeft + 35, this.guiTop + 139, 184, 168, 32, 16);
        }

        if(RESEARCHDUPE && this.note != null && this.note.isComplete()) {
            this.drawTexturedModalRect(this.guiLeft + 37, this.guiTop + 5, 232, 200, 24, 24);
        }

        this.drawAspects(this.guiLeft + 10, this.guiTop + 40);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderHelper.disableStandardItemLighting();
        this.drawResearchData(this.guiLeft, this.guiTop, par2, par3);
    }

    private void drawAspects(int x, int y) {
        AspectList aspects = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(this.username);
        if(aspects != null) {
            int count = aspects.size();
            this.lastPage = (count - 20) / 5;
            count = 0;
            int drawn = 0;

            for(Aspect aspect : aspects.getAspects()) {
                ++count;
                if(count - 1 >= this.page * 5 && drawn < 25) {
                    boolean faded = aspects.getAmount(aspect) <= 0 && this.tileEntity.data.bonusAspects.getAmount(aspect) <= 0;
                    int xx = drawn / 5 * 16;
                    int yy = drawn % 5 * 16;
                    UtilsFX.drawTag(x + xx, y + yy, aspect, (float)aspects.getAmount(aspect), this.tileEntity.data.bonusAspects.getAmount(aspect), (double)this.zLevel, 771, faded?0.33F:1.0F);
                    ++drawn;
                }
            }
        }

        if(this.select1 != null && OldResearch.proxy.playerKnowledge.getAspectPoolFor(this.player.getGameProfile().getName(), this.select1) <= 0 && this.tileEntity.data.bonusAspects.getAmount(this.select1) <= 0) {
            this.select1 = null;
        }

        if(this.select2 != null && OldResearch.proxy.playerKnowledge.getAspectPoolFor(this.player.getGameProfile().getName(), this.select2) <= 0 && this.tileEntity.data.bonusAspects.getAmount(this.select2) <= 0) {
            this.select2 = null;
        }

        if(this.select1 != null) {
            UtilsFX.drawTag(x + 3, y + 99, this.select1, 0.0F, 0, this.zLevel);
        }

        if(this.select2 != null) {
            UtilsFX.drawTag(x + 61, y + 99, this.select2, 0.0F, 0, this.zLevel);
        }

    }

    private void drawAspectText(int x, int y, int mx, int my) {
        int var7;
        int var8;
        AspectList aspects = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(this.username);
        if(aspects != null) {
            int count = 0;
            int drawn = 0;

            for(Aspect aspect : aspects.getAspects()) {
                ++count;
                if(count - 1 >= this.page * 5 && drawn < 25) {
                    int xx = drawn / 5 * 16;
                    int yy = drawn % 5 * 16;
                    var7 = mx - (x + xx);
                    var8 = my - (y + yy);
                    if(var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                        UtilsFX.drawCustomTooltip(this, itemRender, this.fontRenderer, Arrays.asList(aspect.getName(), aspect.getLocalizedDescription()), mx, my - 8, 11);
                        if(RESEARCHER_1 && !aspect.isPrimal()) {
                            GL11.glPushMatrix();
                            GL11.glEnable(3042);
                            GL11.glBlendFunc(770, 771);
                            UtilsFX.bindTexture(new ResourceLocation("thaumcraft", "textures/aspects/_back.png"));
                            GL11.glPushMatrix();
                            GL11.glTranslated(mx + 6, my + 6, 0.0D);
                            GL11.glScaled(1.25D, 1.25D, 0.0D);
                            UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);
                            GL11.glPopMatrix();
                            GL11.glPushMatrix();
                            GL11.glTranslated(mx + 24, my + 6, 0.0D);
                            GL11.glScaled(1.25D, 1.25D, 0.0D);
                            UtilsFX.drawTexturedQuadFull(0, 0, 0.0D);
                            GL11.glPopMatrix();
                            UtilsFX.drawTag(mx + 26, my + 8, aspect.getComponents()[1], 0.0F, 0, 0.0D);
                            UtilsFX.drawTag(mx + 8, my + 8, aspect.getComponents()[0], 0.0F, 0, 0.0D);
                            GL11.glDisable(3042);
                            GL11.glPopMatrix();
                        }

                        return;
                    }

                    ++drawn;
                }
            }
        }

        if(this.select1 != null) {
            var7 = mx - (x + 3);
            var8 = my - (y + 99);
            if(var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                UtilsFX.drawCustomTooltip(this, itemRender, this.fontRenderer, Arrays.asList(this.select1.getName(), this.select1.getLocalizedDescription()), mx, my - 8, 11);
                return;
            }
        }

        if(this.select2 != null) {
            var7 = mx - (x + 61);
            var8 = my - (y + 99);
            if(var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                UtilsFX.drawCustomTooltip(this, itemRender, this.fontRenderer, Arrays.asList(this.select2.getName(), this.select2.getLocalizedDescription()), mx, my - 8, 11);
            }
        }

    }

    private void drawResearchData(int x, int y, int mx, int my) {
        GL11.glPushMatrix();
        GL11.glEnable(3042);
        this.drawSheet(x, y, mx, my);
        GL11.glPopMatrix();
    }

    private void drawHex(HexUtils.Hex hex, int x, int y) {
        GL11.glPushMatrix();
        GL11.glAlphaFunc(516, 0.003921569F);
        GL11.glEnable(3042);
        UtilsFX.bindTexture("textures/gui/hex1.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.25F);
        HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
        GL11.glTranslated((double)x + pix.x, (double)y + pix.y, 0.0D);
        Tessellator tessellator = new Tessellator();
        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 0.25F);
        tessellator.addVertexWithUV(-8.0D, 8.0D, this.zLevel, 0.0D, 1.0D);
        tessellator.addVertexWithUV(8.0D, 8.0D, this.zLevel, 1.0D, 1.0D);
        tessellator.addVertexWithUV(8.0D, -8.0D, this.zLevel, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-8.0D, -8.0D, this.zLevel, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glPopMatrix();
    }

    private void drawHexHighlight(HexUtils.Hex hex, int x, int y) {
        GL11.glPushMatrix();
        GL11.glAlphaFunc(516, 0.003921569F);
        GL11.glEnable(3042);
        GL11.glBlendFunc(770, 1);
        UtilsFX.bindTexture("textures/gui/hex2.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
        GL11.glTranslated((double)x + pix.x, (double)y + pix.y, 0.0D);
        Tessellator tessellator = new Tessellator();
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(1.0F, 1.0F, 1.0F, 1.0F);
        tessellator.addVertexWithUV(-8.0D, 8.0D, this.zLevel, 0.0D, 1.0D);
        tessellator.addVertexWithUV(8.0D, 8.0D, this.zLevel, 1.0D, 1.0D);
        tessellator.addVertexWithUV(8.0D, -8.0D, this.zLevel, 1.0D, 0.0D);
        tessellator.addVertexWithUV(-8.0D, -8.0D, this.zLevel, 0.0D, 0.0D);
        tessellator.draw();
        GL11.glBlendFunc(770, 771);
        GL11.glAlphaFunc(516, 0.1F);
        GL11.glPopMatrix();
    }

    private void drawLine(double x, double y, double x2, double y2) {
        int count = FMLClientHandler.instance().getClient().player.ticksExisted;
        float alpha = 0.3F + MathHelper.sin((float)((double)((float)count) + x)) * 0.3F + 0.3F;
        Tessellator tessellator = new Tessellator();
        GL11.glPushMatrix();
        GL11.glLineWidth(3.0F);
        GL11.glDisable(3553);
        GL11.glEnable('耺');
        GL11.glBlendFunc(770, 1);
        tessellator.startDrawing(3);
        tessellator.setColorRGBA_F(0.0F, 0.6F, 0.8F, alpha);
        tessellator.addVertex(x, y, 0.0D);
        tessellator.addVertex(x2, y2, 0.0D);
        tessellator.draw();
        GL11.glBlendFunc(770, 771);
        GL11.glDisable('耺');
        GL11.glEnable(3553);
        GL11.glPopMatrix();
    }

    private void drawSheet(int x, int y, int mx, int my) {
        this.note = OldResearchManager.getData(this.tileEntity.getStackInSlot(1));
        if(this.note != null && this.note.key != null && this.note.key.length() != 0) {
            UtilsFX.bindTexture("textures/misc/parchment3.png");
            this.drawTexturedModalRect(x + 94, y + 8, 0, 0, 150, 150);
            long time = System.currentTimeMillis();
            if(this.lastRuneCheck < time) {
                this.lastRuneCheck = time + 250L;
                int k = this.mc.world.rand.nextInt(120) - 60;
                int l = this.mc.world.rand.nextInt(120) - 60;
                HexUtils.Hex hp = (new HexUtils.Pixel(k, l)).toHex(HEX_SIZE);
                if(!this.runes.containsKey(hp.toString()) && !this.note.hexes.containsKey(hp.toString())) {
                    this.runes.put(hp.toString(), new Rune(hp.q, hp.r, time, this.lastRuneCheck + 15000L + (long) this.mc.world.rand.nextInt(10000), this.mc.world.rand.nextInt(16)));
                }
            }

            if(this.runes.size() > 0) {
                GuiResearchTable.Rune[] rns = this.runes.values().toArray(new Rune[0]);

                for(int a = 0; a < rns.length; ++a) {
                    GuiResearchTable.Rune rune = rns[a];
                    if(rune.decay < time) {
                        this.runes.remove(rune.q + ":" + rune.r);
                    } else {
                        HexUtils.Pixel pix = (new HexUtils.Hex(rune.q, rune.r)).toPixel(HEX_SIZE);
                        float progress = (float)(time - rune.start) / (float)(rune.decay - rune.start);
                        float alpha = 0.5F;
                        if(progress < 0.25F) {
                            alpha = progress * 2.0F;
                        } else if(progress > 0.5F) {
                            alpha = 1.0F - progress;
                        }

                        this.drawRune((double)(x + 169) + pix.x, (double)(y + 83) + pix.y, rune.rune, alpha * 0.66F);
                    }
                }
            }

            int mouseX = mx - (x + 169);
            int mouseY = my - (y + 83);
            HexUtils.Hex hp = (new HexUtils.Pixel(mouseX, mouseY)).toHex(HEX_SIZE);
            this.lines.clear();
            this.checked.clear();
            this.highlight.clear();

            for(HexUtils.Hex hex : this.note.hexes.values()) {
                if(this.note.hexEntries.get(hex.toString()).type == 1 && OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.username, this.note.hexEntries.get(hex.toString()).aspect)) {
                    this.checkConnections(hex);
                }
            }

            for(HexUtils.Hex[] con : this.lines.values()) {
                HexUtils.Pixel p1 = con[0].toPixel(HEX_SIZE);
                HexUtils.Pixel p2 = con[1].toPixel(HEX_SIZE);
                this.drawLine((double)(x + 169) + p1.x, (double)(y + 83) + p1.y, (double)(x + 169) + p2.x, (double)(y + 83) + p2.y);
            }

            UtilsFX.bindTexture("textures/gui/hex1.png");
            GL11.glPushMatrix();
            if(!this.note.isComplete()) {
                for(HexUtils.Hex hex : this.note.hexes.values()) {
                    if(this.note.hexEntries.get(hex.toString()).type != 1) {
                        if(!this.note.isComplete()) {
                            if(hex.equals(hp)) {
                                this.drawHexHighlight(hex, x + 169, y + 83);
                            }

                            this.drawHex(hex, x + 169, y + 83);
                        }
                    } else {
                        this.drawOrb((double)(x + 161) + hex.toPixel(HEX_SIZE).x, (double)(y + 75) + hex.toPixel(HEX_SIZE).y);
                    }
                }
            }

            for(HexUtils.Hex hex : this.note.hexes.values()) {
                if(this.note.hexEntries.get(hex.toString()).aspect != null && !OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.username, this.note.hexEntries.get(hex.toString()).aspect)) {
                    HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
                    UtilsFX.bindTexture(new ResourceLocation("thaumcraft", "textures/aspects/_unknown.png"));
                    GL11.glPushMatrix();
                    GL11.glColor4f(0.0F, 0.0F, 0.0F, 0.5F);
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glTranslated((double)(x + 161) + pix.x, (double)(y + 75) + pix.y, 0.0D);
                    UtilsFX.drawTexturedQuadFull(0, 0, this.zLevel);
                    GL11.glDisable(3042);
                    GL11.glPopMatrix();
                } else if(this.note.hexEntries.get(hex.toString()).type != 1 && !this.highlight.contains(hex.toString())) {
                    if(this.note.hexEntries.get(hex.toString()).type == 2) {
                        HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
                        UtilsFX.drawTag((double)(x + 161) + pix.x, (double)(y + 75) + pix.y, this.note.hexEntries.get(hex.toString()).aspect, 0.0F, 0, this.zLevel, 771, 0.66F, true);
                    }
                } else {
                    HexUtils.Pixel pix = hex.toPixel(HEX_SIZE);
                    UtilsFX.drawTag((double)(x + 161) + pix.x, (double)(y + 75) + pix.y, this.note.hexEntries.get(hex.toString()).aspect, 0.0F, 0, this.zLevel, 771, 1.0F, false);
                }
            }

            GL11.glPopMatrix();
        } else {
            this.runes.clear();
        }
    }

    private void checkConnections(HexUtils.Hex hex) {
        this.checked.add(hex.toString());

        for(int a = 0; a < 6; ++a) {
            HexUtils.Hex target = hex.getNeighbour(a);
            if(!this.checked.contains(target.toString()) && this.note.hexEntries.containsKey(target.toString()) && this.note.hexEntries.get(target.toString()).type >= 1) {
                Aspect aspect1 = (this.note.hexEntries.get(hex.toString())).aspect;
                Aspect aspect2 = (this.note.hexEntries.get(target.toString())).aspect;
                if(OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.username, aspect1) && OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.username, aspect2) && (!aspect1.isPrimal() && (aspect1.getComponents()[0] == aspect2 || aspect1.getComponents()[1] == aspect2) || !aspect2.isPrimal() && (aspect2.getComponents()[0] == aspect1 || aspect2.getComponents()[1] == aspect1))) {
                    String k1 = hex + ":" + target;
                    String k2 = target + ":" + hex;
                    if(!this.lines.containsKey(k1) && !this.lines.containsKey(k2)) {
                        this.lines.put(k1, new HexUtils.Hex[]{hex, target});
                        this.highlight.add(target.toString());
                    }

                    this.checkConnections(target);
                }
            }
        }

    }

    private void drawRune(double x, double y, int rune, float alpha) {
        GL11.glPushMatrix();
        UtilsFX.bindTexture("textures/misc/script.png");
        GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
        GL11.glTranslated(x, y, 0.0D);
        if(rune < 16) {
            GL11.glRotatef(90.0F, 0.0F, 0.0F, -1.0F);
        }

        Tessellator tessellator = new Tessellator();
        float var8 = 0.0625F * (float)rune;
        float var9 = var8 + 0.0625F;
        float var10 = 0.0F;
        float var11 = 1.0F;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(0.0F, 0.0F, 0.0F, alpha);
        tessellator.addVertexWithUV(-5.0D, 5.0D, this.zLevel, var9, var11);
        tessellator.addVertexWithUV(5.0D, 5.0D, this.zLevel, var9, var10);
        tessellator.addVertexWithUV(5.0D, -5.0D, this.zLevel, var8, var10);
        tessellator.addVertexWithUV(-5.0D, -5.0D, this.zLevel, var8, var11);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    @Override
    protected void mouseClicked(int mx, int my, int par3) {
        try {
            super.mouseClicked(mx, my, par3);
        } catch (IOException ignored) {}
        if(this.butcount1 <= System.nanoTime() && this.butcount2 <= System.nanoTime()) {
            int gx = (this.width - this.xSize) / 2;
            int gy = (this.height - this.ySize) / 2;
            int var7 = mx - (gx + 35);
            int var8 = my - (gy + 139);
            if(var7 >= 0 && var8 >= 0 && var7 < 32 && var8 < 16 && this.butcount2 < System.nanoTime() && this.select1 != null && this.select2 != null) {
                this.butcount2 = System.nanoTime() + 200000000L;
                this.playButtonClick();
                this.playButtonCombine();
                PacketHandler.INSTANCE.sendToServer(new PacketAspectCombinationToServer(this.player, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ(), this.select1, this.select2, this.tileEntity.data.bonusAspects.getAmount(this.select1) > 0, this.tileEntity.data.bonusAspects.getAmount(this.select2) > 0, true));
            } else {
                var7 = mx - (gx + 27);
                var8 = my - (gy + 121);
                if(this.page > 0 && var7 >= 0 && var8 >= 0 && var7 < 24 && var8 < 8) {
                    --this.page;
                    this.playButtonScroll();
                } else {
                    var7 = mx - (gx + 51);
                    var8 = my - (gy + 121);
                    if(this.page < this.lastPage && var7 >= 0 && var8 >= 0 && var7 < 24 && var8 < 8) {
                        ++this.page;
                        this.playButtonScroll();
                    } else {
                        if(this.select1 != null) {
                            var7 = mx - (gx + 11);
                            var8 = my - (gy + 137);
                            if(var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                                this.select1 = null;
                                this.playButtonAspect();
                                return;
                            }
                        }

                        if(this.select2 != null) {
                            var7 = mx - (gx + 71);
                            var8 = my - (gy + 137);
                            if(var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                                this.select2 = null;
                                this.playButtonAspect();
                                return;
                            }
                        }

                        if(this.note != null) {
                            this.checkClickedHex(mx, my, gx, gy);
                            if(RESEARCHDUPE && this.note.isComplete()) {
                                var7 = mx - (gx + 37);
                                var8 = my - (gy + 5);
                                if(var7 >= 0 && var8 >= 0 && var7 < 24 && var8 < 24) {
                                    this.mc.playerController.sendEnchantPacket(this.inventorySlots.windowId, 5);
                                    this.playButtonClick();
                                    return;
                                }
                            }
                        }

                        if(isShiftKeyDown() && RESEARCHER_2) {
                            Aspect aspect = this.getClickedAspect(mx, my, gx, gy, true);
                            if(aspect != null && !aspect.isPrimal()) {
                                AspectList aspects = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(this.username);
                                if(aspects != null && (aspects.getAmount(aspect.getComponents()[0]) > 0 || this.tileEntity.data.bonusAspects.getAmount(aspect.getComponents()[0]) > 0) && (aspects.getAmount(aspect.getComponents()[1]) > 0 || this.tileEntity.data.bonusAspects.getAmount(aspect.getComponents()[1]) > 0)) {
                                    this.draggedAspect = null;
                                    this.playButtonCombine();
                                    PacketHandler.INSTANCE.sendToServer(new PacketAspectCombinationToServer(this.player, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ(), aspect.getComponents()[0], aspect.getComponents()[1], this.tileEntity.data.bonusAspects.getAmount(aspect.getComponents()[0]) > 0, this.tileEntity.data.bonusAspects.getAmount(aspect.getComponents()[1]) > 0, true));
                                }
                            }
                        }

                    }
                }
            }
        }
    }

    private void checkClickedHex(int mx, int my, int gx, int gy) {
        int mouseX = mx - (gx + 169);
        int mouseY = my - (gy + 83);
        HexUtils.Hex hp = (new HexUtils.Pixel(mouseX, mouseY)).toHex(HEX_SIZE);
        if(this.note.hexes.containsKey(hp.toString()) && this.note.hexEntries.get(hp.toString()).type == 2) {
            this.playButtonCombine();
            this.playButtonErase();
            PacketHandler.INSTANCE.sendToServer(new PacketAspectPlaceToServer(this.player, (byte)hp.q, (byte)hp.r, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ(), null));
        }
    }

    private Aspect getClickedAspect(int mx, int my, int gx, int gy, boolean ignoreZero) {
        AspectList aspects = OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(this.username);
        if(aspects != null) {
            int count = 0;
            int drawn = 0;

            for(Aspect aspect : aspects.getAspects()) {
                ++count;
                if(count - 1 >= this.page * 5 && drawn < 25) {
                    int xx = drawn / 5 * 16;
                    int yy = drawn % 5 * 16;
                    int var7 = mx - (gx + xx + 10);
                    int var8 = my - (gy + yy + 40);
                    if((ignoreZero || aspects.getAmount(aspect) > 0 || this.tileEntity.data.bonusAspects.getAmount(aspect) > 0) && var7 >= 0 && var8 >= 0 && var7 < 16 && var8 < 16) {
                        return aspect;
                    }

                    ++drawn;
                }
            }
        }

        return null;
    }

    private void playButtonClick() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.clack, SoundCategory.MASTER, 0.4F, 1.0F, false);
    }

    private void playButtonAspect() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.hhoff, SoundCategory.MASTER, 0.2F, 1.0F + this.mc.getRenderViewEntity().world.rand.nextFloat() * 0.1F, false);
    }

    private void playButtonCombine() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.hhon, SoundCategory.MASTER, 0.3F, 1.0F, false);
    }

    private void playButtonWrite() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.write, SoundCategory.MASTER, 0.2F, 1.0F, false);
    }

    private void playButtonErase() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.erase, SoundCategory.MASTER, 0.2F, 1.0F + this.mc.getRenderViewEntity().world.rand.nextFloat() * 0.1F, false);
    }

    private void playButtonScroll() {
        this.mc.getRenderViewEntity().world.playSound(this.mc.getRenderViewEntity().posX, this.mc.getRenderViewEntity().posY, this.mc.getRenderViewEntity().posZ, SoundsTC.key, SoundCategory.MASTER, 0.3F, 1.0F, false);
    }

    private void drawOrb(double x, double y) {
        int count = FMLClientHandler.instance().getClient().player.ticksExisted;
        float red = 0.7F + MathHelper.sin((float)(((double)((float)count) + x) / 10.0D)) * 0.15F + 0.15F;
        float green = 0.7F + MathHelper.sin((float)(((double)((float)count) + x + y) / 11.0D)) * 0.15F + 0.15F;
        float blue = 0.7F + MathHelper.sin((float)(((double)((float)count) + y) / 12.0D)) * 0.15F + 0.15F;
        Color c = new Color(red, green, blue);
        this.drawOrb(x, y, c.getRGB());
    }

    private void drawOrb(double x, double y, int color) {
        int count = FMLClientHandler.instance().getClient().player.ticksExisted;
        Color c = new Color(color);
        float red = (float)c.getRed() / 255.0F;
        float green = (float)c.getGreen() / 255.0F;
        float blue = (float)c.getBlue() / 255.0F;
//        if(ModConfig.colorBlind) {
//            red /= 1.8F;
//            green /= 1.8F;
//            blue /= 1.8F;
//        }

        GL11.glPushMatrix();
        UtilsFX.bindTexture("textures/misc/particles.png");
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glTranslated(x, y, 0.0D);
        Tessellator tessellator = new Tessellator();
        int part = count % 8;
        float var8 = 0.5F + (float)part / 8.0F;
        float var9 = var8 + 0.0624375F;
        float var10 = 0.5F;
        float var11 = var10 + 0.0624375F;
        tessellator.startDrawingQuads();
        tessellator.setBrightness(240);
        tessellator.setColorRGBA_F(red, green, blue, 1.0F);
        tessellator.addVertexWithUV(0.0D, 16.0D, this.zLevel, var9, var11);
        tessellator.addVertexWithUV(16.0D, 16.0D, this.zLevel, var9, var10);
        tessellator.addVertexWithUV(16.0D, 0.0D, this.zLevel, var8, var10);
        tessellator.addVertexWithUV(0.0D, 0.0D, this.zLevel, var8, var11);
        tessellator.draw();
        GL11.glPopMatrix();
    }

    private static class Rune {
        int q;
        int r;
        long start;
        long decay;
        int rune;

        public Rune(int q, int r, long start, long decay, int rune) {
            this.q = q;
            this.r = r;
            this.start = start;
            this.decay = decay;
            this.rune = rune;
        }
    }
}
