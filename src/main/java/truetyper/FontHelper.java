//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\dylan\Downloads\mcp908"!

//Decompiled by Procyon!

package truetyper;

import net.minecraft.client.*;
import net.minecraft.client.gui.*;
import org.lwjgl.*;
import org.lwjgl.opengl.*;
import org.lwjgl.util.vector.*;
import java.nio.*;

public class FontHelper
{
    private static String formatEscape;
    
    public static void drawString(final String s, final float x, float y, final TrueTypeFont font, final float scaleX, final float scaleY, final int format, final float... rgba) {
        final Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution sr = new ScaledResolution(Minecraft.getMinecraft());
        if (mc.gameSettings.hideGUI) {
            return;
        }
        int amt = 1;
        if (sr.getScaleFactor() == 1) {
            amt = 2;
        }
        final FloatBuffer matrixData = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(2982, matrixData);
        final Matrix4f matrix = new Matrix4f();
        matrix.load(matrixData);
        set2DMode();
        y = mc.displayHeight - y * sr.getScaleFactor() - font.getLineHeight() / amt;
        GL11.glEnable(3042);
        if (s.contains(FontHelper.formatEscape)) {
            final String[] pars = s.split(FontHelper.formatEscape);
            float totalOffset = 0.0f;
            for (int i = 0; i < pars.length; ++i) {
                String par = pars[i];
                float[] c = rgba;
                if (i > 0) {
                    c = Formatter.getFormatted(par.charAt(0));
                    par = par.substring(1, par.length());
                }
                font.drawString(x * sr.getScaleFactor() + totalOffset, y - matrix.m31 * sr.getScaleFactor(), par, scaleX / amt, scaleY / amt, format, c);
                totalOffset += font.getWidth(par);
            }
        }
        else {
            font.drawString(x * sr.getScaleFactor(), y - matrix.m31 * sr.getScaleFactor(), s, scaleX / amt, scaleY / amt, format, rgba);
        }
        GL11.glDisable(3042);
        set3DMode();
    }
    
    private static void set2DMode() {
        final Minecraft mc = Minecraft.getMinecraft();
        GL11.glMatrixMode(5889);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
        GL11.glOrtho(0.0, (double)mc.displayWidth, 0.0, (double)mc.displayHeight, -1.0, 1.0);
        GL11.glMatrixMode(5888);
        GL11.glPushMatrix();
        GL11.glLoadIdentity();
    }
    
    private static void set3DMode() {
        GL11.glMatrixMode(5889);
        GL11.glPopMatrix();
        GL11.glMatrixMode(5888);
        GL11.glPopMatrix();
    }
    
    static {
        FontHelper.formatEscape = "�";
    }
}
