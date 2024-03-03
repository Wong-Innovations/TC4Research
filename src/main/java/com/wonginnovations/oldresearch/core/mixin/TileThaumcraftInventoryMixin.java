package com.wonginnovations.oldresearch.core.mixin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.tiles.TileThaumcraft;
import thaumcraft.common.tiles.TileThaumcraftInventory;

import javax.annotation.Nullable;

@Mixin(TileThaumcraftInventory.class)
public class TileThaumcraftInventoryMixin extends TileThaumcraft {

//    @Shadow(remap = false) private boolean isSyncedSlot(int slot) { return false; }
//    @Shadow(remap = false) protected void syncSlots(EntityPlayerMP player) {}
//    @Shadow(remap = false) protected NonNullList<ItemStack> getItems() { return null; }
//    @Shadow(remap = false, aliases = "func_70297_j_") public int getInventoryStackLimit() { return 64; }

//    @Inject(method = "removeStackFromSlot", at = @At("HEAD"), remap = false, cancellable = true)
//    public void removeStackFromSlotInjection(int index, CallbackInfoReturnable<ItemStack> cir) {
//        ItemStack s = ItemStackHelper.getAndRemove(this.getItems(), index);
//        if (!this.world.isRemote && this.isSyncedSlot(index)) {
//            this.syncSlots(null);
//        }
//
//        this.markDirty();
//        cir.setReturnValue(s);
//    }
//
//    @Inject(method = "setInventorySlotContents", at = @At("HEAD"), remap = false, cancellable = true)
//    public void setInventorySlotContentInjection(int index, @Nullable ItemStack stack, CallbackInfo ci) {
//        this.getItems().set(index, (stack != null)? stack : ItemStack.EMPTY);
//        if (stack != null && stack.getCount() > this.getInventoryStackLimit()) {
//            stack.setCount(this.getInventoryStackLimit());
//        }
//
//        this.markDirty();
//        if (!this.world.isRemote && this.isSyncedSlot(index)) {
//            this.syncSlots(null);
//        }
//        ci.cancel();
//    }

    @Inject(method = "syncSlots", at = @At("HEAD"), remap = false, cancellable = true)
    private void syncSlotsInjection(EntityPlayerMP player, CallbackInfo ci) {
        if (this.world.isRemote) ci.cancel();
    }

    @Inject(method = "syncTile", at = @At("HEAD"), remap = false, cancellable = true)
    private void syncTile(boolean rerender, CallbackInfo ci) {
        if (this.world.isRemote) ci.cancel();
    }

}
