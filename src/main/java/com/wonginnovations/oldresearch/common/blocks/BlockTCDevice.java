package com.wonginnovations.oldresearch.common.blocks;

import com.google.common.collect.UnmodifiableIterator;
import java.util.ArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import thaumcraft.common.blocks.IBlockEnabled;
import thaumcraft.common.blocks.IBlockFacing;
import thaumcraft.common.blocks.IBlockFacingHorizontal;
import thaumcraft.common.lib.utils.BlockStateUtils;

public class BlockTCDevice extends BlockTCTile {
    public BlockTCDevice(Material mat, Class tc, String name) {
        super(mat, tc, name);
        IBlockState bs = this.blockState.getBaseState();
        if (this instanceof IBlockFacingHorizontal) {
            bs.withProperty(IBlockFacingHorizontal.FACING, EnumFacing.NORTH);
        } else if (this instanceof IBlockFacing) {
            bs.withProperty(IBlockFacing.FACING, EnumFacing.UP);
        }

        if (this instanceof IBlockEnabled) {
            bs.withProperty(IBlockEnabled.ENABLED, true);
        }

        this.setDefaultState(bs);
    }

    public boolean rotateBlock(World world, BlockPos pos, EnumFacing axis) {
        IBlockState state = world.getBlockState(pos);
        UnmodifiableIterator var5 = state.getProperties().keySet().iterator();

        IProperty prop;
        do {
            if (!var5.hasNext()) {
                return false;
            }

            prop = (IProperty)var5.next();
        } while(!prop.getName().equals("facing"));

        world.setBlockState(pos, state.cycleProperty(prop));
        return true;
    }

    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        super.onBlockAdded(worldIn, pos, state);
        this.updateState(worldIn, pos, state);
    }

    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos frompos) {
        this.updateState(worldIn, pos, state);
        super.neighborChanged(state, worldIn, pos, blockIn, frompos);
    }

    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState bs = this.getDefaultState();
        if (this instanceof IBlockFacingHorizontal) {
            bs = bs.withProperty(IBlockFacingHorizontal.FACING, placer.isSneaking() ? placer.getHorizontalFacing() : placer.getHorizontalFacing().getOpposite());
        }

        if (this instanceof IBlockFacing) {
            bs = bs.withProperty(IBlockFacing.FACING, placer.isSneaking() ? EnumFacing.getDirectionFromEntityLiving(pos, placer).getOpposite() : EnumFacing.getDirectionFromEntityLiving(pos, placer));
        }

        if (this instanceof IBlockEnabled) {
            bs = bs.withProperty(IBlockEnabled.ENABLED, true);
        }

        return bs;
    }

    protected void updateState(World worldIn, BlockPos pos, IBlockState state) {
        if (this instanceof IBlockEnabled) {
            boolean flag = !worldIn.isBlockPowered(pos);
            if (flag != state.getValue(IBlockEnabled.ENABLED)) {
                worldIn.setBlockState(pos, state.withProperty(IBlockEnabled.ENABLED, flag), 3);
            }
        }

    }

    public void updateFacing(World world, BlockPos pos, EnumFacing face) {
        if (this instanceof IBlockFacing || this instanceof IBlockFacingHorizontal) {
            if (face == BlockStateUtils.getFacing(world.getBlockState(pos))) {
                return;
            }

            if (this instanceof IBlockFacingHorizontal && face.getHorizontalIndex() >= 0) {
                world.setBlockState(pos, world.getBlockState(pos).withProperty(IBlockFacingHorizontal.FACING, face), 3);
            }

            if (this instanceof IBlockFacing) {
                world.setBlockState(pos, world.getBlockState(pos).withProperty(IBlockFacing.FACING, face), 3);
            }
        }

    }

    public IBlockState getStateFromMeta(int meta) {
        IBlockState bs = this.getDefaultState();

        try {
            if (this instanceof IBlockFacingHorizontal) {
                bs = bs.withProperty(IBlockFacingHorizontal.FACING, BlockStateUtils.getFacing(meta));
            }

            if (this instanceof IBlockFacing) {
                bs = bs.withProperty(IBlockFacing.FACING, BlockStateUtils.getFacing(meta));
            }

            if (this instanceof IBlockEnabled) {
                bs = bs.withProperty(IBlockEnabled.ENABLED, BlockStateUtils.isEnabled(meta));
            }
        } catch (Exception var4) {
        }

        return bs;
    }

    public int getMetaFromState(IBlockState state) {
        byte b0 = 0;
        int i = this instanceof IBlockFacingHorizontal ? b0 | state.getValue(IBlockFacingHorizontal.FACING).getIndex() : (this instanceof IBlockFacing ? b0 | state.getValue(IBlockFacing.FACING).getIndex() : b0);
        if (this instanceof IBlockEnabled && !(Boolean)state.getValue(IBlockEnabled.ENABLED)) {
            i |= 8;
        }

        return i;
    }

    protected BlockStateContainer createBlockState() {
        ArrayList<IProperty> ip = new ArrayList();
        if (this instanceof IBlockFacingHorizontal) {
            ip.add(IBlockFacingHorizontal.FACING);
        }

        if (this instanceof IBlockFacing) {
            ip.add(IBlockFacing.FACING);
        }

        if (this instanceof IBlockEnabled) {
            ip.add(IBlockEnabled.ENABLED);
        }

        return ip.size() == 0 ? super.createBlockState() : new BlockStateContainer(this, ip.toArray(new IProperty[ip.size()]));
    }
}