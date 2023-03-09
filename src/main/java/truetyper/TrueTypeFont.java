//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\dylan\Downloads\mcp908"!

//Decompiled by Procyon!

package truetyper;

import java.util.*;

import com.wonginnovations.oldresearch.client.lib.Tessellator;
import org.lwjgl.opengl.*;
import org.lwjgl.*;
import org.lwjgl.util.glu.*;
import java.awt.image.*;
import java.nio.*;
import java.awt.*;

public class TrueTypeFont
{
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_RIGHT = 1;
    public static final int ALIGN_CENTER = 2;
    private FloatObject[] charArray;
    private Map customChars;
    protected boolean antiAlias;
    private float fontSize;
    private float fontHeight;
    private int fontTextureID;
    private int textureWidth;
    private int textureHeight;
    protected Font font;
    private FontMetrics fontMetrics;
    private int correctL;
    private int correctR;
    
    public TrueTypeFont(final Font font, final boolean antiAlias, final char[] additionalChars) {
        this.charArray = new FloatObject[256];
        this.customChars = new HashMap<>();
        this.fontSize = 0.0f;
        this.fontHeight = 0.0f;
        this.textureWidth = 1024;
        this.textureHeight = 1024;
        this.correctL = 9;
        this.correctR = 8;
        this.font = font;
        this.fontSize = (float)(font.getSize() + 3);
        this.antiAlias = antiAlias;
        this.createSet(additionalChars);
        System.out.println("TrueTypeFont loaded: " + font + " - AntiAlias = " + antiAlias);
        --this.fontHeight;
        if (this.fontHeight <= 0.0f) {
            this.fontHeight = 1.0f;
        }
    }
    
    public TrueTypeFont(final Font font, final boolean antiAlias) {
        this(font, antiAlias, null);
    }
    
    public void setCorrection(final boolean on) {
        if (on) {
            this.correctL = 2;
            this.correctR = 1;
        }
        else {
            this.correctL = 0;
            this.correctR = 0;
        }
    }
    
