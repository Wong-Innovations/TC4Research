package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.OldResearchUtils;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketGivePlayerNoteToServer;
import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentString;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.capabilities.IPlayerKnowledge;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.*;
import thaumcraft.client.gui.GuiResearchPage;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

@Mixin(GuiResearchPage.class)
public abstract class GuiResearchPageMixin extends GuiScreen {

    @Shadow(remap = false)
    protected int paneHeight;
    @Shadow(remap = false)
    int hrx;
    @Shadow(remap = false)
    int hry;
    @Shadow(remap = false)
    ResourceLocation tex1;
    @Shadow(remap = false)
    ResourceLocation dummyResearch;
    @Shadow(remap = false)
    ResourceLocation dummyMap;
    @Shadow(remap = false)
    ResourceLocation dummyFlask;
    @Shadow(remap = false)
    ResourceLocation dummyChest;
    @Shadow(remap = false)
    boolean[] hasResearch;
    @Shadow(remap = false)
    boolean[] hasItem;
    @Shadow(remap = false)
    boolean[] hasCraft;
    @Shadow(remap = false)
    boolean hasAllRequisites;
    @Shadow(remap = false)
    boolean hold;
    @Shadow(remap = false)
    AspectList knownPlayerAspects;
    @Shadow(remap = false)
    private int maxAspectPages;
    @Shadow(remap = false)
    private static int aspectsPage;
    @Shadow(remap = false)
    ResourceLocation tex3;
    @Shadow(remap = false)
    static ResourceLocation shownRecipe;
    @Shadow(remap = false)
    boolean allowWithPagePopup;

    @Shadow(remap = false)
    abstract boolean mouseInside(int x, int y, int w, int h, int mx, int my);
    @Shadow(remap = false)
    abstract void drawPopupAt(int x, int y, int mx, int my, String text);
    @Shadow(remap = false)
    abstract void drawStackAt(ItemStack itemstack, int x, int y, int mx, int my, boolean clickthrough);
    @Shadow(remap = false)
    public abstract void drawTexturedModalRectScaled(int par1, int par2, int par3, int par4, int par5, int par6, float scale);

    @Shadow(remap = false)
    boolean isComplete;

    @Shadow(remap = false)
    IPlayerKnowledge playerKnowledge;
    @Shadow(remap = false)
    List tipText;
    @Unique
    private final Map<Point, ItemStack> oldresearch$renderedNotes = new HashMap<>();

    @Unique
    private void oldresearch$drawPopupAt(int x, int y, int mx, int my, String... text) {
        if ((shownRecipe == null || this.allowWithPagePopup) && mx >= x && my >= y && mx < x + 16 && my < y + 16) {
            ArrayList<String> s = new ArrayList<>();
            for (String t : text)
                s.add(I18n.format(t));
            this.tipText = s;
        }
    }

