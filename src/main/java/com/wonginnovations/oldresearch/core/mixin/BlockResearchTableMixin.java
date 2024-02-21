package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.OldResearch;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import thaumcraft.common.blocks.crafting.BlockResearchTable;

@Mixin(value = BlockResearchTable.class, remap = false)
public abstract class BlockResearchTableMixin {

    @Redirect(method = "onBlockActivated", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/EntityPlayer;openGui(Ljava/lang/Object;ILnet/minecraft/world/World;III)V"))
    public void openGui(EntityPlayer player, Object mod, int modGuiId, World world, int x, int y, int z) {
        player.openGui(OldResearch.instance, 1, world, x, y, z);
    }

}
