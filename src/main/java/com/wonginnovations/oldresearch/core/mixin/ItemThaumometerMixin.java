package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import com.wonginnovations.oldresearch.config.ModConfig;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
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

    @Shadow(remap = false)
    protected abstract RayTraceResult getRayTraceResultFromPlayerWild(World worldIn, EntityPlayer playerIn, boolean useLiquids);

    @Shadow(remap = false)
    private void updateAura(ItemStack stack, World world, EntityPlayerMP player) {}

    @Shadow(remap = false)
    public abstract void doScan(World worldIn, EntityPlayer playerIn);

    @Inject(method = "onItemRightClick", at = @At("HEAD"), cancellable = true)
    public void onItemRightClickInject(World world, EntityPlayer p, EnumHand hand, CallbackInfoReturnable<ActionResult<ItemStack>> cir) {
        if (ModConfig.instantScans) {
            if (world.isRemote) {
//                this.drawFX(world, p);
                p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.scan, SoundCategory.PLAYERS, 0.5F, 1.0F, false);
            } else {
                this.doScan(world, p);
            }

            cir.setReturnValue(new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand)));
        }
        ItemStack stack = p.getHeldItem(hand);
        p.setActiveHand(hand);
        cir.setReturnValue(new ActionResult<>(EnumActionResult.PASS, stack));
    }

    @Override
    public void onUsingTick(@NotNull ItemStack stack, @NotNull EntityLivingBase p, int count) {
        if (!(p instanceof EntityPlayer) || ModConfig.instantScans) return;
        if(p.world.isRemote) {
            if (count <= 1) {
                p.stopActiveHand();
                p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.scan, SoundCategory.MASTER, 1F, 1F, false);
            }
            if (count % 2 == 0) {
                p.world.playSound(p.posX, p.posY, p.posZ, SoundsTC.ticks, SoundCategory.MASTER, 0.2F, 0.45F + p.world.rand.nextFloat() * 0.1F, false);
            }
        } else {
            if (count <= 1) {
                this.doScan(p.world, (EntityPlayer) p);
            }
        }
    }

    @Override
    public int getMaxItemUseDuration(@NotNull ItemStack itemstack) {
        return 20;
    }

    @Override
    public @NotNull EnumAction getItemUseAction(@NotNull ItemStack stack) {
        return EnumAction.NONE;
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

    @Inject(method = "doScan", at = @At("HEAD"), cancellable = true, remap = false)
    private void doScanInjection(World worldIn, EntityPlayer playerIn, CallbackInfo ci) {
        if (!worldIn.isRemote) {
            Entity target = EntityUtils.getPointedEntity(worldIn, playerIn, 1.0, 9.0, 0.0F, true);
            if (target != null && ScanManager.canScanThing(playerIn, target, true)) {
                ScanningManager.scanTheThing(playerIn, target);
            } else {
                RayTraceResult mop = this.rayTrace(worldIn, playerIn, true);
                if (mop != null && mop.getBlockPos() != null && ScanManager.canScanThing(playerIn, mop.getBlockPos(), true)) {
                    ScanningManager.scanTheThing(playerIn, mop.getBlockPos());
                } else {
                    // don't prevaildate this scan so things like the sky can still be scanned
                    // Hopefully this won't cause bugs D:
                    ScanningManager.scanTheThing(playerIn, null);
                }
            }
        }
        ci.cancel();
    }

}