    @Inject(method = "drawRequirements", at = @At("HEAD"), cancellable = true, remap = false)
    public void drawRequirementsInjection(int x, int mx, int my, ResearchStage stage, CallbackInfo ci) {
        int y = (this.height - this.paneHeight) / 2 - 16 + 210;
        GlStateManager.pushMatrix();
        boolean b = false;
        int shift;
        int ss;
        String key;
        String s;
        if (stage.getResearch() != null && !Arrays.stream(stage.getResearch()).allMatch(re -> re.startsWith("rn_"))) {
            y -= 18;
            b = true;
            shift = 24;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
            this.mc.renderEngine.bindTexture(this.tex1);
            this.drawTexturedModalRect(x - 12, y - 1, 200, 232, 56, 16);
            this.drawPopupAt(x - 15, y, mx, my, "tc.need.research");
            Object loc = null;
            if (this.hasResearch != null) {
                if (this.hasResearch.length != stage.getResearch().length) {
                    this.hasResearch = new boolean[stage.getResearch().length];
                }

                ss = 18;
                if (stage.getResearch().length > 6) {
                    ss = 110 / stage.getResearch().length;
                }

                for(int a = 0; a < stage.getResearch().length; ++a) {
                    key = stage.getResearch()[a];
                    loc = stage.getResearchIcon()[a] != null ? new ResourceLocation(stage.getResearchIcon()[a]) : this.dummyResearch;
                    s = I18n.format("research." + key + ".text");
                    if (key.startsWith("!")) {
                        String k = key.replaceAll("!", "");
                        Aspect as = Aspect.aspects.get(k);
                        if (as != null) {
                            loc = as;
                            s = as.getName();
                        }
                    }

                    ResearchEntry re = ResearchCategories.getResearch(key);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    if (re != null && re.getIcons() != null) {
                        int idx = (int)(System.currentTimeMillis() / 1000L % (long)re.getIcons().length);
                        loc = re.getIcons()[idx];
                        s = re.getLocalizedName();
                    } else if (key.startsWith("m_")) {
                        loc = this.dummyMap;
                    } else if (key.startsWith("c_")) {
                        loc = this.dummyChest;
                    } else if (key.startsWith("f_")) {
                        loc = this.dummyFlask;
                    } else {
                        GlStateManager.color(0.5F, 0.75F, 1.0F, 1.0F);
                    }

                    if (key.startsWith("rn_")) {
                        continue;
                    }

                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    if (loc instanceof Aspect) {
                        this.mc.renderEngine.bindTexture(((Aspect)loc).getImage());
                        Color cc = new Color(((Aspect)loc).getColor());
                        GlStateManager.color((float)cc.getRed() / 255.0F, (float)cc.getGreen() / 255.0F, (float)cc.getBlue() / 255.0F, 1.0F);
                        UtilsFX.drawTexturedQuadFull((float)(x - 15 + shift), (float)y, this.zLevel);
                    } else if (loc instanceof ResourceLocation) {
                        this.mc.renderEngine.bindTexture((ResourceLocation)loc);
                        UtilsFX.drawTexturedQuadFull((float)(x - 15 + shift), (float)y, this.zLevel);
                    } else if (loc instanceof ItemStack) {
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.disableLighting();
                        GlStateManager.enableRescaleNormal();
                        GlStateManager.enableColorMaterial();
                        GlStateManager.enableLighting();
                        this.itemRender.renderItemAndEffectIntoGUI(InventoryUtils.cycleItemStack(loc), x - 15 + shift, y);
                        GlStateManager.disableLighting();
                        GlStateManager.depthMask(true);
                        GlStateManager.enableDepth();
                    }

                    GlStateManager.popMatrix();
                    if (this.hasResearch[a]) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.mc.renderEngine.bindTexture(this.tex1);
                        GlStateManager.disableDepth();
                        this.drawTexturedModalRect(x - 15 + shift + 8, y, 159, 207, 10, 10);
                        GlStateManager.enableDepth();
                    }

                    this.drawPopupAt(x - 15 + shift, y, mx, my, s);
                    shift += ss;
                }
            }
        }

