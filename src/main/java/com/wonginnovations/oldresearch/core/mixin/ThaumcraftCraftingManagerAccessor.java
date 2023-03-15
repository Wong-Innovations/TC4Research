package com.wonginnovations.oldresearch.core.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.crafting.ThaumcraftCraftingManager;

@Mixin(value = ThaumcraftCraftingManager.class, remap = false)
public interface ThaumcraftCraftingManagerAccessor {

    @Invoker("getBonusTags")
    static AspectList getBonusTags(ItemStack itemstack, AspectList sourcetags) {
        return null;
    }

}
