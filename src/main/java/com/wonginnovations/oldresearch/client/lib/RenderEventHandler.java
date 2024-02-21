package com.wonginnovations.oldresearch.client.lib;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.Tags;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.aspects.IAspectContainer;
import thaumcraft.api.internal.CommonInternals;
import thaumcraft.api.items.IGoggles;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.config.ConfigAspects;
import thaumcraft.common.config.ConfigItems;
import thaumcraft.common.lib.utils.EntityUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = Tags.MODID)
public abstract class RenderEventHandler {
    public static List<Object> blockTags = new ArrayList<>();
    public static float tagscale = 0.0F;
    public static long scanCount = 0L;
    public static int scanX = 0;
    public static int scanY = 0;
    public static int scanZ = 0;
    private static final int[][][] scannedBlocks = new int[17][17][17];
//    private static final TrueTypeFont font = FontLoader.loadSystemFont("Arial", 12.0F, true);
    @SideOnly(Side.CLIENT)
    public static REHNotifyHandler notifyHandler = new REHNotifyHandler();

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderOverlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        long time = System.nanoTime() / 1000000L;

        notifyHandler.handleNotifications(mc, time, event);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void blockHighlight(DrawBlockHighlightEvent event) {
        RayTraceResult target = event.getTarget();
        if(target != null && target.typeOfHit == RayTraceResult.Type.BLOCK
                && (event.getPlayer().getHeldItemMainhand().getItem() == ItemsTC.thaumometer
                    || event.getPlayer().getHeldItemOffhand().getItem() == ItemsTC.thaumometer)
                && !ScanningManager.isThingStillScannable(event.getPlayer(), target.getBlockPos())) {
            IBlockState bs = event.getPlayer().world.getBlockState(target.getBlockPos());
            AspectList ot = CommonInternals.objectTags.get(CommonInternals.generateUniqueItemstackId(new ItemStack(bs.getBlock())));
            boolean spaceAbove = event.getPlayer().world.isAirBlock(target.getBlockPos().up());
            EnumFacing dir = spaceAbove ? EnumFacing.UP : target.sideHit;
            if(tagscale < 0.5F) {
                tagscale += 0.031F - tagscale / 10.0F;
            }

            drawTagsOnContainer((float)target.getBlockPos().getX() + (float)dir.getXOffset() / 2.0F, (float)target.getBlockPos().getY() + (float)dir.getYOffset() / 2.0F, (float)target.getBlockPos().getZ() + (float)dir.getZOffset() / 2.0F, ot, 220, dir, event.getPartialTicks());
        }

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderLast(RenderWorldLastEvent event) {
        if(tagscale > 0.0F) {
            tagscale -= 0.005F;
        }

        float partialTicks = event.getPartialTicks();
        Minecraft mc = Minecraft.getMinecraft();
        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
            long time = System.currentTimeMillis();

            if(scanCount > time) {
                showScannedBlocks(partialTicks, player, time);
            }
        }

    }

    public static void showScannedBlocks(float partialTicks, EntityPlayer player, long time) {
        Minecraft mc = Minecraft.getMinecraft();
        long dif = scanCount - time;
        GL11.glPushMatrix();
        GL11.glDepthMask(false);
        GL11.glDisable(2929);

        for(int xx = -8; xx <= 8; ++xx) {
            for(int yy = -8; yy <= 8; ++yy) {
                for(int zz = -8; zz <= 8; ++zz) {
                    int value = scannedBlocks[xx + 8][yy + 8][zz + 8];
                    float alpha = 1.0F;
                    if(dif > 4750L) {
                        alpha = 1.0F - (float)(dif - 4750L) / 5.0F;
                    }

                    if(dif < 1500L) {
                        alpha = (float)dif / 1500.0F;
                    }

                    float dist = 1.0F - (float)(xx * xx + yy * yy + zz * zz) / 64.0F;
                    alpha = alpha * dist;
                    if(value == -5) {
                        drawSpecialBlockoverlay((double)(scanX + xx), (double)(scanY + yy), (double)(scanZ + zz), partialTicks, 3986684, alpha);
                    } else if(value == -10) {
                        drawSpecialBlockoverlay((double)(scanX + xx), (double)(scanY + yy), (double)(scanZ + zz), partialTicks, 16734721, alpha);
                    } else if(value >= 0) {
                        GL11.glPushMatrix();
                        GL11.glEnable(3042);
                        GL11.glBlendFunc(770, 1);
                        GL11.glAlphaFunc(516, 0.003921569F);
                        GL11.glDisable(2884);
//                        UtilsFX.bindTexture(TileNodeRenderer.nodetex);
                        drawPickScannedObject((double)(scanX + xx), (double)(scanY + yy), (double)(scanZ + zz), partialTicks, alpha, (int)(time / 50L % 32L), (float)value / 7.0F);
                        GL11.glAlphaFunc(516, 0.1F);
                        GL11.glDisable(3042);
                        GL11.glEnable(2884);
                        GL11.glPopMatrix();
                    }
                }
            }
        }

        GL11.glEnable(2929);
        GL11.glDepthMask(true);
        GL11.glPopMatrix();
    }

