//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.wonginnovations.oldresearch.client.gui;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.resources.I18n;
import org.jetbrains.annotations.NotNull;
import thaumcraft.api.research.ResearchEntry;
import thaumcraft.client.gui.GuiResearchBrowser;

public class ResearchNoteToast implements IToast {
    ResearchEntry entry;
    private long firstDrawTime;
    private boolean newDisplay;
    ResourceLocation tex = new ResourceLocation("oldresearch", "textures/gui/toast.png");

    public ResearchNoteToast(ResearchEntry entry) {
        this.entry = entry;
    }

    public @NotNull IToast.Visibility draw(@NotNull GuiToast toastGui, long delta) {
        if (this.newDisplay) {
            this.firstDrawTime = delta;
            this.newDisplay = false;
        }

        int shift = -20;

        toastGui.getMinecraft().getTextureManager().bindTexture(this.tex);
        GlStateManager.color(1.0F, 1.0F, 1.0F);
        Gui.drawModalRectWithCustomSizedTexture(shift, 0, 0, 0, 180, 32, 180, 32);
        GuiResearchBrowser.drawResearchIcon(this.entry, 6 + shift, 8, 0.0F, false);
        toastGui.getMinecraft().fontRenderer.drawString(I18n.format("researchnote.complete"), 30 + shift, 7, 10631665);
        String s = this.entry.getLocalizedName();
        float w = (float)toastGui.getMinecraft().fontRenderer.getStringWidth(s);
        if (w > 150.0F) {
            w = 150.0F / w;
            GlStateManager.pushMatrix();
            GlStateManager.translate(30.0F, 18.0F, 0.0F);
            GlStateManager.scale(w, w, w);
            toastGui.getMinecraft().fontRenderer.drawString(s, shift, 0, 16755465);
            GlStateManager.popMatrix();
        } else {
            toastGui.getMinecraft().fontRenderer.drawString(s, 30 + shift, 18, 16755465);
        }

        return delta - this.firstDrawTime < 5000L ? Visibility.SHOW : Visibility.HIDE;
    }
}
