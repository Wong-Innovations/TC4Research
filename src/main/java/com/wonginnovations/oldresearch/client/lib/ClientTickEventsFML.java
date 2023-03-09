package com.wonginnovations.oldresearch.client.lib;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.client.gui.GuiResearchPopup;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import com.wonginnovations.oldresearch.client.lib.UtilsFX;
import com.wonginnovations.oldresearch.config.ModConfig;
import com.wonginnovations.oldresearch.core.mixin.ThaumcraftCraftingManagerAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

import java.util.List;

public class ClientTickEventsFML {
    public static GuiResearchPopup researchPopup = null;
//    public int tickCount = 0;
//    int prevWorld;
//    boolean checkedDate = false;
//    final ResourceLocation HUD = new ResourceLocation("thaumcraft", "textures/gui/hud.png");
//    RenderItem ri = new RenderItem();
//    DecimalFormat myFormatter = new DecimalFormat("#######.##");
//    DecimalFormat myFormatter2 = new DecimalFormat("#######.#");
//    HashMap<Integer, AspectList> oldvals = new HashMap();
//    long nextsync = 0L;
//    boolean startThread = false;
//    public static int warpVignette = 0;
//    private static final int SHADER_DESAT = 0;
//    private static final int SHADER_BLUR = 1;
//    private static final int SHADER_HUNGER = 2;
//    private static final int SHADER_SUNSCORNED = 3;
//    ResourceLocation[] shader_resources = new ResourceLocation[]{new ResourceLocation("shaders/post/desaturatetc.json"), new ResourceLocation("shaders/post/blurtc.json"), new ResourceLocation("shaders/post/hunger.json"), new ResourceLocation("shaders/post/sunscorned.json")};
//    ItemStack lastItem = null;
//    int lastCount = 0;

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void renderTick(TickEvent.RenderTickEvent event) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        World world = mc.world;
        if(event.phase != TickEvent.Phase.START && Minecraft.getMinecraft().getRenderViewEntity() instanceof EntityPlayer) {
            EntityPlayer player = (EntityPlayer)Minecraft.getMinecraft().getRenderViewEntity();
            long time = System.currentTimeMillis();
            if(researchPopup == null) {
                researchPopup = new GuiResearchPopup(mc);
            }

            researchPopup.updateResearchWindow();
            GuiScreen gui = mc.currentScreen;
            if(gui instanceof GuiContainer && (GuiScreen.isShiftKeyDown() && !ModConfig.showTags || !GuiScreen.isShiftKeyDown() && ModConfig.showTags) && !Mouse.isGrabbed()) {
                this.renderAspectsInGui((GuiContainer)gui, player);
            }

//            if(player != null && mc.inGameHasFocus && Minecraft.isGuiEnabled()) {
//                if(player.inventory.armorItemInSlot(2) != null && player.inventory.armorItemInSlot(2).getItem() == ConfigItems.itemHoverHarness) {
//                    this.renderHoverHUD(event.renderTickTime, player, time, player.inventory.armorItemInSlot(2));
//                }
//
//                if(!player.capabilities.isCreativeMode && Thaumcraft.instance.runicEventHandler.runicCharge.containsKey(Integer.valueOf(player.getEntityId())) && ((Integer)Thaumcraft.instance.runicEventHandler.runicCharge.get(Integer.valueOf(player.getEntityId()))).intValue() > 0 && Thaumcraft.instance.runicEventHandler.runicInfo.containsKey(Integer.valueOf(player.getEntityId()))) {
//                    this.renderRunicArmorBar(event.renderTickTime, player, time);
//                }
//
//                if(player.inventory.getCurrentItem() != null) {
//                    if(player.inventory.getCurrentItem().getItem() instanceof ItemWandCasting) {
//                        this.renderCastingWandHud(Float.valueOf(event.renderTickTime), player, time, player.inventory.getCurrentItem());
//                    } else if(player.inventory.getCurrentItem().getItem() instanceof ItemSanityChecker) {
//                        this.renderSanityHud(Float.valueOf(event.renderTickTime), player, time);
//                    }
//                }
//            }
        }
    }

    public void renderAspectsInGui(GuiContainer gui, EntityPlayer player) {
        Minecraft mc = FMLClientHandler.instance().getClient();
        ScaledResolution var13 = new ScaledResolution(Minecraft.getMinecraft());
        int var14 = var13.getScaledWidth();
        int var15 = var13.getScaledHeight();
        int var16 = Mouse.getX() * var14 / mc.displayWidth;
        int var17 = var15 - Mouse.getY() * var15 / mc.displayHeight - 1;
        GL11.glPushMatrix();
        GL11.glPushAttrib(1048575);
        GL11.glDisable(2896);

        for(int var20 = 0; var20 < gui.inventorySlots.inventorySlots.size(); ++var20) {
            int xs = UtilsFX.getGuiXSize(gui);
            int ys = UtilsFX.getGuiYSize(gui);
            int shift = 0;
            int shift2 = 0;
            int shiftx = -8;
            int shifty = -8;
            if(OldResearch.aspectShift) {
                shiftx -= 8;
                shifty -= 8;
            }

            Slot var23 = gui.inventorySlots.inventorySlots.get(var20);
            int guiLeft = shift + (gui.width - xs - shift2) / 2;
            int guiTop = (gui.height - ys) / 2;
            if(this.isMouseOverSlot(var23, var16, var17, guiLeft, guiTop) && var23.getStack() != null) {
                int h = ScanManager.generateItemHash(var23.getStack().getItem(), var23.getStack().getItemDamage());
                List<String> list = OldResearch.proxy.getScannedObjects().get(player.getGameProfile().getName());
                if(list != null && (list.contains("@" + h) || list.contains("#" + h))) {
                    AspectList tags = ThaumcraftCraftingManager.getObjectTags(var23.getStack());
                    tags = ThaumcraftCraftingManagerAccessor.getBonusTags(var23.getStack(), tags);
                    if(tags != null) {
                        int x = var16 + 17;
                        int y = var17 + 7 - 33;
                        GL11.glDisable(2929);
                        int index = 0;
                        if(tags.size() > 0) {
                            for(Aspect tag : tags.getAspectsSortedByAmount()) {
                                if(tag != null) {
                                    x = var16 + 17 + index * 18;
                                    y = var17 + 7 - 33;
                                    UtilsFX.bindTexture("textures/aspects/_back.png");
                                    GL11.glPushMatrix();
                                    GL11.glEnable(3042);
                                    GL11.glBlendFunc(770, 771);
                                    GL11.glTranslated(x + shiftx - 2, y + shifty - 2, 0.0D);
                                    GL11.glScaled(1.25D, 1.25D, 0.0D);
                                    UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                    GL11.glDisable(3042);
                                    GL11.glPopMatrix();
                                    if(OldResearch.proxy.playerKnowledge.hasDiscoveredAspect(player.getGameProfile().getName(), tag)) {
                                        UtilsFX.drawTag(x + shiftx, y + shifty, tag, (float)tags.getAmount(tag), 0, UtilsFX.getGuiZLevel(gui));
                                    } else {
                                        UtilsFX.bindTexture("textures/aspects/_unknown.png");
                                        GL11.glPushMatrix();
                                        GL11.glEnable(3042);
                                        GL11.glBlendFunc(770, 771);
                                        GL11.glTranslated(x + shiftx, y + shifty, 0.0D);
                                        UtilsFX.drawTexturedQuadFull(0, 0, UtilsFX.getGuiZLevel(gui));
                                        GL11.glDisable(3042);
                                        GL11.glPopMatrix();
                                    }

                                    ++index;
                                }
                            }
                        }

                        GL11.glEnable(2929);
                    }
                }
            }
        }

        GL11.glPopAttrib();
        GL11.glPopMatrix();
    }

    private boolean isMouseOverSlot(Slot par1Slot, int par2, int par3, int par4, int par5) {
        par2 = par2 - par4;
        par3 = par3 - par5;
        return par2 >= par1Slot.xPos - 1 && par2 < par1Slot.xPos + 16 + 1 && par3 >= par1Slot.yPos - 1 && par3 < par1Slot.yPos + 16 + 1;
    }
}
