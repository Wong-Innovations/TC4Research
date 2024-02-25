package com.wonginnovations.oldresearch.proxy;

import com.wonginnovations.oldresearch.Tags;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketScanSelfToServer;
import com.wonginnovations.oldresearch.common.lib.network.PacketScanSlotToServer;
import com.wonginnovations.oldresearch.config.ModConfig;
import com.wonginnovations.oldresearch.core.mixin.vanilla.GuiScreenAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.items.ItemsTC;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.common.lib.SoundsTC;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Tags.MODID, value = Side.CLIENT)
public class ProxyInventoryScanning {
    private static final int SCAN_TICKS = 25;
    private static final int SOUND_TICKS = 2;
    private static final int INVENTORY_PLAYER_X = 26;
    private static final int INVENTORY_PLAYER_Y = 8;
    private static final int INVENTORY_PLAYER_WIDTH = 52;
    private static final int INVENTORY_PLAYER_HEIGHT = 70;
    private static Slot mouseSlot;
    private static Slot lastScannedSlot;
    private static int ticksHovered;
    private static Object currentScan;
    private static boolean isHoveringPlayer;

    public static boolean isHoldingThaumometer() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null) {
            return false;
        } else {
            ItemStack mouseItem = mc.player.inventory.getItemStack();
            return !mouseItem.isEmpty() && mouseItem.getItem() == ItemsTC.thaumometer;
        }
    }

    @SubscribeEvent
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (!ModConfig.inventoryScanning) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer entityPlayer = mc.player;
        if (entityPlayer != null) {
            if (isHoldingThaumometer()) {
                if (isHoveringPlayer && currentScan != null || mouseSlot != null && !mouseSlot.getStack().isEmpty() && mouseSlot.canTakeStack(entityPlayer) && mouseSlot != lastScannedSlot && !(mouseSlot instanceof SlotCrafting)) {
                    ++ticksHovered;
                    if (currentScan == null) {
                        currentScan = mouseSlot.getStack();
                    }

                    if (ScanningManager.isThingStillScannable(entityPlayer, currentScan)) {
                        if (ticksHovered > SOUND_TICKS && ticksHovered % 4 == 0) {
                            entityPlayer.world.playSound(entityPlayer.posX, entityPlayer.posY, entityPlayer.posZ, SoundsTC.ticks, SoundCategory.MASTER, 0.2F, 0.45F + entityPlayer.world.rand.nextFloat() * 0.1F, false);
                        }

                        if (ticksHovered >= SCAN_TICKS) {
                            if (currentScan instanceof EntityPlayer) {
                                PacketHandler.INSTANCE.sendToServer(new PacketScanSelfToServer());
                            } else {
                                PacketHandler.INSTANCE.sendToServer(new PacketScanSlotToServer(mouseSlot.slotNumber));
                            }

                            ticksHovered = 0;
                            lastScannedSlot = mouseSlot;
                            currentScan = null;
                        }
                    } else {
                        currentScan = null;
                        lastScannedSlot = mouseSlot;
                    }
                }
            } else {
                ticksHovered = 0;
                currentScan = null;
                lastScannedSlot = null;
            }
        }

    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        if (event.getItemStack().getItem() == ItemsTC.thaumometer && ModConfig.inventoryScanning) {
            event.getToolTip().add(TextFormatting.GOLD + I18n.format("tc.inventoryscan.tooltip"));
            if (GuiScreen.isShiftKeyDown()) {
                String[] lines = I18n.format("tc.inventoryscan.tooltip.more").split("\\\\n");

                for (String line : lines) {
                    event.getToolTip().add(TextFormatting.DARK_AQUA + line);
                }
            }
        }

    }

//    @SubscribeEvent
//    public static void onTooltipPostText(RenderTooltipEvent.PostText event) {
//        if (isHoldingThaumometer() && !GuiScreen.isShiftKeyDown()) {
//            Minecraft mc = Minecraft.getMinecraft();
//            if (mc.currentScreen instanceof GuiContainer && !ScanningManager.isThingStillScannable(mc.player, event.getStack())) {
//                renderAspectsInGui((GuiContainer)mc.currentScreen, mc.player, event.getStack(), 0, event.getX(), event.getY());
//            }
//        }
//
//    }

    @SubscribeEvent
    public static void onDrawScreen(GuiScreenEvent.DrawScreenEvent.Post event) {
        if (!ModConfig.inventoryScanning) return;
        if (event.getGui() instanceof GuiContainer && !(event.getGui() instanceof GuiContainerCreative)) {
            Minecraft mc = Minecraft.getMinecraft();
            EntityPlayer entityPlayer = mc.player;
            boolean oldHoveringPlayer = isHoveringPlayer;
            isHoveringPlayer = isHoveringPlayer((GuiContainer)event.getGui(), event.getMouseX(), event.getMouseY());
            if (!isHoveringPlayer) {
                Slot oldMouseSlot = mouseSlot;
                mouseSlot = ((GuiContainer)event.getGui()).getSlotUnderMouse();
                if (oldMouseSlot != mouseSlot) {
                    ticksHovered = 0;
                    currentScan = null;
                }
            }

            if (oldHoveringPlayer != isHoveringPlayer) {
                ticksHovered = 0;
                if (isHoveringPlayer) {
                    currentScan = entityPlayer;
                    if (!ScanningManager.isThingStillScannable(entityPlayer, currentScan)) {
                        currentScan = null;
                    }
                }
            }

            ItemStack mouseItem = entityPlayer.inventory.getItemStack();
            if (!mouseItem.isEmpty() && mouseItem.getItem() == ItemsTC.thaumometer) {
                if (mouseSlot != null && !mouseSlot.getStack().isEmpty()) {
                    if (currentScan != null) {
                        renderScanningProgress(event.getGui(), event.getMouseX(), event.getMouseY(), (float)ticksHovered / 25.0F);
                    }

                    ((GuiScreenAccessor) event.getGui()).invokeRenderToolTip(mouseSlot.getStack(), event.getMouseX(), event.getMouseY());
                } else if (isHoveringPlayer) {
                    if (currentScan != null) {
                        renderScanningProgress(event.getGui(), event.getMouseX(), event.getMouseY(), (float)ticksHovered / 25.0F);
                    }

                    if (!ScanningManager.isThingStillScannable(entityPlayer, entityPlayer)) {
                        renderPlayerAspects(event.getGui(), event.getMouseX(), event.getMouseY());
                    }
                }
            }
        }

    }

