package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.api.OldResearchApi;
import com.wonginnovations.oldresearch.api.research.ScanResult;
import com.wonginnovations.oldresearch.common.lib.network.PacketHandler;
import com.wonginnovations.oldresearch.common.lib.network.PacketScannedToServer;
import com.wonginnovations.oldresearch.common.lib.network.PacketSyncScannedToServer;
import com.wonginnovations.oldresearch.common.lib.research.IScanEventHandler;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.client.fx.FXDispatcher;
import thaumcraft.client.lib.events.RenderEventHandler;
import thaumcraft.common.items.tools.ItemThaumometer;
import thaumcraft.common.lib.SoundsTC;
import thaumcraft.common.lib.utils.EntityUtils;

@Mixin(ItemThaumometer.class)
public abstract class ItemThaumometerMixin extends Item {

    @Unique
    ScanResult oldresearch$startScan = null;

    @Shadow(remap = false)
    protected abstract RayTraceResult getRayTraceResultFromPlayerWild(World worldIn, EntityPlayer playerIn, boolean useLiquids);

    @Shadow(remap = false)
    private void updateAura(ItemStack stack, World world, EntityPlayerMP player) {}

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    public void onItemRightClickInject(World world, EntityPlayer p, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        ItemStack stack = p.getHeldItem(hand);
        if (world.isRemote) {
            ScanResult scan = this.oldresearch$doScan(stack, world, p);
            if(scan != null) {
                this.oldresearch$startScan = scan;
            }
        }

        p.setActiveHand(hand);
        cir.setReturnValue(new ActionResult<>(EnumActionResult.PASS, stack));
    }

    @Override
    public void onUsingTick(@NotNull ItemStack stack, EntityLivingBase p, int count) {
        if(p.world.isRemote && p instanceof EntityPlayer) {
            ScanResult scan = this.oldresearch$doScan(stack, p.world, ((EntityPlayer)p));
            if(scan != null && scan.equals(this.oldresearch$startScan)) {
                if(count <= 5) {
                    this.oldresearch$startScan = null;
                    RayTraceResult rtr = this.getRayTraceResultFromPlayerWild(p.world, (EntityPlayer) p, true);
                    BlockPos bp = (rtr != null)? rtr.getBlockPos() : null;
                    if (bp != null && p.world.getBlockState(rtr.getBlockPos()).getBlock() != Block.getBlockFromItem(Item.getItemById(scan.id))) return;
                    p.stopActiveHand();
                    p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.scan, SoundCategory.MASTER, 1F, 1F, false);
                    if(ScanManager.completeScan((EntityPlayer) p, scan, "@")) {
                        PacketHandler.INSTANCE.sendToServer(new PacketScannedToServer(scan, (EntityPlayer) p, "@"));
                        PacketHandler.INSTANCE.sendToServer(new PacketSyncScannedToServer((EntityPlayer) p, scan.entity, bp));
                    }
                }

                if(count % 2 == 0) {
                    p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.ticks, SoundCategory.MASTER, 0.2F, 0.45F + p.world.rand.nextFloat() * 0.1F, false);
                }
            } else {
                this.oldresearch$startScan = null;
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(@NotNull ItemStack itemstack) {
        return 25;
    }

    @Override
    public @NotNull EnumAction getItemUseAction(@NotNull ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public void onPlayerStoppedUsing(@NotNull ItemStack stack, @NotNull World worldIn, @NotNull EntityLivingBase entityLiving, int timeLeft) {
        super.onPlayerStoppedUsing(stack, worldIn, entityLiving, timeLeft);
        this.oldresearch$startScan = null;
    }

    @Inject(method = "onUpdate", at = @At("HEAD"), cancellable = true)
    public void onUpdateInjection(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected, CallbackInfo ci) {
        if (isSelected && !world.isRemote && entity.ticksExisted % 20 == 0 && entity instanceof EntityPlayerMP) {
            this.updateAura(stack, world, (EntityPlayerMP)entity);
        }

        if (isSelected && world.isRemote && entity.ticksExisted % 5 == 0 && entity instanceof EntityPlayer) {
            Entity target = EntityUtils.getPointedEntity(world, entity, 1.0, 16.0, 5.0F, true);
            if (target != null && ScanningManager.isThingStillScannable((EntityPlayer)entity, target)) {
                FXDispatcher.INSTANCE.scanHighlight(target);
            }

            RenderEventHandler.thaumTarget = target;
            RayTraceResult mop = this.getRayTraceResultFromPlayerWild(world, (EntityPlayer)entity, true);
            if (mop != null && ScanningManager.isThingStillScannable((EntityPlayer) entity, mop.getBlockPos())) {
                FXDispatcher.INSTANCE.scanHighlight(mop.getBlockPos());
            }
        }

        ci.cancel();
    }

    @Inject(method = "getRayTraceResultFromPlayerWild", at = @At("HEAD"), cancellable = true, remap = false)
    protected void getRTRInjection(World worldIn, EntityPlayer playerIn, boolean useLiquids, CallbackInfoReturnable<RayTraceResult> cir) {
        float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
        Vec3d vec3d = playerIn.getPositionEyes(partialTicks);
        Vec3d vec3d1 = playerIn.getLook(partialTicks);
        Vec3d vec3d2 = vec3d.add(vec3d1.x * 10.0D, vec3d1.y * 10.0D, vec3d1.z * 10.0D);
        cir.setReturnValue(worldIn.rayTraceBlocks(vec3d, vec3d2, useLiquids, !useLiquids, false));
    }

    @Unique
    private ScanResult oldresearch$doScan(ItemStack stack, World world, EntityPlayer p) {
        Entity pointedEntity = EntityUtils.getPointedEntity(world, p, 0.5D, 10.0D, 0.0F, true);
        if(pointedEntity != null) {
            ScanResult sr = new ScanResult((byte)2, 0, 0, pointedEntity, "");
            if(ScanManager.isValidScanTarget(p, sr, "@")) {
//                Thaumcraft.proxy.blockRunes(world, pointedEntity.posX - 0.5D, pointedEntity.posY + (double)(pointedEntity.getEyeHeight() / 2.0F), pointedEntity.posZ - 0.5D, 0.3F + world.rand.nextFloat() * 0.7F, 0.0F, 0.3F + world.rand.nextFloat() * 0.7F, (int)(pointedEntity.height * 15.0F), 0.03F);
                return sr;
            } else {
                return null;
            }
        } else {
            RayTraceResult mop = this.getRayTraceResultFromPlayerWild(world, p, true);
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
                    ItemStack is = bi.getPickBlock(bs, mop, world, mop.getBlockPos(), p);
                    ScanResult sr = null;

                    try {
                        if(is.isEmpty()) {
                            is = new ItemStack(bi, 1, md);
                        }
                    } catch (Exception ignored) {}

                    try {
                        sr = new ScanResult((byte) 1, Item.getIdFromItem(is.getItem()), is.getItemDamage(), null, "");
                    } catch (Exception ignored) {}

                    if(ScanningManager.isThingStillScannable(p, is)) {
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

}
