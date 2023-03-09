package com.wonginnovations.oldresearch.common.lib.research;

import com.wonginnovations.oldresearch.api.research.ScanResult;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface IScanEventHandler {
    ScanResult scanPhenomena(ItemStack var1, World var2, EntityPlayer var3);
}