    private BufferedImage getFontImage(final char ch) {
        final BufferedImage tempfontImage = new BufferedImage(1, 1, 2);
        final Graphics2D g = (Graphics2D)tempfontImage.getGraphics();
        if (this.antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        g.setFont(this.font);
        this.fontMetrics = g.getFontMetrics();
        float charwidth = (float)(this.fontMetrics.charWidth(ch) + 8);
        if (charwidth <= 0.0f) {
            charwidth = 7.0f;
        }
        float charheight = (float)(this.fontMetrics.getHeight() + 3);
        if (charheight <= 0.0f) {
            charheight = this.fontSize;
        }
        final BufferedImage fontImage = new BufferedImage((int)charwidth, (int)charheight, 2);
        final Graphics2D gt = (Graphics2D)fontImage.getGraphics();
        if (this.antiAlias) {
            gt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }
        gt.setFont(this.font);
        gt.setColor(Color.WHITE);
        final int charx = 3;
        final int chary = 1;
        gt.drawString(String.valueOf(ch), charx, chary + this.fontMetrics.getAscent());
        return fontImage;
    }
    
    private void createSet(final char[] customCharsArray) {
        if (customCharsArray != null && customCharsArray.length > 0) {
            this.textureWidth *= 2;
        }
        try {
            final BufferedImage imgTemp = new BufferedImage(this.textureWidth, this.textureHeight, 2);
            final Graphics2D g = (Graphics2D)imgTemp.getGraphics();
            g.setColor(new Color(0, 0, 0, 1));
            g.fillRect(0, 0, this.textureWidth, this.textureHeight);
            float rowHeight = 0.0f;
            float positionX = 0.0f;
            float positionY = 0.0f;
            for (int customCharsLength = (customCharsArray != null) ? customCharsArray.length : 0, i = 0; i < 256 + customCharsLength; ++i) {
                final char ch = (i < 256) ? ((char)i) : customCharsArray[i - 256];
                BufferedImage fontImage = this.getFontImage(ch);
                final FloatObject newIntObject = new FloatObject();
                newIntObject.width = (float)fontImage.getWidth();
                newIntObject.height = (float)fontImage.getHeight();
                if (positionX + newIntObject.width >= this.textureWidth) {
                    positionX = 0.0f;
                    positionY += rowHeight;
                    rowHeight = 0.0f;
                }
                newIntObject.storedX = positionX;
                newIntObject.storedY = positionY;
                if (newIntObject.height > this.fontHeight) {
                    this.fontHeight = newIntObject.height;
                }
                if (newIntObject.height > rowHeight) {
                    rowHeight = newIntObject.height;
                }
                g.drawImage(fontImage, (int)positionX, (int)positionY, null);
                positionX += newIntObject.width;
                if (i < 256) {
                    this.charArray[i] = newIntObject;
                }
                else {
                    this.customChars.put(new Character(ch), newIntObject);
                }
                fontImage = null;
            }
            this.fontTextureID = loadImage(imgTemp);
        }
        catch (Exception e) {
            System.err.println("Failed to create font.");
            e.printStackTrace();
        }
    }
    
    private void drawQuad(final float drawX, final float drawY, final float drawX2, final float drawY2, final float srcX, final float srcY, final float srcX2, final float srcY2) {
        final float DrawWidth = drawX2 - drawX;
        final float DrawHeight = drawY2 - drawY;
        final float TextureSrcX = srcX / this.textureWidth;
        final float TextureSrcY = srcY / this.textureHeight;
        final float SrcWidth = srcX2 - srcX;
        final float SrcHeight = srcY2 - srcY;
        final float RenderWidth = SrcWidth / this.textureWidth;
        final float RenderHeight = SrcHeight / this.textureHeight;
        final Tessellator t = Tessellator.instance;
        t.addVertexWithUV((double)drawX, (double)drawY, 0.0, (double)TextureSrcX, (double)TextureSrcY);
        t.addVertexWithUV((double)drawX, (double)(drawY + DrawHeight), 0.0, (double)TextureSrcX, (double)(TextureSrcY + RenderHeight));
        t.addVertexWithUV((double)(drawX + DrawWidth), (double)(drawY + DrawHeight), 0.0, (double)(TextureSrcX + RenderWidth), (double)(TextureSrcY + RenderHeight));
        t.addVertexWithUV((double)(drawX + DrawWidth), (double)drawY, 0.0, (double)(TextureSrcX + RenderWidth), (double)TextureSrcY);
    }
    
    public float getWidth(final String whatchars) {
        float totalwidth = 0.0f;
        FloatObject floatObject = null;
        int currentChar = 0;
        float lastWidth = -10.0f;
        for (int i = 0; i < whatchars.length(); ++i) {
            currentChar = whatchars.charAt(i);
            if (currentChar < 256) {
                floatObject = this.charArray[currentChar];
            }
            else {
                floatObject = (FloatObject) this.customChars.get((char) currentChar);
            }
            if (floatObject != null) {
                totalwidth += floatObject.width / 2.0f;
                lastWidth = floatObject.width;
            }
        }
        return (float)this.fontMetrics.stringWidth(whatchars);
    }
    
    public float getHeight() {
        return this.fontHeight;
    }
    
    public float getHeight(final String HeightString) {
        return this.fontHeight;
    }
    
    public float getLineHeight() {
        return this.fontHeight;
    }
    
    public void drawString(final float x, final float y, final String whatchars, final float scaleX, final float scaleY, float... rgba) {
        if (rgba.length == 0) {
            rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        }
        this.drawString(x, y, whatchars, 0, whatchars.length() - 1, scaleX, scaleY, 0, rgba);
    }
    
    public void drawString(final float x, final float y, final String whatchars, final float scaleX, final float scaleY, final int format, float... rgba) {
        if (rgba.length == 0) {
            rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        }
        this.drawString(x, y, whatchars, 0, whatchars.length() - 1, scaleX, scaleY, format, rgba);
    }
    
    public void drawString(final float x, final float y, final String whatchars, final int startIndex, final int endIndex, final float scaleX, final float scaleY, final int format, float... rgba) {
        if (rgba.length == 0) {
            rgba = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };
        }
        GL11.glPushMatrix();
        GL11.glScalef(scaleX, scaleY, 1.0f);
        FloatObject floatObject = null;
        float totalwidth = 0.0f;
        int i = startIndex;
        float startY = 0.0f;
        int d = 0;
        int c = 0;
        Label_0234: {
            switch (format) {
                case 1: {
                    d = -1;
                    c = this.correctR;
                    while (i < endIndex) {
                        if (whatchars.charAt(i) == '\n') {
                            startY -= this.fontHeight;
                        }
                        ++i;
                    }
                    break Label_0234;
                }
                case 2: {
                    for (int l = startIndex; l <= endIndex; ++l) {
                        final int charCurrent = whatchars.charAt(l);
                        if (charCurrent == 10) {
                            break;
                        }
                        if (charCurrent < 256) {
                            floatObject = this.charArray[charCurrent];
                        }
                        else {
                            floatObject = (FloatObject) this.customChars.get((char) charCurrent);
                        }
                        totalwidth += floatObject.width - this.correctL;
                    }
                    totalwidth /= -2.0f;
                    break;
                }
            }
            d = 1;
            c = this.correctL;
        }
        GL11.glBindTexture(3553, this.fontTextureID);
        final Tessellator t = Tessellator.instance;
        t.startDrawingQuads();
        if (rgba.length == 4) {
            t.setColorRGBA_F(rgba[0], rgba[1], rgba[2], rgba[3]);
        }
        while (i >= startIndex && i <= endIndex) {
            int charCurrent = whatchars.charAt(i);
            if (charCurrent < 256) {
                floatObject = this.charArray[charCurrent];
            }
            else {
                floatObject = (FloatObject) this.customChars.get((char) charCurrent);
            }
            if (floatObject != null) {
                if (d < 0) {
                    totalwidth += (floatObject.width - c) * d;
                }
                if (charCurrent == 10) {
                    startY -= this.fontHeight * d;
                    totalwidth = 0.0f;
                    if (format == 2) {
                        for (int j = i + 1; j <= endIndex; ++j) {
                            charCurrent = whatchars.charAt(j);
                            if (charCurrent == 10) {
                                break;
                            }
                            if (charCurrent < 256) {
                                floatObject = this.charArray[charCurrent];
                            }
                            else {
                                floatObject = (FloatObject) this.customChars.get((char) charCurrent);
                            }
                            totalwidth += floatObject.width - this.correctL;
                        }
                        totalwidth /= -2.0f;
                    }
                }
                else {
                    this.drawQuad(totalwidth + floatObject.width + x / scaleX, startY + y / scaleY, totalwidth + x / scaleX, startY + floatObject.height + y / scaleY, floatObject.storedX + floatObject.width, floatObject.storedY + floatObject.height, floatObject.storedX, floatObject.storedY);
                    if (d > 0) {
                        totalwidth += (floatObject.width - c) * d;
                    }
                }
                i += d;
            }
        }
        t.draw();
        GL11.glPopMatrix();
    }
    
