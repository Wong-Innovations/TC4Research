package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.proxy.ProxyInventoryScanning;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.golems.ISealDisplayer;
import thaumcraft.api.items.IArchitect;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.lib.events.RenderEventHandler;
import thaumcraft.common.config.ModConfig;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

import java.awt.*;

@Mixin(value = RenderEventHandler.class, remap = false)
public abstract class RenderEventHandlerMixin {

    @Shadow
    private static void drawSeals(float partialTicks, EntityPlayer player) {}

    @Inject(method = "drawTagsOnContainer", at = @At("HEAD"), cancellable = true)
    private static void drawTagsOnContainerInjection(double x, double y, double z, AspectList tags, int bright, EnumFacing dir, float partialTicks, CallbackInfo ci) {
        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer && tags != null && tags.size() > 0) {
            int fox = 0;
            int foy = 0;
            int foz = 0;
            if (dir != null) {
                fox = dir.getXOffset();
                foy = dir.getYOffset();
                foz = dir.getZOffset();
            } else {
                x -= 0.5;
                z -= 0.5;
            }

            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
            double iPX = player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks;
            double iPY = player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks;
            double iPZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks;
            int rowsize = 5;
            int current = 0;
            float shifty = 0.0F;
            int left = tags.size();
            Aspect[] var24 = tags.getAspects();
            int var25 = var24.length;

            for(int var26 = 0; var26 < var25; ++var26) {
                Aspect tag = var24[var26];
                int div = Math.min(left, rowsize);
                if (current >= rowsize) {
                    current = 0;
                    shifty -= RenderEventHandler.tagscale * 1.05F;
                    left -= rowsize;
                    if (left < rowsize) {
                        div = left % rowsize;
                    }
                }

                float shift = ((float)current - (float)div / 2.0F + 0.5F) * RenderEventHandler.tagscale * 4.0F;
                shift *= RenderEventHandler.tagscale;
                Color color = new Color(tag.getColor());
                GlStateManager.pushMatrix();
                GlStateManager.disableDepth();
                GlStateManager.translate(-iPX + x + 0.5 + (double)(RenderEventHandler.tagscale * 2.0F * (float)fox), -iPY + y - (double)shifty + 0.5 + (double)(RenderEventHandler.tagscale * 2.0F * (float)foy), -iPZ + z + 0.5 + (double)(RenderEventHandler.tagscale * 2.0F * (float)foz));
                float xd = (float)(iPX - (x + 0.5));
                float yd = (float)(iPY - y);
                float zd = (float)(iPZ - (z + 0.5));
                float rotYaw = (float)(Math.atan2(xd, zd) * 180.0 / Math.PI);
                float rotPitch = (float)(Math.atan2(yd, Math.sqrt(xd * xd + zd * zd)) * 180.0D / 3.141592653589793D);
                GlStateManager.rotate(rotYaw + 180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.rotate(rotPitch, 1.0F, 0.0F, 0.0F);
                GlStateManager.translate(shift, 0.0, 0.0);
                GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                GlStateManager.scale(RenderEventHandler.tagscale, RenderEventHandler.tagscale, RenderEventHandler.tagscale);
                UtilsFX.renderQuadCentered(tag.getImage(), 1.0F, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, bright, 771, 0.75F);
                if (tags.getAmount(tag) >= 0) {
                    GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
                    String am = "" + tags.getAmount(tag);
                    GlStateManager.scale(0.04F, 0.04F, 0.04F);
                    GlStateManager.translate(0.0, 6.0, -0.1);
                    int sw = Minecraft.getMinecraft().fontRenderer.getStringWidth(am);
                    GlStateManager.enableBlend();
                    GlStateManager.blendFunc(770, 771);
                    Minecraft.getMinecraft().fontRenderer.drawString(am, 14 - sw, 1, 1118481);
                    GlStateManager.translate(0.0, 0.0, -0.1);
                    Minecraft.getMinecraft().fontRenderer.drawString(am, 13 - sw, 0, 16777215);
                }

                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
                ++current;
            }
        }
        ci.cancel();
    }

