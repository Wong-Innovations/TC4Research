package com.wonginnovations.oldresearch.core.mixin;

import com.wonginnovations.oldresearch.common.blocks.ModBlocks;
import com.wonginnovations.oldresearch.common.tiles.TileResearchTable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import thaumcraft.common.blocks.IBlockFacingHorizontal;
import thaumcraft.common.blocks.basic.BlockTable;
import thaumcraft.common.container.InventoryFake;

@Mixin(value = BlockTable.class, remap = false)
public class BlockTableMixin {

    @Inject(method = "onBlockActivated(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z", shift = At.Shift.BY, by = -2), cancellable = true)
    public void onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        IBlockState bs = ModBlocks.RESEARCHTABLE.getDefaultState();
        bs = bs.withProperty(IBlockFacingHorizontal.FACING, player.getHorizontalFacing());
        world.setBlockState(pos, bs);
        TileResearchTable tile = (TileResearchTable)world.getTileEntity(pos);
        tile.setInventorySlotContents(0, player.getHeldItem(hand).copy());
        player.setHeldItem(hand, ItemStack.EMPTY);
        player.inventory.markDirty();
        tile.markDirty();
        world.markAndNotifyBlock(pos, world.getChunk(pos), bs, bs, 3);
        FMLCommonHandler.instance().firePlayerCraftingEvent(player, new ItemStack(ModBlocks.RESEARCHTABLE), new InventoryFake(1));

        cir.setReturnValue(true);
    }

}
