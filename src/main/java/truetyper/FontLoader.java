//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\dylan\Downloads\mcp908"!

//Decompiled by Procyon!

package truetyper;

import java.awt.*;
import net.minecraft.util.*;
import net.minecraft.client.*;

public class FontLoader
{
    public static TrueTypeFont loadSystemFont(final String name, final float defSize, final boolean antialias) {
        return loadSystemFont(name, defSize, antialias, 0);
    }
    
    public static TrueTypeFont loadSystemFont(final String name, final float defSize, final boolean antialias, final int type) {
        TrueTypeFont out = null;
        try {
            Font font = new Font(name, type, (int)defSize);
            font = font.deriveFont(defSize);
            out = new TrueTypeFont(font, antialias);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
    
    public static TrueTypeFont createFont(final ResourceLocation res, final float defSize, final boolean antialias) {
        return createFont(res, defSize, antialias, 0);
    }
    
    public static TrueTypeFont createFont(final ResourceLocation res, final float defSize, final boolean antialias, final int type) {
        TrueTypeFont out = null;
        try {
            Font font = Font.createFont(type, Minecraft.getMinecraft().getResourceManager().getResource(res).getInputStream());
            font = font.deriveFont(defSize);
            out = new TrueTypeFont(font, antialias);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return out;
    }
}