    @Inject(method = "renderLast", at = @At("HEAD"), cancellable = true)
    private static void renderLast(RenderWorldLastEvent event, CallbackInfo ci) {
        if (RenderEventHandler.tagscale > 0.0F) {
            RenderEventHandler.tagscale -= 0.005F;
        }

        float partialTicks = event.getPartialTicks();
        Minecraft mc = Minecraft.getMinecraft();
        if (Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)mc.getRenderViewEntity();
            if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ISealDisplayer) {
                drawSeals(partialTicks, player);
            } else if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof ISealDisplayer) {
                drawSeals(partialTicks, player);
            }

            RayTraceResult target;
            if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof IArchitect) {
                target = ((IArchitect)player.getHeldItemMainhand().getItem()).getArchitectMOP(player.getHeldItemMainhand(), player.world, player);
                RenderEventHandler.wandHandler.handleArchitectOverlay(player.getHeldItemMainhand(), player, partialTicks, player.ticksExisted, target);
            } else if (player.getHeldItemOffhand() != null && player.getHeldItemOffhand().getItem() instanceof IArchitect) {
                target = ((IArchitect)player.getHeldItemOffhand().getItem()).getArchitectMOP(player.getHeldItemOffhand(), player.world, player);
                RenderEventHandler.wandHandler.handleArchitectOverlay(player.getHeldItemOffhand(), player, partialTicks, player.ticksExisted, target);
            }

            if (RenderEventHandler.thaumTarget != null && !ScanningManager.isThingStillScannable(player, RenderEventHandler.thaumTarget)) {
                AspectList ot = AspectHelper.getEntityAspects(RenderEventHandler.thaumTarget);
                if (ot != null && !ot.aspects.isEmpty()) {
                    if (RenderEventHandler.tagscale < 0.5F) {
                        RenderEventHandler.tagscale += 0.031F - RenderEventHandler.tagscale / 10.0F;
                    }

                    double iPX = RenderEventHandler.thaumTarget.prevPosX + (RenderEventHandler.thaumTarget.posX - RenderEventHandler.thaumTarget.prevPosX) * (double)partialTicks;
                    double iPY = RenderEventHandler.thaumTarget.prevPosY + (RenderEventHandler.thaumTarget.posY - RenderEventHandler.thaumTarget.prevPosY) * (double)partialTicks;
                    double iPZ = RenderEventHandler.thaumTarget.prevPosZ + (RenderEventHandler.thaumTarget.posZ - RenderEventHandler.thaumTarget.prevPosZ) * (double)partialTicks;
                    RenderEventHandler.drawTagsOnContainer(iPX, iPY + (double)RenderEventHandler.thaumTarget.height, iPZ, ot, 220, null, event.getPartialTicks());
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "tooltipEvent(Lnet/minecraftforge/event/entity/player/ItemTooltipEvent;)V", at = @At("HEAD"), cancellable = true)
    private static void tooltipEventInjection1(ItemTooltipEvent event, CallbackInfo ci) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        GuiScreen gui = mc.currentScreen;
        if (oldresearch$shouldRenderAspects(gui, event.getEntityPlayer(), event.getItemStack())) {
            AspectList tags = ThaumcraftCraftingManager.getObjectTags(event.getItemStack());
            int index = 0;
            if (tags != null && tags.size() > 0) {
                Aspect[] var5 = tags.getAspects();
                int var6 = var5.length;

                for(int var7 = 0; var7 < var6; ++var7) {
                    Aspect tag = var5[var7];
                    if (tag != null) {
                        ++index;
                    }
                }
            }

            int width = index * 18;
            if (width > 0) {
                double sw = mc.fontRenderer.getStringWidth(" ");
                int t = MathHelper.ceil((double)width / sw);
                int l = MathHelper.ceil(18.0 / (double)mc.fontRenderer.FONT_HEIGHT);

                for(int a = 0; a < l; ++a) {
                    event.getToolTip().add("                                                                                                                                            ".substring(0, Math.min(120, t)));
                }
            }
        }
        ci.cancel();
    }

    @Inject(method = "tooltipEvent(Lnet/minecraftforge/client/event/RenderTooltipEvent$PostBackground;)V", at = @At("HEAD"), cancellable = true)
    private static void tooltipEventInjection2(RenderTooltipEvent.PostBackground event, CallbackInfo ci) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        GuiScreen gui = mc.currentScreen;
        if (oldresearch$shouldRenderAspects(gui, mc.player, event.getStack())) {
            int bot = event.getHeight();
            if (!event.getLines().isEmpty()) {
                for (int a = event.getLines().size() - 1; a >= 0; --a) {
                    if (event.getLines().get(a) != null && !event.getLines().get(a).contains("    ")) {
                        bot -= 10;
                    } else if (a > 0 && event.getLines().get(a - 1) != null && event.getLines().get(a - 1).contains("    ")) {
                        RenderEventHandler.hudHandler.renderAspectsInGui((GuiContainer) gui, mc.player, event.getStack(), bot, event.getX(), event.getY());
                        break;
                    }
                }
            }
        }
        ci.cancel();
    }

    @Unique
    private static boolean oldresearch$shouldRenderAspects(GuiScreen gui, EntityPlayer player, ItemStack stack) {
        if (!(gui instanceof GuiContainer)) return false;
        if ((GuiScreen.isShiftKeyDown() != ModConfig.CONFIG_GRAPHICS.showTags && !Mouse.isGrabbed())
                || (com.wonginnovations.oldresearch.config.ModConfig.inventoryScanning && ProxyInventoryScanning.isHoldingThaumometer())
        ){
            return !ScanningManager.isThingStillScannable(player, stack);
        }
        return false;
    }

}
