package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.OldResearch;
import com.wonginnovations.oldresearch.common.lib.research.ScanManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectHelper;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.api.research.ScanningManager;
import thaumcraft.common.lib.research.ScanGeneric;

@Mixin(value = ScanGeneric.class, remap = false)
public abstract class ScanGenericMixin {

    @Inject(method = "onSuccess", at = @At("HEAD"), cancellable = true)
    private void onSuccessInjection(EntityPlayer player, Object obj, CallbackInfo ci) {
        if (obj != null) {
            AspectList al = null;
            if (obj instanceof Entity && !(obj instanceof EntityItem)) {
                al = AspectHelper.getEntityAspects((Entity)obj);
            } else {
                ItemStack is = ScanningManager.getItemFromParms(player, obj);
                if (is != null && !is.isEmpty()) {
                    al = AspectHelper.getObjectAspects(is);
                }
            }

            if (al != null) {
                for(Aspect aspect : al.getAspects()) {
                    ScanManager.checkAndSyncAspectKnowledge(player, aspect, al.getAmount(aspect));
                }
//                TODO: Saving this so I can reference it when making curios better.
//                Iterator var6 = ResearchCategories.researchCategories.values().iterator();
//
//                while(var6.hasNext()) {
//                    ResearchCategory category = (ResearchCategory)var6.next();
//                    ThaumcraftApi.internalMethods.addKnowledge(player, IPlayerKnowledge.EnumKnowledgeType.OBSERVATION, category, category.applyFormula(al));
//                }
            }
        }
        ci.cancel();
    }

}