    public static void drawSpecialBlockoverlay(double x, double y, double z, float partialTicks, int color, float alpha) {
        float r = 1.0F;
        float g = 1.0F;
        float b = 1.0F;
        EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
        double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
        double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
        double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
        float time = (float)(System.nanoTime() / 30000000L);
        Color cc = new Color(color);
        r = (float)cc.getRed() / 255.0F;
        g = (float)cc.getGreen() / 255.0F;
        b = (float)cc.getBlue() / 255.0F;

        for(int side = 0; side < 6; ++side) {
            GL11.glPushMatrix();
            EnumFacing dir = EnumFacing.byIndex(side);
            GL11.glTranslated(-iPX + x + 0.5D, -iPY + y + 0.5D, -iPZ + z + 0.5D);
            GL11.glRotatef(90.0F, (float)(-dir.getYOffset()), (float)dir.getXOffset(), (float)(-dir.getZOffset()));
            if(dir.getZOffset() < 0) {
                GL11.glTranslated(0.0D, 0.0D, 0.5D);
            } else {
                GL11.glTranslated(0.0D, 0.0D, -0.5D);
            }

            GL11.glRotatef(0.0F, 0.0F, 0.0F, 1.0F);
            UtilsFX.renderQuadCenteredFromTexture("textures/blocks/wardedglass.png", 1.0F, r, g, b, 200, 1, alpha);
            GL11.glPopMatrix();
        }

    }

    @SideOnly(Side.CLIENT)
    public static void drawPickScannedObject(double x, double y, double z, float partialTicks, float alpha, int cframe, float size) {
        GL11.glPushMatrix();
        UtilsFX.renderFacingStrip(x + 0.5D, y + 0.5D, z + 0.5D, 0.0F, 0.2F * size, alpha, 32, 0, cframe, partialTicks, 11184657);
        GL11.glPopMatrix();
        GL11.glPushMatrix();
        UtilsFX.renderFacingStrip(x + 0.5D, y + 0.5D, z + 0.5D, 0.0F, 0.5F * size, alpha, 32, 0, cframe, partialTicks, 11145506);
        GL11.glPopMatrix();
    }

    public static void drawTagsOnContainer(double x, double y, double z, AspectList tags, int bright, EnumFacing dir, float partialTicks) {
        if(Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer && tags != null && tags.size() > 0) {
            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
            double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            int e = 0;
            int rowsize = 5;
            int current = 0;
            float shifty = 0.0F;
            int left = tags.size();

            for(Aspect tag : tags.getAspects()) {
                int div = Math.min(left, rowsize);
                if(current >= rowsize) {
                    current = 0;
                    shifty -= tagscale * 1.05F;
                    left -= rowsize;
                    if(left < rowsize) {
                        div = left % rowsize;
                    }
                }

                float shift = ((float)current - (float)div / 2.0F + 0.5F) * tagscale * 4.0F;
                shift = shift * tagscale;
                Color color = new Color(tag.getColor());
                GL11.glPushMatrix();
                GL11.glDisable(2929);
                GL11.glTranslated(-iPX + x + 0.5D + (double)(tagscale * 2.0F * (float)dir.getXOffset()), -iPY + y - (double)shifty + 0.5D + (double)(tagscale * 2.0F * (float)dir.getYOffset()), -iPZ + z + 0.5D + (double)(tagscale * 2.0F * (float)dir.getZOffset()));
                float xd = (float)(iPX - (x + 0.5D));
                float yd = (float)(iPY - y);
                float zd = (float)(iPZ - (z + 0.5D));
                float rotYaw = (float)(Math.atan2(xd, zd) * 180.0D / 3.141592653589793D);
                float rotPitch = (float)(Math.atan2(yd, Math.sqrt(xd * xd + zd * zd)) * 180.0D / 3.141592653589793D);
                GL11.glRotatef(rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(rotPitch, 1.0F, 0.0F, 0.0F);
                GL11.glTranslated((double)shift, 0.0D, 0.0D);
                GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
                GL11.glScalef(tagscale, tagscale, tagscale);
                if(!OldResearch.proxy.playerKnowledge.hasDiscoveredAspect(player.getGameProfile().getName(), tag)) {
                    UtilsFX.renderQuadCenteredFromTexture("textures/aspects/_unknown.png", 1.0F, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, bright, 771, 0.75F);
                    new Color(11184810);
                } else {
                    UtilsFX.renderQuadCenteredFromTexture(tag.getImage(), 1.0F, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, bright, 771, 0.75F);
                }

                if(tags.getAmount(tag) >= 0) {
                    String am = "" + tags.getAmount(tag);
                    GL11.glScalef(0.04F, 0.04F, 0.04F);
                    GL11.glTranslated(0.0D, 6.0D, -0.1D);
                    int sw = Minecraft.getMinecraft().fontRenderer.getStringWidth(am);
                    GL11.glEnable(3042);
                    Minecraft.getMinecraft().fontRenderer.drawString(am, 14 - sw, 1, 1118481);
                    GL11.glTranslated(0.0D, 0.0D, -0.1D);
                    Minecraft.getMinecraft().fontRenderer.drawString(am, 13 - sw, 0, 16777215);
                }

                GL11.glEnable(2929);
                GL11.glPopMatrix();
                ++current;
            }
        }

    }

}
