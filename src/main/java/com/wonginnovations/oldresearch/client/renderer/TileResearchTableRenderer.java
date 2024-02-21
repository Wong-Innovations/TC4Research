package com.wonginnovations.oldresearch.client.renderer;

import com.wonginnovations.oldresearch.common.items.ItemResearchNote;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.block.ModelResearchTable;
import thaumcraft.common.lib.utils.BlockStateUtils;

@SideOnly(Side.CLIENT)
public class TileResearchTableRenderer extends TileEntitySpecialRenderer<TileResearchTable> {
    private final ModelResearchTable tableModel = new ModelResearchTable();
    private static final ResourceLocation TEX = new ResourceLocation("thaumcraft", "textures/blocks/research_table_model.png");

    public void render(TileResearchTable table, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        this.bindTexture(TEX);
        GlStateManager.translate((float)x + 0.5F, (float)y + 1.0F, (float)z + 0.5F);
        GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
        switch (BlockStateUtils.getFacing(table.getBlockMetadata())) {
            case EAST:
                GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GlStateManager.rotate(270.0F, 0.0F, 1.0F, 0.0F);
                break;
            case SOUTH:
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        }

        if (table.hasResearchNote()) {
            this.tableModel.renderScroll(ItemResearchNote.getColorFromItemStack(table.getStackInSlot(1)));
        }

        if (table.hasScribingTools()) {
            this.tableModel.renderInkwell();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 771);
            GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.translate(-0.5, 0.1, 0.125);
            GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.scale(0.5, 0.5, 0.5);
            UtilsFX.renderItemIn2D("thaumcraft:research/quill", 0.0625F);
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.popMatrix();
    }
}
