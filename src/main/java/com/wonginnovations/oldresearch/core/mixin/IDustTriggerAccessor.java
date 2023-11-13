package com.wonginnovations.oldresearch.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import thaumcraft.api.crafting.IDustTrigger;

import java.util.ArrayList;

@Mixin(value = IDustTrigger.class, remap = false )
public interface IDustTriggerAccessor {

    @Accessor
    static ArrayList<IDustTrigger> getTriggers() { return null; }

}