    public static int loadImage(final BufferedImage bufferedImage) {
        try {
            final short width = (short)bufferedImage.getWidth();
            final short height = (short)bufferedImage.getHeight();
            final int bpp = (byte)bufferedImage.getColorModel().getPixelSize();
            final DataBuffer db = bufferedImage.getData().getDataBuffer();
            ByteBuffer byteBuffer;
            if (db instanceof DataBufferInt) {
                final int[] intI = ((DataBufferInt)bufferedImage.getData().getDataBuffer()).getData();
                final byte[] newI = new byte[intI.length * 4];
                for (int i = 0; i < intI.length; ++i) {
                    final byte[] b = intToByteArray(intI[i]);
                    final int newIndex = i * 4;
                    newI[newIndex] = b[1];
                    newI[newIndex + 1] = b[2];
                    newI[newIndex + 2] = b[3];
                    newI[newIndex + 3] = b[0];
                }
                byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder()).put(newI);
            }
            else {
                byteBuffer = ByteBuffer.allocateDirect(width * height * (bpp / 8)).order(ByteOrder.nativeOrder()).put(((DataBufferByte)bufferedImage.getData().getDataBuffer()).getData());
            }
            byteBuffer.flip();
            final int internalFormat = 32856;
            final int format = 6408;
            final IntBuffer textureId = BufferUtils.createIntBuffer(1);
            GL11.glGenTextures(textureId);
            GL11.glBindTexture(3553, textureId.get(0));
            GL11.glTexParameteri(3553, 10242, 10496);
            GL11.glTexParameteri(3553, 10243, 10496);
            GL11.glTexParameteri(3553, 10240, 9728);
            GL11.glTexParameteri(3553, 10241, 9728);
            GL11.glTexEnvf(8960, 8704, 8448.0f);
            GLU.gluBuild2DMipmaps(3553, internalFormat, (int)width, (int)height, format, 5121, byteBuffer);
            return textureId.get(0);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
            return -1;
        }
    }
    
    public static boolean isSupported(final String fontname) {
        final Font[] font = getFonts();
        for (int i = font.length - 1; i >= 0; --i) {
            if (font[i].getName().equalsIgnoreCase(fontname)) {
                return true;
            }
        }
        return false;
    }
    
    public static Font[] getFonts() {
        return GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    }
    
    public static byte[] intToByteArray(final int value) {
        return new byte[] { (byte)(value >>> 24), (byte)(value >>> 16), (byte)(value >>> 8), (byte)value };
    }
    
    public void destroy() {
        final IntBuffer scratch = BufferUtils.createIntBuffer(1);
        scratch.put(0, this.fontTextureID);
        GL11.glBindTexture(3553, 0);
        GL11.glDeleteTextures(scratch);
    }
    
    private class FloatObject
    {
        public float width;
        public float height;
        public float storedX;
        public float storedY;
    }
}
