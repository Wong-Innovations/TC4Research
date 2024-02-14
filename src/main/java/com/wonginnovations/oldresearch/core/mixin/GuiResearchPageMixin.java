package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketPlayerCompleteToServer;
import com.wonginnovations.oldresearch.common.lib.research.ResearchManager;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.translation.I18n;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.research.*;
import thaumcraft.client.gui.GuiResearchPage;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.utils.InventoryUtils;

import java.awt.*;
import java.util.*;

@Mixin(value = GuiResearchPage.class, remap = false)
public abstract class GuiResearchPageMixin extends GuiScreen {

    @Shadow
    protected int paneHeight;
    @Shadow
    int hrx;
    @Shadow
    int hry;
    @Shadow
    ResourceLocation tex1;
    @Shadow
    ResourceLocation dummyResearch;
    @Shadow
    ResourceLocation dummyMap;
    @Shadow
    ResourceLocation dummyFlask;
    @Shadow
    ResourceLocation dummyChest;
    @Shadow
    boolean[] hasResearch;
    @Shadow
    boolean[] hasItem;
    @Shadow
    boolean[] hasCraft;
    @Shadow
    boolean hasAllRequisites;
    @Shadow
    boolean hold;
    @Shadow
    abstract boolean mouseInside(int x, int y, int w, int h, int mx, int my);
    @Shadow
    abstract void drawPopupAt(int x, int y, int mx, int my, String text);
    @Shadow
    abstract void drawStackAt(ItemStack itemstack, int x, int y, int mx, int my, boolean clickthrough);

    @Shadow private boolean isComplete;

    @Unique
    private final Map<Point, ItemStack> oldresearch$renderedNotes = new HashMap<>();

    @Inject(method = "drawRequirements", at = @At("HEAD"), cancellable = true)
    public void drawRequirementsInjection(int x, int mx, int my, ResearchStage stage, CallbackInfo ci) {
        int y = (this.height - this.paneHeight) / 2 - 16 + 210;
        GL11.glPushMatrix();
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
                    s = I18n.translateToLocal("research." + key + ".text");
                    if (key.startsWith("!")) {
                        String k = key.replaceAll("!", "");
                        Aspect as = (Aspect)Aspect.aspects.get(k);
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

                    GL11.glPushMatrix();
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    if (loc instanceof Aspect) {
                        this.mc.renderEngine.bindTexture(((Aspect)loc).getImage());
                        Color cc = new Color(((Aspect)loc).getColor());
                        GlStateManager.color((float)cc.getRed() / 255.0F, (float)cc.getGreen() / 255.0F, (float)cc.getBlue() / 255.0F, 1.0F);
                        UtilsFX.drawTexturedQuadFull((float)(x - 15 + shift), (float)y, (double)this.zLevel);
                    } else if (loc instanceof ResourceLocation) {
                        this.mc.renderEngine.bindTexture((ResourceLocation)loc);
                        UtilsFX.drawTexturedQuadFull((float)(x - 15 + shift), (float)y, (double)this.zLevel);
                    } else if (loc instanceof ItemStack) {
                        RenderHelper.enableGUIStandardItemLighting();
                        GL11.glDisable(2896);
                        GL11.glEnable(32826);
                        GL11.glEnable(2903);
                        GL11.glEnable(2896);
                        this.itemRender.renderItemAndEffectIntoGUI(InventoryUtils.cycleItemStack(loc), x - 15 + shift, y);
                        GL11.glDisable(2896);
                        GL11.glDepthMask(true);
                        GL11.glEnable(2929);
                    }

                    GL11.glPopMatrix();
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

                for(ss = 0; ss < stage.getObtain().length; ++ss) {
                    stack = InventoryUtils.cycleItemStack(stage.getObtain()[ss], ss);
                    this.drawStackAt(stack, x - 15 + shift, y, mx, my, true);
                    if (this.hasItem[ss]) {
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

                for(ss = 0; ss < stage.getCraft().length; ++ss) {
                    stack = InventoryUtils.cycleItemStack(stage.getCraft()[ss], ss);
                    this.drawStackAt(stack, x - 15 + shift, y, mx, my, true);
                    if (this.hasCraft[ss]) {
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
                    s = I18n.translateToLocal("research." + key + ".text");

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
                        loc = ResearchManager.getNote(key);
                    }

                    GL11.glPushMatrix();
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    if (loc != null) {
                        RenderHelper.enableGUIStandardItemLighting();
                        GL11.glDisable(2896);
                        GL11.glEnable(32826);
                        GL11.glEnable(2903);
                        GL11.glEnable(2896);
                        oldresearch$renderedNotes.put(new Point(x - 15 + shift, y), (ItemStack) loc);
                        this.itemRender.renderItemAndEffectIntoGUI(InventoryUtils.cycleItemStack(loc), x - 15 + shift, y);
                        GL11.glDisable(2896);
                        GL11.glDepthMask(true);
                        GL11.glEnable(2929);
                    }

                    GL11.glPopMatrix();
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
                    s = I18n.translateToLocal("tc.stage.hold");
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
                    s = I18n.translateToLocal("tc.stage.complete");
                    ss = this.mc.fontRenderer.getStringWidth(s);
                    this.mc.fontRenderer.drawStringWithShadow(s, (float)(x + 52) - (float)ss / 2.0F, (float)(y - 4), 16777215);
                }
            }
        }

        GL11.glPopMatrix();
        ci.cancel();
    }

    @Redirect(
        method = "parsePages",
        at = @At(value = "INVOKE", target = "Lthaumcraft/api/research/ResearchStage;getKnow()[Lthaumcraft/api/research/ResearchStage$Knowledge;")
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
            if ((mx >= p.x - 10 && mx <= p.x + 10) && (my >= p.y - 10 && my <= p.y + 10)) {
                PacketHandler.INSTANCE.sendToServer(
                    new PacketPlayerCompleteToServer(
                        ResearchManager.getData(oldresearch$renderedNotes.get(p)).key,
                        this.mc.player.getGameProfile().getName(),
                        this.mc.player.world.provider.getDimension(),
                        (byte) 1
                    )
                );
                ci.cancel();
            }
        }
    }
}
