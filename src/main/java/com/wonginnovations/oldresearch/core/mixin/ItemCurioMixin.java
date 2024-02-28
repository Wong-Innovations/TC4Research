package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.common.lib.research.OldResearchManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import thaumcraft.common.items.curios.ItemCurio;

@Mixin(ItemCurio.class)
public class ItemCurioMixin {

    /**
     * @author keletu
     * @reason overwrite curio
     */
    @Overwrite
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!worldIn.isRemote && OldResearchManager.CURIOS.get(stack.getMetadata()).onItemRightClick(worldIn, player, hand)) {
            player.sendStatusMessage(new TextComponentString("§5§o" + I18n.format("tc.knowledge.gained")), true);
            if(!player.capabilities.isCreativeMode) {
                stack.setCount(stack.getCount()-1);
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }
}
