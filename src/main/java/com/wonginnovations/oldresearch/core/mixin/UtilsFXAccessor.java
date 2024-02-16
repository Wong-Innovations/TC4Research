package com.wonginnovations.oldresearch.core.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import thaumcraft.client.lib.UtilsFX;

import java.text.DecimalFormat;

@Mixin(value = UtilsFX.class, remap = false)
public interface UtilsFXAccessor {

    @Accessor("myFormatter")
    static DecimalFormat getMyFormatter() {
        return new DecimalFormat();
    }

}
