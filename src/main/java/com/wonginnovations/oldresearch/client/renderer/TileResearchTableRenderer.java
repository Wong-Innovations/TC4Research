package com.wonginnovations.oldresearch.client.renderer;

import com.wonginnovations.oldresearch.common.items.ItemResearchNote;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.items.IScribeTools;
import thaumcraft.client.lib.UtilsFX;
import thaumcraft.client.renderers.models.block.ModelResearchTable;
import thaumcraft.common.lib.utils.BlockStateUtils;

@SideOnly(Side.CLIENT)
public class TileResearchTableRenderer extends TileEntitySpecialRenderer<TileResearchTable> {
    private final ModelResearchTable tableModel = new ModelResearchTable();
    private static final ResourceLocation TEX = new ResourceLocation("thaumcraft", "textures/blocks/research_table_model.png");

    public TileResearchTableRenderer() {
    }

    public void render(TileResearchTable table, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GL11.glPushMatrix();
        this.bindTexture(TEX);
        GL11.glTranslatef((float)x + 0.5F, (float)y + 1.0F, (float)z + 0.5F);
        GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
        switch (BlockStateUtils.getFacing(table.getBlockMetadata())) {
            case EAST:
                GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
                break;
            case WEST:
                GL11.glRotatef(270.0F, 0.0F, 1.0F, 0.0F);
                break;
            case SOUTH:
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
        }

        if (!table.getStackInSlot(1).isEmpty() && table.getStackInSlot(1).getItem() instanceof ItemResearchNote) {
            this.tableModel.renderScroll(Aspect.ALCHEMY.getColor());
        }

        if (!table.getStackInSlot(0).isEmpty() && table.getStackInSlot(0).getItem() instanceof IScribeTools) {
            this.tableModel.renderInkwell();
            GL11.glPushMatrix();
            GL11.glEnable(3042);
            GL11.glBlendFunc(770, 771);
            GL11.glRotatef(180.0F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslated(-0.5, 0.1, 0.125);
            GL11.glRotatef(60.0F, 0.0F, 1.0F, 0.0F);
            GL11.glScaled(0.5, 0.5, 0.5);
            UtilsFX.renderItemIn2D("thaumcraft:research/quill", 0.0625F);
            GL11.glDisable(3042);
            GL11.glPopMatrix();
        }

        GL11.glPopMatrix();
        GL11.glPushMatrix();
        GL11.glPopMatrix();
    }
}
