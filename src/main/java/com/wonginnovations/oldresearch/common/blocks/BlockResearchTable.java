package com.wonginnovations.oldresearch.common.blocks;

import java.util.Random;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncAspects;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXGeneric;
import thaumcraft.common.blocks.IBlockFacingHorizontal;

import javax.annotation.Nonnull;

public class BlockResearchTable extends BlockTCDevice implements IBlockFacingHorizontal {
    public BlockResearchTable() {
        super(Material.WOOD, TileResearchTable.class, "research_table_old");
        this.setSoundType(SoundType.WOOD);
    }

    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    public boolean isSideSolid(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EnumFacing side) {
        return false;
    }

    public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing side, float hitX, float hitY, float hitZ) {
        if (world.isRemote) {
            return true;
        } else {
            player.openGui(OldResearch.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
            return true;
        }
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState bs = this.getDefaultState();
        bs = bs.withProperty(IBlockFacingHorizontal.FACING, placer.getHorizontalFacing());
        return bs;
    }

    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(@NotNull IBlockState state, World world, @NotNull BlockPos pos, Random rand) {
        TileEntity te = world.getTileEntity(pos);
//        if (te != null) te.invalidate();
        if (rand.nextInt(5) == 0 && te != null && ((TileResearchTable)te).hasResearchNote()) {
            double xx = rand.nextGaussian() / 2.0;
            double zz = rand.nextGaussian() / 2.0;
            double yy = 1.5 + (double)rand.nextFloat();
            int a = 40 + rand.nextInt(20);
            FXGeneric fb = new FXGeneric(world, (double)pos.getX() + 0.5 + xx, (double)pos.getY() + yy, (double)pos.getZ() + 0.5 + zz, -xx / (double)a, -(yy - 0.85) / (double)a, -zz / (double)a);
            fb.setMaxAge(a);
            fb.setRBGColorF(0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F, 0.5F + rand.nextFloat() * 0.5F);
            fb.setAlphaF(0.0F, 0.25F, 0.5F, 0.75F, 0.0F);
            fb.setParticles(384 + rand.nextInt(16), 1, 1);
            fb.setScale(0.8F + rand.nextFloat() * 0.3F, 0.3F);
            fb.setLayer(0);
            ParticleEngine.addEffect(world, fb);
        }

    }

    @Override
    public @NotNull String getTranslationKey()
    {
        return "tile.research_table";
    }

}