//    private static void renderAspectsInGui(GuiContainer guiContainer, EntityPlayer player, ItemStack itemStack, int d, int x, int y) {
//        if (!renderAspectsInGuiHasErrored) {
//            if (hudHandlerInstance == null) {
//                try {
//                    Class renderEventHandler = Class.forName("thaumcraft.client.lib.events.RenderEventHandler");
//                    Object instance = renderEventHandler.getField("INSTANCE").get((Object)null);
//                    hudHandlerInstance = renderEventHandler.getField("hudHandler").get(instance);
//                    Class hudHandler = Class.forName("thaumcraft.client.lib.events.HudHandler");
//                    renderAspectsInGuiMethod = hudHandler.getMethod("renderAspectsInGui", GuiContainer.class, EntityPlayer.class, ItemStack.class, Integer.TYPE, Integer.TYPE, Integer.TYPE);
//                } catch (IllegalAccessException | NoSuchFieldException | NoSuchMethodException | ClassNotFoundException var11) {
//                    renderAspectsInGuiHasErrored = true;
//                    var11.printStackTrace();
//                    return;
//                }
//            }
//
//            try {
//                renderAspectsInGuiMethod.invoke(hudHandlerInstance, guiContainer, player, itemStack, d, x, y);
//            } catch (InvocationTargetException | IllegalAccessException var10) {
//                renderAspectsInGuiHasErrored = true;
//                var10.printStackTrace();
//            }
//
//        }
//    }

//    private static void drawTag(int x, int y, Aspect aspect, float amount, int bonus, double zLevel) {
//        if (!drawTagHasErrored) {
//            if (drawTagMethod == null) {
//                try {
//                    Class utilsFX = Class.forName("thaumcraft.client.lib.UtilsFX");
//                    drawTagMethod = utilsFX.getMethod("drawTag", Integer.TYPE, Integer.TYPE, Aspect.class, Float.TYPE, Integer.TYPE, Double.TYPE);
//                } catch (NoSuchMethodException | ClassNotFoundException var10) {
//                    drawTagHasErrored = true;
//                    var10.printStackTrace();
//                    return;
//                }
//            }
//
//            try {
//                drawTagMethod.invoke((Object)null, x, y, aspect, amount, bonus, zLevel);
//            } catch (InvocationTargetException | IllegalAccessException var9) {
//                drawTagHasErrored = true;
//                var9.printStackTrace();
//            }
//
//        }
//    }

    private static void renderPlayerAspects(GuiScreen gui, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glPushAttrib(1048575);
        GlStateManager.disableLighting();
        int x = mouseX + 17;
        int y = mouseY + 7 - 33;
        EntityPlayer entityPlayer = FMLClientHandler.instance().getClientPlayerEntity();
        AspectList aspectList = AspectHelper.getEntityAspects(entityPlayer);
        if (aspectList != null && aspectList.size() > 0) {
            GlStateManager.disableDepth();
            Aspect[] sortedAspects = aspectList.getAspectsSortedByAmount();

            for (Aspect aspect : sortedAspects) {
                if (aspect != null) {
                    UtilsFX.drawTag(x, y, aspect, (float) aspectList.getAmount(aspect), 0, gui.zLevel);
                    x += 18;
                }
            }

            GlStateManager.enableLighting();
            GlStateManager.enableDepth();
        }
        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }

    private static void renderScanningProgress(GuiScreen gui, int mouseX, int mouseY, float progress) {
        StringBuilder sb = new StringBuilder("§6");
        sb.append(I18n.format("tc.inventoryscan.scanning"));
        if (progress >= 0.75F) {
            sb.append("...");
        } else if (progress >= 0.5F) {
            sb.append("..");
        } else if (progress >= 0.25F) {
            sb.append(".");
        }

        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();
        float oldZLevel = gui.zLevel;
        gui.zLevel = 300.0F;
        Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(sb.toString(), (float)mouseX, (float)(mouseY - 30), -1);
        gui.zLevel = oldZLevel;
        GlStateManager.enableLighting();
        GlStateManager.enableDepth();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.enableRescaleNormal();
    }

    private static boolean isHoveringPlayer(GuiContainer gui, int mouseX, int mouseY) {
        return gui instanceof GuiInventory && mouseX >= gui.getGuiLeft() + INVENTORY_PLAYER_X && mouseX < gui.getGuiLeft() + INVENTORY_PLAYER_X + INVENTORY_PLAYER_WIDTH && mouseY >= gui.getGuiTop() + INVENTORY_PLAYER_Y && mouseY < gui.getGuiTop() + INVENTORY_PLAYER_Y + INVENTORY_PLAYER_HEIGHT;
    }
}
