package com.wonginnovations.oldresearch.core.mixin.vanilla;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GuiScreen.class)
public interface GuiScreenAccessor {

    @Invoker("renderToolTip")
    void invokeRenderToolTip(ItemStack stack, int x, int y);

}
