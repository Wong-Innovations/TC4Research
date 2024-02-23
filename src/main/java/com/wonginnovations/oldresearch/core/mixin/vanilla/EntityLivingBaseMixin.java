package com.wonginnovations.oldresearch.core.mixin.vanilla;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import thaumcraft.api.items.ItemsTC;

@Mixin(EntityLivingBase.class)
public abstract class EntityLivingBaseMixin {

    @Inject(method = "playEquipSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;playSound(Lnet/minecraft/util/SoundEvent;FF)V", shift = At.Shift.BEFORE), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    protected void playEquipSoundInjection(ItemStack stack, CallbackInfo ci, SoundEvent soundEvent, Item item) {
        if (item == ItemsTC.thaumometer) {
            ci.cancel();
        }
    }

}
