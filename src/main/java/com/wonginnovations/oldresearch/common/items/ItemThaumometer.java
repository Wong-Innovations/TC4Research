package com.wonginnovations.oldresearch.common.items;

import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.research.ScanResult;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketScannedToServer;
import com.wonginnovations.oldresearch.common.lib.research.IScanEventHandler;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;
import thaumcraft.common.items.ItemTCBase;

public class ItemThaumometer extends ItemTCBase {

    ScanResult startScan = null;

    public ItemThaumometer() {
        super("thaumometer");
        this.setMaxStackSize(1);
    }

    @Override
    public EnumRarity getRarity(ItemStack itemstack) {
        return EnumRarity.UNCOMMON;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack itemstack) {
        return 25;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack) {
        return EnumAction.NONE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer p, EnumHand hand) {
        ItemStack stack = p.getHeldItem(hand);
        if (world.isRemote) {
//            this.drawFX(world, p);
//            p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.scan, SoundCategory.PLAYERS, 0.5F, 1.0F, false);

            ScanResult scan = this.doScan(stack, world, p);
            if(scan != null) {
                this.startScan = scan;
            }
        }

        p.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase p, int count) {
        if(p.world.isRemote && p instanceof EntityPlayer && ((EntityPlayer)p).getGameProfile().getName().equals(Minecraft.getMinecraft().player.getGameProfile().getName())) {
            ScanResult scan = this.doScan(stack, p.world, ((EntityPlayer)p));
            if(scan != null && scan.equals(this.startScan)) {
                if(count <= 5) {
                    this.startScan = null;
                    p.stopActiveHand();
                    p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.scan, SoundCategory.MASTER, 1F, 1F, false);
                    if(ScanManager.completeScan(((EntityPlayer)p), scan, "@")) {
                        PacketHandler.INSTANCE.sendToServer(new PacketScannedToServer(scan, ((EntityPlayer)p), "@"));
                    }
                }

                if(count % 2 == 0) {
                    p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.ticks, SoundCategory.MASTER, 0.2F, 0.45F + p.world.rand.nextFloat() * 0.1F, false);
                }
            } else {
                this.startScan = null;
            }
        }

    }

    private ScanResult doScan(ItemStack stack, World world, EntityPlayer p) {
        Entity pointedEntity = EntityUtils.getPointedEntity(p.world, p, 0.5D, 10.0D, 0.0F, true);
        if(pointedEntity != null) {
            ScanResult sr = new ScanResult((byte)2, 0, 0, pointedEntity, "");
            if(ScanManager.isValidScanTarget(p, sr, "@")) {
//                Thaumcraft.proxy.blockRunes(world, pointedEntity.posX - 0.5D, pointedEntity.posY + (double)(pointedEntity.getEyeHeight() / 2.0F), pointedEntity.posZ - 0.5D, 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, (int)(pointedEntity.height * 15.0F), 0.03F);
                return sr;
            } else {
                return null;
            }
        } else {
            RayTraceResult mop = this.getRayTraceResultFromPlayerWild(p.world, p, true);
            if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
//                TileEntity tile = world.getTileEntity(mop.blockX, mop.blockY, mop.blockZ);
//                if(tile instanceof INode) {
//                    ScanResult sr = new ScanResult((byte)3, 0, 0, (Entity)null, "NODE" + ((INode)tile).getId());
//                    if(ScanManager.isValidScanTarget(p, sr, "@")) {
//                        Thaumcraft.proxy.blockRunes(world, (double)mop.blockX, (double)mop.blockY + 0.25D, (double)mop.blockZ, 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, 15, 0.03F);
//                        return sr;
//                    }
//
//                    return null;
//                }

                IBlockState bs = world.getBlockState(mop.getBlockPos());
                Block bi = bs.getBlock();
                if(bi != Blocks.AIR) {
                    int md = bi.getMetaFromState(bs);
                    ItemStack is = bi.getPickBlock(bs, mop, p.world, mop.getBlockPos(), p);
                    ScanResult sr = null;

                    try {
                        if(is.isEmpty()) {
                            is = new ItemStack(bi, 1, md);
                        }
                    } catch (Exception ignored) {}

                    try {
                        if(is == null) {
                            sr = new ScanResult((byte)1, Block.getIdFromBlock(bi), md, null, "");
                        } else {
                            sr = new ScanResult((byte)1, Item.getIdFromItem(is.getItem()), is.getItemDamage(), null, "");
                        }
                    } catch (Exception ignored) {}

                    if(ScanManager.isValidScanTarget(p, sr, "@")) {
//                        Thaumcraft.proxy.blockRunes(world, (double)mop.blockX, (double)mop.blockY + 0.25D, (double)mop.blockZ, 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, 15, 0.03F);
                        return sr;
                    }

                    return null;
                }
            }

            for(IScanEventHandler seh : OldResearchApi.scanEventhandlers) {
                ScanResult scan = seh.scanPhenomena(stack, world, p);
                if(scan != null) {
                    return scan;
                }
            }

            return null;
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
        this.startScan = null;
    }

    protected RayTraceResult getRayTraceResultFromPlayerWild(World worldIn, EntityPlayer playerIn, boolean useLiquids) {
        float f = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch) + (float)worldIn.rand.nextInt(25) - (float)worldIn.rand.nextInt(25);
        float f1 = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw) + (float)worldIn.rand.nextInt(25) - (float)worldIn.rand.nextInt(25);
        double d0 = playerIn.prevPosX + (playerIn.posX - playerIn.prevPosX);
        double d1 = playerIn.prevPosY + (playerIn.posY - playerIn.prevPosY) + (double)playerIn.getEyeHeight();
        double d2 = playerIn.prevPosZ + (playerIn.posZ - playerIn.prevPosZ);
        Vec3d vec3 = new Vec3d(d0, d1, d2);
        float f2 = MathHelper.cos(-f1 * 0.017453292F - 3.1415927F);
        float f3 = MathHelper.sin(-f1 * 0.017453292F - 3.1415927F);
        float f4 = -MathHelper.cos(-f * 0.017453292F);
        float f5 = MathHelper.sin(-f * 0.017453292F);
        float f6 = f3 * f4;
        float f7 = f2 * f4;
        double d3 = 16.0;
        Vec3d vec31 = vec3.add((double)f6 * d3, (double)f5 * d3, (double)f7 * d3);
        return worldIn.rayTraceBlocks(vec3, vec31, useLiquids, !useLiquids, false);
    }

    private void drawFX(World worldIn, EntityPlayer playerIn) {
        Entity target = EntityUtils.getPointedEntity(worldIn, playerIn, 1.0, 9.0, 0.0F, true);
        if (target != null) {
            for(int a = 0; a < 10; ++a) {
                FXDispatcher.INSTANCE.blockRunes(target.posX - 0.5, target.posY + (double)(target.getEyeHeight() / 2.0F), target.posZ - 0.5, 0.3F + worldIn.rand.nextFloat() * 0.7F, 0.0F, 0.3F + worldIn.rand.nextFloat() * 0.7F, (int)(target.height * 15.0F), 0.03F);
            }
        } else {
            RayTraceResult mop = this.rayTrace(worldIn, playerIn, true);
            if (mop != null && mop.getBlockPos() != null) {
                for(int a = 0; a < 10; ++a) {
                    FXDispatcher.INSTANCE.blockRunes(mop.getBlockPos().getX(), (double)mop.getBlockPos().getY() + 0.25, mop.getBlockPos().getZ(), 0.3F + worldIn.rand.nextFloat() * 0.7F, 0.0F, 0.3F + worldIn.rand.nextFloat() * 0.7F, 15, 0.03F);
                }
            }
        }

    }

//    @Override
//    @SideOnly(Side.CLIENT)
//    public void registerModels() {
//        ModelResourceLocation location0 = new ModelResourceLocation(Thaumcraft.MODID + ":thaumometer", "inventory");
//        ModelLoader.setCustomModelResourceLocation(this, 0, location0);
//    }
}