        ItemStack stack;
        if (stage.getObtain() != null) {
            y -= 18;
            b = true;
            shift = 24;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
            this.mc.renderEngine.bindTexture(this.tex1);
            this.drawTexturedModalRect(x - 12, y - 1, 200, 216, 56, 16);
            this.drawPopupAt(x - 15, y, mx, my, "tc.need.obtain");
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.hasItem != null) {
                if (this.hasItem.length != stage.getObtain().length) {
                    this.hasItem = new boolean[stage.getObtain().length];
                }

                ss = 18;
                if (stage.getObtain().length > 6) {
                    ss = 110 / stage.getObtain().length;
                }

                for(int a = 0; a < stage.getObtain().length; ++a) {
                    stack = InventoryUtils.cycleItemStack(stage.getObtain()[a]);
                    this.drawStackAt(stack, x - 15 + shift, y, mx, my, true);
                    if (this.hasItem[a]) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.mc.renderEngine.bindTexture(this.tex1);
                        GlStateManager.disableDepth();
                        this.drawTexturedModalRect(x - 15 + shift + 8, y, 159, 207, 10, 10);
                        GlStateManager.enableDepth();
                    }

                    shift += ss;
                }
            }
        }

        if (stage.getCraft() != null) {
            y -= 18;
            b = true;
            shift = 24;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
            this.mc.renderEngine.bindTexture(this.tex1);
            this.drawTexturedModalRect(x - 12, y - 1, 200, 200, 56, 16);
            this.drawPopupAt(x - 15, y, mx, my, "tc.need.craft");
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            if (this.hasCraft != null) {
                if (this.hasCraft.length != stage.getCraft().length) {
                    this.hasCraft = new boolean[stage.getCraft().length];
                }

                ss = 18;
                if (stage.getCraft().length > 6) {
                    ss = 110 / stage.getCraft().length;
                }

                for(int a = 0; a < stage.getCraft().length; ++a) {
                    stack = InventoryUtils.cycleItemStack(stage.getCraft()[a]);
                    this.drawStackAt(stack, x - 15 + shift, y, mx, my, true);
                    if (this.hasCraft[a]) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.mc.renderEngine.bindTexture(this.tex1);
                        GlStateManager.disableDepth();
                        this.drawTexturedModalRect(x - 15 + shift + 8, y, 159, 207, 10, 10);
                        GlStateManager.enableDepth();
                    }

                    shift += ss;
                }
            }
        }

        if (stage.getResearch() != null && Arrays.stream(stage.getResearch()).anyMatch(re -> re.startsWith("rn_"))) {
            y -= 18;
            b = true;
            shift = 24;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.25F);
            this.mc.renderEngine.bindTexture(this.tex1);
            this.drawTexturedModalRect(x - 12, y - 1, 200, 184, 56, 16);
            this.drawPopupAt(x - 15, y, mx, my, "tc.need.know");
            Object loc = null;
            if (this.hasResearch != null) {
                ss = 18;
                if (stage.getResearch().length > 6) {
                    ss = 110 / stage.getResearch().length;
                }

                for(int a = 0; a < stage.getResearch().length; ++a) {
                    key = stage.getResearch()[a];
                    loc = stage.getResearchIcon()[a] != null ? new ResourceLocation(stage.getResearchIcon()[a]) : this.dummyResearch;
                    s = I18n.format("research." + key + ".text");

                    ResearchEntry re = ResearchCategories.getResearch(key);
                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    if (re != null && re.getIcons() != null) {
                        int idx = (int)(System.currentTimeMillis() / 1000L % (long)re.getIcons().length);
                        loc = re.getIcons()[idx];
                        s = re.getLocalizedName();
                    }

                    if (!key.startsWith("rn_")) {
                        continue;
                    } else {
                        loc = OldResearchManager.getNote(key);
                        s = I18n.format("tc.researchtheory", I18n.format("research." + OldResearchManager.getStrippedKey(key) + ".title"));
                    }

                    GlStateManager.pushMatrix();
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    if (loc != null) {
                        RenderHelper.enableGUIStandardItemLighting();
                        GlStateManager.disableLighting();
                        GlStateManager.enableRescaleNormal();
                        GlStateManager.enableColorMaterial();
                        GlStateManager.enableLighting();
                        oldresearch$renderedNotes.put(new Point(x - 15 + shift, y), (ItemStack) loc);
                        this.itemRender.renderItemAndEffectIntoGUI(InventoryUtils.cycleItemStack(loc), x - 15 + shift, y);
                        GlStateManager.disableLighting();
                        GlStateManager.depthMask(true);
                        GlStateManager.enableDepth();
                    }

                    GlStateManager.popMatrix();
                    if (this.hasResearch[a]) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        this.mc.renderEngine.bindTexture(this.tex1);
                        GlStateManager.disableDepth();
                        this.drawTexturedModalRect(x - 15 + shift + 8, y, 159, 207, 10, 10);
                        GlStateManager.enableDepth();
                    }

                    this.oldresearch$drawPopupAt(x - 15 + shift, y, mx, my, s, I18n.format("researchnote.click"));
                    shift += ss;
                }
            }
        }

        if (stage.getResearch() == null && stage.getObtain() == null && stage.getCraft() == null) {
            b = true;
            shift = 24;
        }

        if (b) {
            y -= 12;
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.mc.renderEngine.bindTexture(this.tex1);
            this.drawTexturedModalRect(x + 4, y - 2, 24, 184, 96, 8);
            if (this.hasAllRequisites) {
                this.hrx = x + 20;
                this.hry = y - 6;
                if (this.hold) {
                    s = I18n.format("tc.stage.hold");
                    ss = this.mc.fontRenderer.getStringWidth(s);
                    this.mc.fontRenderer.drawStringWithShadow(s, (float)(x + 52) - (float)ss / 2.0F, (float)(y - 4), 16777215);
                } else {
                    if (this.mouseInside(this.hrx, this.hry, 64, 12, mx, my)) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    } else {
                        GlStateManager.color(0.8F, 0.8F, 0.9F, 1.0F);
                    }

                    this.mc.renderEngine.bindTexture(this.tex1);
                    this.drawTexturedModalRect(this.hrx, this.hry, 84, 216, 64, 12);
                    s = I18n.format("tc.stage.complete");
                    ss = this.mc.fontRenderer.getStringWidth(s);
                    this.mc.fontRenderer.drawStringWithShadow(s, (float)(x + 52) - (float)ss / 2.0F, (float)(y - 4), 16777215);
                }
            }
        }

        GlStateManager.popMatrix();
        ci.cancel();
    }

    @Redirect(
        method = "parsePages",
        at = @At(value = "INVOKE", target = "Lthaumcraft/api/research/ResearchStage;getKnow()[Lthaumcraft/api/research/ResearchStage$Knowledge;"),
        remap = false
    )
    public ResearchStage.Knowledge[] parsePagesGetKnow(ResearchStage instance) {
        if (instance.getCraft() == null && instance.getObtain() == null && instance.getResearch() == null) {
            return new ResearchStage.Knowledge[0];
        }
        return null;
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    public void mouseClickedInjection(int mx, int my, int button, CallbackInfo ci) {
        for (Point p : oldresearch$renderedNotes.keySet()) {
            if ((mx >= p.x && mx <= p.x + 16) && (my >= p.y && my <= p.y + 16)) {
                if (!OldResearchUtils.isPlayerCarrying(this.mc.player, ItemsTC.scribingTools)) {
                    this.mc.player.sendMessage(new TextComponentString("§cScribing tools required to create research notes."));
                    this.mc.displayGuiScreen(null);
                } else if (!OldResearchUtils.isPlayerCarrying(this.mc.player, Items.PAPER)) {
                    this.mc.player.sendMessage(new TextComponentString("§cPaper required to create research notes."));
                    this.mc.displayGuiScreen(null);
                } else if (!OldResearchManager.consumeInkFromPlayer(this.mc.player, false)) {
                    this.mc.player.sendMessage(new TextComponentString("§c" + I18n.format("tile.researchtable.noink.0")));
                    this.mc.player.sendMessage(new TextComponentString("§c" + I18n.format("tile.researchtable.noink.1")));
                    this.mc.displayGuiScreen(null);
                } else {

                    PacketHandler.INSTANCE.sendToServer(
                        new PacketGivePlayerNoteToServer(OldResearchManager.getData(oldresearch$renderedNotes.get(p)).key)
                    );
                }
                ci.cancel();
                return;
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void ctorInjection(ResearchEntry research, ResourceLocation recipe, double x, double y, CallbackInfo ci) {
        this.knownPlayerAspects = new AspectList();

        for (Aspect a : OldResearch.proxy.getPlayerKnowledge().getAspectsDiscovered(this.mc.player.getGameProfile().getName()).getAspects()) {
            this.knownPlayerAspects.add(a, OldResearchManager.getAspectComplexity(a));
        }

        this.maxAspectPages = this.knownPlayerAspects != null ? MathHelper.ceil((float)this.knownPlayerAspects.size() / 5.0F) : 0;
    }

    @Inject(method = "drawAspectPage", at = @At("HEAD"), cancellable = true, remap = false)
    public void drawAspectPageInjection(int x, int y, int mx, int my, CallbackInfo ci) {
        if (this.knownPlayerAspects != null && this.knownPlayerAspects.size() > 0) {
            GlStateManager.pushMatrix();
            int count = -1;
            int start = aspectsPage * 5;
            Aspect[] var9 = this.knownPlayerAspects.getAspectsSortedByAmount();
            int var10 = var9.length;

            for(int var11 = 0; var11 < var10; ++var11) {
                Aspect aspect = var9[var11];
                ++count;
                if (count >= start) {
                    if (count > start + 4) {
                        break;
                    }

                    if (aspect.getImage() != null) {
                        int ty = y + count % 5 * 40;
                        if (mx >= x && my >= ty && mx < x + 40 && my < ty + 40) {
                            this.mc.renderEngine.bindTexture(this.tex3);
                            GlStateManager.pushMatrix();
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(770, 771);
                            GlStateManager.translate(x - 2, y + count % 5 * 40 - 2, 0.0);
                            GlStateManager.scale(2.0, 2.0, 0.0);
                            GlStateManager.color(1.0F, 1.0F, 1.0F, 0.5F);
                            UtilsFX.drawTexturedQuadFull(0.0F, 0.0F, this.zLevel);
                            GlStateManager.popMatrix();
                        }

                        GlStateManager.pushMatrix();
                        GlStateManager.translate(x + 2, y + 2 + count % 5 * 40, 0.0);
                        GlStateManager.scale(1.5F, 1.5F, 1.5F);
                        UtilsFX.drawTag(0, 0, aspect, 0.0F, 0, this.zLevel);
                        GlStateManager.popMatrix();
                        GlStateManager.pushMatrix();
                        GlStateManager.translate(x + 16, y + 29 + count % 5 * 40, 0.0);
                        GlStateManager.scale(0.5F, 0.5F, 0.5F);
                        String text = aspect.getName();
                        int offset = this.mc.fontRenderer.getStringWidth(text) / 2;
                        this.mc.fontRenderer.drawString(text, -offset, 0, 5263440);
                        GlStateManager.popMatrix();
                        if (aspect.getComponents() != null) {
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(x + 60, y + 4 + count % 5 * 40, 0.0);
                            GlStateManager.scale(1.25F, 1.25F, 1.25F);
                            if (OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.mc.player.getGameProfile().getName(), aspect.getComponents()[0])) {
                                UtilsFX.drawTag(0, 0, aspect.getComponents()[0], 0.0F, 0, this.zLevel);
                            } else {
                                this.mc.renderEngine.bindTexture(this.dummyResearch);
                                GlStateManager.color(0.8F, 0.8F, 0.8F, 1.0F);
                                UtilsFX.drawTexturedQuadFull(0.0F, 0.0F, this.zLevel);
                            }

                            GlStateManager.popMatrix();
                            GlStateManager.pushMatrix();
                            GlStateManager.translate(x + 102, y + 4 + count % 5 * 40, 0.0);
                            GlStateManager.scale(1.25F, 1.25F, 1.25F);
                            if (OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.mc.player.getGameProfile().getName(), aspect.getComponents()[1])) {
                                UtilsFX.drawTag(0, 0, aspect.getComponents()[1], 0.0F, 0, this.zLevel);
                            } else {
                                this.mc.renderEngine.bindTexture(this.dummyResearch);
                                GlStateManager.color(0.8F, 0.8F, 0.8F, 1.0F);
                                UtilsFX.drawTexturedQuadFull(0.0F, 0.0F, this.zLevel);
                            }

                            GlStateManager.popMatrix();
                            if (OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.mc.player.getGameProfile().getName(), aspect.getComponents()[0])) {
                                text = aspect.getComponents()[0].getName();
                                offset = this.mc.fontRenderer.getStringWidth(text) / 2;
                                GlStateManager.pushMatrix();
                                GlStateManager.translate(x + 22 + 50, y + 29 + count % 5 * 40, 0.0);
                                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                                this.mc.fontRenderer.drawString(text, -offset, 0, 5263440);
                                GlStateManager.popMatrix();
                            }

                            if (OldResearch.proxy.getPlayerKnowledge().hasDiscoveredAspect(this.mc.player.getGameProfile().getName(), aspect.getComponents()[1])) {
                                text = aspect.getComponents()[1].getName();
                                offset = this.mc.fontRenderer.getStringWidth(text) / 2;
                                GlStateManager.pushMatrix();
                                GlStateManager.translate(x + 22 + 92, y + 29 + count % 5 * 40, 0.0);
                                GlStateManager.scale(0.5F, 0.5F, 0.5F);
                                this.mc.fontRenderer.drawString(text, -offset, 0, 5263440);
                                GlStateManager.popMatrix();
                            }

                            this.mc.fontRenderer.drawString("=", x + 9 + 32, y + 12 + count % 5 * 40, 10066329);
                            this.mc.fontRenderer.drawString("+", x + 10 + 79, y + 12 + count % 5 * 40, 10066329);
                        } else {
                            this.mc.fontRenderer.drawString(I18n.format("tc.aspect.primal"), x + 54, y + 12 + count % 5 * 40, 7829367);
                        }
                    }
                }
            }

            this.mc.renderEngine.bindTexture(this.tex1);
            float bob = MathHelper.sin((float)this.mc.player.ticksExisted / 3.0F) * 0.2F + 0.1F;
            if (aspectsPage > 0) {
                this.drawTexturedModalRectScaled(x - 20, y + 208, 0, 184, 12, 8, bob);
            }

            if (aspectsPage < this.maxAspectPages - 1) {
                this.drawTexturedModalRectScaled(x + 144, y + 208, 12, 184, 12, 8, bob);
            }

            GlStateManager.popMatrix();
        }
        ci.cancel();
    }

    @Redirect(method = "drawPage", at = @At(value = "INVOKE", target = "Lthaumcraft/api/capabilities/IPlayerKnowledge;isResearchComplete(Ljava/lang/String;)Z", remap = false), remap = false)
    public boolean drawPageInjection(IPlayerKnowledge instance, String s) {
        return !"KNOWLEDGETYPES".equals(s) && instance.isResearchComplete(s);
    }
}
