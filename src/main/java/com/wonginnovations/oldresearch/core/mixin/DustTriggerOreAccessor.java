package com.wonginnovations.oldresearch.core.mixin;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import thaumcraft.common.lib.crafting.DustTriggerOre;

@Mixin(value = DustTriggerOre.class, remap = false )
public interface DustTriggerOreAccessor {

    @Accessor
    ItemStack getResult();

    @Accessor
    void setResult(ItemStack in);

}
