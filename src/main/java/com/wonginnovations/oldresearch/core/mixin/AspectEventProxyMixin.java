package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.api.OldResearchApi;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.AspectEventProxy;
import thaumcraft.api.aspects.AspectList;

@Mixin(value = AspectEventProxy.class, remap = false)
public abstract class AspectEventProxyMixin {

    @Inject(method = "registerObjectTag(Lnet/minecraft/item/ItemStack;Lthaumcraft/api/aspects/AspectList;)V", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/ConcurrentHashMap;put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER))
    public void injection(ItemStack stack, AspectList list, CallbackInfo ci) {
        OldResearchApi.registerObjectTag(stack, list);
    }

}
